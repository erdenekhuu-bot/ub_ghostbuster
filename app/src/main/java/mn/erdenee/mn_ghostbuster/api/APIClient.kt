package mn.erdenee.mn_ghostbuster.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface APIClient {
    @POST("login/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("members/")
    suspend fun members(
        @Header("Authorization") token: String,
        @Query("page") page:Int,
        @Query("page_size") pageSize:Int
    ): Response<MemberResponse>

    @GET("location/list")
    suspend fun locations(
        @Header("Authorization") token: String,
        @Query("page") page:Int,
        @Query("page_size") pageSize:Int): Response<LocationResponse>

    @Multipart
    @PUT("location/")
    suspend fun uploadLocation(
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("address") address: RequestBody,
        @Part image: List<MultipartBody.Part>,
        @Part video: List<MultipartBody.Part>
    ): Response<Unit>

    @GET
    suspend fun readlocations(
        @Header("Authorization") token: String,
    )
}

data class LoginResponse(
    val tokens: TokenData? = null
)

data class TokenData(
    val access: String,
    val refresh: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class MemberResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<UserResult>
)

data class UserResult(
    val id: Int,
    val username: String,
    val password: String,
    val member: MemberDetail?
)

data class MemberDetail(
    val user: Int,
    val phone: String,
    val location: String,
    val status: String
)

data class LocationResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<LocationResult>
)

data class LocationResult(
    val id: Int,
    val title: String,
    val address: String,
)


class RetrofitCLient {
    private val baseUrl = "http://192.168.6.144:8000/api/"

    fun getInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
