package appmobile.example.quan_ly_kho.data.remote

import appmobile.example.quan_ly_kho.data.model.LoginRequest
import appmobile.example.quan_ly_kho.data.model.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}