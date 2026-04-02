package com.it342.g3.mobile.api

import retrofit2.Call
import retrofit2.http.*

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val fullName: String
)

data class LoginRequest(val email: String, val password: String)

data class ApiError(
    val code: String?,
    val message: String?,
    val details: Any?
)

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?,
    val error: ApiError?,
    val timestamp: String?
)

data class AuthData(
    val userId: Long?,
    val username: String?,
    val email: String?,
    val fullName: String?,
    val token: String?,
    val tokenType: String?,
    val expiresIn: Long?
)

data class MessageResponse(val message: String?)

data class UserProfile(
    val id: Long?,
    val username: String?,
    val email: String?,
    val role: String?,
    val fullName: String?
)

interface ApiService {
    @POST("/api/auth/register")
    fun register(@Body body: RegisterRequest): Call<ApiResponse<AuthData>>

    @POST("/api/auth/login")
    fun login(@Body body: LoginRequest): Call<ApiResponse<AuthData>>

    @GET("/api/user/me")
    fun me(@Header("Authorization") auth: String): Call<UserProfile>

    @POST("/api/auth/logout")
    fun logout(@Header("Authorization") auth: String): Call<MessageResponse>
}
