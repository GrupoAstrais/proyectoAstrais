package tasks

import OK_MESSAGE_RESPONSE
import TASKTYPE_UNIQUE
import com.astrais.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

/**
 * Datos adicionales requeridos para crear una tarea de tipo único.
 * Se incluye como parte de [CreateTareaRequest] cuando `tipo` es `UNICO`.
 *
 * @property fechaLimite Fecha y hora de vencimiento en formato ISO-8601 UTC
 *   (ej. `"2026-12-31T23:59:59Z"`). El backend parsea este valor con
 *   `kotlinx.datetime.Instant.parse`.
 */
@Serializable
data class CreateTareaUniqueData(
    val fechaLimite : String
)

/**
 * Frecuencia de repetición de un hábito.
 *
 * El valor de cadena (`value`) es el literal persistido en la base de datos y
 * devuelto en las respuestas de la API. La conversión entre valor de API y
 * enum se realiza con [fromValue].
 *
 * @property HOURLY El hábito se repite cada hora.
 * @property DAILY El hábito se repite una vez al día.
 * @property WEEKLY El hábito se repite una vez a la semana.
 * @property MONTHLY El hábito se repite una vez al mes.
 * @property YEARLY El hábito se repite una vez al año.
 */
enum class HabitFrequency(val value: String) {
    HOURLY("HOURLY"),
    DAILY("DAILY"),
    WEEKLY("WEEKLY"),
    MONTHLY("MONTHLY"),
    YEARLY("YEARLY");

    companion object {
        /**
         * Convierte un valor de cadena proveniente de la API o la base de datos al
         * enum correspondiente, ignorando diferencias de mayúsculas.
         *
         * @param value Cadena a convertir (p. ej. `"daily"`, `"WEEKLY"`).
         * @return El [HabitFrequency] correspondiente, o `null` si el valor no coincide
         *   con ninguna entrada conocida.
         */
        fun fromValue(value: String): HabitFrequency? =
            entries.find { it.value.equals(value, ignoreCase = true) }
    }
}

/**
 * Datos adicionales requeridos para crear una tarea de tipo hábito.
 * Se incluye como parte de [CreateTareaRequest] cuando `tipo` es `HABITO`.
 *
 * @property numeroFrecuencia Variación numérica de la frecuencia (p. ej. cada cuántas
 *   unidades de [frequency] se repite el hábito). Por defecto `1`.
 * @property frequency Unidad de tiempo de la repetición. Por defecto [HabitFrequency.DAILY].
 */
@Serializable
data class CreateTareaHabitData(
    val numeroFrecuencia : Int = 1,
    val frequency : HabitFrequency = HabitFrequency.DAILY
)

/**
 * Cuerpo de la petición para crear una nueva tarea dentro de un grupo.
 * Enviado al endpoint `POST /tasks`.
 *
 * El campo `tipo` determina qué bloque de datos adicionales es obligatorio:
 * - `"UNICO"` -> se requiere [extraUnico] con la fecha de vencimiento.
 * - `"HABITO"` -> se requiere [extraHabito] con la frecuencia.
 * - `"OBJETIVO"` -> no requiere campos extra.
 *
 * @property gid Identificador del grupo al que pertenecerá la tarea.
 * @property titulo Título descriptivo de la tarea.
 * @property descripcion Descripción extendida o instrucciones de la tarea.
 * @property tipo Tipo de tarea: `"UNICO"`, `"HABITO"` u `"OBJETIVO"`.
 * @property prioridad Nivel de prioridad como entero: `0` = Baja, `1` = Media, `2` = Alta.
 * @property extraUnico Datos específicos para tareas únicas, o `null` si el tipo no es `UNICO`.
 * @property extraHabito Datos específicos para hábitos, o `null` si el tipo no es `HABITO`.
 * @property idObjetivo ID de la tarea objetivo padre si esta tarea es una subtarea, o `null`
 *   si es una tarea de nivel superior.
 */
@Serializable
data class CreateTareaRequest(
    val gid: Int,
    val titulo: String,
    val descripcion: String = "",
    val tipo: String = TASKTYPE_UNIQUE,
    val prioridad: Int = 0,

    val extraUnico : CreateTareaUniqueData? = null,
    val extraHabito : CreateTareaHabitData? = null,
    val idObjetivo: Int? = null // Si no es null, es subtarea del objetivo
)

/**
 * DTO de salida que representa una tarea devuelta por el servidor.
 * Devuelto dentro del campo `taskList` de la respuesta de `POST /tasks/{gid}`.
 *
 * Para tareas de tipo hábito, el campo `estado` puede ser `COMPLETE` aunque
 * la tarea no esté en ese estado en base de datos: el servidor lo establece
 * dinámicamente si el hábito fue completado hoy (`ultima_vez_completada == hoy`).
 *
 * @property id Identificador único de la tarea en el servidor.
 * @property gid Identificador del grupo al que pertenece la tarea, o `null` si es personal.
 * @property uid Identificador del usuario propietario (tareas personales), o `null` si es grupal.
 * @property titulo Título de la tarea.
 * @property descripcion Descripción extendida de la tarea.
 * @property tipo Tipo de tarea: `"UNICO"`, `"HABITO"` u `"OBJETIVO"`.
 * @property estado Estado actual: `"ACTIVE"` o `"COMPLETE"`.
 * @property prioridad Nivel de prioridad: `0` = Baja, `1` = Media, `2` = Alta.
 * @property recompensaXp Puntos de experiencia que otorga completar la tarea.
 * @property recompensaLudion Ludiones (moneda del juego) que otorga completar la tarea.
 * @property fecha_creacion Fecha de creación como cadena ISO-8601, o `null`.
 * @property fecha_actualizado Fecha de la última modificación como cadena ISO-8601, o `null`.
 * @property fecha_completado Fecha en que se completó la tarea como cadena ISO-8601, o `null`
 *   si aún no está completada.
 * @property fechaValida Fecha de vencimiento para tareas únicas, o `null` si no aplica.
 * @property extraUnico Datos extra de tareas únicas (fecha de vencimiento), o `null`.
 * @property extraHabito Datos extra de hábitos (frecuencia), o `null`.
 * @property idObjetivo ID de la tarea objetivo padre si es subtarea, o `null`.
 */
@Serializable
data class TareaResponse(
    val id: Int,
    val gid: Int?,
    val uid: Int?,
    val titulo: String,
    val descripcion: String,
    val tipo: String,
    val estado: String,
    val prioridad: Int,
    val recompensaXp: Int,
    val recompensaLudion: Int,
    val fecha_creacion: String? = null,
    val fecha_actualizado: String? = null,
    val fecha_completado: String? = null,
    val fechaValida: String? = null,
    val extraUnico : CreateTareaUniqueData? = null,
    val extraHabito : CreateTareaHabitData? = null,
    val idObjetivo: Int? = null
)

/**
 * Cuerpo de la petición para editar una tarea existente.
 * Enviado al endpoint `PATCH /tasks/{tid}/edit`.
 * Todos los campos son opcionales; los `null` no se modifican en el servidor.
 *
 * @property titulo Nuevo título de la tarea, o `null` para no modificarlo.
 * @property descripcion Nueva descripción, o `null` para no modificarla.
 * @property prioridad Nueva prioridad como entero, o `null` para no modificarla.
 * @property extraUnico Nueva fecha de vencimiento para tareas únicas, o `null`.
 * @property extraHabito Nueva configuración de frecuencia para hábitos, o `null`.
 * @property idObjetivo ID del nuevo objetivo padre, o `null`.
 */
@Serializable
data class EditTareaRequest(
    val titulo: String? = null,
    val descripcion: String? = null,
    val prioridad: Int? = null,
    val extraUnico: CreateTareaUniqueData? = null,
    val extraHabito: CreateTareaHabitData? = null,
    val idObjetivo: Int? = null
)

/**
 * Registra todas las rutas HTTP relacionadas con la gestión de tareas.
 *
 * Todas las rutas requieren un JWT de acceso válido (`access-jwt`). El UID del
 * usuario se extrae del subject del token en cada handler.
 *
 * Rutas registradas:
 * - `POST   /tasks`                   — Crea una nueva tarea en un grupo.
 * - `POST   /tasks/{gid}`             — Lista las tareas de un grupo.
 * - `PATCH  /tasks/{tid}/complete`    — Marca una tarea como completada.
 * - `PATCH  /tasks/{tid}/edit`        — Edita los campos de una tarea.
 * - `DELETE /tasks/{tid}/delete`      — Elimina permanentemente una tarea.
 * - `PATCH  /tasks/{tid}/uncomplete`  — Revierte el estado completado de una tarea.
 */
fun Route.tareaRoutes() {
    authenticate("access-jwt") {
        /**
         * Crea una nueva tarea dentro de un grupo.
         * El solicitante debe ser Owner o Moderador del grupo.
         * Calcula automáticamente la recompensa de XP y Ludiones según el tipo y prioridad.
         *
         * Cuerpo: [CreateTareaRequest].
         *
         * Respuestas:
         * - `201 Created`              — JSON `{"id": <Int>}` con el ID de la tarea creada.
         * - `400 Bad Request`          — Tipo inválido o datos extra faltantes.
         * - `403 Forbidden`            — El solicitante no es miembro o no tiene permisos.
         * - `500 Internal Server Error`— Error de base de datos.
         */
        post("/tasks") {
            try {
                val uid = call.principal<JWTPrincipal>()!!.subject?.toInt() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val requestData = call.receive<CreateTareaRequest>()
                val response = getTaskDaoImpl().createTask(requestData, uid)

                when (response.first) {
                    CreateTaskRepoResponse.RESP_OK -> {
                        call.respond(HttpStatusCode.Created, mapOf("id" to response.second))
                    }
                    CreateTaskRepoResponse.RESP_NOTMEMBER -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "Not a member"))
                    CreateTaskRepoResponse.RESP_EXPOSEDERR -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Database error"))
                    CreateTaskRepoResponse.RESP_MISSINGDATA -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "Missing data"))
                    CreateTaskRepoResponse.RESP_INVALIDTYPE -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal, "Invalid task type"))
                    else -> call.respond(HttpStatusCode.BadRequest)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("errorText" to "Fallo en Ktor: ${e.stackTraceToString()}")
                )
            }
        }

        /**
         * Lista todas las tareas activas de un grupo.
         * Cualquier miembro del grupo puede consultar las tareas.
         * Para hábitos, el estado `COMPLETE` se establece dinámicamente si fue completado hoy.
         *
         * Parámetro de ruta:
         * - `gid` — Identificador del grupo.
         *
         * Respuestas:
         * - `200 OK`                   — JSON `{"taskList": [...]}`  con la lista de [TareaResponse].
         * - `400 Bad Request`          — GID inválido o datos mal formados.
         * - `403 Forbidden`            — El solicitante no es miembro del grupo.
         * - `500 Internal Server Error`— Error de base de datos.
         */
        post("/tasks/{gid}") {
            val uid = call.principal<JWTPrincipal>()!!.subject?.toInt() ?: return@post call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "No UID available"))
            val gid = call.parameters["gid"]?.toInt() ?: return@post call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "Didn't catch the Group ID"))

            val response = getTaskDaoImpl().getGroupTasks(gid, uid)

            when (response.first){
                CreateTaskRepoResponse.RESP_OK -> call.respond(HttpStatusCode.OK, mapOf("taskList" to response.second))
                CreateTaskRepoResponse.RESP_NOTMEMBER -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "Not a member of this group"))
                CreateTaskRepoResponse.RESP_EXPOSEDERR -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Exposed error"))
                CreateTaskRepoResponse.RESP_INVALIDTYPE -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal, "Invalid task type"))
                CreateTaskRepoResponse.RESP_NOPERMISSION -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "No permission to do the action"))
                CreateTaskRepoResponse.RESP_INVALIDDATE -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "The date couldn't be parsed"))
                CreateTaskRepoResponse.RESP_MISSINGDATA -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "Missing extra task data"))
            }
        }

        /**
         * Marca una tarea como completada y otorga la recompensa de XP y Ludiones al usuario.
         * Para hábitos actualiza `ultima_vez_completada` y el contador de racha.
         * Solo miembros del grupo pueden completar tareas.
         *
         * Parámetro de ruta:
         * - `tid` — Identificador de la tarea.
         *
         * Respuestas:
         * - `200 OK`       — Tarea completada correctamente.
         * - `400 Bad Request` — TID inválido.
         * - `404 Not Found`   — La tarea no existe o no se pudo completar.
         */
        patch("/tasks/{tid}/complete") {
            val uid = call.principal<JWTPrincipal>()!!.subject?.toInt() ?: return@patch call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "No UID available"))
            val tid = call.parameters["tid"]?.toInt() ?: return@patch call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "Didn't catch the Task ID"))

            val ok = getTaskDaoImpl().completeTask(tid, uid)
            if (ok) call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
            else call.respond(HttpStatusCode.NotFound, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "Couldn't complete task"))
        }

        /**
         * Edita los campos de una tarea existente. Solo el Owner y los Moderadores
         * del grupo pueden editar tareas. Recalcula la recompensa si cambia la prioridad
         * o la frecuencia del hábito.
         *
         * Parámetro de ruta:
         * - `tid` — Identificador de la tarea a editar.
         *
         * Cuerpo: [EditTareaRequest] (todos los campos son opcionales).
         *
         * Respuestas:
         * - `200 OK`                   — Tarea editada correctamente.
         * - `400 Bad Request`          — TID inválido o datos mal formados.
         * - `403 Forbidden`            — Sin permisos de edición.
         * - `500 Internal Server Error`— Error de base de datos.
         */
        patch("/tasks/{tid}/edit") {
            val uid = call.principal<JWTPrincipal>()!!.subject?.toInt() ?: return@patch call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "No UID available"))
            val tid = call.parameters["tid"]?.toInt() ?: return@patch call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "Didn't catch the Task ID"))


            val data = call.receive<EditTareaRequest>()
            val response = getTaskDaoImpl().editTask(uid, tid, data)
            when (response){
                CreateTaskRepoResponse.RESP_OK -> call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
                CreateTaskRepoResponse.RESP_NOTMEMBER -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "Not a member of this group"))
                CreateTaskRepoResponse.RESP_EXPOSEDERR -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Exposed error"))
                CreateTaskRepoResponse.RESP_INVALIDTYPE -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal, "Invalid task type"))
                CreateTaskRepoResponse.RESP_NOPERMISSION -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "No permission to do the action"))
                CreateTaskRepoResponse.RESP_INVALIDDATE -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "The date couldn't be parsed"))
                CreateTaskRepoResponse.RESP_MISSINGDATA -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "Missing extra task data"))
            }
        }

        /**
         * Elimina permanentemente una tarea del sistema.
         * Solo el Owner y los Moderadores del grupo pueden eliminar tareas.
         * Si la tarea tiene subtareas (es un objetivo), estas también son eliminadas
         * en cascada por la base de datos.
         *
         * Parámetro de ruta:
         * - `tid` — Identificador de la tarea a eliminar.
         *
         * Respuestas:
         * - `200 OK`                   — Tarea eliminada correctamente.
         * - `400 Bad Request`          — TID inválido.
         * - `403 Forbidden`            — Sin permisos de eliminación.
         * - `500 Internal Server Error`— Error de base de datos.
         */
        delete("/tasks/{tid}/delete") {
            val uid = call.principal<JWTPrincipal>()!!.subject?.toInt() ?: return@delete call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "No UID available"))
            val tid = call.parameters["tid"]?.toInt() ?: return@delete call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "Didn't catch the Task ID"))

            val response = getTaskDaoImpl().deleteTask(tid, uid)
            when (response){
                CreateTaskRepoResponse.RESP_OK -> call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
                CreateTaskRepoResponse.RESP_NOTMEMBER -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "Not a member of this group"))
                CreateTaskRepoResponse.RESP_EXPOSEDERR -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Exposed error"))
                CreateTaskRepoResponse.RESP_INVALIDTYPE -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal, "Invalid task type"))
                CreateTaskRepoResponse.RESP_NOPERMISSION -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "No permission to do the action"))
                CreateTaskRepoResponse.RESP_INVALIDDATE -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "The date couldn't be parsed"))
                CreateTaskRepoResponse.RESP_MISSINGDATA -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "Missing extra task data"))
            }
        }

        /**
         * Revierte el estado de una tarea completada a activa y devuelve la recompensa
         * al usuario (XP y Ludiones). Para hábitos, retrocede la racha si fue completado hoy.
         * En cascada, si la tarea pertenece a un objetivo que estaba completado, lo reactiva.
         *
         * Parámetro de ruta:
         * - `tid` — Identificador de la tarea a revertir.
         *
         * Respuestas:
         * - `200 OK`       — Estado revertido correctamente.
         * - `400 Bad Request` — TID inválido.
         * - `404 Not Found`   — La tarea no existe o no se pudo revertir.
         */
        patch("/tasks/{tid}/uncomplete") {
            val uid = call.principal<JWTPrincipal>()!!.subject?.toInt() ?: return@patch call.respond(HttpStatusCode.Unauthorized)
            val tid = call.parameters["tid"]?.toInt() ?: return@patch call.respond(HttpStatusCode.BadRequest)

            val ok = getTaskDaoImpl().uncompleteTask(tid, uid)
            if (ok) call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
            else call.respond(HttpStatusCode.NotFound, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "Couldn't uncomplete task"))
        }
    }
}

