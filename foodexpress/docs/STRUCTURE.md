# ESTRUCTURA DEL PROYECTO - El Mandado

## Descripción

El Mandado es una aplicación Spring Boot con arquitectura por capas para administrar pedidos de una tienda de domicilios. Este documento describe cómo está organizado el proyecto y qué responsabilidad tiene cada carpeta y archivo.

Documentación relacionada (carpeta `docs/`):

| Archivo | Contenido |
|---------|-----------|
| `ENDPOINTS.md` | Endpoints de la API REST y de las vistas web |
| `SERVICE.md` | Capa de servicio, DTOs y manejo de excepciones |
| `STRUCTURE.md` | Estructura del proyecto (este documento) |

---

# Vista general

```text
/
├── .github/
│   └── modernize/java-upgrade/        Herramientas de actualización de Java
├── .vscode/
│   └── settings.json                  Configuración del editor (raíz)
├── foodexpress/                       Proyecto Maven (la aplicación)
│   ├── .mvn/wrapper/                  Maven Wrapper
│   ├── .vscode/launch.json            Configuración de ejecución
│   ├── docs/                          Documentación
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/elmandado/    Código fuente
│   │   │   └── resources/             Vistas, estáticos y configuración
│   │   └── test/                      Pruebas
│   ├── target/                        Archivos compilados (generado)
│   ├── pom.xml                        Dependencias y build
│   ├── mvnw / mvnw.cmd                Scripts del Maven Wrapper
│   ├── HELP.md                        Ayuda generada por Spring Initializr
│   ├── .gitignore
│   └── .gitattributes
└── README.md
```

---

# Código fuente (`src/main/java/com/elmandado`)

```text
com.elmandado
│
├── controlador
│   ├── PedidoController.java
│   └── PedidoViewController.java
│
├── dto
│   ├── CrearPedidoDTO.java
│   └── PedidoDTO.java
│
├── exepciones
│   ├── GlobalExceptionHandler.java
│   ├── RecursoNoEncontradoException.java
│   └── ValidacionException.java
│
├── modelo
│   └── Pedido.java
│
├── repositorio
│   ├── PedidoRepository.java
│   └── PedidoRepositoryMemoria.java
│
├── servicio
│   ├── PedidoService.java
│   └── PedidoServiceImpl.java
│
└── elmandadoApplication.java
```

## controlador

Capa web. Recibe las peticiones, activa la validación y delega en el servicio. No contiene lógica de negocio ni manejo de excepciones.

| Archivo | Función |
|---------|---------|
| `PedidoController` | API REST (`@RestController`) bajo `/api/pedidos`. Devuelve JSON. |
| `PedidoViewController` | Vistas web (`@Controller`) bajo `/pedidos`. Devuelve plantillas Thymeleaf y redirecciones. |

## dto

Objetos de transferencia de datos. Separan lo que se expone por la API del modelo interno.

| Archivo | Función |
|---------|---------|
| `CrearPedidoDTO` | Entrada para crear y actualizar. Incluye las validaciones (`@NotBlank`, `@Size`, `@NotNull`, `@Positive`). |
| `PedidoDTO` | Salida: `id`, `cliente`, `plato`, `precio`, `entregado`. |

## exepciones

Manejo centralizado de errores.

| Archivo | Función |
|---------|---------|
| `RecursoNoEncontradoException` | Se lanza cuando un pedido no existe (→ `404`). |
| `ValidacionException` | Se lanza cuando se incumple una regla de negocio (→ `400`). |
| `GlobalExceptionHandler` | `@RestControllerAdvice` que traduce las excepciones a respuestas HTTP. |

## modelo

| Archivo | Función |
|---------|---------|
| `Pedido` | Entidad del dominio: `id`, `cliente`, `plato`, `precio`, `entregado`. |

## repositorio

Acceso a los datos.

| Archivo | Función |
|---------|---------|
| `PedidoRepository` | Interfaz con las operaciones de almacenamiento (`buscarTodos`, `buscarPorId`, `guardar`, `existePorId`, `eliminar`). |
| `PedidoRepositoryMemoria` | Implementación que guarda los pedidos en memoria. Los datos se pierden al reiniciar la aplicación. |

## servicio

Lógica de negocio.

| Archivo | Función |
|---------|---------|
| `PedidoService` | Interfaz con las operaciones disponibles sobre pedidos. |
| `PedidoServiceImpl` | Implementación (`@Service`): aplica reglas de negocio y convierte entre `Pedido` y DTOs. |

## elmandadoApplication

Clase principal. Arranca la aplicación Spring Boot.

---

# Recursos (`src/main/resources`)

```text
resources
│
├── static
│   ├── css
│   │   └── estilos.css
│   ├── icon.png
│   └── index.html
│
├── templates
│   ├── fragmentos
│   │   └── comunes.html
│   └── pedidos
│       ├── formulario.html
│       └── lista.html
│
└── application.properties
```

## static

Archivos que Spring Boot sirve tal cual, sin procesar.

| Archivo | Función |
|---------|---------|
| `index.html` | Página de inicio. Al estar en `static`, Spring Boot la sirve automáticamente en la raíz (`http://localhost:8080`). |
| `css/estilos.css` | Estilos compartidos por las páginas. |
| `icon.png` | Ícono de la aplicación. |

## templates

Plantillas Thymeleaf que el `PedidoViewController` renderiza con datos del modelo.

| Archivo | Función |
|---------|---------|
| `pedidos/lista.html` | Tabla con todos los pedidos. |
| `pedidos/formulario.html` | Formulario compartido para crear y editar (según `esEdicion`). |
| `fragmentos/comunes.html` | Fragmentos reutilizables entre plantillas. |

## application.properties

Configuración de la aplicación (puerto, Thymeleaf, etc.).

---

# Pruebas (`src/test`)

```text
test/java/com/example/foodexpress
└── FoodexpressApplicationTests.java
```

Contiene la prueba de arranque generada por Spring Initializr.

---

# Archivos de configuración y build

| Archivo | Función |
|---------|---------|
| `pom.xml` | Dependencias (Spring Web, Thymeleaf, Validation, etc.), versión de Java y configuración de Maven. |
| `mvnw`, `mvnw.cmd` | Maven Wrapper para Linux/macOS y Windows: permiten compilar sin tener Maven instalado. |
| `.mvn/wrapper/maven-wrapper.properties` | Versión de Maven que usa el wrapper. |
| `.vscode/launch.json` | Configuración para ejecutar y depurar la app desde VS Code. |
| `.gitignore` / `.gitattributes` | Reglas de Git. |
| `HELP.md` | Ayuda generada automáticamente por Spring Initializr. |
| `README.md` | Presentación general del proyecto. |

## Carpeta `target/`

Contiene el resultado de compilar (`.class` y copia de los recursos). **Se genera automáticamente** con Maven y no debe editarse ni subirse al repositorio.

## Carpeta `.github/modernize`

Contiene scripts (`recordToolUse.ps1` y `recordToolUse.sh`) usados por la herramienta de actualización de Java. No forma parte de la lógica de la aplicación.

---

# Flujo de una petición

```text
Navegador / Cliente HTTP
          ↓
┌──────────────────────────────┐
│ controlador                  │  PedidoController (JSON)
│                              │  PedidoViewController (HTML)
└──────────────┬───────────────┘
               │  usa DTOs
               ↓
┌──────────────────────────────┐
│ servicio                     │  PedidoServiceImpl
└──────────────┬───────────────┘
               │  usa la entidad Pedido
               ↓
┌──────────────────────────────┐
│ repositorio                  │  PedidoRepositoryMemoria
└──────────────────────────────┘

Si algo falla en el servicio:
  RecursoNoEncontradoException / ValidacionException
          ↓
  GlobalExceptionHandler → respuesta HTTP de error
```

---

# Dependencias entre paquetes

```text
controlador ──→ servicio ──→ repositorio ──→ modelo
     │             │
     └──→ dto ←────┘
              
exepciones ←── servicio   (lanza las excepciones)
exepciones ←── controlador (las captura GlobalExceptionHandler)
```

Los controladores no conocen al repositorio y el repositorio no conoce ni a los controladores ni a los DTOs.