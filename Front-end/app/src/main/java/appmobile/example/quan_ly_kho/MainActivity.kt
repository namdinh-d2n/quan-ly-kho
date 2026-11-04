package appmobile.example.quan_ly_kho

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import appmobile.example.quan_ly_kho.ui.features.auth.LoginViewModel
import appmobile.example.quan_ly_kho.ui.theme.Quan_ly_khoTheme
import androidx.compose.material3.Surface
import appmobile.example.quan_ly_kho.ui.features.auth.LoginScreen

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
                    val loginViewModel: LoginViewModel = viewModel(

                    )

                    LoginScreen(
                        viewModel = loginViewModel,
                        onLoginSuccess = { authResponse ->
                            // In log kiểm tra dữ liệu trả về
                            android.util.Log.d("LOGIN_SUCCESS", "Xin chào ${authResponse.fullName} - Quyền: ${authResponse.role}")

                            // Hiện Toast thông báo nhanh lên màn hình
                            Toast.makeText(
                                applicationContext,
                                "Đăng nhập thành công! Xin chào ${authResponse.fullName}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }
            }
        }
    }
}