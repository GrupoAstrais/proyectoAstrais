package tasks

enum class CreateTaskRepoResponse{
    RESP_OK,
    RESP_NOTMEMBER,
    RESP_EXPOSEDERR,
    RESP_INVALIDTYPE,
    RESP_NOPERMISSION,
    RESP_INVALIDDATE,
    RESP_MISSINGDATA
}

interface TaskRepo {
    /**
     * Se crea una tarea por el usuario indicado.
     * @param req Request de creacion de tarea.
     * @param uid Usuario que crea la tarea, solo puede hacerlo si es moderador o dueño.
     * @return Un par, el primero es el codigo de respuesta, el otro el ID de la tarea si se pudo hacer.
     */
    suspend fun createTask(req : CreateTareaRequest, uid : Int) : Pair<CreateTaskRepoResponse, Int>

    /**
     * Se listan todas las tareas de un grupo
     * @param gid El ID del grupo
     * @param uid El ID del usuario que quiere consultarlo, cualquiera puede hacer esto.
     */
    suspend fun getGroupTasks(gid : Int, uid : Int) : Pair<CreateTaskRepoResponse, List<TareaResponse>>

    /**
     * Se marca como completada una tarea
     * @param tid El ID de la tarea a completar
     * @param uid El ID del usuario que completo la tarea
     */
    suspend fun completeTask(tid : Int, uid : Int) : Boolean

    /**
     * Se intenta editar la tarea
     * @param uid ID del usuario que quiere editar la tarea
     * @param tid ID de la tarea a editar
     */
    suspend fun editTask(uid: Int, tid: Int, request: EditTareaRequest): CreateTaskRepoResponse

    /**
     * Se intenta borrar la tarea
     * @param tid ID de la tarea a borrar
     * @param uid ID del usuario que intenta borrar la tarea
     */
    suspend fun deleteTask(tid : Int, uid : Int) : CreateTaskRepoResponse

    suspend fun uncompleteTask(tid: Int, uid: Int): Boolean
}

fun getTaskDaoImpl() : TaskRepo{
    return TaskRepoImpl()
}