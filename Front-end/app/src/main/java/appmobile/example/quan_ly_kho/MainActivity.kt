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
import appmobile.example.quan_ly_kho.ui.features.auth.LoginScreen
import appmobile.example.quan_ly_kho.ui.features.auth.LoginViewModel
import appmobile.example.quan_ly_kho.ui.features.product.AddProductScreen
import appmobile.example.quan_ly_kho.ui.navigation.MainNavigation
import appmobile.example.quan_ly_kho.ui.theme.Quan_ly_khoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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