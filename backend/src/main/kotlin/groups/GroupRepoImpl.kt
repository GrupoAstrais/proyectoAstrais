package com.astrais.groups

import ROLE_USERMOD
import ROLE_USERNORMAL
import ROLE_USEROWNER
import com.astrais.db.GroupRoles
import com.astrais.db.getDatabaseDaoImpl
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.slf4j.LoggerFactory
import groups.types.*

private val log = LoggerFactory.getLogger(GroupRepoImpl::class.java)

class GroupRepoImpl : GroupRepo{
    override suspend fun getAllUserGroups(userId : Int) : List<SingleGroupOut>{
        log.info("Function: getAllUserGroups($userId)")
        try {
            val d = getDatabaseDaoImpl().getGroupsOfUser(userId)
            if (d.isNotEmpty()){
                log.info("User joined groups, preparing them to return...")
                return d.map { gp->
                    var r = ROLE_USEROWNER
                    if (gp.owner.value != userId){
                        val da = getDatabaseDaoImpl().getUserRoleOnGroup(idusuario = userId, idgrupo = gp.id.value)
                        r = if (da == GroupRoles.MOD){
                            ROLE_USERMOD
                        }else{
                            ROLE_USERNORMAL
                        }
                    }

                    SingleGroupOut(
                        id = gp.id.value,
                        name = gp.nombre,
                        description = gp.descripcion,
                        role = r
                    )
                }
            }
        } catch (e : ExposedSQLException){
            log.error("Error querying all user $userId groups! Message: ${e.message}")
        }
        log.info("No groups, returning empty list")
        return emptyList()
    }

    override suspend fun createGroup(name: String, desc: String, ownerId: Int) : Int{
        try {
            val gid = getDatabaseDaoImpl().createGroup(ownerId, name, desc)
            log.info("Created group with ID $gid for owner $ownerId")
            return gid
        } catch (e : ExposedSQLException){
            log.error("Error creating the group $name for $ownerId. Message: ${e.message}")
        }
        return -1
    }

    override suspend fun addUser(requesterId: Int, userId: Int, gid: Int): AddUserReturn {
        try {
            val gp = getDatabaseDaoImpl().getGroupById(gid) ?: return AddUserReturn.NOGROUP
            var role = ROLE_USEROWNER

            if (gp.owner.value != requesterId){
                val d = getDatabaseDaoImpl().getUserRoleOnGroup(idusuario = requesterId, idgrupo = gid)
                role = if (d == GroupRoles.MOD){
                    ROLE_USERMOD
                }else{
                    ROLE_USERNORMAL
                }
            }

            if (role != ROLE_USERNORMAL){
                val add = getDatabaseDaoImpl().addUserToGroup(userId, gid)
                if (add){
                    log.info("Added user $userId to group $gid by $requesterId.")
                    return AddUserReturn.OK
                }else{
                    log.info("User $userId is already on group $gid.")
                    return AddUserReturn.ALREADYJOINED
                }
            }
            log.info("User $requesterId has no permission for adding someone.")
            return AddUserReturn.NOPERMISSION
        } catch (e : ExposedSQLException){
            log.error("Couldn't add $userId to $gid with requester $requesterId. Message: ${e.message}")
            return AddUserReturn.CONNERR
        }
    }

    override suspend fun deleteGroup(gid: Int, uid: Int): Boolean {
        if (getDatabaseDaoImpl().checkIfUserIsAdmin(uid = uid, gid = gid)){
            return getDatabaseDaoImpl().deleteGroup(gid)
        }
        return false
    }

    override suspend fun editGroup(gid: Int, uid: Int, name: String?, desc: String?): Boolean {
        if (getDatabaseDaoImpl().checkIfUserIsAdmin(uid = uid, gid = gid) || getDatabaseDaoImpl().getUserRoleOnGroup(uid,gid) == GroupRoles.MOD){
            return getDatabaseDaoImpl().editGroup(gid = gid, name = name, desc = desc)
        }
        return false
    }
}

