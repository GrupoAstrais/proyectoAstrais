package com.astrais.groups

import com.astrais.*
import com.astrais.db.GroupRoles
import com.astrais.db.getDatabaseDaoImpl
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(GroupRepoImpl::class.java)

class GroupRepoImpl : GroupRepo{
    override suspend fun getAllUserGroups(userId : Int) : List<SingleGroupOut>{
        try {
            val d = getDatabaseDaoImpl().getGroupsOfUser(userId)
            if (d.isNotEmpty()){
                return d.map { gp->
                    var r = ROLE_USEROWNER
                    if (gp.owner.value != userId){
                        val d = getDatabaseDaoImpl().getUserRoleOnGroup(idusuario = userId, idgrupo = gp.id.value)
                        r = if (d == GroupRoles.MOD){
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
        return emptyList()
    }

    override suspend fun createGroup(name: String, desc: String, ownerId: Int) : Int{
        try {
            val gid = getDatabaseDaoImpl().createGroup(ownerId, name, desc)
            log.debug("Created group with ID $gid for owner $ownerId")
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
                    return AddUserReturn.OK
                }else{
                    return AddUserReturn.ALREADYJOINED
                }
            }
            return AddUserReturn.NOPERMISSION
        } catch (e : ExposedSQLException){
            log.error("Couldn't add $userId to $gid with requester $requesterId. Message: ${e.message}")
            return AddUserReturn.CONNERR
        }
    }
}

