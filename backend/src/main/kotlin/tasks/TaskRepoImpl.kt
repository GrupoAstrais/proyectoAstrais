package tasks

import TASKTYPE_HABIT
import TASKTYPE_OBJECTIVE
import TASKTYPE_UNIQUE
import com.astrais.db.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
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
                TASKTYPE_UNIQUE -> {
                    if (req.extraUnico != null){
                        extraUnica = TareaUniqueData(
                            fechaLimite = Instant.parse(req.extraUnico.fechaLimite).toLocalDateTime(TimeZone.UTC),
                            idObjetivo = null
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
                            frequency = req.extraHabito.frequency.value
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

            val tareasEntidades = getDatabaseDaoImpl().getTareasByGroup(gid)

            val tareas = suspendTransaction {
                tareasEntidades.map { it ->
                    val typeStr = when (it.tipo) {
                        TaskType.UNICO -> TASKTYPE_UNIQUE
                        TaskType.OBJETIVO -> TASKTYPE_OBJECTIVE
                        TaskType.HABITO -> TASKTYPE_HABIT
                    }

                    var extraUnico : CreateTareaUniqueData? = null
                    var extraHabito : CreateTareaHabitData? = null
                    var estadoActual = it.estado.name

                    if (it.tipo == TaskType.UNICO) {
                        val unica = EntidadTareaUnica.find { TablaTareaUnica.id_tarea eq it.id }.singleOrNull()
                        if (unica != null && unica.fecha_vencimiento != null) {
                            extraUnico = CreateTareaUniqueData(fechaLimite = unica.fecha_vencimiento.toString())
                        }
                    } else if (it.tipo == TaskType.HABITO) {
                        val habito = EntidadTareaHabito.find { TablaTareaHabito.id_tarea eq it.id }.singleOrNull()
                        if (habito != null) {
                            val fEnum = HabitFrequency.entries.find { f -> f.value == habito.frecuencia } ?: HabitFrequency.DAILY
                            extraHabito = CreateTareaHabitData(habito.variacion_freq, fEnum)

                            val hoy = java.time.LocalDate.now().toKotlinLocalDate()
                            if (habito.ultima_vez_completada == hoy) {
                                estadoActual = TaskState.COMPLETE.name
                            }
                        }
                    }

                    TareaResponse(
                        id = it.id.value,
                        titulo = it.titulo,
                        descripcion = it.descripcion,
                        tipo = typeStr,
                        estado = estadoActual,
                        prioridad = it.prioridad,
                        recompensaXp = it.recompensa_xp,
                        recompensaLudion = it.recompensa_ludion,
                        extraUnico = extraUnico,
                        extraHabito = extraHabito,
                        idObjetivo = it.id_objetivo?.value
                    )
                }
            }
            return Pair(CreateTaskRepoResponse.RESP_OK, tareas)
        } catch (e : Exception) {
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

fun getFrecuencyMultiplier(frecuencia: HabitFrequency?): Double {
    if (frecuencia == null) return 1.0
    return when (frecuencia) {
        HabitFrequency.HOURLY -> 0.2
        HabitFrequency.DAILY -> 1.0
        HabitFrequency.WEEKLY -> 5.0
        HabitFrequency.MONTHLY -> 20.0
        HabitFrequency.YEARLY -> 100.0
    }
}

fun calcularXp(tipo: TaskType, prioridad: Int, frecuencia: HabitFrequency? = null): Int {
    val multiplicadorPrio = 1.0 + (prioridad * 0.5) // Baja x1, Media x1.5, Alta x2
    val multiplicadorFreq = getFrecuencyMultiplier(frecuencia)

    val xpBase = when (tipo) {
        TaskType.HABITO -> 20
        TaskType.UNICO -> 50
        TaskType.OBJETIVO -> 400
    }

    return (xpBase * multiplicadorPrio * multiplicadorFreq).toInt()
}

fun calcularLudiones(tipo: TaskType, prioridad: Int, frecuencia: HabitFrequency? = null): Int {
    val multiplicadorPrio = 1.0 + (prioridad * 0.5)
    val multiplicadorFreq = getFrecuencyMultiplier(frecuencia)

    val ludionesBase = when (tipo) {
        TaskType.HABITO -> 25
        TaskType.UNICO -> 80
        TaskType.OBJETIVO -> 800
    }

    return (ludionesBase * multiplicadorPrio * multiplicadorFreq).toInt()
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