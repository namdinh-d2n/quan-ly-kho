package appmobile.example.quan_ly_kho.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/auth/login") // Thay đổi đường dẫn endpoint thực tế của backend bạn
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>
}