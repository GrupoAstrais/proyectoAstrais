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

@Serializable
data class CreateTareaUniqueData(
    val fechaLimite : String
)

// const val HABIT_FREQ_HOURLY = "HOURLY"
// const val HABIT_FREQ_DAILY = "DAILY"
// const val HABIT_FREQ_WEEKLY = "WEEKLY"
// const val HABIT_FREQ_MONTHLY = "MONTHLY"
// const val HABIT_FREQ_YEARLY = "YEARLY"

/*
* Si se necesita el valor desde la API se puede poner
* val fromApi = HabitFrequency.fromValue("weekly")
* y lo tranforma
* */

enum class HabitFrequency(val value: String) {
    HOURLY("HOURLY"),
    DAILY("DAILY"),
    WEEKLY("WEEKLY"),
    MONTHLY("MONTHLY"),
    YEARLY("YEARLY");

    companion object {
        fun fromValue(value: String): HabitFrequency? =
            entries.find { it.value.equals(value, ignoreCase = true) }
    }
}

@Serializable
data class CreateTareaHabitData(
    val numeroFrecuencia : Int,
    val frequency : HabitFrequency = HabitFrequency.DAILY
)

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

@Serializable
data class TareaResponse(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val tipo: String,
    val estado: String,
    val prioridad: Int,
    val recompensaXp: Int,
    val recompensaLudion: Int,

    val extraUnico : CreateTareaUniqueData? = null,
    val extraHabito : CreateTareaHabitData? = null,
    val idObjetivo: Int? = null
)

@Serializable
data class EditTareaRequest(
    val titulo : String? = null,
    val descripcion: String? = null,
    val prioridad: Int? = null
)

fun Route.tareaRoutes() {
    post("/tasks") {
        val uid = call.principal<JWTPrincipal>()!!.subject?.toInt() ?: return@post call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "No UID available"))
        val body = call.receive<CreateTareaRequest>()

        val resp = getTaskDaoImpl().createTask(body, uid)
        when (resp.first){
            CreateTaskRepoResponse.RESP_OK -> call.respond(HttpStatusCode.OK, mapOf("id" to resp.second))
            CreateTaskRepoResponse.RESP_NOTMEMBER -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "Not a member of this group"))
            CreateTaskRepoResponse.RESP_EXPOSEDERR -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Exposed error"))
            CreateTaskRepoResponse.RESP_INVALIDTYPE -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid task type"))
            CreateTaskRepoResponse.RESP_NOPERMISSION -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "No permission to do the action"))
            CreateTaskRepoResponse.RESP_INVALIDDATE -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "The date couldn't be parsed"))
            CreateTaskRepoResponse.RESP_MISSINGDATA -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "Missing extra task data"))
        }
    }

    // Obtener tareas de un grupo
    get("/tasks/{gid}") {
        val uid = call.principal<JWTPrincipal>()!!.subject?.toInt() ?: return@get call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "No UID available"))
        val gid = call.parameters["gid"]?.toInt() ?: return@get call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "Didn't catch the Group ID"))

        val response = getTaskDaoImpl().getGroupTasks(gid, uid)

        when (response.first){
            CreateTaskRepoResponse.RESP_OK -> call.respond(HttpStatusCode.OK, mapOf("taskList" to response.second))
            CreateTaskRepoResponse.RESP_NOTMEMBER -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "Not a member of this group"))
            CreateTaskRepoResponse.RESP_EXPOSEDERR -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Exposed error"))
            CreateTaskRepoResponse.RESP_INVALIDTYPE -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid task type"))
            CreateTaskRepoResponse.RESP_NOPERMISSION -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "No permission to do the action"))
            CreateTaskRepoResponse.RESP_INVALIDDATE -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "The date couldn't be parsed"))
            CreateTaskRepoResponse.RESP_MISSINGDATA -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "Missing extra task data"))
        }
    }

    // Completar tarea
    patch("/tasks/{tid}/complete") {
        val uid = call.principal<JWTPrincipal>()!!.subject?.toInt() ?: return@patch call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "No UID available"))
        val tid = call.parameters["tid"]?.toInt() ?: return@patch call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "Didn't catch the Task ID"))

        val ok = getTaskDaoImpl().completeTask(tid, uid)
        if (ok) call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
        else call.respond(HttpStatusCode.NotFound, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "Couldn't complete task"))
    }

    patch("/tasks/{tid}/edit") {
        val uid = call.principal<JWTPrincipal>()!!.subject?.toInt() ?: return@patch call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "No UID available"))
        val tid = call.parameters["tid"]?.toInt() ?: return@patch call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "Didn't catch the Task ID"))


        val data = call.receive<EditTareaRequest>()
        val response = getTaskDaoImpl().editTask(uid, tid, data.titulo, data.descripcion, data.prioridad)
        when (response){
            CreateTaskRepoResponse.RESP_OK -> call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
            CreateTaskRepoResponse.RESP_NOTMEMBER -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "Not a member of this group"))
            CreateTaskRepoResponse.RESP_EXPOSEDERR -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Exposed error"))
            CreateTaskRepoResponse.RESP_INVALIDTYPE -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid task type"))
            CreateTaskRepoResponse.RESP_NOPERMISSION -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "No permission to do the action"))
            CreateTaskRepoResponse.RESP_INVALIDDATE -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "The date couldn't be parsed"))
            CreateTaskRepoResponse.RESP_MISSINGDATA -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "Missing extra task data"))
        }
    }

    delete("/tasks/{tid}/delete") {
        val uid = call.principal<JWTPrincipal>()!!.subject?.toInt() ?: return@delete call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "No UID available"))
        val tid = call.parameters["tid"]?.toInt() ?: return@delete call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "Didn't catch the Task ID"))

        val response = getTaskDaoImpl().deleteTask(tid, uid)
        when (response){
            CreateTaskRepoResponse.RESP_OK -> call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
            CreateTaskRepoResponse.RESP_NOTMEMBER -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "Not a member of this group"))
            CreateTaskRepoResponse.RESP_EXPOSEDERR -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Exposed error"))
            CreateTaskRepoResponse.RESP_INVALIDTYPE -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid task type"))
            CreateTaskRepoResponse.RESP_NOPERMISSION -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "No permission to do the action"))
            CreateTaskRepoResponse.RESP_INVALIDDATE -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "The date couldn't be parsed"))
            CreateTaskRepoResponse.RESP_MISSINGDATA -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "Missing extra task data"))
        }
    }
}

