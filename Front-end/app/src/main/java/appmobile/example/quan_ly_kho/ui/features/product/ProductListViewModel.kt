package appmobile.example.quan_ly_kho.ui.features.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import appmobile.example.quan_ly_kho.data.model.product.ProductResponse
import appmobile.example.quan_ly_kho.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface ProductListUiState {
    object Loading : ProductListUiState
    data class Success(val products: List<ProductResponse>) : ProductListUiState
    data class Error(val message: String) : ProductListUiState
}

class ProductListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ProductListUiState>(ProductListUiState.Loading)
    val uiState: StateFlow<ProductListUiState> = _uiState

    init {
        fetchProducts()
    }

    fun fetchProducts(searchQuery: String? = null) {
        viewModelScope.launch {
            _uiState.value = ProductListUiState.Loading
            try {
                val query = searchQuery?.takeIf { it.isNotBlank() }
                val response = RetrofitClient.apiService.getProducts(search = query)

                if (response.isSuccessful && response.body()?.success == true) {
                    val productList = response.body()?.data?.items ?: emptyList()
                    _uiState.value = ProductListUiState.Success(productList)
                } else {
                    val errorMsg = response.body()?.message ?: "Không thể tải danh sách sản phẩm"
                    _uiState.value = ProductListUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = ProductListUiState.Error("Lỗi kết nối: ${e.localizedMessage}")
            }
        }
    }
}