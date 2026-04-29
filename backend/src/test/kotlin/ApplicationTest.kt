package com.astrais

import com.astrais.groups.*
import com.astrais.groups.groupRoutes
import groups.types.CreateInviteRequest
import groups.types.JoinByCodeRequest
import groups.types.RevokeInviteRequest
import groups.types.InviteOut
import groups.types.GroupMembersResponse
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

class ApplicationTest {

    private fun testToken(uid: Int = 123): String {
        return JWT.create()
            .withSubject(uid.toString())
            .sign(Algorithm.HMAC256("test-secret"))
    }

    @Test
    fun testCreateInviteOk() = testApplication {
        val prev = groupRepoProvider
        groupRepoProvider = {
            object : GroupRepo {
                override suspend fun getAllUserGroups(userId: Int) = emptyList<groups.types.SingleGroupOut>()
                override suspend fun createGroup(name: String, desc: String, ownerId: Int) = -1
                override suspend fun addUser(requesterId: Int, userId: Int, gid: Int) = AddUserReturn.CONNERR
                override suspend fun removeUser(requesterId: Int, userId: Int, gid: Int) = RemoveUserReturn.CONNERR
                override suspend fun passOwnership(requesterId: Int, newOwnerUserId: Int, gid: Int) = PassOwnershipReturn.CONNERR
                override suspend fun generateInviteUrl(requesterId: Int, gid: Int) = Pair(InviteUrlReturn.CONNERR, null)
                override suspend fun joinByInviteUrl(requesterId: Int, inviteUrl: String) = InviteUrlReturn.CONNERR
                override suspend fun createInvite(requesterId: Int, gid: Int, expiresInSeconds: Long?, maxUses: Int?) =
                    Pair(InviteUrlReturn.OK, InviteOut(code = "abc", inviteUrl = "astrais://groups/join?code=abc"))
                override suspend fun listInvites(requesterId: Int, gid: Int) = Pair(InviteUrlReturn.OK, emptyList<InviteOut>())
                override suspend fun revokeInvite(requesterId: Int, gid: Int, code: String) = InviteUrlReturn.OK
                override suspend fun joinByCode(requesterId: Int, code: String) = InviteUrlReturn.OK
                override suspend fun listMembers(requesterId: Int, gid: Int) = Pair(MemberListReturn.OK, emptyList<groups.types.GroupMemberOut>())
                override suspend fun leaveGroup(requesterId: Int, gid: Int) = LeaveGroupReturn.OK
                override suspend fun setMemberRole(requesterId: Int, gid: Int, userId: Int, role: Int) = SetMemberRoleReturn.OK
                override suspend fun listAudit(requesterId: Int, gid: Int, limit: Int, offset: Long) = Pair(MemberListReturn.OK, emptyList<groups.types.AuditEventOut>())
                override suspend fun deleteGroup(gid: Int, uid: Int) = false
                override suspend fun editGroup(gid: Int, uid: Int, name: String?, desc: String?) = false
            }
        }

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(Authentication) {
                jwt("access-jwt") {
                    verifier(JWT.require(Algorithm.HMAC256("test-secret")).build())
                    validate { cred -> JWTPrincipal(cred.payload) }
                }
            }
            routing {
                groupRoutes()
            }
        }

        val res = client.post("/groups/invites") {
            header(HttpHeaders.Authorization, "Bearer ${testToken()}")
            contentType(ContentType.Application.Json)
            setBody("""{"gid":1}""")
        }
        assertEquals(HttpStatusCode.OK, res.status)

        groupRepoProvider = prev
    }

    @Test
    fun testJoinByCodeMaxUses() = testApplication {
        val prev = groupRepoProvider
        groupRepoProvider = {
            object : GroupRepo {
                override suspend fun getAllUserGroups(userId: Int) = emptyList<groups.types.SingleGroupOut>()
                override suspend fun createGroup(name: String, desc: String, ownerId: Int) = -1
                override suspend fun addUser(requesterId: Int, userId: Int, gid: Int) = AddUserReturn.CONNERR
                override suspend fun removeUser(requesterId: Int, userId: Int, gid: Int) = RemoveUserReturn.CONNERR
                override suspend fun passOwnership(requesterId: Int, newOwnerUserId: Int, gid: Int) = PassOwnershipReturn.CONNERR
                override suspend fun generateInviteUrl(requesterId: Int, gid: Int) = Pair(InviteUrlReturn.CONNERR, null)
                override suspend fun joinByInviteUrl(requesterId: Int, inviteUrl: String) = InviteUrlReturn.CONNERR
                override suspend fun createInvite(requesterId: Int, gid: Int, expiresInSeconds: Long?, maxUses: Int?) =
                    Pair(InviteUrlReturn.CONNERR, null)
                override suspend fun listInvites(requesterId: Int, gid: Int) = Pair(InviteUrlReturn.CONNERR, null)
                override suspend fun revokeInvite(requesterId: Int, gid: Int, code: String) = InviteUrlReturn.CONNERR
                override suspend fun joinByCode(requesterId: Int, code: String) = InviteUrlReturn.MAX_USES_REACHED
                override suspend fun listMembers(requesterId: Int, gid: Int) = Pair(MemberListReturn.OK, emptyList<groups.types.GroupMemberOut>())
                override suspend fun leaveGroup(requesterId: Int, gid: Int) = LeaveGroupReturn.OK
                override suspend fun setMemberRole(requesterId: Int, gid: Int, userId: Int, role: Int) = SetMemberRoleReturn.OK
                override suspend fun listAudit(requesterId: Int, gid: Int, limit: Int, offset: Long) = Pair(MemberListReturn.OK, emptyList<groups.types.AuditEventOut>())
                override suspend fun deleteGroup(gid: Int, uid: Int) = false
                override suspend fun editGroup(gid: Int, uid: Int, name: String?, desc: String?) = false
            }
        }

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(Authentication) {
                jwt("access-jwt") {
                    verifier(JWT.require(Algorithm.HMAC256("test-secret")).build())
                    validate { cred -> JWTPrincipal(cred.payload) }
                }
            }
            routing {
                groupRoutes()
            }
        }

        val res = client.post("/groups/joinByCode") {
            header(HttpHeaders.Authorization, "Bearer ${testToken()}")
            contentType(ContentType.Application.Json)
            setBody("""{"code":"x"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, res.status)

        groupRepoProvider = prev
    }

}
