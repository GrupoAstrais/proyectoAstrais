package com.mm.astraisandroid.ui.features.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.data.local.entities.toDomain
import com.mm.astraisandroid.data.preferences.SessionManager
import com.mm.astraisandroid.data.repository.GroupRepository
import com.mm.astraisandroid.ui.components.SnackbarManager
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
 * @property generatedInviteUrl URL de invitación generada para un grupo, o `null` si no se ha generado.
 */
data class GroupScreenState(
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val groups: List<Grupo> = emptyList(),
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
 * - Maneja errores de red y los expone vía [SnackbarManager].
 *
 * @property repository Repositorio de grupos que actúa como fuente de verdad.
 */
@HiltViewModel
class GroupViewModel @Inject constructor(
    private val repository: GroupRepository,
    private val sessionManager: SessionManager,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private val _state = MutableStateFlow(GroupScreenState())

    val state: StateFlow<GroupScreenState> = combine(
        repository.allGroups,
        _state
    ) { dbGroups, currentState ->
        currentState.copy(groups = dbGroups.map { it.toDomain() })
    }.stateIn(viewModelScope, SharingStarted.Eagerly, GroupScreenState())

    init {
        loadGroups()
    }

    /**
     * Carga los grupos del usuario desde el servidor.
     * Bloqueada para usuarios invitados (guest) que no pueden acceder al backend.
     */
    fun loadGroups() {
        if (sessionManager.isGuest()) return
        viewModelScope.launch {
            refreshGroupsSuspend()
        }
    }

    private suspend fun refreshGroupsSuspend() {
        _state.update { it.copy(isLoading = true) }
        val result = repository.refreshGroups()
        _state.update { it.copy(isLoading = false, isOffline = result.isFailure) }
    }

    /**
     * Crea un nuevo grupo con nombre y descripción, luego refresca la lista.
     *
     * @param name Nombre del nuevo grupo.
     * @param desc Descripción del nuevo grupo.
     */
    fun createGroup(name: String, desc: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                repository.createGroup(name, desc)
                refreshGroupsSuspend()
                snackbarManager.showMessage("Grupo creado correctamente")
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                snackbarManager.showMessage(e.message ?: "Error al crear grupo")
            }
        }
    }

    /**
     * Edita los metadatos de un grupo existente y refresca la lista.
     *
     * @param gid Identificador del grupo a editar.
     * @param name Nuevo nombre del grupo, o `null` para no cambiarlo.
     * @param desc Nueva descripción del grupo, o `null` para no cambiarla.
     */
    fun editGroup(gid: Int, name: String?, desc: String?) {
        viewModelScope.launch {
            try {
                repository.editGroup(gid, name, desc)
                loadGroups()
                snackbarManager.showMessage("Grupo actualizado")
            } catch (e: Exception) {
                snackbarManager.showMessage(e.message ?: "Error al editar grupo")
            }
        }
    }

    /**
     * Elimina un grupo y refresca la lista.
     *
     * @param gid Identificador del grupo a eliminar.
     */
    fun deleteGroup(gid: Int) {
        viewModelScope.launch {
            try {
                repository.deleteGroup(gid)
                loadGroups()
                snackbarManager.showMessage("Grupo eliminado")
            } catch (e: Exception) {
                snackbarManager.showMessage(e.message ?: "Error al eliminar grupo")
            }
        }
    }

    /**
     * Limpia la URL de invitación generada almacenada en el estado.
     */
    fun clearInviteUrl() {
        _state.update { it.copy(generatedInviteUrl = null) }
    }

    /**
     * Agrega un usuario existente a un grupo por su ID.
     *
     * @param gid Identificador del grupo.
     * @param userId Identificador del usuario a agregar.
     */
    fun addUser(gid: Int, userId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                repository.addUser(gid = gid, userId = userId)
                snackbarManager.showMessage("Usuario agregado al grupo")
                refreshGroupsSuspend()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                snackbarManager.showMessage(e.message ?: "Error al agregar usuario")
            }
        }
    }

    /**
     * Elimina un usuario de un grupo.
     *
     * @param gid Identificador del grupo.
     * @param userId Identificador del usuario a eliminar.
     */
    fun removeUser(gid: Int, userId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                repository.removeUser(gid = gid, userId = userId)
                snackbarManager.showMessage("Usuario eliminado del grupo")
                refreshGroupsSuspend()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                snackbarManager.showMessage(e.message ?: "Error al eliminar usuario")
            }
        }
    }

    /**
     * Transfiere la propiedad del grupo a otro usuario.
     *
     * @param gid Identificador del grupo.
     * @param newOwnerUserId Identificador del nuevo propietario.
     */
    fun passOwnership(gid: Int, newOwnerUserId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                repository.passOwnership(gid = gid, newOwnerUserId = newOwnerUserId)
                snackbarManager.showMessage("Propiedad transferida correctamente")
                refreshGroupsSuspend()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                snackbarManager.showMessage(e.message ?: "Error al transferir propiedad")
            }
        }
    }

    /**
     * Genera una URL de invitación para un grupo y la almacena en el estado.
     *
     * @param gid Identificador del grupo.
     */
    fun createInviteUrl(gid: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, generatedInviteUrl = null) }
            try {
                val url = repository.createInviteUrl(gid)
                _state.update { it.copy(isLoading = false, generatedInviteUrl = url) }
                snackbarManager.showMessage("URL de invitacion generada")
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                snackbarManager.showMessage(e.message ?: "Error al generar URL")
            }
        }
    }

    /**
     * Une al usuario actual a un grupo mediante una URL de invitación.
     *
     * @param inviteUrl URL de invitación generada por un administrador del grupo.
     */
    fun joinByUrl(inviteUrl: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                repository.joinByUrl(inviteUrl)
                snackbarManager.showMessage("Te has unido al grupo correctamente")
                refreshGroupsSuspend()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                snackbarManager.showMessage(e.message ?: "Error al unirse al grupo")
            }
        }
    }
}
