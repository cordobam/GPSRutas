1. Restricción de Alcance y Tareas Atómicas

    Trabaja estrictamente por tareas atómicas. Nunca intentes resolver componentes completos, refactorizar múltiples archivos o escribir lógicas complejas de una sola vez.

    Si una tarea requiere más de un archivo o múltiples pasos lógicos, propon solo el primer paso y espera confirmación antes de continuar.

    No inventes funciones de bibliotecas estándar ni supongas comportamientos: si hay dudas sobre la disponibilidad de una API, limítate a usar lo estrictamente estándar.

2. Consistencia de Stack Tecnológico (UI & Dependencias)

    Diseño UI: Respeta de forma absoluta el sistema de diseño ya implementado en la aplicación. Si el proyecto usa Material Design 2 (MD2), está prohibido importar componentes o atributos de Material Design 3 (MD3) (y viceversa). Revisa el código existente antes de sugerir componentes visuales.

    Gradle / Dependencias: Prohibido agregar, actualizar o modificar dependencias en archivos build.gradle o build.gradle.kts a menos que el usuario lo solicite explícitamente y bajo su supervisión. No inventes versiones de librerías ni agregues plugins externos.

3. Compilación y Responsabilidad

    El usuario se encarga de compilar y probar el código. Tu trabajo es entregar fragmentos sintácticamente correctos, limpios y listos para integrarse.

    No escribas código incompleto con // TODO: implementar aquí a menos que sea un punto de extensión explícito. Todo bloque de código debe ser funcional por sí mismo.

4. Estilo de Código y Contexto

    Imports: No omitas imports necesarios, pero tampoco agregues imports redundantes o de paquetes inexistentes. Especifica claramente de qué librería proviene cada función si no es nativa de Kotlin/Java estándar.

    Sigue siempre las convenciones de nomenclatura, patrones arquitectónicos (MVVM, Clean Architecture, etc.) y estilo que ya se evidencian en el código que te comparte el usuario.