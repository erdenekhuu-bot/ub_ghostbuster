package mn.erdenee.mn_ghostbuster.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface APIClient {
    @POST("login/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @Multipart
    @POST("location/")
    suspend fun uploadLocation(
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("address") address: RequestBody,
        @Part image: List<MultipartBody.Part>,
        @Part video: List<MultipartBody.Part>
    ): Response<Unit>
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

class RetrofitCLient {
    private val baseUrl = "http://192.168.6.144:8000/api/"

    fun getInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
