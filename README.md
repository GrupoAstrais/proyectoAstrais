<div align="center">

# Proyecto final: Astraïs

![Banner principal](./docs/images/banner.svg)

[![Kotlin Backend](https://img.shields.io/badge/Kotlin_Backend-2.1.0-purple.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Kotlin Android](https://img.shields.io/badge/Kotlin_Android-2.0.21-purple.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![React](https://img.shields.io/badge/React-19.2.0-blue.svg?logo=react&logoColor=white)](https://react.dev/)
[![Android](https://img.shields.io/badge/Android-API%2035-green.svg?logo=android&logoColor=white)](https://developer.android.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue.svg?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Ktor](https://img.shields.io/badge/Ktor-3.1.3-orange.svg?logo=ktor&logoColor=white)](https://ktor.io/)

> **Convierte tu productividad en una aventura** <br />
> Sistema de gamificación para hábitos y tareas con progresión de personaje, recompensas y colaboración social.

<br />

<a href="#"><img src="https://img.shields.io/badge/Descargar-Android-3DDC84?style=flat&logo=android&logoColor=white" alt="Descargar Android"></a>
<a href="#"><img src="https://img.shields.io/badge/Visitar-Web_App-0078D7?style=flat&logo=googlechrome&logoColor=white" alt="Web App"></a>
<a href="./API.md"><img src="https://img.shields.io/badge/Leer-Documentación-FFB000?style=flat&logo=readme&logoColor=white" alt="Documentación"></a>
<a href="https://github.com/GrupoAstrais/proyectoAstrais/issues"><img src="https://img.shields.io/badge/Reportar-Issues-EA4335?style=flat&logo=github&logoColor=white" alt="Reportar Issues"></a>

</div>

---

## Sobre el Proyecto
 
Astraïs resuelve uno de los mayores obstáculos para mantener la productividad personal: la falta de constancia. Transforma tareas cotidianas en una experiencia gamificada donde los usuarios pueden:
 
- **Gestionar tareas** personales y grupales con seguimiento visual
- **Subir de nivel** mediante experiencia (XP) acumulada
- **Comprar cosméticos** con Ludiones (moneda virtual)
- **Colaborar en grupos** con roles definidos
- **Acceder a minijuegos** como recompensa
- **Personalizar avatar** y adoptar mascotas
 
### Problema que resuelve:
- Falta de motivación inmediata en sistemas tradicionales
- Monotonía en rutinas repetitivas
- Ausencia de refuerzo positivo visible
- Dificultad para compartir objetivos colaborativamente

## Capturas de Pantalla

### Interfaz Principal
![Dashboard principal](./docs/images/dashboard.png)

### Gestión de Tareas
![Tareas](./docs/images/dashboard.png)

### Sistema de Grupos
![Grupos](./docs/images/dashboard.png)


### Tienda de Cosméticos
![Tienda](./docs/images/dashboard.png)


## Características Principales

### Implementadas:
- Autenticación segura (JWT + OAuth Google)
- Gestión completa de tareas
- Sistema de grupos colaborativos
- Tienda virtual con cosméticos
- Sistema de niveles y XP
- Personalización de avatar

### En Desarrollo:
- Sistema de logros y achievements
- Minijuegos integrados
- Sistema de amigos
- Notificaciones push
- Rachas avanzadas
- Eventos en tiempo real mediante SSE

## Stack Tecnológico

### Backend

- **Kotlin**
- **Ktor**
- **Exposed ORM**
- **PostgreSQL**
- **JWT**
- **Docker**

El backend está desarrollado con Ktor sobre Kotlin/JVM. Expone una API REST con autenticación JWT, OAuth con Google, gestión de usuarios, tareas, grupos, tienda, inventario, recompensas y comunicación con PostgreSQL mediante Exposed ORM.

### Aplicación Web

- **React**
- **TypeScript**
- **Vite**
- **Tailwind CSS**
- **React Router**
- **Axios**

La aplicación web está desarrollada con React, TypeScript y Vite. Utiliza Tailwind CSS para estilos, React Router para navegación y Axios para la comunicación con el backend.
 
### Aplicación Android

- **Kotlin**
- **Jetpack Compose**
- **Ktor Client**
- **Room**
- **DataStore**
- **Hilt**

La aplicación Android está desarrollada en Kotlin con Jetpack Compose. Sigue una arquitectura basada en estado, cliente Ktor para comunicación con la API, Room para persistencia local, DataStore para preferencias y Hilt para inyección de dependencias.


### Infraestructura

- **Docker**
- **Docker Compose**
- **PostgreSQL 17**
- **Nginx**

El proyecto incluye un `docker-compose.yml` con servicios para backend y base de datos. PostgreSQL se ejecuta en contenedor con volumen persistente y el backend se construye mediante un Dockerfile basado en Gradle y Eclipse Temurin.