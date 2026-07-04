# AudioTunning - Sistema de Gestión de Ventas e Inventario

**AudioTunning** es una aplicación móvil robusta desarrollada en Kotlin para Android, diseñada para optimizar los procesos de venta, control de inventario y gestión de usuarios en entornos comerciales. La aplicación integra potentes herramientas como el escaneo de códigos de barras y sincronización en tiempo real.

##  Características Principales

*   **Gestión de Ventas:** Interfaz intuitiva para registrar ventas, manejar un carrito de compras y generar facturas.
*   **Control de Inventario:** Consulta de existencias en tiempo real, actualización de stock y visualización detallada de productos.
*   **Escaneo de Códigos:** Integración con **ML Kit** y **CameraX** para el escaneo rápido de productos mediante la cámara del dispositivo.
*   **Roles de Usuario:** Sistema de permisos diferenciado para:
    *   **Administrador:** Control total del sistema y gestión de usuarios.
    *   **Gerente:** Visualización de reportes y supervisión operativa.
    *   **Empleado:** Operaciones de venta y consulta de inventario.
*   **Historial y Reportes:** Seguimiento detallado de las ventas realizadas con filtros y visualización de rankings.
*   **Gestión de Usuarios:** Registro, edición y recuperación de contraseñas mediante Firebase.
*   **Diseño Responsivo:** Adaptado para diferentes orientaciones (Land) y tamaños de pantalla (Tablets/sw600dp).

##  Stack Tecnológico

*   **Lenguaje:** [Kotlin](https://kotlinlang.org/)
*   **Arquitectura:** Orientada a componentes de Android con soporte de Activities y Adapters.
*   **Base de Datos y Auth:** [Firebase](https://firebase.google.com/) (Realtime Database y Firebase Authentication).
*   **Escaneo:** [Google ML Kit Barcode Scanning](https://developers.google.com/ml-kit/vision/barcode-scanning).
*   **Cámara:** [CameraX](https://developer.android.com/training/camerax).
*   **UI:** Material Components y ConstraintLayout para diseños modernos y fluidos.

##  Estructura del Proyecto

El código fuente principal se encuentra en `com.tuempresa.facturacion`:

*   **Activities:** Manejan la lógica de las pantallas (Ventas, Inventario, Scanner, etc.).
*   **Adapters:** Gestionan la visualización de listas en `RecyclerView` (Productos, Carrito, Historial).
*   **Models:** Clases de datos (`Producto`, `Usuario`, `Venta`).
*   **Layouts:** Archivos XML que definen la interfaz de usuario, incluyendo optimizaciones para modo horizontal y tablets.
