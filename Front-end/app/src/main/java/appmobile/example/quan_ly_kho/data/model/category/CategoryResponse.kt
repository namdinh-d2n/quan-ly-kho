package appmobile.example.quan_ly_kho.data.model.category

import com.google.gson.annotations.SerializedName

data class CategoryResponse(
    @SerializedName("categoryId") val categoryId: Int,
    @SerializedName("categoryName") val categoryName: String
)