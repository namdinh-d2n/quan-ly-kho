package appmobile.example.quan_ly_kho.presentation.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    data class Success(val message: String) : LoginUiState
    data class Error(val errorMsg: String) : LoginUiState
}

class LoginViewModel : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")

    var uiState: LoginUiState by mutableStateOf(LoginUiState.Idle)
        private set

    fun onEmailChanged(newEmail: String) { email = newEmail }
    fun onPasswordChanged(newPassword: String) { password = newPassword }

    fun login(onSuccessNavigation: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            uiState = LoginUiState.Error("Vui lòng nhập đầy đủ email và mật khẩu")
            return
        }

        viewModelScope.launch {
            uiState = LoginUiState.Loading
            try {
                val response = RetrofitClient.apiService.login(
                    LoginRequest(email = email, password = password)
                )
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    uiState = LoginUiState.Success(data.message)
                    // Lưu token vào SharedPreferences / DataStore ở đây nếu cần
                    onSuccessNavigation()
                } else {
                    uiState = LoginUiState.Error("Đăng nhập thất bại: ${response.errorBody()?.string() ?: "Lỗi không xác định"}")
                }
            } catch (e: Exception) {
                uiState = LoginUiState.Error("Lỗi kết nối mạng: ${e.localizedMessage}")
            }
        }
    }
}