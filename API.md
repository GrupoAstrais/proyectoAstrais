# Astrais Backend — Documentación de API

## Generalidades

### Codificación
Todos los mensajes deben estar codificados en **UTF-8 (ISO 10646)**. Tanto el frontend Android como el de React usan esta codificación por defecto.

---

### Tokens JWT

Se generan **dos tipos** de tokens JWT:

| Token | Propósito | Duración |
|---|---|---|
| `AccessToken` | Autenticar peticiones | Muy corta |
| `RefreshToken` | Regenerar el `AccessToken` cuando caduca | Larga |

> Si el token enviado no es válido o ha expirado, el servidor responde con **401 Unauthorized**.

**Logout:** basta con destruir ambos tokens en el cliente. No es estrictamente necesario notificar al servidor.

---

### Formato de errores

Los errores se devuelven con un código HTTP distinto de `200` y un JSON con:

```json
{
  "errorCode": 9,
  "errorText": "Unknown exception happened while processing. Message: ..."
}
```

---

### Errores globales (aplican a cualquier ruta)

| Situación | HTTP | `errorCode` | Mensaje |
|---|---|---|---|
| Mensaje mal formateado | `400 Bad Request` | `ERR_MALFORMEDMESSAGE (1)` | `The data sent by the client was not in the accepted format` |
| Error de conversión a número (UID, etc.) | `400 Bad Request` | `ERR_BADVALUE (6)` | `Couldn't parse to int (Likely the UID)` |
| Excepción desconocida | `500 Internal Server Error` | `ERR_INTERNALERROR (9)` | `Unknown exception happened while processing. Message: ${except.message}` |
| Token caducado o inválido | `401 Unauthorized` | `ERR_INVALIDTOKEN (0)` | `Invalid/expired token` |

---

### Tabla de códigos de error internos

| Código | Constante | Descripción general |
|---|---|---|
| 0 | `ERR_INVALIDTOKEN` | Token inválido o expirado |
| 1 | `ERR_MALFORMEDMESSAGE` | Mensaje mal formado |
| 3 | `ERR_RESOURCEMISSING` | Recurso no encontrado |
| 4 | `ERR_RESOURCEALREADYEXISTS` | El recurso ya existe |
| 5 | `ERR_BLANKVALUE` | Campo requerido vacío |
| 6 | `ERR_BADVALUE` | Valor inválido o no parseable |
| 7 | `ERR_RESOURCENOTCREATED` | No se pudo crear el recurso |
| 8 | `ERR_FORBIDDEN` | Sin permisos para la operación |
| 9 | `ERR_INTERNALERROR` | Error interno del servidor |

---

## Auth

### `POST /auth/login`

Autentica al usuario con credenciales y devuelve los tokens JWT.

**Entrada:**
```json
{
  "email": "string",
  "passwd": "string (sin hashear)"
}
```

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "jwtAccessToken": "...", "jwtRefreshToken": "..." }` |
| `400 Bad Request` | Campo vacío o usuario inexistente |
| `403 Forbidden` | Cuenta no confirmada |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_INVALIDTOKEN (0)` | Token inválido |
| `ERR_RESOURCEMISSING (3)` | El usuario no existe |
| `ERR_BLANKVALUE (5)` | Email o contraseña vacíos |
| `ERR_FORBIDDEN (8)` | Cuenta no confirmada |

---

### `POST /auth/register`

Registra un nuevo usuario. No inicia sesión. Solo crea usuarios normales (no admins).

**Entrada:**
```json
{
  "name": "string",
  "email": "string",
  "passwd": "string (sin hashear)",
  "lang": "string (3 chars: ENG, ESP, FRA...)",
  "utcOffset": "number (opcional)"
}
```

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `409 Conflict` | El usuario ya existe |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_RESOURCEALREADYEXISTS (4)` | El usuario ya existe |
| `ERR_BLANKVALUE (5)` | Algún campo obligatorio está vacío |
| `ERR_BADVALUE (6)` | Idioma no soportado |

---

### `POST /auth/verify`

Verifica la cuenta del usuario mediante un código enviado por email.

**Entrada:**
```json
{
  "email": "string",
  "code": "string (6 caracteres)"
}
```

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `400 Bad Request` | Código incorrecto o campo vacío |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_BLANKVALUE (5)` | Algún campo vacío |
| `ERR_BADVALUE (6)` | Código incorrecto |

---

### `POST /auth/regenAccess`

Regenera el `AccessToken` usando el `RefreshToken`. Llamar cuando el `AccessToken` caduca.

**Autenticación:** `RefreshToken` en el header `Authorization`.  
**Entrada:** ninguna.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "newAccessToken": "..." }` |
| `401 Unauthorized` | RefreshToken inválido o no se pudo generar el nuevo token |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_INVALIDTOKEN (0)` | Token inválido |
| `ERR_MALFORMEDMESSAGE (1)` | Token no numérico |
| `ERR_RESOURCEMISSING (3)` | No se pudo generar el AccessToken |

---

### `DELETE /auth/deleteUser`

Elimina el usuario autenticado.

**Autenticación:** `AccessToken`.  
**Entrada:** ninguna.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `400 Bad Request` | Petición mal formada o no se pudo borrar |
| `401 Unauthorized` | AccessToken inválido |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_INVALIDTOKEN (0)` | Token inválido |
| `ERR_MALFORMEDMESSAGE (1)` | Mensaje mal formado o token no numérico |
| `ERR_BADVALUE (6)` | No se pudo borrar el usuario |

---

### `GET /auth/me`

Devuelve los datos del usuario autenticado.

**Autenticación:** `AccessToken`.  
**Entrada:** ninguna.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | JSON con datos del usuario (ver abajo) |
| `404 Not Found` | Usuario no encontrado |

**Cuerpo de respuesta `200`:**
```json
{
  "id": 1,
  "nombre": "string",
  "nivel": 5,
  "xpActual": 320,
  "xpTotal": 4320,
  "ludiones": 150,
  "personalGid": 12,
  "equipperPetRef": "string | null",
  "themeColors": "string | null"
}
```

---

### `GET /auth/google/login`

Inicia el flujo OAuth con Google. Redirige al usuario a la pantalla de login de Google.

---

### `GET /auth/google/callback`

Solo debe ser llamada por Google como callback del OAuth. No llamar directamente.

**Respuesta:**
```json
{
  "uid": 1,
  "hadToRegister": false,
  "jwtAccessToken": "...",
  "jwtRefreshToken": "..."
}
```

> Si no existe cuenta asociada al email de Google, se crea una con datos vacíos. `hadToRegister: true` indica que se creó una cuenta nueva y el frontend debe solicitar al usuario que complete sus datos.

---

## Groups

### `GET /group/userGroups`

Devuelve todos los grupos a los que pertenece el usuario autenticado.

**Autenticación:** `AccessToken`.  
**Entrada:** ninguna.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "groupList": [ ... ] }` |

**Objeto de grupo:**
```json
{
  "id": 1,
  "name": "string",
  "description": "string",
  "role": 0
}
```

| `role` | Significado |
|---|---|
| `0` | Usuario normal |
| `1` | Moderador |
| `2` | Dueño |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_BADVALUE (6)` | UID inválido |

---

### `POST /groups/createGroup`

Crea un nuevo grupo con el usuario autenticado como dueño.

**Autenticación:** `AccessToken`.

**Entrada:**
```json
{
  "name": "string",
  "desc": "string"
}
```

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `400 Bad Request` | Petición mal formada |
| `409 Conflict` | No se pudo crear el grupo |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_MALFORMEDMESSAGE (1)` | Mensaje no es JSON o UID no numérico |
| `ERR_BADVALUE (6)` | UID inválido |
| `ERR_RESOURCENOTCREATED (7)` | No se pudo crear el grupo |

---

### `POST /groups/addUser`

Agrega un usuario a un grupo. Solo moderadores y dueños pueden hacerlo.

**Autenticación:** `AccessToken`.

**Entrada:**
```json
{
  "gid": 1,
  "userId": 2
}
```

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `401 Unauthorized` | UID del token inválido |
| `404 Not Found` | GID inválido |
| `400 Bad Request` | El usuario ya se unió |
| `403 Forbidden` | Sin permisos para agregar |
| `500 Internal Server Error` | Error en la query |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_RESOURCEMISSING (3)` | Grupo inválido |
| `ERR_RESOURCEALREADYEXISTS (4)` | El usuario ya es miembro |
| `ERR_BADVALUE (6)` | UID del token inválido |
| `ERR_FORBIDDEN (8)` | Sin permisos |
| `ERR_INTERNALERROR (9)` | Error con la query |

---

### `PATCH /groups/editGroup`

Edita el nombre o descripción de un grupo. Solo moderadores y dueños.

**Autenticación:** `AccessToken`.

**Entrada:**
```json
{
  "gid": 1,
  "name": "string (opcional)",
  "desc": "string (opcional)"
}
```

> Los campos `name` y `desc` son opcionales; si se omiten o están vacíos, no se modifican.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `401 Unauthorized` | Sin privilegios |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_FORBIDDEN (8)` | Sin privilegios para editar |

---

### `DELETE /groups/deleteGroup`

Elimina un grupo. Solo el dueño puede hacerlo.

**Autenticación:** `AccessToken`.

**Entrada:**
```json
{
  "gid": 1
}
```

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `401 Unauthorized` | Sin privilegios |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_FORBIDDEN (8)` | Sin privilegios para borrar |

---

### `PATCH /groups/passOwnership` *(sin implementar)*

Transfiere la propiedad del grupo a otro miembro.

---

## Tasks

### `POST /tasks`

Crea una nueva tarea en un grupo. Solo moderadores y dueños pueden crear tareas.

**Autenticación:** `AccessToken`.

**Entrada:**
```json
{
  "gid": 1,
  "titulo": "string",
  "descripcion": "string",
  "tipo": "UNIQUE | HABIT | OBJECTIVE",
  "prioridad": 0,
  "extraUnico": {
    "fechaLimite": "ISO 8601"
  },
  "extraHabito": {
    "numeroFrecuencia": 1,
    "frequency": "HOURLY | DAILY | WEEKLY | MONTHLY | YEARLY"
  },
  "idObjetivo": "number (opcional)"
}
```

> `extraUnico` es obligatorio para tipo `UNIQUE`. `extraHabito` es obligatorio para tipo `HABIT`. `idObjetivo` indica que la tarea es subtarea de un objetivo.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "taskId": 5 }` |
| `403 Forbidden` | No es miembro o sin permisos |
| `400 Bad Request` | Tipo inválido, fecha no parseable o faltan datos extra |
| `500 Internal Server Error` | Error en base de datos |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_MALFORMEDMESSAGE (1)` | Tipo de tarea inválido |
| `ERR_RESOURCEMISSING (3)` | Faltan datos extra |
| `ERR_BADVALUE (6)` | Fecha no parseable |
| `ERR_FORBIDDEN (8)` | No es miembro o sin permisos |
| `ERR_INTERNALERROR (9)` | Error en base de datos |

---

### `GET /tasks/{gid}`

Devuelve todas las tareas de un grupo.

**Autenticación:** `AccessToken`.  
**Entrada:** ninguna. El `gid` va en la URL.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "taskList": [ ... ] }` |
| `403 Forbidden` | No es miembro |
| `400 Bad Request` | GID ausente u otros errores |
| `500 Internal Server Error` | Error en base de datos |

**Objeto de tarea:**
```json
{
  "id": 1,
  "titulo": "string",
  "descripcion": "string",
  "tipo": "UNIQUE | HABIT | OBJECTIVE",
  "estado": "ACTIVE | COMPLETE",
  "fecha_creacion": "ISO 8601",
  "fecha_actualizado": "ISO 8601",
  "fecha_completado": "ISO 8601 | null",
  "prioridad": 0,
  "recompensaXp": 50,
  "recompensaLudion": 10,
  "extraUnico": {
    "fechaLimite": "ISO 8601"
  },
  "extraHabito": {
    "numeroFrecuencia": 1,
    "frequency": "HOURLY | DAILY | WEEKLY | MONTHLY | YEARLY"
  },
  "idObjetivo": "number | null"
}
```

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_MALFORMEDMESSAGE (1)` | Tipo de tarea inválido |
| `ERR_RESOURCEMISSING (3)` | Faltan datos extra |
| `ERR_BADVALUE (6)` | Fecha no parseable |
| `ERR_FORBIDDEN (8)` | No es miembro, sin GID o sin permisos |
| `ERR_INTERNALERROR (9)` | Error en base de datos |

---

### `PATCH /tasks/{tid}/complete`

Marca una tarea como completada. Si ya estaba completada, la revierte a activa.

**Autenticación:** `AccessToken`.  
**Entrada:** ninguna. El `tid` va en la URL.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `403 Forbidden` | No es miembro o sin permisos |
| `400 Bad Request` | TID ausente u otros errores |
| `500 Internal Server Error` | Error en base de datos |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_MALFORMEDMESSAGE (1)` | Tipo de tarea inválido |
| `ERR_RESOURCEMISSING (3)` | Faltan datos |
| `ERR_BADVALUE (6)` | Fecha no parseable |
| `ERR_FORBIDDEN (8)` | No es miembro, sin TID o sin permisos |
| `ERR_INTERNALERROR (9)` | Error en base de datos |

---

### `PATCH /tasks/{tid}/edit`

Edita los datos de una tarea.

**Autenticación:** `AccessToken`.

**Entrada** (todos los campos son opcionales):
```json
{
  "titulo": "string",
  "descripcion": "string",
  "prioridad": 0
}
```

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `403 Forbidden` | No es miembro o sin permisos |
| `400 Bad Request` | TID ausente u otros errores |
| `500 Internal Server Error` | Error en base de datos |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_MALFORMEDMESSAGE (1)` | Tipo de tarea inválido |
| `ERR_RESOURCEMISSING (3)` | Faltan datos |
| `ERR_BADVALUE (6)` | Fecha no parseable |
| `ERR_FORBIDDEN (8)` | No es miembro, sin TID o sin permisos |
| `ERR_INTERNALERROR (9)` | Error en base de datos |

---

### `DELETE /tasks/{tid}/delete`

Elimina una tarea.

**Autenticación:** `AccessToken`.  
**Entrada:** ninguna. El `tid` va en la URL.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `403 Forbidden` | No es miembro o sin permisos |
| `400 Bad Request` | TID ausente u otros errores |
| `500 Internal Server Error` | Error en base de datos |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_MALFORMEDMESSAGE (1)` | Tipo de tarea inválido |
| `ERR_RESOURCEMISSING (3)` | Faltan datos |
| `ERR_BADVALUE (6)` | Fecha no parseable |
| `ERR_FORBIDDEN (8)` | No es miembro, sin TID o sin permisos |
| `ERR_INTERNALERROR (9)` | Error en base de datos |

---

## Avatar

### `GET /avatar/`

Devuelve todas las piezas de avatar del usuario, ordenadas.

**Autenticación:** `AccessToken`.  
**Entrada:** ninguna.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | JSON con datos del avatar (o `null` si no hay avatar equipado) |

**Cuerpo de respuesta `200`:**
```json
{
  "cosmeticId": 1,
  "name": "string",
  "imageRef": "string",
  "rareza": "COMUN | RARO | EPICO | LEGENDARIO"
}
```

---

## Store

### `GET /store/items`

Devuelve todos los ítems disponibles en la tienda.

**Autenticación:** `AccessToken`.  
**Entrada:** ninguna.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | Array de objetos cosmético (ver abajo) |

**Objeto cosmético:**
```json
{
  "id": 1,
  "name": "string",
  "desc": "string",
  "type": "PET | APP_THEME | AVATAR_PART",
  "price": 100,
  "assetRef": "string",
  "theme": "JSON con colores | null",
  "coleccion": "string",
  "owned": false,
  "equipped": false
}
```

---

### `POST /store/buy/{id}`

Intenta comprar un cosmético. El `id` va en la URL.

**Autenticación:** `AccessToken`.  
**Entrada:** ninguna.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `400 Bad Request` | Usuario/cosmético no encontrado, sin fondos o ya comprado |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_RESOURCEMISSING (3)` | Usuario o cosmético no encontrado |
| `ERR_RESOURCEALREADYEXISTS (4)` | El usuario ya tiene el cosmético |
| `ERR_INTERNALERROR (9)` | Fondos insuficientes |

---

### `POST /store/equip/{id}`

Equipa un cosmético que el usuario ya posee. El `id` va en la URL.

**Autenticación:** `AccessToken`.  
**Entrada:** ninguna.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `400 Bad Request` | Usuario/cosmético no encontrado o el usuario no lo posee |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_RESOURCEMISSING (3)` | Usuario o cosmético no encontrado |
| `ERR_RESOURCENOTCREATED (4)` | El usuario no tiene el cosmético |
| `ERR_INTERNALERROR (9)` | Error interno |

---

## SSE (Server-Sent Events)

### `GET /events/global`

Bus global de eventos SSE. Cualquier cliente conectado recibe estos eventos.

| Evento | Cuerpo | Descripción |
|---|---|---|
| `RELOAD.STORE` | *(vacío)* | La tienda ha cambiado; el cliente debe recargar los ítems |

---

### `GET /events/user`

Bus SSE por usuario. Protegido con `AccessToken`.

*(Eventos específicos por usuario — pendiente de documentar)*

---

## Pendiente (TODO)

- Métodos para asociar OAuth a una cuenta existente.
- Sistema completo de avatares.
- Seguridad: invalidar tokens en el servidor al cerrar sesión para evitar uso posterior.
