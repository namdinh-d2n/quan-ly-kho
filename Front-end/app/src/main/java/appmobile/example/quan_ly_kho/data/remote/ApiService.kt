package appmobile.example.quan_ly_kho.data.remote

import appmobile.example.quan_ly_kho.data.model.AuthResponse
import appmobile.example.quan_ly_kho.data.model.LoginRequest
import appmobile.example.quan_ly_kho.data.model.statistics.ChartStatisticsResponse
import appmobile.example.quan_ly_kho.data.model.statistics.TopExportedResponse
import appmobile.example.quan_ly_kho.data.model.statistics.OldestStockResponse

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Header

interface ApiService {

    // 1. API ĐĂNG NHẬP
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>


    // 2. API BIỂU ĐỒ NHẬP - XUẤT
    @GET("api/v1/statistics/chart")
    suspend fun getChartStatistics(
        @Header("Authorization") token: String,
        @Query("filter") filter: String,
        @Query("groupBy") groupBy: String
    ): Response<ChartStatisticsResponse>


    // 3. API TOP SẢN PHẨM XUẤT NHIỀU NHẤT
    @GET("api/v1/statistics/top-exported")
    suspend fun getTopExportedProducts(
        @Header("Authorization") token: String,
        @Query("filter") filter: String,
        @Query("top") top: Int
    ): Response<TopExportedResponse>


    // 4. API TOP SẢN PHẨM TỒN KHO LÂU NHẤT
    @GET("api/v1/statistics/oldest-stock")
    suspend fun getOldestStockProducts(
        @Header("Authorization") token: String,
        @Query("filter") filter: String,
        @Query("top") top: Int
    ): Response<OldestStockResponse>
}