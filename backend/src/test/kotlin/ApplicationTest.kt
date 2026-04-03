package com.astrais

import com.astrais.auth.authRoutes
import com.astrais.auth.installAuth
import com.astrais.groups.groupRoutes
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testUserGroups() = testApplication {
        application {
            module()
            installAuth()
            routing {
                authRoutes()
                groupRoutes()
            }
        }
        client.get("/group/userGroups").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

}
