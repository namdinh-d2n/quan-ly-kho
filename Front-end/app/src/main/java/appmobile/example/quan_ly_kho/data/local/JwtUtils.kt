package appmobile.example.quan_ly_kho.data.local

import android.util.Base64
import org.json.JSONObject
import java.nio.charset.StandardCharsets

data class DecodedToken(
    val userId: Int?,
    val fullName: String?,
    val role: String?,
    val expiresAtSeconds: Long?
) {
    val isExpired: Boolean
        get() {
            if (expiresAtSeconds == null) return false
            val currentSeconds = System.currentTimeMillis() / 1000
            return currentSeconds >= expiresAtSeconds
        }
}

object JwtUtils {
    /**
     * ST-1.02.3: Giải mã JWT Token để lấy thông tin người dùng và vai trò (Role)
     */
    fun decodeJwt(token: String?): DecodedToken? {
        if (token.isNullOrBlank()) return null
        return try {
            val cleanToken = if (token.startsWith("Bearer ", ignoreCase = true)) {
                token.substring(7).trim()
            } else {
                token.trim()
            }

            val parts = cleanToken.split(".")
            if (parts.size < 2) return null

            // JWT Base64URL decoding
            val payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            val jsonString = String(payloadBytes, StandardCharsets.UTF_8)
            val json = JSONObject(jsonString)

            // Trích xuất Role từ các chuẩn claim khác nhau của ASP.NET Core & JWT tiêu chuẩn
            val role = when {
                json.has("http://schemas.microsoft.com/ws/2008/06/identity/claims/role") ->
                    json.optString("http://schemas.microsoft.com/ws/2008/06/identity/claims/role")
                json.has("role") -> json.optString("role")
                json.has("roles") -> json.optString("roles")
                else -> ""
            }

            // Trích xuất FullName
            val fullName = when {
                json.has("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name") ->
                    json.optString("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name")
                json.has("unique_name") -> json.optString("unique_name")
                json.has("name") -> json.optString("name")
                else -> ""
            }

            // Trích xuất User ID
            val nameId = when {
                json.has("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier") ->
                    json.optString("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier")
                json.has("nameid") -> json.optString("nameid")
                json.has("sub") -> json.optString("sub")
                else -> ""
            }
            val userId = nameId.toIntOrNull()

            val exp = if (json.has("exp")) json.optLong("exp") else null

            DecodedToken(
                userId = userId,
                fullName = fullName.ifBlank { null },
                role = role.ifBlank { null },
                expiresAtSeconds = exp
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Kiểm tra xem JWT Token đã hết hạn chưa
     */
    fun isExpired(token: String?): Boolean {
        val decoded = decodeJwt(token) ?: return true
        return decoded.isExpired
    }

    /**
     * Lấy nhanh Role từ JWT Token
     */
    fun extractRole(token: String?): String? {
        return decodeJwt(token)?.role
    }
}
