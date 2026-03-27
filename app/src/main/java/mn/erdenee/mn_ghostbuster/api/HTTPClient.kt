package mn.erdenee.mn_ghostbuster.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface HTTPClient {
    @Headers("Accept: application/json")
    @POST("/api/login")
    abstract fun login(@Body login: Login): Call<ResponseBody>

}
