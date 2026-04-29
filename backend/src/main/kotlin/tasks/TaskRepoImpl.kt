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

/**
 * Implementación concreta de [TaskRepo].
 *
 * Realiza todas las operaciones de tareas contra la base de datos relacional a través
 * de Exposed ORM y el DAO centralizado (`getDatabaseDaoImpl()`). Gestiona tres tipos
 * de tareas: únicas (con fecha límite), hábitos (con frecuencia y racha) y objetivos
 * (con subtareas anidadas). Calcula automáticamente las recompensas de XP y Ludiones
 * en función del tipo y prioridad de la tarea.
 */
class TaskRepoImpl : TaskRepo{

    /**
     * Crea una nueva tarea en la base de datos para el grupo indicado.
     * Verifica que el solicitante sea miembro y tenga permisos (Owner o Moderador).
     * Según el tipo de tarea, parsea y persiste los datos extra:
     * - `UNICO`: parsea [CreateTareaUniqueData.fechaLimite] como `Instant` ISO-8601.
     * - `HABITO`: persiste la frecuencia de [CreateTareaHabitData.frequency].
     * - `OBJETIVO`: no requiere datos extra.
     * Calcula las recompensas con `calcularXp` y `calcularLudiones`.
     *
     * @param req Datos de la tarea a crear.
     * @param uid Identificador del usuario solicitante.
     * @return Par `(código, tid)` donde `tid` es `-1` si la operación falló.
     */
    @OptIn(ExperimentalTime::class)
    override suspend fun createTask(req : CreateTareaRequest, uid : Int) : Pair<CreateTaskRepoResponse, Int> {
        try {
            val group = getDatabaseDaoImpl().getGroupById(req.gid)
            val prioCode = checkTaskPriority(uid, group)
            if (prioCode == 1) return Pair(CreateTaskRepoResponse.RESP_NOTMEMBER, -1)
            if (prioCode == 2) return Pair(CreateTaskRepoResponse.RESP_NOPERMISSION, -1)

            var extraUnica : TareaUniqueData? = null
            var extraHabito : TareaHabitData? = null

            val tipo = when (req.tipo) {
                TASKTYPE_UNIQUE -> {
                    if (req.extraUnico != null){
                        extraUnica = TareaUniqueData(
                            fechaLimite = kotlin.time.Instant.parse(req.extraUnico.fechaLimite).toLocalDateTime(TimeZone.UTC)
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
                idObjetivo = req.idObjetivo
            )
            return Pair(CreateTaskRepoResponse.RESP_OK, tid)
        } catch (e : ExposedSQLException) {
            return Pair(CreateTaskRepoResponse.RESP_EXPOSEDERR, -1)
        }
    }

    /**
     * Lista todas las tareas de un grupo, mapeando cada entidad de base de datos a
     * un [TareaResponse] con los datos extra del tipo correspondiente.
     * Para hábitos, establece el estado como `COMPLETE` dinámicamente si
     * `ultima_vez_completada` coincide con la fecha de hoy.
     * Resuelve el TID del objetivo padre para subtareas.
     *
     * @param gid Identificador del grupo.
     * @param uid Identificador del usuario que consulta (debe ser miembro o Owner).
     * @return Par `(código, lista)`; lista vacía si el usuario no tiene acceso o
     *   si ocurre un error de base de datos.
     */
    override suspend fun getGroupTasks(gid: Int, uid: Int): Pair<CreateTaskRepoResponse, List<TareaResponse>> {
        try {
            val role = getDatabaseDaoImpl().getUserRoleOnGroup(uid, gid)
            val isOwner = getDatabaseDaoImpl().checkIfUserIsGroupAdmin(uid = uid, gid)
            if (role == null && !isOwner) {
                return Pair(CreateTaskRepoResponse.RESP_NOTMEMBER, emptyList())
            }

            val tareasEntidades = getDatabaseDaoImpl().getTareasByGroup(gid)

            val tareas = suspendTransaction {
                tareasEntidades.map {
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
                        idObjetivo = idObjetivoPadreTid ?: it.id_objetivo?.value,
                        fecha_creacion = it.fecha_creacion.toString(),
                        fecha_actualizado = it.fecha_actualizado.toString(),
                        fecha_completado = it.fecha_completado?.toString()
                    )

                }
            }
            return Pair(CreateTaskRepoResponse.RESP_OK, tareas)
        } catch (e : Exception) {
            return Pair(CreateTaskRepoResponse.RESP_EXPOSEDERR, emptyList())
        }
    }

    /**
     * Marca la tarea como completada delegando en `getDatabaseDaoImpl().completeTarea`.
     * Verifica que el usuario sea miembro del grupo propietario de la tarea antes de
     * llamar al DAO. Si la tarea ya estaba completada el DAO decide el comportamiento.
     *
     * @param tid Identificador de la tarea a completar.
     * @param uid Identificador del usuario que completa la tarea.
     * @return `true` si se completó con éxito; `false` si el usuario no tiene acceso
     *   o si ocurre una excepción de SQL.
     */
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

    /**
     * Edita una tarea existente de forma parcial dentro de una transacción Exposed.
     * Solo el Owner y los Moderadores del grupo pueden editar.
     * Aplica los campos no-nulos del [EditTareaRequest]:
     * - Título y descripción: actualización directa.
     * - Prioridad: actualiza el campo y recalcula recompensas.
     * - `extraUnico`: actualiza la fecha límite de `EntidadTareaUnica`.
     * - `extraHabito`: actualiza la frecuencia de `EntidadTareaHabito`.
     * - `idObjetivo`: reasigna la subtarea a un nuevo objetivo padre.
     * Actualiza `fecha_actualizado` al día actual.
     *
     * @param uid Identificador del usuario que solicita la edición.
     * @param tid Identificador de la tarea a editar.
     * @param request Datos de edición; los campos `null` no se modifican.
     * @return [CreateTaskRepoResponse] indicando el resultado.
     */
    @OptIn(ExperimentalTime::class)
    override suspend fun editTask(uid: Int, tid: Int, request: EditTareaRequest): CreateTaskRepoResponse {
        return try {
            suspendTransaction {
                val tarea = EntidadTarea.findById(tid) ?: return@suspendTransaction CreateTaskRepoResponse.RESP_EXPOSEDERR
                val grupo = EntidadGrupo.findById(tarea.id_grupo.value) ?: return@suspendTransaction CreateTaskRepoResponse.RESP_EXPOSEDERR

                val prio = checkTaskPriority(uid, grupo)
                if (prio == 1) return@suspendTransaction CreateTaskRepoResponse.RESP_NOTMEMBER
                if (prio == 2) return@suspendTransaction CreateTaskRepoResponse.RESP_NOPERMISSION

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

    /**
     * Revierte el estado de completado de una tarea dentro de una transacción Exposed.
     * Devuelve las recompensas al usuario llamando a [quitarRecompensaUsuario].
     * Para hábitos: si fue completado hoy, retrocede `ultima_vez_completada` un día
     * y decrementa `racha_actual`.
     * En cascada: si la tarea pertenece a un objetivo que estaba completado, lo reactiva
     * y también le devuelve sus recompensas.
     *
     * @param tid Identificador de la tarea a revertir.
     * @param uid Identificador del usuario que solicita el descomplete.
     * @return `true` si se revirtió con éxito; `false` si el usuario no tiene acceso,
     *   la tarea no existe, o ocurre una excepción.
     */
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

                if (tarea.id_objetivo != null) {
                    val objetivoPadre = EntidadTareaObjetivo.findById(tarea.id_objetivo!!.value)
                    val tareaPadre = objetivoPadre?.id_tarea?.let { EntidadTarea.findById(it.value) }

                    if (tareaPadre != null && tareaPadre.estado == TaskState.COMPLETE) {
                        tareaPadre.estado = TaskState.ACTIVE
                        tareaPadre.fecha_completado = null

                        if (tareaPadre.recompensa_reclamada) {
                            tareaPadre.recompensa_reclamada = false
                            quitarRecompensaUsuario(usuario, tareaPadre)
                        }
                    }
                }

                true
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Elimina permanentemente una tarea y sus datos asociados (cascada de base de datos).
     * Verifica que el solicitante sea Owner o Moderador antes de delegar en
     * `getDatabaseDaoImpl().deleteTarea`.
     *
     * @param tid Identificador de la tarea a eliminar.
     * @param uid Identificador del usuario que solicita la eliminación.
     * @return [CreateTaskRepoResponse] con el resultado de la operación.
     */
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

/**
 * Descuenta las recompensas de una tarea del perfil del usuario.
 * Utilizado por [TaskRepoImpl.uncompleteTask] al revertir el estado de completado.
 *
 * El algoritmo:
 * 1. Calcula los Ludiones a reembolsar priorizando `ludiones_otorgados` (campo
 *    que registra exactamente lo que se otorgó); si es 0, usa `recompensa_ludion`
 *    como fallback para tareas antiguas.
 * 2. Descuenta `xp_total`, `xp_actual` y retrocede el nivel si `xp_actual` baja
 *    de cero, reconstituyendo el XP del nivel anterior (`(nivel+1) * 100`).
 * 3. Descuenta `ludiones` y actualiza `ludiones_ganados_hoy` (para no bloquear el
 *    contador diario si el reembolso ocurre el mismo día).
 * 4. Decrementa `total_tareas_completadas`.
 * 5. Limpia `ludiones_otorgados` para que un re-complete escriba el nuevo valor.
 *
 * @param usuario Entidad del usuario al que se le descuentan las recompensas.
 * @param tarea   Entidad de la tarea cuyas recompensas se devuelven.
 */
fun quitarRecompensaUsuario(usuario: EntidadUsuario, tarea: EntidadTarea) {
    val ludionesARefundar =
        if (tarea.ludiones_otorgados > 0) tarea.ludiones_otorgados
        else tarea.recompensa_ludion

    usuario.ludiones = maxOf(0, usuario.ludiones - ludionesARefundar)
    usuario.xp_total = maxOf(0, usuario.xp_total - tarea.recompensa_xp)
    usuario.xp_actual -= tarea.recompensa_xp

    while (usuario.xp_actual < 0 && usuario.nivel > 0) {
        usuario.nivel -= 1
        val xpNivelAnterior = (usuario.nivel + 1) * 100
        usuario.xp_actual += xpNivelAnterior
    }
    if (usuario.xp_actual < 0) usuario.xp_actual = 0

    usuario.ludiones_ganados_hoy = maxOf(0, usuario.ludiones_ganados_hoy - ludionesARefundar)
    usuario.total_tareas_completadas = maxOf(0, usuario.total_tareas_completadas - 1)

    tarea.ludiones_otorgados = 0
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