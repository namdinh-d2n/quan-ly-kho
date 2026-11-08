package appmobile.example.quan_ly_kho.data.model.product

import com.google.gson.annotations.SerializedName
import java.util.Locale

// 1. Container hứng vỏ bọc dữ liệu danh sách từ Backend { totalItems, items }
data class ProductListContainer(
    @SerializedName("totalItems") val totalItems: Int = 0,
    @SerializedName("items") val items: List<ProductResponse> = emptyList()
)

// 2. Ruột dữ liệu chi tiết từng sản phẩm
data class ProductResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("sku") val sku: String,
    @SerializedName("barcode") val barcode: String? = null,
    @SerializedName("unit") val unit: String? = null,
    @SerializedName("categoryId") val categoryId: Int,
    @SerializedName("categoryName") val categoryName: String = "",
    @SerializedName("price") val price: Double = 0.0,
    @SerializedName("stock") val stock: Int = 0,
    @SerializedName("minStock") val minStock: Int = 0,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("status") val status: Boolean = true,
    @SerializedName("createdAt") val createdAt: String? = null
) {
    // Tự động kiểm tra tồn kho tối thiểu
    val isWarning: Boolean
        get() = stock <= minStock

    val priceFormatted: String
        get() = String.format(Locale.getDefault(), "%,.0f đ", price)
}