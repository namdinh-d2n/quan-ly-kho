
package appmobile.example.quan_ly_kho.ui.features.dashboard

import android.graphics.Color as AndroidColor

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

import java.util.Locale

// =====================================================
// MODEL DANH MỤC CHỨC NĂNG
// =====================================================

private data class DashboardFunctionItem(
    val icon: String,
    val title: String
)

// =====================================================
// DASHBOARD CHUNG
// =====================================================

@Composable
fun DashboardScreen(
    userName: String,
    userRole: String,
    jwtToken: String
) {
    val normalizedRole = userRole.trim().lowercase()

    val displayRole = when (normalizedRole) {
        "admin" -> "Quản lý"
        "manager" -> "Thủ kho"
        "staff" -> "Nhân viên siêu thị"
        else -> userRole
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        // HEADER
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "MEGAMART",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color(0xFF526B9A),
                letterSpacing = 2.sp
            )

            Text(
                text = "Trang chủ",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Tổng quan hoạt động kho hàng",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // THÔNG TIN NGƯỜI DÙNG
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = androidx.compose.ui.graphics.Color(0xFF4D628F)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Xin chào,",
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                )

                Text(
                    text = userName.ifBlank { "Người dùng" },
                    color = androidx.compose.ui.graphics.Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = displayRole,
                        color = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        )
                    )
                }
            }
        }

        // PHÂN NHÁNH DASHBOARD THEO VAI TRÒ
        when (normalizedRole) {
            "admin" -> AdminDashboardContent(
                jwtToken = jwtToken
            )

            "manager" -> ManagerDashboardContent()

            "staff" -> StaffDashboardContent()

            else -> {
                Text(
                    text = "Tài khoản không có quyền truy cập.",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// =====================================================
// DANH MỤC CHỨC NĂNG - BỐ CỤC 2 CỘT
// =====================================================

@Composable
private fun FunctionCatalog(
    functions: List<DashboardFunctionItem>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "DANH MỤC CHỨC NĂNG",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        functions.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { item ->
                    FunctionMenuCard(
                        item = item,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Giữ bố cục 2 cột ở hàng cuối
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// =====================================================
// THẺ CHỨC NĂNG NHỎ GỌN
// =====================================================

@Composable
private fun FunctionMenuCard(
    item: DashboardFunctionItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(68.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = androidx.compose.ui.graphics.Color(0xFFE2EDFF)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.icon,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color(0xFF3478E5)
                    )
                }
            }

            Text(
                text = item.title,
                modifier = Modifier.weight(1f),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
        }
    }
}

// =====================================================
// TIÊU ĐỀ KHU VỰC DASHBOARD
// =====================================================

@Composable
private fun DashboardSectionTitle(
    title: String,
    description: String? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (!description.isNullOrBlank()) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// =====================================================
// 1. ADMIN - QUẢN LÝ
// US-1.02, US-2.03, US-3.04, US-4.01, US-4.03, US-4.04
// =====================================================

@Composable
private fun AdminDashboardContent(
    jwtToken: String
) {
    val dashboardViewModel: DashboardViewModel = viewModel()

    val uiState by dashboardViewModel.uiState.collectAsState()

    // Bộ lọc thời gian
    var selectedFilter by rememberSaveable {
        mutableStateOf("thismonth")
    }

    val filters = listOf(
        "7days" to "7 ngày qua",
        "thismonth" to "Tháng này",
        "lastmonth" to "Tháng trước"
    )

    val selectedIndex = filters.indexOfFirst {
        it.first == selectedFilter
    }.coerceAtLeast(0)

    // Gọi API khi mở Dashboard hoặc đổi bộ lọc
    LaunchedEffect(jwtToken, selectedFilter) {
        dashboardViewModel.loadDashboard(
            jwtToken = jwtToken,
            filter = selectedFilter
        )
    }

    val chartData = uiState.chart?.data.orEmpty()

    val totalImport = chartData.sumOf {
        it.totalImport
    }

    val totalExport = chartData.sumOf {
        it.totalExport
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Dashboard Quản lý",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // DANH MỤC CHỨC NĂNG ADMIN
        FunctionCatalog(
            functions = listOf(
                DashboardFunctionItem("⇄", "Nhập/Xuất"),
                DashboardFunctionItem("▤", "Kiểm kho"),
                DashboardFunctionItem("▥", "Báo cáo"),
                DashboardFunctionItem("◇", "Sản phẩm"),
                DashboardFunctionItem("♙", "Tài khoản"),
                DashboardFunctionItem("!", "Cảnh báo tồn")
            )
        )

        HorizontalDivider()

        // BỘ LỌC THỜI GIAN
        DashboardSectionTitle(
            title = "Thời gian thống kê"
        )

        PrimaryTabRow(
            selectedTabIndex = selectedIndex
        ) {
            filters.forEachIndexed { index, filter ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = {
                        selectedFilter = filter.first
                    },
                    text = {
                        Text(
                            text = filter.second,
                            maxLines = 1
                        )
                    }
                )
            }
        }

        // TRẠNG THÁI TẢI DỮ LIỆU
        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Đang tải dữ liệu từ máy chủ...",
                style = MaterialTheme.typography.bodySmall
            )
        }

        // THÔNG BÁO LỖI
        if (uiState.error != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = uiState.error ?: "Có lỗi xảy ra.",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )

                    Button(
                        onClick = {
                            dashboardViewModel.loadDashboard(
                                jwtToken = jwtToken,
                                filter = selectedFilter
                            )
                        }
                    ) {
                        Text("Thử tải lại")
                    }
                }
            }
        }

        // TỔNG QUAN NHẬP - XUẤT
        DashboardSectionTitle(
            title = "Tổng quan nhập - xuất"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatisticCard(
                title = "Tổng lượng nhập",
                value = if (uiState.chart != null) {
                    formatQuantity(totalImport)
                } else {
                    "--"
                },
                description = "Sản phẩm nhập kho",
                modifier = Modifier.weight(1f),
                containerColor = androidx.compose.ui.graphics.Color(0xFFE8EFFB)
            )

            StatisticCard(
                title = "Tổng lượng xuất",
                value = if (uiState.chart != null) {
                    formatQuantity(totalExport)
                } else {
                    "--"
                },
                description = "Sản phẩm xuất kho",
                modifier = Modifier.weight(1f),
                containerColor = androidx.compose.ui.graphics.Color(0xFFFFEFE1)
            )
        }

        uiState.chart?.let { chart ->
            Text(
                text = "Dữ liệu từ ${chart.fromDate} đến ${chart.toDate}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // BIỂU ĐỒ NHẬP - XUẤT
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Biểu đồ nhập - xuất kho",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (uiState.chart == null) {
                    Text(
                        text = if (uiState.isLoading) {
                            "Đang tải biểu đồ..."
                        } else {
                            "Chưa có dữ liệu biểu đồ."
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        textAlign = TextAlign.Center
                    )
                } else if (chartData.isEmpty()) {
                    Text(
                        text = "Không có dữ liệu trong khoảng thời gian này.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        AndroidView(
                            modifier = Modifier
                                .width(
                                    (chartData.size * 56)
                                        .coerceAtLeast(360)
                                        .dp
                                )
                                .height(270.dp),

                            factory = { context ->
                                BarChart(context).apply {
                                    description.isEnabled = false

                                    setDrawGridBackground(false)
                                    setDrawBarShadow(false)
                                    setDrawValueAboveBar(false)

                                    setPinchZoom(false)
                                    setScaleEnabled(false)
                                    isDoubleTapToZoomEnabled = false

                                    axisRight.isEnabled = false

                                    axisLeft.apply {
                                        axisMinimum = 0f
                                        setDrawGridLines(true)
                                        textColor = AndroidColor.DKGRAY
                                    }

                                    xAxis.apply {
                                        position = XAxis.XAxisPosition.BOTTOM
                                        setDrawGridLines(false)
                                        granularity = 1f
                                        isGranularityEnabled = true
                                        setCenterAxisLabels(true)
                                        textColor = AndroidColor.DKGRAY
                                    }

                                    legend.apply {
                                        isEnabled = true
                                        horizontalAlignment =
                                            Legend.LegendHorizontalAlignment.CENTER
                                        verticalAlignment =
                                            Legend.LegendVerticalAlignment.BOTTOM
                                        orientation =
                                            Legend.LegendOrientation.HORIZONTAL
                                        form = Legend.LegendForm.SQUARE
                                    }

                                    setExtraOffsets(8f, 12f, 8f, 8f)
                                    setNoDataText("Không có dữ liệu biểu đồ")
                                }
                            },

                            update = { chart ->
                                val importEntries = ArrayList<BarEntry>()
                                val exportEntries = ArrayList<BarEntry>()

                                chartData.forEachIndexed { index, item ->
                                    importEntries.add(
                                        BarEntry(
                                            index.toFloat(),
                                            item.totalImport.toFloat()
                                        )
                                    )

                                    exportEntries.add(
                                        BarEntry(
                                            index.toFloat(),
                                            item.totalExport.toFloat()
                                        )
                                    )
                                }

                                val importSet = BarDataSet(
                                    importEntries,
                                    "Nhập kho"
                                ).apply {
                                    color = AndroidColor.rgb(84, 133, 197)
                                    setDrawValues(false)
                                }

                                val exportSet = BarDataSet(
                                    exportEntries,
                                    "Xuất kho"
                                ).apply {
                                    color = AndroidColor.rgb(230, 154, 70)
                                    setDrawValues(false)
                                }

                                val barData = BarData(
                                    importSet,
                                    exportSet
                                )

                                barData.barWidth = 0.35f
                                chart.data = barData

                                chart.xAxis.valueFormatter =
                                    IndexAxisValueFormatter(
                                        chartData.map { it.label }
                                    )

                                chart.groupBars(
                                    0f,
                                    0.20f,
                                    0.05f
                                )

                                chart.setFitBars(true)
                                chart.notifyDataSetChanged()
                                chart.invalidate()
                            }
                        )
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Tổng nhập",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = formatQuantity(totalImport),
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color(0xFF5485C5)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Tổng xuất",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = formatQuantity(totalExport),
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color(0xFFE69A46)
                        )
                    }
                }
            }
        }

        // TOP SẢN PHẨM XUẤT NHIỀU
        DashboardSectionTitle(
            title = "Top sản phẩm xuất nhiều nhất"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val products = uiState.topExported?.data.orEmpty()

                if (products.isEmpty()) {
                    Text(
                        text = if (uiState.isLoading) {
                            "Đang tải danh sách..."
                        } else if (uiState.topExported != null) {
                            "Không có sản phẩm xuất trong khoảng thời gian này."
                        } else {
                            "Chưa có dữ liệu sản phẩm."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    products.forEachIndexed { index, product ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}.",
                                fontWeight = FontWeight.Bold,
                                color = androidx.compose.ui.graphics.Color(0xFF526B9A)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = product.productName,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    text = product.sku,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = formatQuantity(
                                    product.totalExportedQuantity
                                ),
                                fontWeight = FontWeight.Bold,
                                color = androidx.compose.ui.graphics.Color(0xFFE69A46)
                            )
                        }

                        if (index != products.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }

        // TOP SẢN PHẨM TỒN KHO LÂU
        DashboardSectionTitle(
            title = "Top sản phẩm tồn kho lâu nhất"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val products = uiState.oldestStock?.data.orEmpty()

                if (products.isEmpty()) {
                    Text(
                        text = if (uiState.isLoading) {
                            "Đang tải danh sách..."
                        } else if (uiState.oldestStock != null) {
                            "Không có sản phẩm phù hợp trong khoảng thời gian này."
                        } else {
                            "Chưa có dữ liệu tồn kho."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    products.forEachIndexed { index, product ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = "${index + 1}. ${product.productName}",
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "Mã sản phẩm: ${product.sku}",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tồn kho: ${formatQuantity(product.currentStock)}"
                                )

                                Text(
                                    text = formatApiDate(product.oldestImportDate),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = androidx.compose.ui.graphics.Color(0xFFD77A27)
                                )
                            }
                        }

                        if (index != products.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

// =====================================================
// 2. MANAGER - THỦ KHO
// US-2.01, US-2.02, US-2.04, US-3.01, US-3.02, US-4.02
// =====================================================

@Composable
private fun ManagerDashboardContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Dashboard Thủ kho",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Quản lý sản phẩm và hoạt động trong kho.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // DANH MỤC CHỨC NĂNG
        FunctionCatalog(
            functions = listOf(
                DashboardFunctionItem("◇", "Sản phẩm"),
                DashboardFunctionItem("⌕", "Quét mã vạch"),
                DashboardFunctionItem("⇩", "Nhập kho"),
                DashboardFunctionItem("▤", "Kiểm kho")
            )
        )

        HorizontalDivider()

        // THÔNG TIN CÔNG VIỆC
        DashboardSectionTitle(
            title = "Công việc của Thủ kho"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WorkflowStep(
                    number = "1",
                    title = "Quản lý sản phẩm",
                    description = "Thêm sản phẩm mới, quét mã vạch và tìm kiếm thông tin sản phẩm."
                )

                HorizontalDivider()

                WorkflowStep(
                    number = "2",
                    title = "Tiếp nhận hàng hóa",
                    description = "Tạo phiếu nhập và thêm sản phẩm vào phiếu nhập kho."
                )

                HorizontalDivider()

                WorkflowStep(
                    number = "3",
                    title = "Kiểm kê kho",
                    description = "Xem phiên kiểm kê được giao, nhập số lượng thực tế và đối chiếu với hệ thống."
                )
            }
        }
    }
}

// =====================================================
// 3. STAFF - NHÂN VIÊN SIÊU THỊ
// US-3.03
// =====================================================

@Composable
private fun StaffDashboardContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Dashboard Nhân viên siêu thị",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Thực hiện hoạt động xuất hàng từ kho ra quầy kệ.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // DANH MỤC CHỨC NĂNG
        FunctionCatalog(
            functions = listOf(
                DashboardFunctionItem("⇧", "Xuất kho")
            )
        )

        HorizontalDivider()

        // QUY TRÌNH XUẤT KHO
        DashboardSectionTitle(
            title = "Quy trình xuất kho"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WorkflowStep(
                    number = "1",
                    title = "Chọn sản phẩm",
                    description = "Chọn sản phẩm cần xuất khỏi kho."
                )

                HorizontalDivider()

                WorkflowStep(
                    number = "2",
                    title = "Nhập số lượng",
                    description = "Nhập số lượng sản phẩm cần xuất."
                )

                HorizontalDivider()

                WorkflowStep(
                    number = "3",
                    title = "Tạo phiếu xuất",
                    description = "Gửi phiếu xuất cho Quản lý xem xét và phê duyệt."
                )
            }
        }
    }
}

// =====================================================
// COMPONENT: QUY TRÌNH CÔNG VIỆC
// =====================================================

@Composable
private fun WorkflowStep(
    number: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// =====================================================
// COMPONENT: THẺ THỐNG KÊ
// =====================================================

@Composable
private fun StatisticCard(
    title: String,
    value: String,
    description: String,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = androidx.compose.ui.graphics.Color(0xFF424B5B)
            )

            Text(
                text = value,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color(0xFF263B60)
            )

            Text(
                text = description,
                fontSize = 11.sp,
                color = androidx.compose.ui.graphics.Color(0xFF626D7C)
            )
        }
    }
}

// =====================================================
// HÀM ĐỊNH DẠNG SỐ
// =====================================================

private fun formatQuantity(value: Int): String {
    return String.format(
        Locale.US,
        "%,d",
        value
    ).replace(",", ".")
}

// =====================================================
// HÀM ĐỊNH DẠNG NGÀY TỪ API
// =====================================================

private fun formatApiDate(value: String?): String {
    if (value.isNullOrBlank()) {
        return "Không rõ ngày nhập"
    }

    val parts = value.take(10).split("-")

    return if (parts.size == 3) {
        "${parts[2]}/${parts[1]}/${parts[0]}"
    } else {
        value
    }
}
