package com.mm.astraisandroid.data.repository

import android.content.Context
import com.mm.astraisandroid.data.preferences.SessionManager
import com.mm.astraisandroid.sync.scheduleSync
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Coordinador de operaciones de sesión que trascienden un único repositorio.
 *
 * Gestiona las acciones que deben ejecutarse al iniciar o cerrar sesión,
 * como la limpieza de datos locales, la migración de tareas invitadas y
 * la programación de la sincronización en segundo plano.
 *
 * @property taskRepository Repositorio de tareas para migración y limpieza.
 * @property groupRepository Repositorio de grupos para limpieza de caché.
 * @property sessionManager Gestor de sesión para consultar y limpiar estado.
 * @property context Contexto de la aplicación necesario para programar WorkManager.
 */
class SessionOrchestrator @Inject constructor(
    private val taskRepository: TaskRepository,
    private val groupRepository: GroupRepository,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) {
    /**
     * Cierra la sesión del usuario: limpia tokens, elimina datos locales de tareas y grupos.
     */
    suspend fun logout() {
        sessionManager.clear()
        taskRepository.clearLocalData()
        groupRepository.clearLocalData()
    }

    /**
     * Ejecuta las operaciones post-login exitoso: migra tareas invitadas si aplica
     * y programa la sincronización inicial con el servidor.
     *
     * @param wasGuest `true` si el usuario estaba en modo invitado antes del login.
     * @param personalGid Identificador del grupo personal del usuario recién autenticado.
     */
    suspend fun onLoginSuccess(wasGuest: Boolean, personalGid: Int) {
        if (wasGuest) {
            taskRepository.migrateGuestTasksToServer(personalGid)
        }
        scheduleSync(context)
    }
}
