package com.mm.astraisandroid.ui.features.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.data.local.entities.toDomain
import com.mm.astraisandroid.data.preferences.SessionManager
import com.mm.astraisandroid.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la pantalla de lista de grupos.
 *
 * @property isLoading Indica si hay una operación de red o datos en curso.
 * @property isOffline `true` si el último intento de sincronización falló por falta
 *   de conectividad o error de servidor; los datos mostrados son del caché local.
 * @property groups Lista de grupos del usuario extraída de la base de datos local.
 * @property error Mensaje de error de la última operación fallida, o `null` si no hay error.
 * @property infoMessage Mensaje informativo, o `null` si no hay mensaje.
 * @property generatedInviteUrl URL de invitación generada para un grupo, o `null` si no se ha generado.
 */
data class GroupScreenState(
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val groups: List<Grupo> = emptyList(),
    val error: String? = null,
    val infoMessage: String? = null,
    val generatedInviteUrl: String? = null
)

/**
 * ViewModel de la pantalla de lista de grupos de Astrais.
 *
 * Gestiona el ciclo de vida de los datos de grupos para la UI, actuando como puente
 * entre la capa de datos ([GroupRepository]) y la pantalla Compose. Expone un
 * [StateFlow] inmutable ([state]) con el estado actual de la pantalla.
 *
 * Funcionalidades clave:
 * - Observa la base de datos local de Room vía [GroupRepository.allGroups] para
 *   actualizar la UI en tiempo real sin esperar respuestas de red.
 * - Sincroniza los grupos con el servidor a través de [loadGroups].
 * - Implementa un guard de invitado: los usuarios no registrados (guest) no pueden
 *   realizar llamadas al backend.
 * - Maneja errores de red y los expone en [GroupScreenState.error].
 *
 * @property repository Repositorio de grupos que actúa como fuente de verdad.
 */
@HiltViewModel
class GroupViewModel @Inject constructor(
    private val repository: GroupRepository
) : ViewModel() {

    /**
     * Estado mutable interno; sólo modificable desde el ViewModel.
     */
    private val _state = MutableStateFlow(GroupScreenState())

    /**
     * Estado público expuesto a la UI como flujo de sólo lectura.
     */
    val state: StateFlow<GroupScreenState> = combine(
        repository.allGroups,
        _state
    ) { dbGroups, currentState ->
        currentState.copy(groups = dbGroups.map { it.toDomain() })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GroupScreenState())

    /**
     * Bloque de inicialización: suscribe al [Flow] de grupos locales de Room para que
     * la UI se actualice automáticamente cuando cambia la base de datos, y dispara la
     * primera sincronización con el servidor.
     */
    init {
        loadGroups()
    }

    /**
     * Solicita una sincronización de grupos con el servidor.
     * Marca [GroupScreenState.isLoading] durante la operación.
     * Si falla, activa [GroupScreenState.isOffline] y los datos del caché local siguen
     * siendo visibles.
     * Los usuarios invitados son bloqueados antes de realizar la llamada de red.
     */
    fun loadGroups() {
        // STRICT GUEST GUARD: groups are locked for guests, never hit backend
        if (SessionManager.isGuest()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = repository.refreshGroups()
            _state.update { it.copy(isLoading = false, isOffline = result.isFailure) }
        }
    }

    /**
     * Crea un nuevo grupo en el servidor y refresca la lista local.
     * El usuario autenticado pasa a ser el Owner del grupo creado.
     * Actualiza [GroupScreenState.error] si la operación falla.
     *
     * @param name Nombre del grupo.
     * @param desc Descripción del grupo.
     */
    fun createGroup(name: String, desc: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                repository.createGroup(name, desc)
                loadGroups()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /**
     * Edita los metadatos de un grupo existente.
     * Solo Owners y Moderadores pueden editar grupos.
     * Refresca la lista local tras la operación.
     *
     * @param gid Identificador del grupo a editar.
     * @param name Nuevo nombre, o `null` para no cambiarlo.
     * @param desc Nueva descripción, o `null` para no cambiarla.
     */
    fun editGroup(gid: Int, name: String?, desc: String?) {
        viewModelScope.launch {
            try {
                repository.editGroup(gid, name, desc)
                loadGroups()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * Elimina permanentemente un grupo del servidor.
     * Solo el Owner puede eliminar un grupo. Refresca la lista local tras la operación.
     *
     * @param gid Identificador del grupo a eliminar.
     */
    fun deleteGroup(gid: Int) {
        viewModelScope.launch {
            try {
                repository.deleteGroup(gid)
                loadGroups()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * Limpia el mensaje de error actual del estado.
     * Debe llamarse desde la UI después de que el error haya sido mostrado al usuario
     * para evitar que persista en la pantalla.
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Limpia el mensaje informativo del estado.
     * Debe llamarse desde la UI después de que el mensaje informativo haya sido mostrado.
     */
    fun clearInfoMessage() {
        _state.update { it.copy(infoMessage = null, generatedInviteUrl = null) }
    }

    /**
     * Agrega un usuario a un grupo de forma directa.
     * Solo Owners y Moderadores pueden añadir usuarios.
     * Actualiza [GroupScreenState.infoMessage] con un mensaje de éxito o [GroupScreenState.error]
     * si falla.
     *
     * @param gid Identificador del grupo.
     * @param userId Identificador del usuario que se desea añadir.
     */
    fun addUser(gid: Int, userId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                repository.addUser(gid = gid, userId = userId)
                _state.update { it.copy(infoMessage = "Usuario agregado al grupo") }
                loadGroups()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /**
     * Expulsa a un miembro de un grupo.
     * Solo Owners y Moderadores pueden expulsar miembros.
     * Actualiza [GroupScreenState.error] si la operación falla.
     *
     * @param gid Identificador del grupo.
     * @param userId Identificador del usuario a expulsar.
     */
    fun removeUser(gid: Int, userId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                repository.removeUser(gid = gid, userId = userId)
                _state.update { it.copy(infoMessage = "Usuario eliminado del grupo") }
                loadGroups()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /**
     * Transfiere la propiedad de un grupo a otro miembro.
     * Solo el Owner actual puede realizar esta operación.
     * Refresca la lista local para reflejar el cambio de rol.
     *
     * @param gid Identificador del grupo.
     * @param newOwnerUserId Identificador del miembro que pasará a ser Owner.
     */
    fun passOwnership(gid: Int, newOwnerUserId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                repository.passOwnership(gid = gid, newOwnerUserId = newOwnerUserId)
                _state.update { it.copy(infoMessage = "Propiedad transferida correctamente") }
                loadGroups()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /**
     * Crea una invitación segura para el grupo y muestra la URL resultante en
     * [GroupScreenState.generatedInviteUrl].
     * Solo Owners y Moderadores pueden crear invitaciones.
     *
     * @param gid Identificador del grupo para el que se genera la invitación.
     */
    fun createInviteUrl(gid: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, generatedInviteUrl = null) }
            try {
                val url = repository.createInviteUrl(gid)
                _state.update { it.copy(isLoading = false, generatedInviteUrl = url, infoMessage = "URL de invitacion generada") }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /**
     * Une al usuario autenticado a un grupo mediante una URL de invitación.
     * Soporta URLs con `?code=` (token seguro) y `?gid=` (flujo legado).
     * Refresca la lista de grupos si la operación es exitosa.
     *
     * @param inviteUrl URL de invitación completa.
     */
    fun joinByUrl(inviteUrl: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                repository.joinByUrl(inviteUrl)
                _state.update { it.copy(infoMessage = "Te has unido al grupo correctamente") }
                loadGroups()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
