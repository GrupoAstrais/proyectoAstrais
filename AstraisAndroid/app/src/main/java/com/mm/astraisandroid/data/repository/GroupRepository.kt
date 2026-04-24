package com.mm.astraisandroid.data.repository

import com.mm.astraisandroid.data.api.CreateGroupRequest
import com.mm.astraisandroid.data.api.DeleteGroupRequest
import com.mm.astraisandroid.data.api.EditGroupRequest
import com.mm.astraisandroid.data.api.services.GroupApi
import com.mm.astraisandroid.data.local.dao.GrupoDao
import com.mm.astraisandroid.data.local.entities.GrupoEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GroupRepository @Inject constructor(
    private val groupApi: GroupApi,
    private val grupoDao: GrupoDao
) {
    val allGroups: Flow<List<GrupoEntity>> = grupoDao.getAllGroups()

    suspend fun refreshGroups(): Result<Unit> {
        return try {
            val response = groupApi.getGroups()
            val entities = response.groupList.map {
                GrupoEntity(
                    id = it.id,
                    name = it.name,
                    description = it.description,
                    role = it.role
                )
            }
            grupoDao.insertGroups(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createGroup(name: String, desc: String): Int {
        val req = CreateGroupRequest(name, desc)
        return groupApi.createGroup(req)
    }

    suspend fun editGroup(gid: Int, name: String?, desc: String?) {
        val req = EditGroupRequest(gid, name, desc)
        groupApi.editGroup(req)
    }

    suspend fun deleteGroup(gid: Int) {
        val req = DeleteGroupRequest(gid)
        groupApi.deleteGroup(req)
        grupoDao.deleteGroupById(gid)
    }
}
