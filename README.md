# El Mandado

## Descripción

El Mandado es una aplicación desarrollada con **Java y Spring Boot** que permite gestionar los pedidos de una tienda de domicilios. Ofrece dos formas de usarla sobre la misma lógica de negocio:

* Una **API REST** que trabaja con JSON.
* Una **interfaz web** con páginas HTML (plantillas Thymeleaf) para gestionar los pedidos desde el navegador.

El sistema permite:

* Registrar pedidos.
* Consultar todos los pedidos.
* Buscar pedidos por identificador.
* Actualizar pedidos.
* Marcar pedidos como entregados.
* Eliminar pedidos.

El proyecto fue desarrollado aplicando una arquitectura por capas y conceptos fundamentales de Spring Boot como la inyección de dependencias, la separación de responsabilidades, el uso de DTOs, la validación de datos, el manejo centralizado de excepciones y la creación de APIs RESTful.

---

## Objetivo

Desarrollar una aplicación sencilla que permita administrar el ciclo básico de los pedidos de una tienda de domicilios, aplicando los conceptos de Spring Boot vistos durante el desarrollo de la asignatura.

---

## Tecnologías utilizadas

* **Java**
* **Spring Boot**
* **Spring Web (Spring MVC)**
* **Thymeleaf**
* **Jakarta Bean Validation**
* **Maven**
* **HTML y CSS**
* **JSON**
* **Git / GitHub**

---

## Accesos rápidos

Con la aplicación en ejecución:

| Acceso | URL |
|--------|-----|
| Página de inicio | `http://localhost:8080` |
| Vistas web (gestión de pedidos) | `http://localhost:8080/pedidos` |
| API REST | `http://localhost:8080/api/pedidos` |

---

## Arquitectura del proyecto

El proyecto utiliza una arquitectura por capas:

```text
com.elmandado
│
├── controlador
│   ├── PedidoController
│   └── PedidoViewController
│
├── dto
│   ├── CrearPedidoDTO
│   └── PedidoDTO
│
├── servicio
│   ├── PedidoService
│   └── PedidoServiceImpl
│
├── repositorio
│   ├── PedidoRepository
│   └── PedidoRepositoryMemoria
│
├── modelo
│   └── Pedido
│
├── exepciones
│   ├── GlobalExceptionHandler
│   ├── RecursoNoEncontradoException
│   └── ValidacionException
│
└── elmandadoApplication
```

```text
Controlador → Servicio → Repositorio
```

### Modelo

La clase `Pedido` representa la información de cada pedido.

### DTOs

Los DTOs separan lo que entra y sale de la aplicación del modelo interno:

* `CrearPedidoDTO`: datos de entrada para crear y actualizar un pedido. Incluye las validaciones de formato.
* `PedidoDTO`: datos de salida que se devuelven al cliente.

### Controladores

* `PedidoController`: API REST bajo `/api/pedidos`. Devuelve JSON.
* `PedidoViewController`: vistas web bajo `/pedidos`. Devuelve páginas HTML y redirecciones.

Ninguno de los dos contiene lógica de negocio ni manejo de excepciones.

### Servicio

`PedidoService` y `PedidoServiceImpl` contienen las operaciones y reglas de negocio relacionadas con los pedidos, y convierten entre la entidad `Pedido` y los DTOs.

### Repositorio

`PedidoRepository` y `PedidoRepositoryMemoria` se encargan de almacenar y consultar los pedidos en memoria.

### Excepciones

* `RecursoNoEncontradoException`: el pedido solicitado no existe.
* `ValidacionException`: se incumple una regla de negocio.
* `GlobalExceptionHandler`: traduce las excepciones a respuestas HTTP desde un único lugar.

---

## Documentación

Para consultar información más detallada sobre cada parte del proyecto (carpeta `foodexpress/docs`):

* 📌 **[`ENDPOINTS.md`](foodexpress/docs/ENDPOINTS.md)**
  Explica la API REST y las vistas web: rutas, métodos HTTP, cuerpos JSON, errores y códigos de respuesta, en el orden en que se usan.

* ⚙️ **[`SERVICE.md`](foodexpress/docs/SERVICE.md)**
  Explica la capa de servicio, los DTOs, las validaciones, las excepciones y la comunicación con el repositorio.

* 🗂️ **[`STRUCTURE.md`](foodexpress/docs/STRUCTURE.md)**
  Explica la estructura de carpetas y archivos del proyecto y la responsabilidad de cada uno.

---

## Modelo de datos

Cada pedido contiene:

| Campo       | Tipo      | Descripción                       |
| ----------- | --------- | --------------------------------- |
| `id`        | `Long`    | Identificador del pedido (lo asigna el sistema) |
| `cliente`   | `String`  | Nombre del cliente                |
| `plato`     | `String`  | Plato solicitado                  |
| `precio`    | `Double`  | Precio del pedido                 |
| `entregado` | `boolean` | Indica si el pedido fue entregado (empieza en `false`) |

Ejemplo:

```json
{
  "id": 1,
  "cliente": "Carlos",
  "plato": "Pizza Pepperoni",
  "precio": 12.5,
  "entregado": false
}
```

### Validaciones

Los datos de entrada (`CrearPedidoDTO`) se validan automáticamente:

| Campo     | Reglas                            |
| --------- | --------------------------------- |
| `cliente` | Obligatorio, máximo 25 caracteres |
| `plato`   | Obligatorio, máximo 50 caracteres |
| `precio`  | Obligatorio, mayor a 0            |

Además, el servicio aplica reglas de negocio, por ejemplo: un pedido que ya fue entregado no puede marcarse como entregado nuevamente.

---

## Endpoints principales

La API utiliza la siguiente dirección base:

```text
http://localhost:8080/api/pedidos
```

| Método   | Endpoint                     | Función               |
| -------- | ---------------------------- | --------------------- |
| `POST`   | `/api/pedidos`               | Crear un pedido       |
| `GET`    | `/api/pedidos`               | Listar pedidos        |
| `GET`    | `/api/pedidos/{id}`          | Consultar un pedido   |
| `PUT`    | `/api/pedidos/{id}`          | Actualizar un pedido  |
| `PUT`    | `/api/pedidos/{id}/entregar` | Marcar como entregado |
| `DELETE` | `/api/pedidos/{id}`          | Eliminar un pedido    |

Los endpoints están listados en el orden natural de uso: primero se registran pedidos y después se consultan y gestionan.

Para conocer el funcionamiento y ejemplos de cada endpoint, consultar **[`ENDPOINTS.md`](foodexpress/docs/ENDPOINTS.md)**.

### Vistas web

| Método | Ruta                       | Función                          |
| ------ | -------------------------- | -------------------------------- |
| `GET`  | `/`                        | Página de inicio                 |
| `GET`  | `/pedidos/nuevo`           | Formulario para crear un pedido  |
| `POST` | `/pedidos`                 | Guardar un pedido nuevo          |
| `GET`  | `/pedidos`                 | Lista de pedidos                 |
| `GET`  | `/pedidos/{id}/editar`     | Formulario para editar un pedido |
| `POST` | `/pedidos/{id}`            | Guardar la edición               |
| `POST` | `/pedidos/{id}/entregar`   | Marcar como entregado            |
| `POST` | `/pedidos/{id}/eliminar`   | Eliminar un pedido               |

Las vistas usan `POST` para editar, entregar y eliminar porque los formularios HTML solo soportan `GET` y `POST`.

---

## Códigos HTTP

La API utiliza códigos de estado HTTP para indicar el resultado de las operaciones:

| Código                      | Significado                                         |
| --------------------------- | --------------------------------------------------- |
| `200 OK`                    | Consulta o actualización realizada correctamente    |
| `201 Created`               | Pedido creado correctamente                         |
| `204 No Content`            | Operación realizada correctamente sin contenido     |
| `400 Bad Request`           | Datos inválidos o regla de negocio incumplida       |
| `404 Not Found`             | Pedido no encontrado                                |
| `500 Internal Server Error` | Error no previsto en el servidor                    |

### Formato de los errores

Errores de negocio y recurso no encontrado:

```json
{ "error": "No existe el pedido con ID: 99" }
```

Errores de validación de los datos de entrada (un mensaje por campo):

```json
{
  "cliente": "El cliente es obligatorio",
  "precio": "El precio debe ser positivo"
}
```

---

## Almacenamiento

Actualmente los pedidos se almacenan **en memoria**, por lo que los datos se mantienen solo mientras la aplicación está ejecutándose. Al reiniciarla, la lista queda vacía.

Para realizar el almacenamiento se utiliza:

* `ConcurrentHashMap`
* `AtomicLong`

No se requiere una base de datos externa para ejecutar el proyecto.

---

## Configuración

La aplicación utiliza el puerto `8080`.

Archivo:

```text
src/main/resources/application.properties
```

Configuración:

```properties
spring.application.name=elmandado
server.port=8080
```

Por lo tanto, la aplicación estará disponible en:

```text
http://localhost:8080
```

---

## Ejecución del proyecto

### 1. Clonar el repositorio

```bash
git clone <URL_DEL_REPOSITORIO>
```

### 2. Entrar al proyecto

```bash
cd foodexpress
```

### 3. Ejecutar la aplicación

Con Maven:

```bash
mvn spring-boot:run
```

O con el Maven Wrapper incluido (no requiere tener Maven instalado):

```bash
./mvnw spring-boot:run
```

En Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

También puede ejecutarse desde el IDE utilizando la clase principal de Spring Boot.

Una vez iniciada la aplicación:

* La página de inicio estará en `http://localhost:8080`.
* La API estará en `http://localhost:8080/api/pedidos`.

---

## Ejemplos de prueba

### Crear un pedido

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/pedidos" `
-Method Post `
-ContentType "application/json" `
-Body '{"cliente":"Carlos","plato":"Pizza Pepperoni","precio":12.50}'
```

### Consultar pedidos

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/pedidos"
```

### Consultar un pedido

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/pedidos/1"
```

### Actualizar un pedido

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/pedidos/1" `
-Method Put `
-ContentType "application/json" `
-Body '{"cliente":"Carlos","plato":"Pizza Hawaiana","precio":13.00}'
```

### Marcar como entregado

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/pedidos/1/entregar" `
-Method Put
```

### Eliminar un pedido

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/pedidos/1" `
-Method Delete
```

---

## Conceptos de Spring Boot aplicados

Durante el desarrollo se aplicaron los siguientes conceptos:

* Spring Boot.
* Spring MVC.
* `@RestController` y `@Controller`.
* `@RequestMapping`.
* `@GetMapping`.
* `@PostMapping`.
* `@PutMapping`.
* `@DeleteMapping`.
* `@RequestBody`.
* `@PathVariable`.
* `@ResponseStatus`.
* DTOs con `record`.
* Validación con Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@NotNull`, `@Size`, `@Positive`).
* `@ModelAttribute`, `Model` y `BindingResult`.
* Plantillas Thymeleaf y fragmentos reutilizables.
* Inyección de dependencias por constructor.
* `@Service`.
* `@Repository`.
* Interfaces e implementaciones.
* Arquitectura por capas.
* APIs RESTful.
* JSON.
* Códigos de estado HTTP.
* Excepciones personalizadas.
* Manejo global de excepciones con `@RestControllerAdvice` y `@ExceptionHandler`.

---

## Documentación del proyecto

```text
README.md
│
└── foodexpress/docs
    ├── ENDPOINTS.md
    │   └── Rutas de la API y de las vistas web
    │
    ├── SERVICE.md
    │   └── Lógica de la capa de servicio, DTOs y excepciones
    │
    └── STRUCTURE.md
        └── Estructura de carpetas y archivos del proyecto
```

El `README.md` presenta una visión general del proyecto, mientras que los archivos de documentación contienen información específica sobre su funcionamiento interno.

---

## Autor

Proyecto académico desarrollado como práctica de Spring Boot para comprender la creación de APIs REST, interfaces web con plantillas y la aplicación de una arquitectura por capas.