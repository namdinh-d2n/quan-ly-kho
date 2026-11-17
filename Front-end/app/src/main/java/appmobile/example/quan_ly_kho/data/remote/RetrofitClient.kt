package appmobile.example.quan_ly_kho.data.remote

import appmobile.example.quan_ly_kho.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = SessionManager.token

        val requestBuilder = original.newBuilder()
        if (!token.isNullOrBlank() && original.header("Authorization") == null) {
            val bearerToken = if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
            requestBuilder.addHeader("Authorization", bearerToken)
        }

        val response = chain.proceed(requestBuilder.build())
        if (response.code == 401) {
            // Token hết hạn hoặc không hợp lệ -> xóa session
            SessionManager.clear()
        }
        return response
    }
}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:5028/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = AuthInterceptor()

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }
}
