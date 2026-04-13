package tasks

import com.astrais.*
import com.astrais.db.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class TaskRepoImpl : TaskRepo{
    @OptIn(ExperimentalTime::class)
    override suspend fun createTask(req : CreateTareaRequest, uid : Int) : Pair<CreateTaskRepoResponse, Int> {
        try {
            val role = getDatabaseDaoImpl().getUserRoleOnGroup(uid, req.gid)
            val isOwner = getDatabaseDaoImpl().checkIfUserIsAdmin(uid = uid, req.gid)

            if (role == null && !isOwner) {
                return Pair(CreateTaskRepoResponse.RESP_NOTMEMBER, -1)
            }

            var extraUnica : TareaUniqueData? = null
            var extraHabito : TareaHabitData? = null

            var tipo: TaskType
            tipo = when (req.tipo) {
                TASKTYPE_UNIQUE-> {
                    if (req.extraUnico != null){
                        extraUnica = TareaUniqueData(
                            fechaLimite = Instant.parse(req.extraUnico.fechaLimite).toLocalDateTime(TimeZone.UTC)
                        )

                    }else{
                        return Pair(CreateTaskRepoResponse.RESP_MISSINGDATA, -2)
                    }

                    TaskType.UNICO
                }

                TASKTYPE_HABIT -> {
                    if (req.extraHabito != null){
                        extraHabito = TareaHabitData(
                            numeroFrecuencia = req.extraHabito.numeroFrecuencia,
                            frequency = req.extraHabito.frequency
                        )

                    }else{
                        return Pair(CreateTaskRepoResponse.RESP_MISSINGDATA, -3)
                    }

                    TaskType.HABITO
                }

                TASKTYPE_OBJECTIVE -> TaskType.OBJETIVO

                else -> return Pair(CreateTaskRepoResponse.RESP_INVALIDTYPE, -1)
            }

            val tid = getDatabaseDaoImpl().createTarea(
                    gid = req.gid,
                    titulo = req.titulo,
                    descripcion = req.descripcion,
                    tipo = tipo,
                    prioridad = req.prioridad,
                    recompensaXp = calcularXp(tipo, req.prioridad),
                    recompensaLudion = calcularLudiones(tipo, req.prioridad),
                    extraUnico = extraUnica,
                    extraHabito = extraHabito
                )
            return Pair(CreateTaskRepoResponse.RESP_OK, tid)
        } catch (e : ExposedSQLException) {
            return Pair(CreateTaskRepoResponse.RESP_EXPOSEDERR, -1)
        }
    }

    override suspend fun getGroupTasks(gid: Int, uid: Int): Pair<CreateTaskRepoResponse, List<TareaResponse>> {
        try {
            val role = getDatabaseDaoImpl().getUserRoleOnGroup(uid, gid)
            val isOwner = getDatabaseDaoImpl().checkIfUserIsAdmin(uid = uid, gid)
            if (role == null && !isOwner) {
                return Pair(CreateTaskRepoResponse.RESP_NOTMEMBER, emptyList())
            }

            val tareas =
                getDatabaseDaoImpl().getTareasByGroup(gid).map {
                    val typeStr = when (it.tipo) {
                        TaskType.UNICO -> TASKTYPE_UNIQUE
                        TaskType.OBJETIVO -> TASKTYPE_OBJECTIVE
                        TaskType.HABITO -> TASKTYPE_HABIT
                    }
                    val extraUnico : CreateTareaUniqueData? = null
                    val extraHabito : CreateTareaHabitData? = null

                    // TODO: Parsear los datos extra

                    TareaResponse(
                        id = it.id.value,
                        titulo = it.titulo,
                        descripcion = it.descripcion,
                        tipo = typeStr,
                        estado = it.estado.name,
                        prioridad = it.prioridad,
                        recompensaXp = it.recompensa_xp,
                        recompensaLudion = it.recompensa_ludion,
                        extraUnico = extraUnico,
                        extraHabito = extraHabito
                    )
                }
            return Pair(CreateTaskRepoResponse.RESP_OK, tareas)
        } catch (e : ExposedSQLException) {
            return Pair(CreateTaskRepoResponse.RESP_EXPOSEDERR, emptyList())
        }
    }

    override suspend fun completeTask(tid: Int, uid: Int): Boolean {
        try {
            val gid = getDatabaseDaoImpl().getGroupByTask(tid) ?: return false
            val role = getDatabaseDaoImpl().getUserRoleOnGroup(uid, gid.id.value)
            val isOwner = gid.owner.value == uid
            if (role == null && !isOwner) {
                return false
            }

            return getDatabaseDaoImpl().completeTarea(tid, uid)
        } catch (e : ExposedSQLException) {
            return false
        }
    }

    override suspend fun editTask(uid : Int, tid: Int, titulo: String?, desc: String?, prio: Int?) : CreateTaskRepoResponse{
        val gid = getDatabaseDaoImpl().getGroupByTask(tid)
        when (checkTaskPriority(uid, gid)){
            1->return CreateTaskRepoResponse.RESP_NOTMEMBER
            2->return CreateTaskRepoResponse.RESP_NOPERMISSION
            0->{
                if (getDatabaseDaoImpl().editTask(gid!!.id.value, titulo, desc, prio)){
                    return CreateTaskRepoResponse.RESP_OK
                }else{
                    return CreateTaskRepoResponse.RESP_EXPOSEDERR
                }
            }
            else->return CreateTaskRepoResponse.RESP_INVALIDTYPE
        }


    }

    override suspend fun deleteTask(tid: Int, uid: Int): CreateTaskRepoResponse {
        // Solo borra si tiene permiso
        val gid = getDatabaseDaoImpl().getGroupByTask(tid)
        when (checkTaskPriority(uid, gid)){
            1->return CreateTaskRepoResponse.RESP_NOTMEMBER
            2->return CreateTaskRepoResponse.RESP_NOPERMISSION
            0->{
                if (getDatabaseDaoImpl().deleteTarea(tid)) {
                    return CreateTaskRepoResponse.RESP_OK
                }
                else {
                    return CreateTaskRepoResponse.RESP_EXPOSEDERR
                }
            }
            else->return CreateTaskRepoResponse.RESP_INVALIDTYPE
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

suspend fun checkTaskPriority(uid : Int, gid : EntidadGrupo?) : Int{
    if (gid == null) {
        return 1
    }
    if (gid.owner.value != uid){
        val d = getDatabaseDaoImpl().getUserRoleOnGroup(idusuario = uid, idgrupo = gid.id.value)
        if (d != GroupRoles.MOD){
            return 2
        }
    }
    return 0
}