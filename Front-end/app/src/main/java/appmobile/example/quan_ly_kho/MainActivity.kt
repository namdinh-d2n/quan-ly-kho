package appmobile.example.quan_ly_kho

import android.os.Bundle
import android.util.Log
import android.widget.Toast

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import appmobile.example.quan_ly_kho.data.local.SessionManager
import appmobile.example.quan_ly_kho.data.remote.ApiService
import appmobile.example.quan_ly_kho.presentation.authorization.MainAppRouting
import appmobile.example.quan_ly_kho.ui.theme.Quan_ly_khoTheme
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ST-1.02.3: Khởi tạo SessionManager đồng thời giải mã JWT token kiểm tra hạn và lấy vai trò
        SessionManager.init(this)
        enableEdgeToEdge()

        // 1. Tạo OkHttpClient bỏ qua lỗi chứng chỉ SSL cho môi trường Local
        val unsafeOkHttpClient = try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .build()
        } catch (e: Exception) {
            OkHttpClient.Builder().build()
        }

        // 2. Khởi tạo Retrofit client với OkHttpClient đã cấu hình
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5028/") // Cổng HTTP của Backend .NET Core
            .client(unsafeOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        setContent {
            Quan_ly_khoTheme {
                // 3. Gọi MainAppRouting để quản lý luồng Đăng nhập & Phân quyền
                MainAppRouting(apiService = apiService)
            }
        }
    }
}
