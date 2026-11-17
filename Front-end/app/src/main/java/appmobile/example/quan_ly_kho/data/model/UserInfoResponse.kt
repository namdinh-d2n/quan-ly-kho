package appmobile.example.quan_ly_kho.data.model

data class UserInfoResponse(
    val userID: Int,
    val fullName: String,
    val role: String,
    val phone_Email: String? = null,
    val status: Boolean = true
)
