package appmobile.example.quan_ly_kho.data.model.product

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("code") val code: Int = 200,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: T? = null,
    @SerializedName("errors") val errors: Any? = null,
    @SerializedName("timestamp") val timestamp: String? = null
)