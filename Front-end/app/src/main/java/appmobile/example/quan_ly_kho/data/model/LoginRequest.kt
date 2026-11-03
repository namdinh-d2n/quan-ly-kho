package appmobile.example.quan_ly_kho.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
   @SerializedName("phone_Email")
   val phone_Email: String,

   @SerializedName("password")
   val password: String
)
