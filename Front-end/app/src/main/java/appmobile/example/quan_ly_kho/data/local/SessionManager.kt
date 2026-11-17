package appmobile.example.quan_ly_kho.data.local

import android.content.Context
import android.content.SharedPreferences
import appmobile.example.quan_ly_kho.data.model.AuthResponse
import appmobile.example.quan_ly_kho.data.model.UserInfoResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SessionManager {
    private const val PREF_NAME = "megamart_session"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_ROLE = "user_role"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_ID = "user_id"

    private var sharedPreferences: SharedPreferences? = null

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    var token: String? = null
        private set

    var userRole: String? = null
        private set

    var userName: String? = null
        private set

    var userId: Int = 0
        private set

    fun init(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val savedToken = sharedPreferences?.getString(KEY_TOKEN, null)

            if (!savedToken.isNullOrBlank()) {
                // ST-1.02.3: Giải mã JWT token khi khởi chạy ứng dụng để lấy vai trò & kiểm tra thời hạn
                if (JwtUtils.isExpired(savedToken)) {
                    clear()
                    return
                }

                token = savedToken
                val decoded = JwtUtils.decodeJwt(savedToken)

                userRole = sharedPreferences?.getString(KEY_ROLE, null) ?: decoded?.role
                userName = sharedPreferences?.getString(KEY_USER_NAME, null) ?: decoded?.fullName
                userId = sharedPreferences?.getInt(KEY_USER_ID, 0) ?: (decoded?.userId ?: 0)

                // Nếu trong prefs chưa có hoặc rỗng thì ghi lại từ decoded token
                if (sharedPreferences?.getString(KEY_ROLE, null).isNullOrBlank() && !userRole.isNullOrBlank()) {
                    sharedPreferences?.edit()?.putString(KEY_ROLE, userRole)?.apply()
                }

                _isLoggedIn.value = true
            } else {
                _isLoggedIn.value = false
            }
        }
    }

    fun saveAuth(response: AuthResponse) {
        token = response.token
        // Giải mã JWT để đối chiếu/bổ sung nếu cần
        val decoded = JwtUtils.decodeJwt(response.token)
        userRole = response.role.ifBlank { decoded?.role ?: "" }
        userName = response.fullName.ifBlank { decoded?.fullName ?: "" }
        userId = if (response.userID > 0) response.userID else (decoded?.userId ?: 0)
        _isLoggedIn.value = true

        sharedPreferences?.edit()?.apply {
            putString(KEY_TOKEN, token)
            putString(KEY_ROLE, userRole)
            putString(KEY_USER_NAME, userName)
            putInt(KEY_USER_ID, userId)
            apply()
        }
    }

    fun updateUserInfo(userInfo: UserInfoResponse) {
        userRole = userInfo.role
        userName = userInfo.fullName
        userId = userInfo.userID

        sharedPreferences?.edit()?.apply {
            putString(KEY_ROLE, userInfo.role)
            putString(KEY_USER_NAME, userInfo.fullName)
            putInt(KEY_USER_ID, userInfo.userID)
            apply()
        }
    }

    fun clear() {
        token = null
        userRole = null
        userName = null
        userId = 0
        _isLoggedIn.value = false

        sharedPreferences?.edit()?.clear()?.apply()
    }
}
