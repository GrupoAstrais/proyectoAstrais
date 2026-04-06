package com.astrais

import com.astrais.db.EntidadGrupo
import com.astrais.db.GroupRoles
import com.astrais.db.TaskType
import com.astrais.db.getDatabaseDaoImpl
import com.astrais.groups.AddUserReturn
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class CreateTareaRequest(
    val gid: Int,
    val titulo: String,
    val descripcion: String = "",
    val tipo: String = "UNICO",
    val prioridad: Int = 0
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

@Serializable
data class EditTareaRequest(
    val titulo : String? = null,
    val descripcion: String? = null,
    val prioridad: Int? = null
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
            if (ok) call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
            else call.respond(HttpStatusCode.NotFound)
        }

        patch("/tasks/{tid}/edit") {
            val uid = call.principal<JWTPrincipal>()!!.subject?.toInt() ?: return@patch call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "No UID available"))
            val tid = call.parameters["tid"]?.toInt() ?: return@patch call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "Didn't catch the Task ID"))

            val gid = getDatabaseDaoImpl().getGroupByTask(tid)
            if (!checkTaskPriority(tid, uid, gid, call)){
                return@patch
            }

            val data = call.receive<EditTareaRequest>()
            getDatabaseDaoImpl().editTask(gid!!.id.value, data.titulo, data.descripcion, data.prioridad)
            call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
        }

        delete("/tasks/{tid}/delete") {
            val uid = call.principal<JWTPrincipal>()!!.subject?.toInt() ?: return@delete call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "No UID available"))
            val tid = call.parameters["tid"]?.toInt() ?: return@delete call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "Didn't catch the Task ID"))

            // Solo borra si tiene permiso
            val gid = getDatabaseDaoImpl().getGroupByTask(tid)
            if (!checkTaskPriority(tid, uid, gid, call)){
                return@delete
            }

            if (getDatabaseDaoImpl().deleteTarea(tid)) {
                call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
            }
            else {
                call.respond(HttpStatusCode.NotFound, Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "Couldn't delete the task"))
            }
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

suspend fun checkTaskPriority(tid : Int, uid : Int, gid : EntidadGrupo?, call : RoutingCall) : Boolean{
    if (gid == null) {
        call.respond(HttpStatusCode.BadGateway)
        return false
    }

    if (gid.owner.value != uid){
        val d = getDatabaseDaoImpl().getUserRoleOnGroup(idusuario = uid, idgrupo = gid.id.value)
        if (d != GroupRoles.MOD){
            call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "You don't have enough priorities to do that"))
            return false
        }
    }
    return true
}