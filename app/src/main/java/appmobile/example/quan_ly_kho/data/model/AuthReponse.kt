package appmobile.example.quan_ly_kho.data.model

data class AuthResponse(
    val token: String,
    val userID: Int,
    val fullName: String,
    val role: String
)