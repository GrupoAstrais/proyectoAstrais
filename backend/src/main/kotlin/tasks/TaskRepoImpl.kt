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
import java.time.LocalDate
import kotlin.time.ExperimentalTime

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

            val tipo = when (req.tipo) {
                TASKTYPE_UNIQUE -> {
                    if (req.extraUnico != null){
                        extraUnica = TareaUniqueData(
                            fechaLimite = kotlinx.datetime.Instant.parse(req.extraUnico.fechaLimite).toLocalDateTime(TimeZone.UTC),
                            idObjetivo = req.idObjetivo
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
                recompensaXp = calcularXp(tipo, req.prioridad, extraHabito?.let { HabitFrequency.fromValue(it.frequency) }),
                recompensaLudion = calcularLudiones(tipo, req.prioridad, extraHabito?.let { HabitFrequency.fromValue(it.frequency) }),
                extraUnico = extraUnica,
                extraHabito = extraHabito,
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
                tareasEntidades.map {
<<<<<<< Updated upstream
                    val typeStr = when (it.tipo) {
                        TaskType.UNICO -> TASKTYPE_UNIQUE
                        TaskType.OBJETIVO -> TASKTYPE_OBJECTIVE
                        TaskType.HABITO -> TASKTYPE_HABIT
                    }

=======
>>>>>>> Stashed changes
                    var extraUnico : CreateTareaUniqueData? = null
                    var extraHabito : CreateTareaHabitData? = null
                    var estadoActual = it.estado.name

                    if (it.tipo == TaskType.UNICO) {
                        val unica = EntidadTareaUnica.find { TablaTareaUnica.id_tarea eq it.id }.singleOrNull()
                        if (unica?.fecha_vencimiento != null) {
                            extraUnico = CreateTareaUniqueData(fechaLimite = unica.fecha_vencimiento.toString())
                        }
                    } else if (it.tipo == TaskType.HABITO) {
                        val habito = EntidadTareaHabito.find { TablaTareaHabito.id_tarea eq it.id }.singleOrNull()
                        if (habito != null) {
                            val fEnum = HabitFrequency.entries.find { f -> f.value == habito.frecuencia } ?: HabitFrequency.DAILY
                            extraHabito = CreateTareaHabitData(habito.variacion_freq, fEnum)

                            val hoy = LocalDate.now().toKotlinLocalDate()
                            if (habito.ultima_vez_completada == hoy) {
                                estadoActual = TaskState.COMPLETE.name
                            }
                        }
                    }

                    var idObjetivoPadreTid: Int? = null
                    if (it.id_objetivo != null) {
                        val obj = EntidadTareaObjetivo.findById(it.id_objetivo!!.value)
                        idObjetivoPadreTid = obj?.id_tarea?.value
                    }

                    TareaResponse(
                        id = it.id.value,
                        gid = gid,
                        uid = null,
                        titulo = it.titulo,
                        descripcion = it.descripcion,
                        tipo = it.tipo.name,
                        estado = estadoActual,
                        prioridad = it.prioridad,
                        recompensaXp = it.recompensa_xp,
                        recompensaLudion = it.recompensa_ludion,
                        fechaValida = extraUnico?.fechaLimite,
                        extraUnico = extraUnico,
                        extraHabito = extraHabito,
<<<<<<< Updated upstream
                        idObjetivo = it.id_objetivo?.value,
                        fecha_creacion = it.fecha_creacion.toString(),
                        fecha_actualizado = it.fecha_actualizado.toString(),
                        fecha_completado = it.fecha_completado.toString()
=======
                        idObjetivo = idObjetivoPadreTid
>>>>>>> Stashed changes
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

    @OptIn(ExperimentalTime::class)
    override suspend fun editTask(uid: Int, tid: Int, request: EditTareaRequest): CreateTaskRepoResponse {
        return try {
            suspendTransaction {
                val tarea = EntidadTarea.findById(tid) ?: return@suspendTransaction CreateTaskRepoResponse.RESP_EXPOSEDERR
                val grupo = EntidadGrupo.findById(tarea.id_grupo.value) ?: return@suspendTransaction CreateTaskRepoResponse.RESP_EXPOSEDERR

                val prioCode = checkTaskPriority(uid, grupo)
                if (prioCode == 1) return@suspendTransaction CreateTaskRepoResponse.RESP_NOTMEMBER
                if (prioCode == 2) return@suspendTransaction CreateTaskRepoResponse.RESP_NOPERMISSION

                if (request.titulo != null) tarea.titulo = request.titulo
                if (request.descripcion != null) tarea.descripcion = request.descripcion

                val nuevaPrioridad = request.prioridad ?: tarea.prioridad
                tarea.prioridad = nuevaPrioridad

                if (request.idObjetivo != null) {
                    val nuevoPadre = EntidadTareaObjetivo.find { TablaTareaObjetivo.id_tarea eq request.idObjetivo }.singleOrNull()
                    if (nuevoPadre != null) {
                        tarea.id_objetivo = nuevoPadre.id
                    }
                }

                var freqParaCalculo: HabitFrequency? = null

                when (tarea.tipo) {
                    TaskType.UNICO -> {
                        if (request.extraUnico != null) {
                            val tareaUnica = EntidadTareaUnica.find { TablaTareaUnica.id_tarea eq tid }.singleOrNull()
                            if (tareaUnica != null) {
                                tareaUnica.fecha_vencimiento = kotlinx.datetime.Instant.parse(request.extraUnico.fechaLimite).toLocalDateTime(TimeZone.UTC)
                            }
                        }
                    }
                    TaskType.HABITO -> {
                        val tareaHabito = EntidadTareaHabito.find { TablaTareaHabito.id_tarea eq tid }.singleOrNull()
                        if (tareaHabito != null) {
                            if (request.extraHabito != null) {
                                tareaHabito.variacion_freq = request.extraHabito.numeroFrecuencia
                                tareaHabito.frecuencia = request.extraHabito.frequency.value
                                freqParaCalculo = request.extraHabito.frequency
                            } else {
                                freqParaCalculo = HabitFrequency.entries.find { it.value == tareaHabito.frecuencia }
                            }
                        }
                    }
                    TaskType.OBJETIVO -> { }
                }

                tarea.recompensa_xp = calcularXp(tarea.tipo, nuevaPrioridad, freqParaCalculo)
                tarea.recompensa_ludion = calcularLudiones(tarea.tipo, nuevaPrioridad, freqParaCalculo)

                tarea.fecha_actualizado = LocalDate.now().toKotlinLocalDate()

                CreateTaskRepoResponse.RESP_OK
            }
        } catch (e: Exception) {
            CreateTaskRepoResponse.RESP_EXPOSEDERR
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun uncompleteTask(tid: Int, uid: Int): Boolean {
        return try {
            suspendTransaction {
                val tarea = EntidadTarea.findById(tid) ?: return@suspendTransaction false
                val grupo = EntidadGrupo.findById(tarea.id_grupo.value) ?: return@suspendTransaction false

                if (checkTaskPriority(uid, grupo) != 0) {
                    return@suspendTransaction false
                }

                val usuario = EntidadUsuario.findById(uid) ?: return@suspendTransaction false

                tarea.estado = TaskState.ACTIVE
                tarea.fecha_completado = null

                if (tarea.tipo == TaskType.HABITO) {
                    val tareaHabito = EntidadTareaHabito.find { TablaTareaHabito.id_tarea eq tid }.singleOrNull()
                    if (tareaHabito != null) {
                        val hoy = LocalDate.now().toKotlinLocalDate()

                        if (tareaHabito.ultima_vez_completada == hoy) {
                            tareaHabito.ultima_vez_completada = LocalDate.now().minusDays(1).toKotlinLocalDate()
                            if (tareaHabito.racha_actual > 0) {
                                tareaHabito.racha_actual -= 1
                            }
                            quitarRecompensaUsuario(usuario, tarea)
                        }
                    }
                } else {
                    if (tarea.recompensa_reclamada) {
                        tarea.recompensa_reclamada = false
                        quitarRecompensaUsuario(usuario, tarea)
                    }
                }

                true
            }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteTask(tid: Int, uid: Int): CreateTaskRepoResponse {
        return try {
            suspendTransaction {
                val tarea = EntidadTarea.findById(tid) ?: return@suspendTransaction CreateTaskRepoResponse.RESP_EXPOSEDERR
                val grupo = EntidadGrupo.findById(tarea.id_grupo.value) ?: return@suspendTransaction CreateTaskRepoResponse.RESP_EXPOSEDERR

                val prio = checkTaskPriority(uid, grupo)
                if (prio == 1) return@suspendTransaction CreateTaskRepoResponse.RESP_NOTMEMBER
                if (prio == 2) return@suspendTransaction CreateTaskRepoResponse.RESP_NOPERMISSION

                if (getDatabaseDaoImpl().deleteTarea(tid)) {
                    CreateTaskRepoResponse.RESP_OK
                } else {
                    CreateTaskRepoResponse.RESP_EXPOSEDERR
                }
            }
        } catch (e: Exception) {
            CreateTaskRepoResponse.RESP_EXPOSEDERR
        }
    }
}

fun quitarRecompensaUsuario(usuario: EntidadUsuario, tarea: EntidadTarea) {
    usuario.ludiones = maxOf(0, usuario.ludiones - tarea.recompensa_ludion)
    usuario.xp_total = maxOf(0, usuario.xp_total - tarea.recompensa_xp)
    usuario.xp_actual -= tarea.recompensa_xp

    while (usuario.xp_actual < 0 && usuario.nivel > 0) {
        usuario.nivel -= 1
        val xpNivelAnterior = (usuario.nivel + 1) * 100
        usuario.xp_actual += xpNivelAnterior
    }
    if (usuario.xp_actual < 0) usuario.xp_actual = 0

    usuario.total_tareas_completadas = maxOf(0, usuario.total_tareas_completadas - 1)
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
    val multiplicadorPrio = 1.0 + (prioridad * 0.5)
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