package appmobile.example.quan_ly_kho.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import appmobile.example.quan_ly_kho.data.model.AuthResponse
import appmobile.example.quan_ly_kho.data.model.LoginRequest
import appmobile.example.quan_ly_kho.data.remote.ApiService

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: AuthResponse) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(phoneEmail: String, pass: String) {
        if (phoneEmail.isBlank() || pass.isBlank()) {
            _uiState.value = LoginUiState.Error("Vui lòng điền đầy đủ thông tin!")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                // Sửa thành phone_Email để khớp hoàn toàn với DTO LoginRequest.cs ở Backend
                val response = apiService.login(LoginRequest(phone_Email = phoneEmail, password = pass))

                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = LoginUiState.Success(response.body()!!)
                } else {
                    // Đọc chi tiết lỗi trả về từ Backend nếu có (ví dụ: tài khoản bị khóa, sai thông tin)
                    val errorMsg = response.errorBody()?.string() ?: "Sai email/SĐT hoặc mật khẩu!"
                    _uiState.value = LoginUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("Lỗi kết nối mạng: ${e.localizedMessage}")
            }
        }
    }
}
