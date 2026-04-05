package com.astrais

import com.astrais.db.TaskType
import com.astrais.db.getDatabaseDaoImpl
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable

@Serializable
data class CreateTareaRequest(
    val gid: Int,
    val titulo: String,
    val descripcion: String = "",
    val tipo: String = "UNICO",
    val prioridad: Int = 0,
    val recompensaXp: Int = 0,
    val recompensaLudion: Int = 0
)

@Serializable
data class TareaResponse(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val tipo: String,
    val estado: String,
    val prioridad: Int,
    val recompensaXp: Int,
    val recompensaLudion: Int
)

fun Route.tareaRoutes() {
    authenticate("access-jwt") {

        // Crear tarea
        post("/tasks") {
            val uid =
                call.principal<JWTPrincipal>()!!.subject?.toInt()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val body = call.receive<CreateTareaRequest>()

            // Verificar que el usuario pertenece al grupo
            val role = getDatabaseDaoImpl().getUserRoleOnGroup(uid, body.gid)
            val group = getDatabaseDaoImpl().getGroupById(body.gid)
            val isOwner = group?.owner?.value == uid

            if (role == null && !isOwner) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    Errors(
                        ErrorCodes.EER_FORBIDDEN.ordinal,
                        "Not a member of this group"
                    )
                )
                return@post
            }

            val tipo =
                runCatching { TaskType.valueOf(body.tipo) }.getOrElse {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        Errors(
                            ErrorCodes.ERR_BADVALUE.ordinal,
                            "Invalid task type"
                        )
                    )
                }

            val tid =
                getDatabaseDaoImpl()
                    .createTarea(
                        gid = body.gid,
                        titulo = body.titulo,
                        descripcion = body.descripcion,
                        tipo = tipo,
                        prioridad = body.prioridad,
                        recompensaXp = calcularXp(tipo, body.prioridad),
                        recompensaLudion =
                            calcularLudiones(tipo, body.prioridad)
                    )

            call.respond(HttpStatusCode.Created, mapOf("id" to tid))
        }

        // Obtener tareas de un grupo
        get("/tasks/{gid}") {
            val uid =
                call.principal<JWTPrincipal>()!!.subject?.toInt()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val gid =
                call.parameters["gid"]?.toInt()
                    ?: return@get call.respond(HttpStatusCode.BadRequest)

            val role = getDatabaseDaoImpl().getUserRoleOnGroup(uid, gid)
            val group = getDatabaseDaoImpl().getGroupById(gid)
            val isOwner = group?.owner?.value == uid

            if (role == null && !isOwner) {
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            val tareas =
                getDatabaseDaoImpl().getTareasByGroup(gid).map {
                    TareaResponse(
                        id = it.id.value,
                        titulo = it.titulo,
                        descripcion = it.descripcion,
                        tipo = it.tipo.name,
                        estado = it.estado.name,
                        prioridad = it.prioridad,
                        recompensaXp = it.recompensa_xp,
                        recompensaLudion = it.recompensa_ludion
                    )
                }
            call.respond(HttpStatusCode.OK, tareas)
        }

        // Completar tarea
        patch("/tasks/{tid}/complete") {
            val uid =
                call.principal<JWTPrincipal>()!!.subject?.toInt()
                    ?: return@patch call.respond(HttpStatusCode.Unauthorized)
            val tid =
                call.parameters["tid"]?.toInt()
                    ?: return@patch call.respond(HttpStatusCode.BadRequest)

            val ok = getDatabaseDaoImpl().completeTarea(tid, uid)
            if (ok) call.respond(HttpStatusCode.OK, mapOf("aknowledged" to true))
            else call.respond(HttpStatusCode.NotFound)
        }
    }
}

// Esto es placeholder chavaloides, hay que cambiarlo
fun calcularXp(tipo: TaskType, prioridad: Int): Int {
    val base =
        when (tipo) {
            TaskType.HABITO -> 30
            TaskType.OBJETIVO -> 50
            TaskType.UNICO -> 20
        }
    return base + (prioridad * 10)
}

fun calcularLudiones(tipo: TaskType, prioridad: Int): Int {
    val base =
        when (tipo) {
            TaskType.HABITO -> 900
            TaskType.OBJETIVO -> 5000
            TaskType.UNICO -> 2
        }
    return base + (prioridad * 10)
}
