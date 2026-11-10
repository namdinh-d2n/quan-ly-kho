package appmobile.example.quan_ly_kho.data.local

import android.content.Context
import android.content.SharedPreferences
import appmobile.example.quan_ly_kho.data.model.AuthResponse
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
            token = sharedPreferences?.getString(KEY_TOKEN, null)
            userRole = sharedPreferences?.getString(KEY_ROLE, null)
            userName = sharedPreferences?.getString(KEY_USER_NAME, null)
            userId = sharedPreferences?.getInt(KEY_USER_ID, 0) ?: 0
            _isLoggedIn.value = !token.isNullOrBlank()
        }
    }

    fun saveAuth(response: AuthResponse) {
        token = response.token
        userRole = response.role
        userName = response.fullName
        userId = response.userID
        _isLoggedIn.value = true

        sharedPreferences?.edit()?.apply {
            putString(KEY_TOKEN, response.token)
            putString(KEY_ROLE, response.role)
            putString(KEY_USER_NAME, response.fullName)
            putInt(KEY_USER_ID, response.userID)
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
