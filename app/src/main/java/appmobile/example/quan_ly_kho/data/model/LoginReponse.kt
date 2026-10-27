package appmobile.example.quan_ly_kho.data.model

data class LoginResponse(
    val token: String,
    val message: String,
    val user: UserDto?
)

data class UserDto(
    val id: String,
    val name: String,
    val email: String
)