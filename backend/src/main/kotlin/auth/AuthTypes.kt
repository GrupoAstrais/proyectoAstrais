package auth.types

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val passwd: String
)

@Serializable
data class LoginResponse(
    val jwtAccessToken: String,
    val jwtRefreshToken: String
)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val passwd: String,
    val lang: String,
    val utcOffset: Float = 0f
)

@Serializable
data class MailVerifierRequest(
    val email: String,
    val code: String
)

@Serializable
data class RegenAccessResponse(
    val newAccessToken: String
)

@Serializable
data class EditUserResponse(
    val uid : Int,
    val nombreusu: String?,
    val lang: String?,
    val utcOffset: Float?
)


@Serializable
data class GoogleUserInfo(
    val sub: String,
    val name: String? = null,
    val given_name: String? = null,
    val family_name: String? = null,
    val picture: String? = null
)

@Serializable
data class OauthLoginResponse(
    val uid : Int,
    val hadToRegister : Boolean,
    val jwtAccessToken : String,
    val jwtRefreshToken : String
)

@Serializable
data class SetOauthResponse(
    val providerUid : String,
    val authProvider: String
)

@Serializable
data class DeleteOauthResponse(
    val authProvider: String
)

@Serializable
data class AndroidGoogleLoginRequest(
    val idToken: String
)

enum class EditUserReturn {
    OK,
    ERROR,
    INVALID_LANGUAGE
}

@Serializable
data class SetMailUserRequest(
    val email: String?,
    val passwd: String?
)
