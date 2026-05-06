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
  "equippedPetRef": "string | null",
  "themeColors": "string | null",
  "isAdmin": false
}
```

---

### `PATCH /auth/editUser`

Edita los datos del usuario autenticado (nombre, idioma y offset UTC).

**Autenticación:** `AccessToken`.

**Entrada:**
```json
{
  "uid": 1,
  "nombreusu": "string (opcional)",
  "lang": "string (3 chars, opcional)",
  "utcOffset": 2.0
}
```

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `400 Bad Request` | Idioma no soportado |
| `500 Internal Server Error` | No se pudo editar el usuario |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_BADVALUE (6)` | Idioma no soportado |
| `ERR_RESOURCENOTMODIFIED` | No se pudo editar por razones desconocidas |

---

### `PATCH /auth/setEmailLogin`

Establece o actualiza el email y contraseña de acceso en una cuenta creada via OAuth.

**Autenticación:** `AccessToken`.

**Entrada:**
```json
{
  "email": "string (opcional)",
  "passwd": "string sin hashear (opcional)"
}
```

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `409 Conflict` | No se pudo actualizar el login de email |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_RESOURCENOTMODIFIED` | Error al actualizar las credenciales |

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

### `POST /auth/google/androidlogin`

Login/registro via Google para clientes Android. Recibe el `idToken` de Google Sign-In y lo verifica en el servidor.

**Autenticación:** ninguna.  

**Entrada:**
```json
{
  "idToken": "string (Google ID token)"
}
```

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "jwtAccessToken": "...", "jwtRefreshToken": "..." }` |
| `401 Unauthorized` | Token de Google inválido |
| `500 Internal Server Error` | Error al crear la cuenta o usuario no encontrado |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_INVALIDTOKEN (0)` | Token de Google inválido |
| `ERR_INTERNALERROR (9)` | Error al crear la cuenta |
| `ERR_RESOURCEMISSING (3)` | Usuario no encontrado tras crear cuenta |

---

### `POST /auth/setOauth`

Vincula un proveedor OAuth externo a la cuenta del usuario autenticado.

**Autenticación:** `AccessToken`.

**Entrada:**
```json
{
  "providerUid": "string (UID del proveedor)",
  "authProvider": "string (ej. GOOGLE)"
}
```

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `400 Bad Request` | Proveedor no permitido |
| `304 Not Modified` | Ya existe un OAuth del mismo proveedor vinculado |
| `401 Unauthorized` | UID inválido |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_INVALIDTOKEN (0)` | UID inválido |
| `ERR_MALFORMEDMESSAGE (1)` | Proveedor OAuth no reconocido |
| `ERR_RESOURCEALREADYEXISTS (4)` | Ya hay un OAuth del mismo proveedor vinculado |
| `ERR_FORBIDDEN (8)` | UID no válido |

---

### `POST /auth/deleteOauth`

Desvincula un proveedor OAuth de la cuenta del usuario autenticado.

**Autenticación:** `AccessToken`.

**Entrada:**
```json
{
  "authProvider": "string (ej. GOOGLE)"
}
```

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `400 Bad Request` | Proveedor no permitido |
| `304 Not Modified` | No se puede eliminar: la cuenta quedaría sin método de acceso |
| `401 Unauthorized` | UID inválido |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_INVALIDTOKEN (0)` | UID inválido |
| `ERR_MALFORMEDMESSAGE (1)` | Proveedor OAuth no reconocido |
| `ERR_RESOURCEMISSING (3)` | Eliminar este método dejaría la cuenta huérfana |
| `ERR_FORBIDDEN (8)` | UID no válido |

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
| `200 OK` | `{ "groupId": 5 }` con el ID del grupo creado |
| `400 Bad Request` | UID inválido |
| `409 Conflict` | No se pudo crear el grupo |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_BADVALUE (6)` | UID inválido en el token |
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

### `POST /groups/removeUser`

Expulsa a un miembro de un grupo. El dueño no puede ser expulsado. Solo moderadores y dueños pueden expulsar.

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
| `403 Forbidden` | Sin permisos o intento de expulsar al dueño |
| `404 Not Found` | Grupo inexistente o usuario no es miembro |
| `500 Internal Server Error` | Error en la base de datos |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_RESOURCEMISSING (3)` | Grupo inexistente o usuario no es miembro |
| `ERR_BADVALUE (6)` | UID inválido o intento de expulsar al dueño |
| `ERR_FORBIDDEN (8)` | Sin permisos para expulsar |
| `ERR_INTERNALERROR (9)` | Error con la base de datos |

---

### `POST /groups/leave`

El usuario autenticado abandona voluntariamente un grupo. El dueño no puede usar este endpoint; debe transferir la propiedad primero.

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
| `400 Bad Request` | El solicitante es el dueño del grupo |
| `401 Unauthorized` | UID del token inválido |
| `404 Not Found` | Grupo inexistente o usuario no es miembro |
| `500 Internal Server Error` | Error en la base de datos |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_RESOURCEMISSING (3)` | Grupo inexistente o usuario no es miembro |
| `ERR_BADVALUE (6)` | El dueño intenta abandonar sin transferir la propiedad |
| `ERR_INTERNALERROR (9)` | Error con la base de datos |

---

### `GET /groups/{gid}/members`

Devuelve la lista de miembros de un grupo con su nombre, rol y fecha de incorporación. Cualquier miembro puede consultarla.

**Autenticación:** `AccessToken`.  
**Entrada:** ninguna. El `gid` va en la URL.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "members": [ ... ] }` |
| `400 Bad Request` | GID no es un entero válido |
| `401 Unauthorized` | UID del token inválido |
| `403 Forbidden` | El solicitante no es miembro del grupo |
| `404 Not Found` | Grupo inexistente |
| `500 Internal Server Error` | Error en la base de datos |

**Objeto miembro:**
```json
{
  "uid": 1,
  "name": "string",
  "role": 0,
  "joinedAt": "ISO 8601 | null"
}
```

| `role` | Significado |
|---|---|
| `0` | Miembro normal |
| `1` | Moderador |
| `2` | Dueño |

---

### `PATCH /groups/setMemberRole`

Cambia el rol de un miembro. Solo el dueño puede invocar esta operación. No se puede asignar el rol de dueño ni cambiar el rol del propio dueño.

**Autenticación:** `AccessToken`.

**Entrada:**
```json
{
  "gid": 1,
  "userId": 2,
  "role": 1
}
```

> `role`: `0` = Miembro normal, `1` = Moderador. No se puede asignar `2` (dueño) por esta ruta.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `400 Bad Request` | Rol inválido o intento de cambiar el rol del dueño |
| `401 Unauthorized` | UID del token inválido |
| `403 Forbidden` | El solicitante no es el dueño |
| `404 Not Found` | Grupo inexistente o usuario no es miembro |
| `500 Internal Server Error` | Error en la base de datos |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_RESOURCEMISSING (3)` | Grupo inexistente o usuario no es miembro |
| `ERR_BADVALUE (6)` | Rol inválido |
| `ERR_FORBIDDEN (8)` | Solo el dueño puede cambiar roles |
| `ERR_INTERNALERROR (9)` | Error con la base de datos |

---

### `GET /groups/{gid}/audit`

Recupera el historial de eventos de auditoría de un grupo con paginación. Cualquier miembro puede consultarlo.

**Autenticación:** `AccessToken`.  
**Entrada:** ninguna. El `gid` va en la URL.

**Query params:**

| Parámetro | Tipo | Defecto | Descripción |
|---|---|---|---|
| `limit` | `int` | `50` | Número máximo de eventos (máx. 200) |
| `offset` | `long` | `0` | Desplazamiento para paginación |

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "events": [ ... ] }` |
| `400 Bad Request` | GID no es un entero válido |
| `401 Unauthorized` | UID del token inválido |
| `403 Forbidden` | El solicitante no es miembro del grupo |
| `404 Not Found` | Grupo inexistente |
| `500 Internal Server Error` | Error en la base de datos |

**Objeto evento de auditoría:**
```json
{
  "id": 1,
  "actorUid": 2,
  "eventType": "string",
  "payloadJson": "string | null",
  "createdAt": "ISO 8601"
}
```

> Valores conocidos de `eventType`: `invite_created`, `invite_revoked`, `member_joined_by_invite`, `member_left`, `member_role_changed`.

---

### `POST /groups/inviteUrl`

Genera una URL de invitación para un grupo. Solo moderadores y dueños pueden generarla.

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
| `200 OK` | `{ "inviteUrl": "string" }` |
| `400 Bad Request` | Código inválido, expirado, revocado o usos agotados |
| `401 Unauthorized` | UID del token inválido |
| `403 Forbidden` | Sin permisos o grupo personal |
| `404 Not Found` | Grupo inexistente |
| `500 Internal Server Error` | Error en la base de datos |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_RESOURCEMISSING (3)` | Grupo inexistente |
| `ERR_RESOURCEALREADYEXISTS (4)` | El usuario ya es miembro |
| `ERR_BADVALUE (6)` | URL o código inválido, expirado, revocado o usos agotados |
| `ERR_FORBIDDEN (8)` | Sin permisos o grupo personal |
| `ERR_INTERNALERROR (9)` | Error con la base de datos |

---

### `POST /groups/joinByUrl`

Une al usuario autenticado a un grupo mediante una URL de invitación. Soporta el formato legado (`?gid=`) y el seguro (`?code=`).

**Autenticación:** `AccessToken`.

**Entrada:**
```json
{
  "inviteUrl": "string"
}
```

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `400 Bad Request` | URL inválida, código inválido, expirado, revocado, usos agotados o ya miembro |
| `401 Unauthorized` | UID del token inválido |
| `403 Forbidden` | Sin permisos o grupo personal |
| `404 Not Found` | Grupo inexistente |
| `500 Internal Server Error` | Error en la base de datos |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_RESOURCEMISSING (3)` | Grupo inexistente |
| `ERR_RESOURCEALREADYEXISTS (4)` | El usuario ya es miembro |
| `ERR_BADVALUE (6)` | URL o código inválido, expirado, revocado o usos agotados |
| `ERR_FORBIDDEN (8)` | Sin permisos o grupo personal |
| `ERR_INTERNALERROR (9)` | Error con la base de datos |

---

### `POST /groups/joinByCode`

Une al usuario autenticado a un grupo mediante un código de invitación directo.

**Autenticación:** `AccessToken`.

**Entrada:**
```json
{
  "code": "string"
}
```

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `400 Bad Request` | Código inválido, expirado, revocado, usos agotados o ya miembro |
| `401 Unauthorized` | UID del token inválido |
| `403 Forbidden` | Intento de unirse a un grupo personal |
| `404 Not Found` | Grupo inexistente |
| `500 Internal Server Error` | Error en la base de datos |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_RESOURCEMISSING (3)` | Grupo inexistente |
| `ERR_RESOURCEALREADYEXISTS (4)` | El usuario ya es miembro |
| `ERR_BADVALUE (6)` | Código inválido, expirado, revocado o usos agotados |
| `ERR_FORBIDDEN (8)` | Intento de unirse a un grupo personal |
| `ERR_INTERNALERROR (9)` | Error con la base de datos |

---

### `POST /groups/invites`

Crea una invitación segura basada en token para un grupo. Permite configurar expiración y límite de usos. Solo moderadores y dueños.

**Autenticación:** `AccessToken`.

**Entrada:**
```json
{
  "gid": 1,
  "expiresInSeconds": 86400,
  "maxUses": 10
}
```

> `expiresInSeconds` y `maxUses` son opcionales; si se omiten la invitación no expira y tiene usos ilimitados.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | Objeto `InviteOut` con el código y URL (ver abajo) |
| `401 Unauthorized` | UID del token inválido |
| `403 Forbidden` | Sin permisos o grupo personal |
| `404 Not Found` | Grupo inexistente |
| `500 Internal Server Error` | Error en la base de datos |

**Cuerpo de respuesta `200`:**
```json
{
  "code": "string",
  "inviteUrl": "string",
  "expiresAt": "ISO 8601 | null",
  "maxUses": 10,
  "usesCount": 0,
  "revokedAt": "ISO 8601 | null"
}
```

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_RESOURCEMISSING (3)` | Grupo inexistente |
| `ERR_FORBIDDEN (8)` | Sin permisos para crear la invitación |
| `ERR_INTERNALERROR (9)` | Error con la base de datos |

---

### `GET /groups/{gid}/invites`

Lista todas las invitaciones de un grupo (activas, revocadas y expiradas). Solo moderadores y dueños.

**Autenticación:** `AccessToken`.  
**Entrada:** ninguna. El `gid` va en la URL.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "invites": [ ... ] }` con objetos `InviteOut` |
| `400 Bad Request` | GID no es un entero válido |
| `401 Unauthorized` | UID del token inválido |
| `403 Forbidden` | Sin permisos |
| `404 Not Found` | Grupo inexistente |
| `500 Internal Server Error` | Error en la base de datos |

---

### `POST /groups/invites/revoke`

Revoca una invitación activa de un grupo. Solo moderadores y dueños.

**Autenticación:** `AccessToken`.

**Entrada:**
```json
{
  "gid": 1,
  "code": "string"
}
```

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `400 Bad Request` | Código de invitación no encontrado |
| `401 Unauthorized` | UID del token inválido |
| `403 Forbidden` | Sin permisos |
| `404 Not Found` | Grupo inexistente |
| `500 Internal Server Error` | Error en la base de datos |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_RESOURCEMISSING (3)` | Grupo inexistente |
| `ERR_BADVALUE (6)` | Código de invitación no encontrado |
| `ERR_FORBIDDEN (8)` | Sin permisos para revocar |
| `ERR_INTERNALERROR (9)` | Error con la base de datos |

---

### `GET /groups/redirectInvite`

Puente de compatibilidad para URLs de invitación legadas. Redirige al frontend según los parámetros de consulta.

**Autenticación:** ninguna.

**Query params:**

| Parámetro | Descripción |
|---|---|
| `code` | Código de invitación seguro (tiene prioridad sobre `gid`) |
| `gid` | Identificador de grupo del flujo legado |

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `302 Found` | Redirección a la URL configurada en `INVITE_BASE_URL` |
| `400 Bad Request` | Ninguno de los parámetros esperados está presente |

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

### `PATCH /groups/passOwnership`

Transfiere la propiedad del grupo a otro miembro existente. Solo el dueño actual puede realizar esta operación.

**Autenticación:** `AccessToken`.

**Entrada:**
```json
{
  "gid": 1,
  "newOwnerUserId": 2
}
```

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `400 Bad Request` | El destino no es miembro o ya es el dueño |
| `401 Unauthorized` | UID del token inválido |
| `403 Forbidden` | El solicitante no es el dueño actual |
| `404 Not Found` | Grupo inexistente |
| `500 Internal Server Error` | Error en la base de datos |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_RESOURCEMISSING (3)` | Grupo inexistente |
| `ERR_BADVALUE (6)` | El nuevo dueño no es miembro o ya es el dueño |
| `ERR_FORBIDDEN (8)` | Solo el dueño puede transferir la propiedad |
| `ERR_INTERNALERROR (9)` | Error con la base de datos |

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
  "tipo": "UNICO | HABITO | OBJETIVO",
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

> `extraUnico` es obligatorio para tipo `UNICO`. `extraHabito` es obligatorio para tipo `HABITO`. `idObjetivo` indica que la tarea es subtarea de un objetivo.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `201 Created` | `{ "id": 5 }` con el ID de la tarea creada |
| `403 Forbidden` | No es miembro o sin permisos |
| `400 Bad Request` | Tipo inválido, fecha no parseable o faltan datos extra |
| `500 Internal Server Error` | Error en base de datos |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_MALFORMEDMESSAGE (1)` | Tipo de tarea inválido |
| `ERR_RESOURCEMISSING (3)` | Faltan datos extra |
| `ERR_FORBIDDEN (8)` | No es miembro o sin permisos |
| `ERR_INTERNALERROR (9)` | Error en base de datos |

---

### `POST /tasks/{gid}`

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
  "gid": 1,
  "uid": null,
  "titulo": "string",
  "descripcion": "string",
  "tipo": "UNICO | HABITO | OBJETIVO",
  "estado": "ACTIVE | COMPLETE",
  "fecha_creacion": "ISO 8601 | null",
  "fecha_actualizado": "ISO 8601 | null",
  "fecha_completado": "ISO 8601 | null",
  "fechaValida": "ISO 8601 | null",
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

> Para tareas de tipo `HABITO`, el campo `estado` puede ser `COMPLETE` dinámicamente si el hábito fue completado hoy (`ultima_vez_completada == hoy`).

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

Marca una tarea como completada y otorga la recompensa de XP y Ludiones al usuario. Para hábitos actualiza `ultima_vez_completada` y el contador de racha.

**Autenticación:** `AccessToken`.  
**Entrada:** ninguna. El `tid` va en la URL.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `400 Bad Request` | TID ausente u inválido |
| `404 Not Found` | Tarea no encontrada o no se pudo completar |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_RESOURCEMISSING (3)` | No se pudo completar la tarea |

---

### `PATCH /tasks/{tid}/uncomplete`

Revierte el estado de una tarea completada a activa y devuelve la recompensa (XP y Ludiones) al usuario. Para hábitos, retrocede la racha si fue completado hoy. Si la tarea pertenece a un objetivo completado, lo reactiva en cascada.

**Autenticación:** `AccessToken`.  
**Entrada:** ninguna. El `tid` va en la URL.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `400 Bad Request` | TID ausente u inválido |
| `404 Not Found` | Tarea no encontrada o no se pudo revertir |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_RESOURCEMISSING (3)` | No se pudo revertir la tarea |

---

### `PATCH /tasks/{tid}/edit`

Edita los datos de una tarea. Solo moderadores y dueños del grupo pueden editar. Recalcula la recompensa si cambia la prioridad o la frecuencia del hábito.

**Autenticación:** `AccessToken`.

**Entrada** (todos los campos son opcionales):
```json
{
  "titulo": "string",
  "descripcion": "string",
  "prioridad": 0,
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

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `400 Bad Request` | TID ausente, tipo inválido, fecha no parseable o faltan datos |
| `403 Forbidden` | No es miembro o sin permisos |
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

### `POST /avatar/`

Devuelve la pieza de avatar equipada actualmente por el usuario.

**Autenticación:** `AccessToken`.  
**Entrada:** ninguna.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | JSON con datos del avatar equipado, o `{ "avatar": null }` si no hay ninguno |

**Cuerpo de respuesta `200`:**
```json
{
  "avatar": {
    "cosmeticId": 1,
    "name": "string",
    "imageRef": "string",
    "rareza": "COMUN | RARO | EPICO | LEGENDARIO"
  }
}
```

---

## Store

### `GET /store/items`

Devuelve todos los ítems disponibles en la tienda (excluye ítems ocultos).

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

### `GET /store/items/admin`

Devuelve todos los ítems de la tienda incluyendo los ocultos. Solo administradores del servidor.

**Autenticación:** `AccessToken`.  
**Entrada:** ninguna.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | Array de objetos cosmético (igual que `/store/items`) |
| `403 Forbidden` | El usuario no es administrador |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_FORBIDDEN (8)` | El usuario no es administrador del servidor |

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

Bus global de eventos SSE. Cualquier cliente conectado recibe estos eventos. No requiere autenticación.

| Evento | Cuerpo | Descripción |
|---|---|---|
| `RELOAD.STORE` | *(vacío)* | La tienda ha cambiado; el cliente debe recargar los ítems |

---

### `GET /events/user`

Bus SSE por usuario. Protegido con `AccessToken`. Al desconectarse, el servidor emite internamente `SIGN.OFF` para ese usuario.

| Evento | Cuerpo | Descripción |
|---|---|---|
| `ADDED.TASK` | `{ "gid": 1, "tid": 5 }` | Se ha creado una nueva tarea en el grupo `gid` con ID `tid` |
| `SIGN.OFF` | *(vacío)* | El usuario ha sido desconectado |

---

## Admin

> Todas las rutas de este bloque requieren `AccessToken` de un usuario con rol **administrador del servidor**. Si el usuario no es admin, el servidor responde con `403 Forbidden` y `ERR_FORBIDDEN (8)`.

### `POST /admin/cosmetic/upload`

Crea un nuevo cosmético y sube el archivo de asset asociado. Se envía como `multipart/form-data`.

**Autenticación:** `AccessToken` (admin).

**Campos del formulario multipart:**

| Campo | Tipo | Descripción |
|---|---|---|
| `engName` | string | Nombre en inglés |
| `espName` | string | Nombre en español (fallback a `engName` si vacío) |
| `rusName` | string | Nombre en ruso (fallback a `engName` si vacío) |
| `desc` | string | Descripción del cosmético |
| `type` | string | `PET \| APP_THEME \| AVATAR_PART` |
| `price` | int | Precio en Ludiones (0 = calcular automáticamente según rareza y tipo) |
| `rarity` | string | `COMUN \| RARO \| EPICO \| LEGENDARIO` |
| `collection` | string | Colección (por defecto `DEFAULT`) |
| `theme` | string | JSON de colores (solo para `APP_THEME`) |
| archivo | file | Archivo Lottie `.json` (obligatorio para `PET` y `AVATAR_PART`) |

> **Precio automático** según tipo y rareza: `AVATAR_PART` base 100, `APP_THEME` base 500, `PET` base 1000; multiplicadores: `COMUN`×1, `RARO`×2.5, `EPICO`×5, `LEGENDARIO`×10.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `201 Created` | `{ "acknowledged": true }` |
| `400 Bad Request` | Falta el archivo Lottie o el JSON de tema |
| `401 Unauthorized` | Token inválido |
| `403 Forbidden` | El usuario no es admin |
| `500 Internal Server Error` | Error al guardar en BD o al escribir el archivo |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_INVALIDTOKEN (0)` | Token inválido |
| `ERR_MALFORMEDMESSAGE (1)` | Falta el archivo Lottie o el JSON de tema |
| `ERR_FORBIDDEN (8)` | El usuario no es admin |
| `ERR_INTERNALERROR (9)` | Error al guardar en BD |

---

### `POST /admin/cosmetic/upload/{cid}`

Actualiza el asset y los metadatos de un cosmético existente. Se envía como `multipart/form-data`. Los mismos campos que `/admin/cosmetic/upload`. El `cid` va en la URL.

**Autenticación:** `AccessToken` (admin).

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `201 Created` | `{ "acknowledged": true }` |
| `400 Bad Request` | Falta el archivo Lottie o CID inválido |
| `401 Unauthorized` | Token inválido |
| `403 Forbidden` | El usuario no es admin |
| `500 Internal Server Error` | Error al guardar en BD o al escribir el archivo |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_INVALIDTOKEN (0)` | Token inválido |
| `ERR_MALFORMEDMESSAGE (1)` | Falta el archivo Lottie |
| `ERR_FORBIDDEN (8)` | CID inválido o usuario no es admin |
| `ERR_INTERNALERROR (9)` | Error al guardar en BD |

---

### `DELETE /admin/cosmetic/delete/{cid}`

Elimina un cosmético del servidor. El `cid` va en la URL.

**Autenticación:** `AccessToken` (admin).  
**Entrada:** ninguna.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `400 Bad Request` | CID inválido o no se pudo eliminar |
| `401 Unauthorized` | Token inválido |
| `403 Forbidden` | El usuario no es admin |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_INVALIDTOKEN (0)` | Token inválido |
| `ERR_FORBIDDEN (8)` | CID inválido o usuario no es admin |
| `ERR_RESOURCENOTMODIFIED` | No se pudo eliminar el cosmético |

---

### `POST /admin/users`

Devuelve datos básicos de todos los usuarios del servidor.

**Autenticación:** `AccessToken` (admin).  
**Entrada:** ninguna.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | Array de objetos de usuario simplificado |
| `401 Unauthorized` | Token inválido |
| `403 Forbidden` | El usuario no es admin |

**Objeto usuario:**
```json
{
  "id": 1,
  "nombre": "string",
  "rol": "Admin | User",
  "nivel": 5
}
```

---

### `GET /admin/users/{uid}`

Devuelve datos básicos de un usuario específico. El `uid` va en la URL.

**Autenticación:** `AccessToken` (admin).  
**Entrada:** ninguna.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | Objeto usuario simplificado (ver arriba) |
| `401 Unauthorized` | Token inválido |
| `403 Forbidden` | El usuario no es admin |
| `404 Not Found` | Usuario no encontrado |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_INVALIDTOKEN (0)` | Token inválido |
| `ERR_MALFORMEDMESSAGE (1)` | UID no numérico |
| `ERR_RESOURCEMISSING (3)` | Usuario no existe |
| `ERR_FORBIDDEN (8)` | El usuario no es admin |

---

### `POST /admin/groups`

Devuelve todos los grupos del servidor.

**Autenticación:** `AccessToken` (admin).  
**Entrada:** ninguna.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | Array de objetos de grupo |
| `401 Unauthorized` | Token inválido |
| `403 Forbidden` | El usuario no es admin |

---

### `POST /admin/groups/add`

Crea un grupo asignándolo a un usuario específico como dueño.

**Autenticación:** `AccessToken` (admin).

**Entrada:**
```json
{
  "name": "string",
  "uid": 1,
  "desc": "string"
}
```

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `400 Bad Request` | No se pudo crear el grupo |
| `401 Unauthorized` | Token inválido |
| `403 Forbidden` | El usuario no es admin |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_INVALIDTOKEN (0)` | Token inválido |
| `ERR_FORBIDDEN (8)` | El usuario no es admin |
| `ERR_RESOURCENOTCREATED (7)` | No se pudo crear el grupo |

---

### `DELETE /admin/groups/delete/{gid}`

Elimina un grupo del servidor. El `gid` va en la URL.

**Autenticación:** `AccessToken` (admin).  
**Entrada:** ninguna.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `304 Not Modified` | No se pudo eliminar |
| `400 Bad Request` | GID inválido |
| `401 Unauthorized` | Token inválido |
| `403 Forbidden` | El usuario no es admin |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_INVALIDTOKEN (0)` | Token inválido |
| `ERR_MALFORMEDMESSAGE (1)` | GID no numérico |
| `ERR_FORBIDDEN (8)` | El usuario no es admin |
| `ERR_RESOURCENOTMODIFIED` | No se pudo eliminar el grupo |

---

### `POST /admin/user/create`

Crea un usuario directamente, permitiendo asignarle un rol (normal o admin).

**Autenticación:** `AccessToken` (admin).

**Entrada:**
```json
{
  "name": "string",
  "email": "string",
  "password": "string (sin hashear)",
  "lang": "string (3 chars)",
  "utcOffset": 2.0,
  "role": "ADMIN | USER"
}
```

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `400 Bad Request` | No se pudo crear el usuario |
| `401 Unauthorized` | Token inválido |
| `403 Forbidden` | El usuario no es admin |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_INVALIDTOKEN (0)` | Token inválido |
| `ERR_FORBIDDEN (8)` | El usuario no es admin |
| `ERR_RESOURCENOTCREATED (7)` | No se pudo crear el usuario |

---

### `POST /admin/user/delete/{cid}`

Elimina un usuario del servidor. El `cid` (ID del usuario) va en la URL.

**Autenticación:** `AccessToken` (admin).  
**Entrada:** ninguna.

**Respuestas HTTP:**

| HTTP | Descripción |
|---|---|
| `200 OK` | `{ "acknowledged": true }` |
| `400 Bad Request` | CID inválido o no se pudo eliminar |
| `401 Unauthorized` | Token inválido |
| `403 Forbidden` | El usuario no es admin |

**Códigos de error del servidor:**

| Código | Cuándo |
|---|---|
| `ERR_INVALIDTOKEN (0)` | Token inválido |
| `ERR_MALFORMEDMESSAGE (1)` | CID no numérico |
| `ERR_FORBIDDEN (8)` | El usuario no es admin |
| `ERR_RESOURCENOTCREATED (7)` | No se pudo eliminar el usuario |


