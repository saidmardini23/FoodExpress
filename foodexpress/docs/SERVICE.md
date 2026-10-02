# Servicio de Pedidos

## Descripción

La capa de servicio contiene la lógica de negocio de la aplicación **El Mandado**.

Actúa como intermediario entre el controlador y el repositorio: el controlador recibe las peticiones HTTP, y el servicio decide qué operación realizar, aplica las reglas de negocio y convierte las entidades en DTOs para devolverlas.

La capa está compuesta por:

```text
servicio
│
├── PedidoService
└── PedidoServiceImpl
```

Y se apoya en:

```text
dto
│
├── CrearPedidoDTO   (entrada)
└── PedidoDTO        (salida)

exepciones
│
├── RecursoNoEncontradoException
├── ValidacionException
└── GlobalExceptionHandler
```

---

# DTOs

Desde esta versión el servicio **ya no recibe ni devuelve la entidad `Pedido` directamente**. Usa DTOs para separar el modelo interno de lo que se expone por la API.

## CrearPedidoDTO (entrada)

Se usa al crear y al actualizar un pedido. Es un `record` con validaciones de Jakarta Validation:

```java
public record CrearPedidoDTO(
        @NotBlank(message = "El cliente es obligatorio")
        @Size(max = 25, message = "El cliente no puede superar los 25 caracteres")
        String cliente,

        @NotBlank(message = "El plato es obligatorio")
        @Size(max = 50, message = "El nombre del plato no puede superar los 50 caracteres")
        String plato,

        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser positivo")
        Double precio) {
}
```

| Campo | Reglas |
|-------|--------|
| `cliente` | Obligatorio, máximo 25 caracteres |
| `plato` | Obligatorio, máximo 50 caracteres |
| `precio` | Obligatorio, mayor a 0 |

Estas anotaciones se activan cuando el controlador recibe el DTO con `@Valid`.

## PedidoDTO (salida)

Es la representación del pedido que se devuelve al cliente:

```java
public record PedidoDTO(
        Long id,
        String cliente,
        String plato,
        Double precio,
        boolean entregado) {
}
```

---

# PedidoService

`PedidoService` es la interfaz que define las operaciones disponibles sobre los pedidos.

```java
public interface PedidoService {

    List<PedidoDTO> listarTodos();

    PedidoDTO obtenerPorId(Long id);

    PedidoDTO crear(CrearPedidoDTO dto);

    PedidoDTO actualizar(Long id, CrearPedidoDTO dto);

    void marcarEntregado(Long id);

    void eliminar(Long id);
}
```

### Cambios respecto a la versión anterior

| Antes | Ahora |
|-------|-------|
| Devolvía `Pedido` | Devuelve `PedidoDTO` |
| `crear(String, String, double)` | `crear(CrearPedidoDTO)` |
| No existía actualización | Nuevo método `actualizar(Long, CrearPedidoDTO)` |

---

# PedidoServiceImpl

Implementación de `PedidoService`, registrada en Spring con `@Service`:

```java
@Service
public class PedidoServiceImpl implements PedidoService {
```

Recibe el `PedidoRepository` por inyección de dependencias en el constructor:

```java
private final PedidoRepository repositorio;

public PedidoServiceImpl(PedidoRepository repositorio) {
    this.repositorio = repositorio;
}
```

Comunicación entre capas:

```text
PedidoController
       ↓
PedidoService      (trabaja con DTOs)
       ↓
PedidoRepository   (trabaja con la entidad Pedido)
       ↓
PedidoRepositoryMemoria
```

---

# Excepciones

El servicio usa excepciones propias en lugar de las de Java, y un manejador global que las traduce a respuestas HTTP.

*(Antes se usaban `NoSuchElementException` e `IllegalArgumentException`, y el controlador las convertía.)*

## Excepciones propias

Ambas extienden `RuntimeException` (no son checked, así que no hay que declararlas con `throws`) y solo reciben un mensaje:

```java
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
```

```java
public class ValidacionException extends RuntimeException {
    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
```

| Excepción | Cuándo se lanza | Respuesta HTTP |
|-----------|-----------------|----------------|
| `RecursoNoEncontradoException` | El pedido con ese ID no existe | `404 Not Found` |
| `ValidacionException` | Se rompe una regla de negocio | `400 Bad Request` |

## GlobalExceptionHandler

Es una clase anotada con `@RestControllerAdvice` que intercepta las excepciones lanzadas desde cualquier controlador (o desde las capas que este llama) y las convierte en respuestas JSON. Gracias a esto el servicio solo lanza la excepción y **no decide el código HTTP**.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
```

| Excepción capturada | Código | Cuerpo de la respuesta |
|---------------------|--------|------------------------|
| `RecursoNoEncontradoException` | `404 Not Found` | `{ "error": "<mensaje>" }` |
| `ValidacionException` | `400 Bad Request` | `{ "error": "<mensaje>" }` |
| `MethodArgumentNotValidException` | `400 Bad Request` | `{ "<campo>": "<mensaje>", ... }` |
| `Exception` (cualquier otra) | `500 Internal Server Error` | `{ "error": "...", "detalle": "<mensaje>" }` |

### 1. Recurso no encontrado

```java
@ExceptionHandler(RecursoNoEncontradoException.class)
public ResponseEntity<Map<String, String>> handleNoEncontrado(RecursoNoEncontradoException ex) {
    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", ex.getMessage()));
}
```

Ejemplo de respuesta:

```json
{ "error": "No existe el pedido con ID: 99" }
```

### 2. Reglas de negocio

```java
@ExceptionHandler(ValidacionException.class)
public ResponseEntity<Map<String, String>> handleValidacion(ValidacionException ex) {
    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Map.of("error", ex.getMessage()));
}
```

Ejemplo de respuesta:

```json
{ "error": "El pedido con ID 1 ya había sido marcado como entregado" }
```

### 3. Validaciones del DTO (`@Valid`)

Cuando `CrearPedidoDTO` no cumple sus anotaciones, Spring lanza `MethodArgumentNotValidException` **antes de entrar al servicio**. El handler arma un mapa `campo → mensaje`:

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<Map<String, String>> handleValidacionDTO(MethodArgumentNotValidException ex) {
    Map<String, String> errores = new HashMap<>();
    ex.getBindingResult().getFieldErrors()
            .forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
}
```

Ejemplo de respuesta:

```json
{
  "cliente": "El cliente es obligatorio",
  "precio": "El precio debe ser positivo"
}
```

Nota: el formato es distinto al de los otros errores (`{ "error": ... }`), porque aquí puede haber varios campos fallando a la vez.

### 4. Errores no previstos

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<Map<String, String>> handleGlobalException(Exception ex) {
    return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of(
                    "error", "Ha ocurrido un error interno en el servidor",
                    "detalle", ex.getMessage() != null ? ex.getMessage() : "Error no especificado"));
}
```

Es la red de seguridad final: cualquier excepción que no tenga un handler más específico termina aquí y se responde con `500`. Spring elige siempre el handler más específico, por eso este solo se usa como último recurso.

### Flujo de un error

```text
PedidoService lanza RecursoNoEncontradoException
        ↓
Spring la propaga hasta el controlador
        ↓
GlobalExceptionHandler.handleNoEncontrado()
        ↓
404 Not Found + { "error": "No existe el pedido con ID: 99" }
```

---

# Operaciones del Servicio

## 1. Listar todos los pedidos

```java
@Override
public List<PedidoDTO> listarTodos() {
    return repositorio.buscarTodos().stream()
            .map(p -> new PedidoDTO(p.getId(), p.getCliente(), p.getPlato(), p.getPrecio(), p.isEntregado()))
            .toList();
}
```

Obtiene todos los pedidos del repositorio y convierte cada `Pedido` en un `PedidoDTO`.

```text
GET /api/pedidos
   ↓
PedidoController
   ↓
PedidoService
   ↓
PedidoRepository.buscarTodos()
   ↓
Mapeo Pedido → PedidoDTO
   ↓
Lista de PedidoDTO
```

---

## 2. Obtener pedido por ID

```java
@Override
public PedidoDTO obtenerPorId(Long id) {
    Pedido p = repositorio.buscarPorId(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe el pedido con ID: " + id));
    return new PedidoDTO(p.getId(), p.getCliente(), p.getPlato(), p.getPrecio(), p.isEntregado());
}
```

El repositorio devuelve un `Optional<Pedido>`. Si existe, se convierte a DTO; si no, se lanza `RecursoNoEncontradoException` (→ `404 Not Found`).

---

## 3. Crear un pedido

```java
@Override
public PedidoDTO crear(CrearPedidoDTO dto) {
    if (dto == null) {
        throw new ValidacionException("Los datos del pedido son obligatorios");
    }

    Pedido pedido = new Pedido(dto.cliente(), dto.plato(), dto.precio());
    Pedido guardado = repositorio.guardar(pedido);
    return new PedidoDTO(
            guardado.getId(),
            guardado.getCliente(),
            guardado.getPlato(),
            guardado.getPrecio(),
            guardado.isEntregado());
}
```

Pasos:

1. Comprueba que el DTO no sea `null` (si lo es, lanza `ValidacionException`).
2. Crea la entidad `Pedido` con los datos del DTO. El ID lo asigna el repositorio al guardar.
3. Guarda el pedido.
4. Devuelve un `PedidoDTO` con los datos del pedido ya guardado (incluyendo el ID).

> **Importante:** las validaciones de `cliente`, `plato` y `precio` **ya no están en el servicio**. Ahora viven en `CrearPedidoDTO` (Bean Validation) y se ejecutan antes de llegar aquí, cuando el controlador usa `@Valid`.

```text
POST /api/pedidos
        ↓
PedidoController (@Valid CrearPedidoDTO)
        ↓
Validaciones del DTO
        ↓
PedidoService.crear()
        ↓
Crear Pedido
        ↓
PedidoRepository.guardar()
        ↓
Mapeo Pedido → PedidoDTO
```

---

## 4. Actualizar un pedido (nuevo)

```java
@Override
public PedidoDTO actualizar(Long id, CrearPedidoDTO dto) {
    Pedido pedido = repositorio.buscarPorId(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe el pedido con ID: " + id));

    pedido.setCliente(dto.cliente());
    pedido.setPlato(dto.plato());
    pedido.setPrecio(dto.precio());

    Pedido actualizado = repositorio.guardar(pedido);
    return new PedidoDTO(
            actualizado.getId(),
            actualizado.getCliente(),
            actualizado.getPlato(),
            actualizado.getPrecio(),
            actualizado.isEntregado());
}
```

Pasos:

1. Busca el pedido por ID (si no existe → `RecursoNoEncontradoException`).
2. Reemplaza `cliente`, `plato` y `precio` con los valores del DTO.
3. Guarda los cambios y devuelve el `PedidoDTO` actualizado.

El estado `entregado` **no se modifica** aquí; eso se hace con `marcarEntregado`.

```text
PUT /api/pedidos/{id}
        ↓
PedidoController (@Valid CrearPedidoDTO)
        ↓
PedidoService.actualizar()
        ↓
Buscar pedido
        ↓
Actualizar campos
        ↓
PedidoRepository.guardar()
        ↓
Mapeo Pedido → PedidoDTO
```

---

## 5. Marcar pedido como entregado

```java
@Override
public void marcarEntregado(Long id) {
    Pedido pedido = repositorio.buscarPorId(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe el pedido con ID: " + id));

    if (pedido.isEntregado()) {
        throw new ValidacionException("El pedido con ID " + id + " ya había sido marcado como entregado");
    }

    pedido.setEntregado(true);
    repositorio.guardar(pedido);
}
```

Pasos:

1. Busca el pedido (si no existe → `RecursoNoEncontradoException`).
2. **Nueva regla de negocio:** si ya estaba entregado, lanza `ValidacionException`.
3. Cambia `entregado` a `true` y guarda.

```http
PUT /api/pedidos/{id}/entregar
```

```text
PUT /api/pedidos/1/entregar
        ↓
PedidoController
        ↓
PedidoService.marcarEntregado()
        ↓
Buscar pedido
        ↓
¿Ya entregado? → sí: ValidacionException
        ↓ no
entregado = true
        ↓
Guardar pedido
```

---

## 6. Eliminar pedido

```java
@Override
public void eliminar(Long id) {
    if (!repositorio.existePorId(id)) {
        throw new RecursoNoEncontradoException("No existe el pedido con ID: " + id);
    }
    repositorio.eliminar(id);
}
```

Ahora la existencia se comprueba con `repositorio.existePorId(id)` en lugar de cargar el pedido completo. Si no existe se lanza `RecursoNoEncontradoException`; si existe se elimina.

> Esto requiere que `PedidoRepository` tenga el método `existePorId(Long id)`.

```text
DELETE /api/pedidos/1
        ↓
PedidoController
        ↓
PedidoService.eliminar()
        ↓
PedidoRepository.existePorId()
        ↓
PedidoRepository.eliminar()
```

---

# Manejo de Validaciones

Las validaciones ahora se reparten en dos niveles:

**1. Validaciones de formato (en el DTO).** Verifican que los datos sean válidos antes de entrar al servicio:

- `cliente`: no vacío, máximo 25 caracteres.
- `plato`: no vacío, máximo 50 caracteres.
- `precio`: no nulo y positivo.

Si fallan, Spring lanza `MethodArgumentNotValidException` antes de ejecutar el servicio y el `GlobalExceptionHandler` responde `400 Bad Request` con un mapa `campo → mensaje`.

**2. Reglas de negocio (en el servicio).** Dependen del estado del sistema:

- El pedido debe existir → `RecursoNoEncontradoException`.
- Un pedido no puede marcarse como entregado dos veces → `ValidacionException`.
- El DTO de creación no puede ser `null` → `ValidacionException`.

---

# Responsabilidades del Servicio

La capa de servicio se encarga de:

* Aplicar las reglas de negocio.
* Crear, consultar, actualizar y eliminar pedidos.
* Cambiar el estado de entrega.
* Convertir entre la entidad `Pedido` y los DTOs.
* Lanzar excepciones propias cuando algo falla.
* Comunicarse con el repositorio.

La capa de servicio **no** se encarga de:

* Manejar peticiones HTTP (`@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping` pertenecen al controlador).
* Decidir los códigos de estado HTTP.
* Validar el formato de los datos de entrada (eso lo hace el DTO).

---

# Principio de Separación de Responsabilidades

```text
┌─────────────────────────────┐
│       CONTROLADOR           │
│                             │
│ Recibe peticiones HTTP      │
│ Activa validación (@Valid)  │
│ Devuelve respuestas HTTP    │
└──────────────┬──────────────┘
               │  DTOs
               ↓
┌─────────────────────────────┐
│          SERVICIO           │
│                             │
│ Reglas de negocio           │
│ Mapeo Pedido ↔ DTO          │
│ Excepciones propias         │
└──────────────┬──────────────┘
               │  Entidad Pedido
               ↓
┌─────────────────────────────┐
│        REPOSITORIO          │
│                             │
│ Almacenamiento de datos     │
└─────────────────────────────┘
```

---

# Resumen de cambios respecto a la versión anterior

| Aspecto | Antes | Ahora |
|---------|-------|-------|
| Tipos de entrada/salida | `Pedido` y parámetros sueltos | `CrearPedidoDTO` / `PedidoDTO` |
| Validación de cliente, plato y precio | En el servicio con `if` | En el DTO con Bean Validation |
| Excepciones | `NoSuchElementException`, `IllegalArgumentException` | `RecursoNoEncontradoException`, `ValidacionException` |
| Actualizar pedido | No existía | `actualizar(Long, CrearPedidoDTO)` |
| Marcar entregado | Sin restricción | No permite marcar dos veces |
| Eliminar | Llamaba a `obtenerPorId` | Usa `existePorId` |
| Limpieza de espacios (`trim`) | Se aplicaba en el servicio | Ya no se aplica |

```text
Controller → Service → Repository
```