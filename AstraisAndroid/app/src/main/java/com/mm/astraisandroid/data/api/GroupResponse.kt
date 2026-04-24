package com.mm.astraisandroid.data.api

import kotlinx.serialization.Serializable

@Serializable
data class SingleGroupOut(
    val id: Int,
    val name: String,
    val description: String,
    val role: Int
)

@Serializable
data class AllGroupsResponse(
    val groupList: List<SingleGroupOut>
)

@Serializable
data class CreateGroupRequest(
    val name: String,
    val desc: String
)

@Serializable
data class AddUserRequest(
    val gid: Int,
    val userId: Int
)

@Serializable
data class DeleteGroupRequest(
    val gid: Int
)

@Serializable
data class EditGroupRequest(
    val gid: Int,
    val name: String? = null,
    val desc: String? = null
)

@Serializable
data class CreateGroupResponse(
    val groupId: Int
)
