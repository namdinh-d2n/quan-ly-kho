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

import appmobile.example.quan_ly_kho.ui.features.auth.LoginScreen
import appmobile.example.quan_ly_kho.ui.features.auth.LoginViewModel
import appmobile.example.quan_ly_kho.ui.features.product.AddProductScreen
import appmobile.example.quan_ly_kho.ui.navigation.MainNavigation
import appmobile.example.quan_ly_kho.ui.theme.Quan_ly_khoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            Quan_ly_khoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isLoggedIn by rememberSaveable {
                        mutableStateOf(false)
                    }

                    var userName by rememberSaveable {
                        mutableStateOf("")
                    }

                    var userRole by rememberSaveable {
                        mutableStateOf("")
                    }

                    // Lưu JWT token sau khi đăng nhập
                    var jwtToken by remember {
                        mutableStateOf("")
                    }

                    // State quản lý màn hình & tab đang chọn
                    var currentScreen by rememberSaveable { mutableStateOf("main") }
                    var selectedTab by rememberSaveable { mutableStateOf("home") }

                    val loginViewModel: LoginViewModel = viewModel()

                    if (!isLoggedIn) {

                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginSuccess = { authResponse ->

                                userName = authResponse.fullName
                                userRole = authResponse.role

                                // Nhận token từ API đăng nhập
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
                                        currentScreen = "add_product"
                                    }
                                )
                            }
                            "add_product" -> {
                                AddProductScreen(
                                    onBackClick = {
                                        selectedTab = "products" // Chuyển về đúng tab sản phẩm
                                        currentScreen = "main"
                                    },
                                    onSaveSuccess = {
                                        selectedTab = "products" // Chuyển về đúng tab sản phẩm sau khi lưu
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