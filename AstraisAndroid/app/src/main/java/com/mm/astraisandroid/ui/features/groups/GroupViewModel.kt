package com.mm.astraisandroid.ui.features.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.data.local.entities.toDomain
import com.mm.astraisandroid.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupScreenState(
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val groups: List<Grupo> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val repository: GroupRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GroupScreenState())
    val state: StateFlow<GroupScreenState> = combine(
        repository.allGroups,
        _state
    ) { dbGroups, currentState ->
        currentState.copy(groups = dbGroups.map { it.toDomain() })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GroupScreenState())

    init {
        loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = repository.refreshGroups()
            _state.update { it.copy(isLoading = false, isOffline = result.isFailure) }
        }
    }

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

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
