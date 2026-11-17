package appmobile.example.quan_ly_kho

import android.os.Bundle
import android.util.Log
import android.widget.Toast

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier

import androidx.lifecycle.viewmodel.compose.viewModel

import appmobile.example.quan_ly_kho.data.local.SessionManager
import appmobile.example.quan_ly_kho.data.model.UserRole
import appmobile.example.quan_ly_kho.data.remote.RetrofitClient
import appmobile.example.quan_ly_kho.ui.features.auth.LoginScreen
import appmobile.example.quan_ly_kho.ui.features.auth.LoginViewModel
import appmobile.example.quan_ly_kho.ui.features.product.AddProductScreen
import appmobile.example.quan_ly_kho.ui.navigation.MainNavigation
import appmobile.example.quan_ly_kho.ui.theme.Quan_ly_khoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ST-1.02.3: Khởi tạo SessionManager đồng thời giải mã JWT token kiểm tra hạn và lấy vai trò
        SessionManager.init(this)
        enableEdgeToEdge()

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

                    // ST-1.02.3: Khi khởi chạy ứng dụng, nếu đã có token thì gọi API GET /api/v1/auth/me
                    // để xác thực lại và lấy vai trò mới nhất của người dùng từ máy chủ
                    LaunchedEffect(isLoggedIn, jwtToken) {
                        if (isLoggedIn && jwtToken.isNotBlank()) {
                            try {
                                val response = RetrofitClient.apiService.getCurrentUser("Bearer $jwtToken")
                                if (response.isSuccessful && response.body() != null) {
                                    val userInfo = response.body()!!
                                    userName = userInfo.fullName
                                    userRole = userInfo.role
                                    SessionManager.updateUserInfo(userInfo)
                                    Log.d("AUTH_ME", "Lấy vai trò từ GET /api/v1/auth/me thành công: ${userInfo.role}")
                                } else if (response.code() == 401 || response.code() == 403) {
                                    Log.w("AUTH_ME", "Token hết hạn hoặc tài khoản bị khóa")
                                    SessionManager.clear()
                                    isLoggedIn = false
                                    Toast.makeText(
                                        applicationContext,
                                        "Phiên đăng nhập đã hết hạn hoặc tài khoản bị khóa!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Log.e("AUTH_ME", "Lỗi kết nối khi gọi /api/v1/auth/me: ${e.message}")
                            }
                        }
                    }

                    // Hàm tiện ích hiển thị Toast khi cố truy cập luồng chưa phân quyền (ST-1.02.5)
                    val showUnauthorizedToast = {
                        Toast.makeText(
                            applicationContext,
                            "Bạn không có quyền truy cập tính năng này",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

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
                                        // ST-1.02.4 & ST-1.02.5: Kiểm tra quyền thêm sản phẩm
                                        if (UserRole.canAddProduct(userRole)) {
                                            currentScreen = "add_product"
                                        } else {
                                            showUnauthorizedToast()
                                        }
                                    },
                                    onUnauthorizedAccess = {
                                        showUnauthorizedToast()
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
                                    // ST-1.02.5: Chặn nếu không đủ quyền & hiển thị Toast
                                    showUnauthorizedToast()
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
