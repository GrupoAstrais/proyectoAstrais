package tasks

/**
 * Códigos de resultado para las operaciones del repositorio de tareas.
 *
 * @property RESP_OK La operación se completó correctamente.
 * @property RESP_NOTMEMBER El usuario no es miembro del grupo al que pertenece la tarea.
 * @property RESP_EXPOSEDERR Error en la capa de base de datos (SQL/Exposed).
 * @property RESP_INVALIDTYPE El tipo de tarea indicado no es válido (`UNICO`, `HABITO` u `OBJETIVO`).
 * @property RESP_NOPERMISSION El usuario no tiene el rol necesario (se requiere Owner o Moderador).
 * @property RESP_INVALIDDATE La fecha de vencimiento no pudo ser parseada como ISO-8601.
 * @property RESP_MISSINGDATA Faltan los datos adicionales obligatorios según el tipo de tarea.
 */
enum class CreateTaskRepoResponse{
    RESP_OK,
    RESP_NOTMEMBER,
    RESP_EXPOSEDERR,
    RESP_INVALIDTYPE,
    RESP_NOPERMISSION,
    RESP_INVALIDDATE,
    RESP_MISSINGDATA
}

/**
 * Contrato del repositorio de tareas de Astrais.
 *
 * Define todas las operaciones disponibles sobre tareas: creación, listado,
 * completado, descomplete, edición y eliminación. El repositorio valida los
 * permisos del usuario en cada operación de escritura.
 *
 * La implementación concreta se obtiene mediante [getTaskDaoImpl].
 * Todas las funciones son suspendidas y deben invocarse desde una corrutina.
 */
interface TaskRepo {
    /**
     * Crea una nueva tarea en el grupo indicado.
     * Valida que el solicitante sea Owner o Moderador del grupo antes de persistir.
     * Calcula automáticamente las recompensas de XP y Ludiones según el tipo y prioridad.
     *
     * @param req Datos de la tarea a crear, incluyendo tipo y campos extra según corresponda.
     * @param uid Identificador del usuario que solicita la creación.
     * @return Par donde el primer elemento es el código de resultado y el segundo el ID
     *   de la tarea creada (`-1` si la operación falló).
     */
    suspend fun createTask(req : CreateTareaRequest, uid : Int) : Pair<CreateTaskRepoResponse, Int>

    /**
     * Lista todas las tareas de un grupo con sus datos extra según el tipo.
     * Para hábitos, el estado `COMPLETE` se calcula dinámicamente comparando
     * `ultima_vez_completada` con la fecha actual.
     * Cualquier miembro del grupo puede invocar esta operación.
     *
     * @param gid Identificador del grupo cuyas tareas se quieren listar.
     * @param uid Identificador del usuario que realiza la consulta (para verificar membresía).
     * @return Par donde el primer elemento es el código de resultado y el segundo la lista
     *   de [TareaResponse] (vacía si falló).
     */
    suspend fun getGroupTasks(gid : Int, uid : Int) : Pair<CreateTaskRepoResponse, List<TareaResponse>>

    /**
     * Marca una tarea como completada y otorga las recompensas (XP y Ludiones) al usuario.
     * Para hábitos actualiza `ultima_vez_completada` y el contador de racha.
     * Solo miembros del grupo pueden completar tareas.
     *
     * @param tid Identificador de la tarea a completar.
     * @param uid Identificador del usuario que completa la tarea.
     * @return `true` si la operación fue exitosa; `false` si falló o el usuario no tiene
     *   acceso.
     */
    suspend fun completeTask(tid : Int, uid : Int) : Boolean

    /**
     * Edita los campos de una tarea existente de forma parcial.
     * Solo el Owner y los Moderadores del grupo pueden editar tareas.
     * Recalcula las recompensas si cambia la prioridad o la frecuencia del hábito.
     *
     * @param uid Identificador del usuario que solicita la edición.
     * @param tid Identificador de la tarea a editar.
     * @param request Datos de edición; los campos `null` no se modifican.
     * @return [CreateTaskRepoResponse] indicando el resultado de la operación.
     */
    suspend fun editTask(uid: Int, tid: Int, request: EditTareaRequest): CreateTaskRepoResponse

    /**
     * Elimina permanentemente una tarea y sus datos asociados.
     * Solo el Owner y los Moderadores del grupo pueden eliminar tareas.
     * Las subtareas (si la tarea es un objetivo) se eliminan en cascada.
     *
     * @param tid Identificador de la tarea a eliminar.
     * @param uid Identificador del usuario que solicita la eliminación.
     * @return [CreateTaskRepoResponse] indicando el resultado de la operación.
     */
    suspend fun deleteTask(tid : Int, uid : Int) : CreateTaskRepoResponse

    /**
     * Revierte el estado de una tarea completada a activa y devuelve las recompensas
     * (XP y Ludiones) al usuario. Para hábitos retrocede la racha si fue completado hoy.
     * En cascada reactiva el objetivo padre si estaba completado.
     *
     * @param tid Identificador de la tarea a revertir.
     * @param uid Identificador del usuario que solicita el descomplete.
     * @return `true` si la operación fue exitosa; `false` si falló o el usuario no tiene
     *   acceso.
     */
    suspend fun uncompleteTask(tid: Int, uid: Int): Boolean
}

/**
 * Devuelve la implementación activa de [TaskRepo].
 * En producción retorna una instancia de [TaskRepoImpl].
 *
 * @return La implementación concreta de [TaskRepo].
 */
fun getTaskDaoImpl() : TaskRepo{
    return TaskRepoImpl()
}