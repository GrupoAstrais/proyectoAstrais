package com.mm.astraisandroid.data.repository

import android.content.Context
import com.mm.astraisandroid.data.preferences.SessionManager
import com.mm.astraisandroid.sync.scheduleSync
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SessionOrchestrator @Inject constructor(
    private val taskRepository: TaskRepository,
    private val groupRepository: GroupRepository,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) {
    suspend fun logout() {
        sessionManager.clear()
        taskRepository.clearLocalData()
        groupRepository.clearLocalData()
    }

    suspend fun onLoginSuccess(wasGuest: Boolean, personalGid: Int) {
        if (wasGuest) {
            taskRepository.migrateGuestTasksToServer(personalGid)
        }
        scheduleSync(context)
    }
}
