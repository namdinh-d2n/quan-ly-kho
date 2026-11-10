package appmobile.example.quan_ly_kho.ui.features.product

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import appmobile.example.quan_ly_kho.data.model.category.CategoryResponse
import appmobile.example.quan_ly_kho.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

sealed interface AddProductUiState {
    object Idle : AddProductUiState
    object Loading : AddProductUiState
    data class Success(val message: String) : AddProductUiState
    data class Error(val message: String) : AddProductUiState
}

class AddProductViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<AddProductUiState>(AddProductUiState.Idle)
    val uiState: StateFlow<AddProductUiState> = _uiState

    private val _categories = MutableStateFlow<List<CategoryResponse>>(emptyList())
    val categories: StateFlow<List<CategoryResponse>> = _categories

    init {
        fetchCategories()
    }

    fun fetchCategories() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getCategories()
                if (response.isSuccessful && response.body()?.success == true) {
                    _categories.value = response.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createProduct(
        context: Context,
        productName: String,
        categoryId: Int,
        sku: String,
        sellingPrice: String,
        stockQuantity: String,
        minStockLevel: String,
        barcode: String?,
        unit: String?,
        importPrice: String?,
        imageUri: Uri
    ) {
        viewModelScope.launch {
            _uiState.value = AddProductUiState.Loading

            try {
                // 1. Chuyển Uri từ Android thành File tạm
                val file = uriToFile(context, imageUri)
                if (file == null) {
                    _uiState.value = AddProductUiState.Error("Không thể đọc file ảnh đã chọn!")
                    return@launch
                }

                // 2. Đóng gói RequestBody & MultipartBody
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("ImageFile", file.name, requestFile)

                val namePart = productName.toRequestBody("text/plain".toMediaTypeOrNull())
                val categoryIdPart = categoryId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val skuPart = sku.toRequestBody("text/plain".toMediaTypeOrNull())
                val sellingPricePart = sellingPrice.toRequestBody("text/plain".toMediaTypeOrNull())
                val stockPart = stockQuantity.ifBlank { "0" }.toRequestBody("text/plain".toMediaTypeOrNull())
                val minStockPart = minStockLevel.ifBlank { "10" }.toRequestBody("text/plain".toMediaTypeOrNull())

                val barcodePart = barcode?.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull())
                val unitPart = unit?.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull())
                val importPricePart = importPrice?.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull())

                // 3. Gọi API qua Retrofit
                val response = RetrofitClient.apiService.createProduct(
                    productName = namePart,
                    categoryId = categoryIdPart,
                    sku = skuPart,
                    sellingPrice = sellingPricePart,
                    stockQuantity = stockPart,
                    minStockLevel = minStockPart,
                    barcode = barcodePart,
                    unit = unitPart,
                    importPrice = importPricePart,
                    imageFile = imagePart
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = AddProductUiState.Success(
                        response.body()?.message ?: "Thêm sản phẩm thành công!"
                    )
                } else {
                    val errorMsg = response.body()?.message ?: "Thêm sản phẩm thất bại (${response.code()})"
                    _uiState.value = AddProductUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = AddProductUiState.Error("Lỗi kết nối: ${e.localizedMessage}")
            }
        }
    }

    fun resetState() {
        _uiState.value = AddProductUiState.Idle
    }

    // Hàm chuyển Uri thành File vật lý tạm thời
    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}