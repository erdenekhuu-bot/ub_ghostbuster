package mn.erdenee.mn_ghostbuster.api

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface APIClient {
    @POST("login/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
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
