
package appmobile.example.quan_ly_kho.ui.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import appmobile.example.quan_ly_kho.data.remote.RetrofitClient
import appmobile.example.quan_ly_kho.data.model.statistics.ChartStatisticsResponse
import appmobile.example.quan_ly_kho.data.model.statistics.TopExportedResponse
import appmobile.example.quan_ly_kho.data.model.statistics.OldestStockResponse

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import retrofit2.Response
import java.io.IOException

data class DashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val chart: ChartStatisticsResponse? = null,
    val topExported: TopExportedResponse? = null,
    val oldestStock: OldestStockResponse? = null
)

class DashboardViewModel : ViewModel() {

    private val apiService = RetrofitClient.apiService

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Lưu công việc gọi API hiện tại
    private var loadJob: Job? = null

    fun loadDashboard(
        jwtToken: String,
        filter: String = "thismonth"
    ) {

        // Hủy lần tải trước nếu người dùng đổi bộ lọc
        loadJob?.cancel()

        if (jwtToken.isBlank()) {
            _uiState.value = DashboardUiState(
                error = "Không tìm thấy JWT token. Vui lòng đăng nhập lại."
            )
            return
        }

        loadJob = viewModelScope.launch {

            // Xóa dữ liệu cũ trong khi tải bộ lọc mới
            _uiState.value = DashboardUiState(
                isLoading = true
            )

            try {
                val authorization = if (
                    jwtToken.startsWith("Bearer ")
                ) {
                    jwtToken
                } else {
                    "Bearer $jwtToken"
                }

                // API 1: Biểu đồ nhập - xuất
                val chartResponse = apiService.getChartStatistics(
                    token = authorization,
                    filter = filter,
                    groupBy = "day"
                )

                val chart = chartResponse.requireBody(
                    "Biểu đồ nhập - xuất"
                )

                if (!chart.success) {
                    throw IOException("API biểu đồ trả về thất bại.")
                }

                // API 2: Top sản phẩm xuất nhiều
                val topResponse = apiService.getTopExportedProducts(
                    token = authorization,
                    filter = filter,
                    top = 5
                )

                val topExported = topResponse.requireBody(
                    "Top sản phẩm xuất nhiều"
                )

                if (!topExported.success) {
                    throw IOException(
                        "API top sản phẩm xuất trả về thất bại."
                    )
                }

                // API 3: Sản phẩm tồn kho lâu
                val oldestResponse = apiService.getOldestStockProducts(
                    token = authorization,
                    filter = filter,
                    top = 5
                )

                val oldestStock = oldestResponse.requireBody(
                    "Sản phẩm tồn kho lâu"
                )

                if (!oldestStock.success) {
                    throw IOException(
                        "API tồn kho lâu trả về thất bại."
                    )
                }

                // Cập nhật dữ liệu sau khi cả 3 API thành công
                _uiState.value = DashboardUiState(
                    isLoading = false,
                    error = null,
                    chart = chart,
                    topExported = topExported,
                    oldestStock = oldestStock
                )

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                _uiState.value = DashboardUiState(
                    isLoading = false,
                    error = e.message
                        ?: "Không thể tải dữ liệu Dashboard."
                )
            }
        }
    }

    // Kiểm tra kết quả trả về từ Backend
    private fun <T> Response<T>.requireBody(
        apiName: String
    ): T {

        if (!isSuccessful) {

            val message = when (code()) {
                401 -> "JWT token không hợp lệ hoặc đã hết hạn."
                403 -> "Tài khoản không có quyền truy cập API này."
                else -> "Lỗi HTTP ${code()} khi gọi $apiName."
            }

            throw IOException(message)
        }

        return body()
            ?: throw IOException(
                "$apiName không trả về dữ liệu."
            )
    }
}
