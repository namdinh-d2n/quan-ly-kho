package appmobile.example.quan_ly_kho

import android.os.Bundle
import android.util.Log
import android.widget.Toast

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
<<<<<<< HEAD
=======

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier

import androidx.lifecycle.viewmodel.compose.viewModel

import appmobile.example.quan_ly_kho.data.local.SessionManager
import appmobile.example.quan_ly_kho.data.model.UserRole
import appmobile.example.quan_ly_kho.ui.features.auth.LoginScreen
import appmobile.example.quan_ly_kho.ui.features.auth.LoginViewModel
import appmobile.example.quan_ly_kho.ui.features.product.AddProductScreen
import appmobile.example.quan_ly_kho.ui.navigation.MainNavigation
>>>>>>> develop
import appmobile.example.quan_ly_kho.ui.theme.Quan_ly_khoTheme
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import appmobile.example.quan_ly_kho.data.remote.ApiService
import appmobile.example.quan_ly_kho.presentation.authorization.MainAppRouting
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SessionManager.init(this)
        enableEdgeToEdge()

<<<<<<< HEAD
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
=======
        setContent {
            Quan_ly_khoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isLoggedIn by rememberSaveable {
                        mutableStateOf(SessionManager.token != null)
                    }

                    var userName by rememberSaveable {
                        mutableStateOf(SessionManager.userName ?: "")
                    }

                    var userRole by rememberSaveable {
                        mutableStateOf(SessionManager.userRole ?: "")
                    }

                    var jwtToken by remember {
                        mutableStateOf(SessionManager.token ?: "")
                    }

                    // State quản lý màn hình & tab đang chọn
                    var currentScreen by rememberSaveable { mutableStateOf("main") }
                    var selectedTab by rememberSaveable { mutableStateOf("home") }

                    val loginViewModel: LoginViewModel = viewModel()

                    if (!isLoggedIn) {

                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginSuccess = { authResponse ->
                                SessionManager.saveAuth(authResponse)

                                userName = authResponse.fullName
                                userRole = authResponse.role
                                jwtToken = authResponse.token

                                Log.d(
                                    "LOGIN_SUCCESS",
                                    "Xin chào $userName - Quyền: $userRole"
                                )

                                Toast.makeText(
                                    applicationContext,
                                    "Đăng nhập thành công! Xin chào $userName",
                                    Toast.LENGTH_LONG
                                ).show()

                                isLoggedIn = true
                            }
                        )

                    } else {
                        when (currentScreen) {
                            "main" -> {
                                MainNavigation(
                                    userName = userName,
                                    userRole = userRole,
                                    jwtToken = jwtToken,
                                    selectedTab = selectedTab,
                                    onTabSelected = { newTab ->
                                        selectedTab = newTab
                                    },
                                    onAddProductClick = {
                                        if (UserRole.canAddProduct(userRole)) {
                                            currentScreen = "add_product"
                                        } else {
                                            Toast.makeText(
                                                applicationContext,
                                                "Bạn không có quyền thực hiện thao tác này!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    onLogout = {
                                        SessionManager.clear()
                                        isLoggedIn = false
                                        userName = ""
                                        userRole = ""
                                        jwtToken = ""
                                        selectedTab = "home"
                                        currentScreen = "main"

                                        Toast.makeText(
                                            applicationContext,
                                            "Đã đăng xuất tài khoản",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                            "add_product" -> {
                                if (!UserRole.canAddProduct(userRole)) {
                                    // Chặn nếu không đủ quyền
                                    currentScreen = "main"
                                } else {
                                    AddProductScreen(
                                        onBackClick = {
                                            selectedTab = "products"
                                            currentScreen = "main"
                                        },
                                        onSaveSuccess = {
                                            selectedTab = "products"
                                            currentScreen = "main"
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
>>>>>>> develop
