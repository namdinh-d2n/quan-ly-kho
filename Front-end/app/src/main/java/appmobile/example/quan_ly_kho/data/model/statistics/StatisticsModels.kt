
package appmobile.example.quan_ly_kho.data.model.statistics

// =====================================================
// 1. DTO BIỂU ĐỒ NHẬP - XUẤT
// API: GET /api/v1/statistics/chart
// =====================================================

data class ChartDataPointDto(
    val label: String,
    val totalImport: Int,
    val totalExport: Int
)

data class ChartStatisticsResponse(
    val success: Boolean,
    val filter: String,
    val groupBy: String,
    val fromDate: String,
    val toDate: String,
    val data: List<ChartDataPointDto>
)

// =====================================================
// 2. DTO TOP SẢN PHẨM XUẤT NHIỀU NHẤT
// API: GET /api/v1/statistics/top-exported
// =====================================================

data class TopExportedProductDto(
    val productId: Int,
    val sku: String,
    val productName: String,
    val imageUrl: String?,
    val totalExportedQuantity: Int
)

data class TopExportedResponse(
    val success: Boolean,
    val filter: String,
    val fromDate: String,
    val toDate: String,
    val totalProducts: Int,
    val data: List<TopExportedProductDto>
)

// =====================================================
// 3. DTO TOP SẢN PHẨM TỒN KHO LÂU NHẤT
// API: GET /api/v1/statistics/oldest-stock
// =====================================================

data class OldestStockProductDto(
    val productId: Int,
    val sku: String,
    val productName: String,
    val imageUrl: String?,
    val currentStock: Int,
    val oldestImportDate: String?
)

data class OldestStockResponse(
    val success: Boolean,
    val filter: String,
    val fromDate: String,
    val toDate: String,
    val totalProducts: Int,
    val data: List<OldestStockProductDto>
)
