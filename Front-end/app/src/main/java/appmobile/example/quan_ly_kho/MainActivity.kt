package appmobile.example.quan_ly_kho

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import appmobile.example.quan_ly_kho.ui.features.auth.LoginViewModel
import appmobile.example.quan_ly_kho.ui.theme.Quan_ly_khoTheme
import appmobile.example.quan_ly_kho.data.remote.RetrofitClient
import androidx.compose.material3.Surface
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
                            println("Xin chào ${authResponse.fullName} - Quyền: ${authResponse.role}")
                        }
                    )
                }
            }
        }
    }
}