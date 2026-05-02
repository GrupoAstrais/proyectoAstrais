package com.mm.astraisandroid.ui.features.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.data.api.CreateTareaHabitData
import com.mm.astraisandroid.data.api.CreateTareaRequest
import com.mm.astraisandroid.data.api.CreateTareaUniqueData
import com.mm.astraisandroid.data.api.HabitFrequency
import com.mm.astraisandroid.data.api.AuditEventOut
import com.mm.astraisandroid.data.api.GroupMemberOut
import com.mm.astraisandroid.data.api.InviteOut
import com.mm.astraisandroid.data.models.TaskPriority
import com.mm.astraisandroid.data.models.TaskType
import com.mm.astraisandroid.data.repository.GroupRepository
import com.mm.astraisandroid.data.repository.TaskRepository
import com.mm.astraisandroid.ui.features.tasks.TaskUIModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la pantalla de detalle de un grupo.
 *
 * Cada sección de contenido tiene su propio indicador de carga independiente para
 * que la UI pueda mostrar skeletons por sección en lugar de un único spinner global.
 *
 * @property isLoadingTasks `true` mientras se cargan las tareas del grupo.
 * @property tasks Lista de tareas del grupo mapeadas al modelo de UI [TaskUIModel].
 * @property isLoadingMembers `true` mientras se carga la lista de miembros.
 * @property members Lista de miembros del grupo con sus roles y fechas de incorporación.
 * @property isLoadingInvites `true` mientras se carga la lista de invitaciones.
 * @property invites Lista de invitaciones del grupo (activas, expiradas y revocadas).
 * @property isLoadingAudit `true` mientras se carga el log de auditoría.
 * @property auditEvents Lista paginada de eventos de auditoría del grupo.
 * @property error Mensaje de error de la última operación fallida, o `null` si no hay error.
 */
data class GroupDetailState(
    val isLoadingTasks: Boolean = false,
    val tasks: List<TaskUIModel> = emptyList(),
    val isLoadingMembers: Boolean = false,
    val members: List<GroupMemberOut> = emptyList(),
    val isLoadingInvites: Boolean = false,
    val invites: List<InviteOut> = emptyList(),
    val isLoadingAudit: Boolean = false,
    val auditEvents: List<AuditEventOut> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel de la pantalla de detalle de un grupo.
 *
 * Gestiona las distintas secciones de contenido que se muestran dentro de la pantalla
 * de detalle de un grupo: tareas, miembros, invitaciones y auditoría. Cada sección
 * se carga de forma independiente bajo demanda para minimizar el tiempo de carga
 * inicial.
 *
 * Las operaciones de administración (expulsar miembros, cambiar roles, crear/revocar
 * invitaciones, abandonar el grupo, editar metadatos) también se delegan aquí y
 * recargan automáticamente los datos afectados tras completarse.
 *
 * @property taskRepository Repositorio de tareas para obtener y manipular tareas del grupo.
 * @property groupRepository Repositorio de grupos para operaciones de miembros, invitaciones
 *   y auditoría.
 */
@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val groupRepository: GroupRepository
) : ViewModel() {
    /** Estado mutable interno; solo modificable desde este ViewModel. */
    private val _state = MutableStateFlow(GroupDetailState())

    /** Estado público expuesto a la UI como flujo de solo lectura. */
    val state: StateFlow<GroupDetailState> = _state

    /**
     * Carga inicial de la pantalla de detalle. Actualmente carga solo las tareas del grupo.
     * Las demás secciones (miembros, invitaciones, auditoría) se cargan bajo demanda
     * cuando el usuario navega a la pestaña correspondiente.
     *
     * @param gid Identificador del grupo cuyo detalle se va a mostrar.
     */
    fun load(gid: Int) {
        loadTasks(gid)
    }

    /**
     * Carga las tareas del grupo desde el servidor y las mapea al modelo de UI.
     * Cada tarea se convierte en un [TaskUIModel] con el tipo, prioridad, recompensas y
     * fechas adecuadas.
     * Actualiza [GroupDetailState.isLoadingTasks] durante la operación.
     *
     * @param gid Identificador del grupo cuyas tareas se quieren cargar.
     */
    fun loadTasks(gid: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingTasks = true, error = null) }
            try {
                val responses = taskRepository.getTasksRemote(gid)
                val tasks = responses.map { r ->
                    TaskUIModel(
                        id = r.id,
                        title = r.titulo,
                        description = r.descripcion,
                        priority = TaskPriority.entries.getOrNull(r.prioridad) ?: TaskPriority.LOW,
                        xp = r.recompensaXp,
                        ludiones = r.recompensaLudion,
                        tipo = r.tipo,
                        isCompleted = r.estado == "COMPLETE",
                        parentId = r.idObjetivo,
                        habitFrequency = r.extraHabito?.frequency?.name,
                        dueDate = r.extraUnico?.fechaLimite ?: r.fechaValida
                    )
                }
                _state.update { it.copy(isLoadingTasks = false, tasks = tasks) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoadingTasks = false, error = e.message) }
            }
        }
    }

    /**
     * Carga la lista de miembros del grupo desde el servidor.
     * Actualiza [GroupDetailState.isLoadingMembers] durante la operación.
     *
     * @param gid Identificador del grupo cuyos miembros se quieren cargar.
     */
    fun loadMembers(gid: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingMembers = true, error = null) }
            try {
                val res = groupRepository.getMembers(gid)
                _state.update { it.copy(isLoadingMembers = false, members = res.members) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoadingMembers = false, error = e.message) }
            }
        }
    }

    /**
     * Carga la lista de invitaciones del grupo (activas, expiradas y revocadas).
     * Actualiza [GroupDetailState.isLoadingInvites] durante la operación.
     * Solo Owners y Moderadores pueden ver las invitaciones; el servidor devolverá
     * un error 403 si el usuario no tiene permisos.
     *
     * @param gid Identificador del grupo cuyas invitaciones se quieren listar.
     */
    fun loadInvites(gid: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingInvites = true, error = null) }
            try {
                val res = groupRepository.listInvites(gid)
                _state.update { it.copy(isLoadingInvites = false, invites = res.invites) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoadingInvites = false, error = e.message) }
            }
        }
    }

    /**
     * Carga el historial de eventos de auditoría del grupo con paginación.
     * Actualiza [GroupDetailState.isLoadingAudit] durante la operación.
     *
     * @param gid Identificador del grupo cuyo historial se quiere cargar.
     * @param limit Número máximo de eventos a cargar (por defecto 50).
     * @param offset Desplazamiento para paginación (por defecto 0).
     */
    fun loadAudit(gid: Int, limit: Int = 50, offset: Long = 0L) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingAudit = true, error = null) }
            try {
                val res = groupRepository.getAudit(gid, limit = limit, offset = offset)
                _state.update { it.copy(isLoadingAudit = false, auditEvents = res.events) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoadingAudit = false, error = e.message) }
            }
        }
    }

    /**
     * Marca una tarea del grupo como completada en el servidor.
     * Recarga las tareas del grupo tras la operación para reflejar el cambio de estado.
     *
     * @param gid Identificador del grupo al que pertenece la tarea.
     * @param tid Identificador de la tarea a completar.
     */
    fun completeTask(gid: Int, tid: Int) {
        viewModelScope.launch {
            try {
                taskRepository.completarTarea(tid)
                loadTasks(gid)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * Revierte el estado de completado de una tarea del grupo (la marca como pendiente).
     * Recarga las tareas del grupo tras la operación.
     *
     * @param gid Identificador del grupo al que pertenece la tarea.
     * @param tid Identificador de la tarea a revertir.
     */
    fun uncompleteTask(gid: Int, tid: Int) {
        viewModelScope.launch {
            try {
                taskRepository.uncompleteTarea(tid)
                loadTasks(gid)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * Alterna el estado de completado de una tarea según su estado actual.
     * Si la tarea está completada, la revierte a pendiente; si está pendiente, la completa.
     *
     * @param gid Identificador del grupo al que pertenece la tarea.
     * @param task Modelo de UI de la tarea cuyo estado se va a alternar.
     */
    fun toggleTaskCompletion(gid: Int, task: TaskUIModel) {
        if (task.isCompleted) uncompleteTask(gid, task.id) else completeTask(gid, task.id)
    }

    /**
     * Elimina una tarea del grupo del servidor.
     * Implementa una actualización optimista de la UI: elimina la tarea localmente
     * de inmediato y sincroniza con el servidor en segundo plano. Si la operación
     * falla, recarga las tareas para restaurar el estado correcto.
     *
     * @param gid Identificador del grupo al que pertenece la tarea.
     * @param tid Identificador de la tarea a eliminar.
     */
    fun deleteTask(gid: Int, tid: Int) {
        viewModelScope.launch {
            // Optimistic UI: remove locally first.
            _state.update { s -> s.copy(tasks = s.tasks.filterNot { it.id == tid }) }
            try {
                taskRepository.eliminarTarea(tid)
                loadTasks(gid)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
                loadTasks(gid)
            }
        }
    }

    /**
     * Edita los metadatos de una tarea del grupo en el servidor.
     * Si la tarea es de tipo Hábito, convierte la frecuencia de `String` a [HabitFrequency].
     * Recarga las tareas del grupo tras la operación.
     *
     * @param gid Identificador del grupo al que pertenece la tarea.
     * @param tid Identificador de la tarea a editar.
     * @param titulo Nuevo título de la tarea.
     * @param descripcion Nueva descripción de la tarea.
     * @param prioridad Nueva prioridad de la tarea.
     * @param fechaLimite Nueva fecha límite en formato ISO-8601 (solo para tareas únicas),
     *   o `null` si no aplica.
     * @param frecuencia Nueva frecuencia como nombre del enum [HabitFrequency] (solo para
     *   hábitos), o `null` si no aplica.
     */
    fun editTask(
        gid: Int,
        tid: Int,
        titulo: String,
        descripcion: String,
        prioridad: TaskPriority,
        fechaLimite: String?,
        frecuencia: String?
    ) {
        viewModelScope.launch {
            try {
                val freqEnum = frecuencia?.let { runCatching { HabitFrequency.valueOf(it) }.getOrNull() }
                taskRepository.editarTarea(tid, titulo, descripcion, prioridad, fechaLimite, freqEnum)
                loadTasks(gid)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * Expulsa a un miembro del grupo. Solo Owners y Moderadores pueden usar esta acción.
     * Recarga la lista de miembros tras la operación.
     *
     * @param gid Identificador del grupo.
     * @param userId Identificador del miembro a expulsar.
     */
    fun kickMember(gid: Int, userId: Int) {
        viewModelScope.launch {
            try {
                groupRepository.removeUser(gid = gid, userId = userId)
                loadMembers(gid)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * Cambia el rol de un miembro del grupo. Solo el Owner puede usar esta acción.
     * Recarga la lista de miembros tras la operación para reflejar el nuevo rol.
     *
     * @param gid Identificador del grupo.
     * @param userId Identificador del miembro cuyo rol se va a modificar.
     * @param role Nuevo rol: `0` = Miembro normal, `1` = Moderador.
     */
    fun setMemberRole(gid: Int, userId: Int, role: Int) {
        viewModelScope.launch {
            try {
                groupRepository.setMemberRole(gid = gid, userId = userId, role = role)
                loadMembers(gid)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * Crea una invitación segura para el grupo y recarga la lista de invitaciones.
     * Solo Owners y Moderadores pueden crear invitaciones.
     *
     * @param gid Identificador del grupo.
     * @param expiresInSeconds Segundos de validez desde la creación, o `null` para sin
     *   expiración.
     * @param maxUses Límite máximo de usos, o `null` para usos ilimitados.
     */
    fun createInvite(gid: Int, expiresInSeconds: Long? = null, maxUses: Int? = null) {
        viewModelScope.launch {
            try {
                groupRepository.createInvite(gid = gid, expiresInSeconds = expiresInSeconds, maxUses = maxUses)
                loadInvites(gid)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * Revoca una invitación activa del grupo y recarga la lista de invitaciones.
     * Solo Owners y Moderadores pueden revocar invitaciones.
     *
     * @param gid Identificador del grupo.
     * @param code Código de invitación en texto claro a revocar.
     */
    fun revokeInvite(gid: Int, code: String) {
        viewModelScope.launch {
            try {
                groupRepository.revokeInvite(gid = gid, code = code)
                loadInvites(gid)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * Abandona voluntariamente el grupo y refresca la lista global de grupos.
     * El Owner no puede abandonar el grupo sin transferir antes la propiedad.
     * Tras la operación, llama a [GroupRepository.refreshGroups] para actualizar el
     * caché local y que el grupo desaparezca de la pantalla de lista.
     *
     * @param gid Identificador del grupo a abandonar.
     */
    fun leaveGroup(gid: Int) {
        viewModelScope.launch {
            try {
                groupRepository.leaveGroup(gid)
                groupRepository.refreshGroups()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * Edita los metadatos del grupo (nombre y/o descripción) y refresca la lista global.
     * Solo Owners y Moderadores pueden editar grupos.
     *
     * @param gid Identificador del grupo a editar.
     * @param name Nuevo nombre del grupo, o `null` para no cambiarlo.
     * @param desc Nueva descripción del grupo, o `null` para no cambiarla.
     */
    fun editGroup(gid: Int, name: String?, desc: String?) {
        viewModelScope.launch {
            try {
                groupRepository.editGroup(gid = gid, name = name, desc = desc)
                groupRepository.refreshGroups()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * Crea una nueva tarea en el grupo desde el diálogo de creación rápida.
     * Construye el [CreateTareaRequest] adecuado según el tipo de tarea:
     * - Para tareas únicas: añade [CreateTareaUniqueData] con la fecha límite.
     * - Para hábitos: añade [CreateTareaHabitData] con la frecuencia.
     * Recarga las tareas del grupo tras la creación.
     *
     * @param gid Identificador del grupo en el que se crea la tarea.
     * @param title Título de la nueva tarea.
     * @param description Descripción de la nueva tarea.
     * @param tipo Tipo de tarea como nombre del enum [TaskType] (p. ej. `"UNICO"`, `"HABITO"`).
     * @param prioridad Prioridad de la tarea como entero (0 = Baja, 1 = Media, 2 = Alta).
     * @param frecuencia Frecuencia del hábito como nombre del enum [HabitFrequency], o `null`
     *   si no aplica.
     * @param fechaLimite Fecha límite en formato `YYYY-MM-DD` para tareas únicas, o `null`
     *   si no aplica.
     */
    fun createTaskFromDialog(
        gid: Int,
        title: String,
        description: String,
        tipo: String,
        prioridad: Int,
        frecuencia: String?,
        fechaLimite: String?
    ) {
        viewModelScope.launch {
            try {
                val extraUnico = if (tipo == TaskType.UNICO.name) {
                    val safeDate = if (!fechaLimite.isNullOrBlank()) "${fechaLimite}T23:59:59Z" else "2026-12-31T23:59:59Z"
                    CreateTareaUniqueData(fechaLimite = safeDate)
                } else null

                val extraHabito = if (tipo == TaskType.HABITO.name && !frecuencia.isNullOrBlank()) {
                    val f = runCatching { HabitFrequency.valueOf(frecuencia) }.getOrNull() ?: HabitFrequency.DAILY
                    CreateTareaHabitData(numeroFrecuencia = 1, frequency = f)
                } else null

                val req = CreateTareaRequest(
                    gid = gid,
                    titulo = title,
                    descripcion = description,
                    tipo = tipo,
                    prioridad = prioridad,
                    extraUnico = extraUnico,
                    extraHabito = extraHabito
                )
                taskRepository.createTareaDirect(req)
                loadTasks(gid)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }
}

