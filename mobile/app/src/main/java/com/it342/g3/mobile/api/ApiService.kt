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
    val fullName: String?,
    val createdAt: String?
)

data class DashboardMetrics(
    val totalPigs: Int?,
    val addedThisMonth: Int?,
    val activePens: Int?,
    val pensAtCapacity: Int?,
    val pendingSales: Int?,
    val pendingSalesValue: Double?,
    val healthAlerts: Int?,
    val healthDueToday: Int?
)

data class DashboardTrendPoint(
    val label: String?,
    val value: Double?
)

data class DashboardActivityItem(
    val title: String?,
    val timeAgo: String?,
    val tone: String?
)

data class PenSummary(
    val id: Long?,
    val identifier: String?,
    val name: String?,
    val description: String?,
    val capacity: Int?,
    val occupied: Int?,
    val available: Int?,
    val utilization: Double?,
    val status: String?
)

data class DashboardPayload(
    val profile: UserProfile?,
    val metrics: DashboardMetrics?,
    val weightTrend: List<DashboardTrendPoint>?,
    val activities: List<DashboardActivityItem>?,
    val pens: List<PenSummary>?
)

data class PigSummary(
    val id: Long?,
    val identifier: String?,
    val breed: String?,
    val birthdate: String?,
    val weight: Double?,
    val weightUnit: String?,
    val gender: String?,
    val status: String?,
    val notes: String?,
    val penId: Long?,
    val penName: String?,
    val penIdentifier: String?,
    val addedAt: String?
)

data class PenDetailsPayload(
    val pen: PenSummary?,
    val pigs: List<PigSummary>?
)

data class CreatePenRequest(
    val penIdentifier: String?,
    val penName: String,
    val description: String?,
    val capacity: Int
)

data class UpdatePenRequest(
    val penIdentifier: String?,
    val penName: String,
    val description: String?,
    val capacity: Int
)

data class CreatePigRequest(
    val pigIdentifier: String?,
    val breed: String?,
    val gender: String?,
    val birthdate: String?,
    val currentWeight: Double?,
    val weightUnit: String?,
    val status: String?,
    val notes: String?
)

data class UpdatePigRequest(
    val pigIdentifier: String?,
    val breed: String?,
    val gender: String?,
    val birthdate: String?,
    val currentWeight: Double?,
    val weightUnit: String?,
    val status: String?,
    val notes: String?
)

data class FeedingRecord(
    val id: Long?,
    val penId: Long?,
    val penName: String?,
    val penIdentifier: String?,
    val feedType: String?,
    val quantity: Double?,
    val unit: String?,
    val cost: Double?,
    val feedingTime: String?,
    val notes: String?,
    val createdAt: String?,
    val recordedBy: String?
)

data class CreateFeedingRequest(
    val penId: Long,
    val feedType: String,
    val quantity: Double,
    val unit: String?,
    val cost: Double?,
    val feedingTime: String?,
    val notes: String?
)

data class UpdateFeedingRequest(
    val penId: Long?,
    val feedType: String?,
    val quantity: Double?,
    val unit: String?,
    val cost: Double?,
    val feedingTime: String?,
    val notes: String?
)

data class HealthRecord(
    val id: Long?,
    val pigId: Long?,
    val pigIdentifier: String?,
    val penId: Long?,
    val penName: String?,
    val weight: Double?,
    val healthCondition: String?,
    val temperature: Double?,
    val treatmentGiven: String?,
    val medicationUsed: String?,
    val nextTreatmentDate: String?,
    val nextTreatmentType: String?,
    val checkupDate: String?,
    val notes: String?,
    val createdAt: String?,
    val recordedBy: String?
)

data class CreateHealthRecordRequest(
    val pigId: Long?,
    val pigIdentifier: String?,
    val weight: Double?,
    val healthCondition: String?,
    val temperature: Double?,
    val treatmentGiven: String?,
    val medicationUsed: String?,
    val nextTreatmentDate: String?,
    val nextTreatmentType: String?,
    val checkupDate: String?,
    val notes: String?
)

data class SaleRecord(
    val id: Long?,
    val pigId: Long?,
    val pigIdentifier: String?,
    val buyerName: String?,
    val buyerContact: String?,
    val salePrice: Double?,
    val saleDate: String?,
    val expectedPickupDate: String?,
    val actualPickupDate: String?,
    val status: String?,
    val paymentStatus: String?,
    val notes: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class CreateSaleRequest(
    val pigId: Long?,
    val pigIdentifier: String?,
    val buyerName: String,
    val buyerContact: String?,
    val salePrice: Double,
    val saleDate: String?,
    val expectedPickupDate: String?,
    val actualPickupDate: String?,
    val status: String?,
    val paymentStatus: String?,
    val notes: String?
)

data class UpdateSaleRequest(
    val buyerName: String?,
    val buyerContact: String?,
    val salePrice: Double?,
    val saleDate: String?,
    val expectedPickupDate: String?,
    val actualPickupDate: String?,
    val status: String?,
    val paymentStatus: String?,
    val notes: String?
)

data class MortalityRecord(
    val id: Long?,
    val pigId: Long?,
    val pigIdentifier: String?,
    val dateOfDeath: String?,
    val ageAtDeath: Int?,
    val causeOfDeath: String?,
    val weightAtDeath: Double?,
    val symptoms: String?,
    val actionsTaken: String?,
    val notes: String?,
    val recordedAt: String?,
    val recordedBy: String?
)

data class CreateMortalityRecordRequest(
    val pigId: Long?,
    val pigIdentifier: String?,
    val dateOfDeath: String?,
    val ageAtDeath: Int?,
    val causeOfDeath: String?,
    val weightAtDeath: Double?,
    val symptoms: String?,
    val actionsTaken: String?,
    val notes: String?
)

data class UpdateProfileRequest(
    val fullName: String?,
    val username: String?,
    val email: String?
)

data class UpdatePasswordRequest(
    val currentPassword: String,
    val newPassword: String
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

    @GET("/api/user/dashboard")
    fun dashboard(@Header("Authorization") auth: String): Call<ApiResponse<DashboardPayload>>

    @POST("/api/user/pens")
    fun createPen(
        @Header("Authorization") auth: String,
        @Body body: CreatePenRequest
    ): Call<ApiResponse<PenSummary>>

    @PUT("/api/user/pens/{penId}")
    fun updatePen(
        @Header("Authorization") auth: String,
        @Path("penId") penId: Long,
        @Body body: UpdatePenRequest
    ): Call<ApiResponse<PenSummary>>

    @GET("/api/user/pens/{penId}")
    fun getPenDetails(
        @Header("Authorization") auth: String,
        @Path("penId") penId: Long
    ): Call<ApiResponse<PenDetailsPayload>>

    @POST("/api/user/pens/{penId}/pigs")
    fun createPig(
        @Header("Authorization") auth: String,
        @Path("penId") penId: Long,
        @Body body: CreatePigRequest
    ): Call<ApiResponse<PigSummary>>

    @PUT("/api/user/pigs/{pigId}")
    fun updatePig(
        @Header("Authorization") auth: String,
        @Path("pigId") pigId: Long,
        @Body body: UpdatePigRequest
    ): Call<ApiResponse<PigSummary>>

    @DELETE("/api/user/pigs/{pigId}")
    fun deletePig(
        @Header("Authorization") auth: String,
        @Path("pigId") pigId: Long
    ): Call<ApiResponse<Map<String, Long>>>

    @GET("/api/user/pigs")
    fun getPigs(@Header("Authorization") auth: String): Call<ApiResponse<List<PigSummary>>>

    @GET("/api/user/feeding")
    fun getFeedings(@Header("Authorization") auth: String): Call<ApiResponse<List<FeedingRecord>>>

    @POST("/api/user/feeding")
    fun createFeeding(
        @Header("Authorization") auth: String,
        @Body body: CreateFeedingRequest
    ): Call<ApiResponse<FeedingRecord>>

    @PUT("/api/user/feeding/{feedingId}")
    fun updateFeeding(
        @Header("Authorization") auth: String,
        @Path("feedingId") feedingId: Long,
        @Body body: UpdateFeedingRequest
    ): Call<ApiResponse<FeedingRecord>>

    @DELETE("/api/user/feeding/{feedingId}")
    fun deleteFeeding(
        @Header("Authorization") auth: String,
        @Path("feedingId") feedingId: Long
    ): Call<ApiResponse<Map<String, Long>>>

    @GET("/api/user/health-records")
    fun getHealthRecords(@Header("Authorization") auth: String): Call<ApiResponse<List<HealthRecord>>>

    @POST("/api/user/health-records")
    fun createHealthRecord(
        @Header("Authorization") auth: String,
        @Body body: CreateHealthRecordRequest
    ): Call<ApiResponse<HealthRecord>>

    @GET("/api/user/sales")
    fun getSales(@Header("Authorization") auth: String): Call<ApiResponse<List<SaleRecord>>>

    @POST("/api/user/sales")
    fun createSale(
        @Header("Authorization") auth: String,
        @Body body: CreateSaleRequest
    ): Call<ApiResponse<SaleRecord>>

    @PUT("/api/user/sales/{saleId}")
    fun updateSale(
        @Header("Authorization") auth: String,
        @Path("saleId") saleId: Long,
        @Body body: UpdateSaleRequest
    ): Call<ApiResponse<SaleRecord>>

    @GET("/api/user/mortality")
    fun getMortality(@Header("Authorization") auth: String): Call<ApiResponse<List<MortalityRecord>>>

    @POST("/api/user/mortality")
    fun createMortality(
        @Header("Authorization") auth: String,
        @Body body: CreateMortalityRecordRequest
    ): Call<ApiResponse<MortalityRecord>>

    @PUT("/api/user/me")
    fun updateProfile(
        @Header("Authorization") auth: String,
        @Body body: UpdateProfileRequest
    ): Call<ApiResponse<UserProfile>>

    @PUT("/api/user/password")
    fun updatePassword(
        @Header("Authorization") auth: String,
        @Body body: UpdatePasswordRequest
    ): Call<ApiResponse<Any>>
}
