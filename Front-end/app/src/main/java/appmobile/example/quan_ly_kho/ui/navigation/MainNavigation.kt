package appmobile.example.quan_ly_kho.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import appmobile.example.quan_ly_kho.data.model.UserRole
import appmobile.example.quan_ly_kho.ui.features.dashboard.DashboardScreen
import appmobile.example.quan_ly_kho.ui.features.product.ProductListScreen

private data class BottomNavItem(
    val key: String,
    val title: String,
    val icon: String
)

private val navigationItems = listOf(
    BottomNavItem("home", "Trang chủ", "⌂"),
    BottomNavItem("products", "Sản phẩm", "▣"),
    BottomNavItem("transactions", "Giao dịch", "⇄"),
    BottomNavItem("settings", "Cài đặt", "⚙")
)

@Composable
fun MainNavigation(
    userName: String,
    userRole: String,
    jwtToken: String,

    selectedTab: String = "home",
    onTabSelected: (String) -> Unit = {},
    onAddProductClick: () -> Unit = {},
    onUnauthorizedAccess: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                navigationItems.forEach { item ->
                    NavigationBarItem(
                        selected = selectedTab == item.key,
                        onClick = {
                            onTabSelected(item.key)
                        },
                        icon = {
                            Text(
                                text = item.icon,
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        label = {
                            Text(item.title)
                        },
                        alwaysShowLabel = true
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                "home" -> {
                    DashboardScreen(
                        userName = userName,
                        userRole = userRole,
                        jwtToken = jwtToken,
                        onUnauthorizedAccess = onUnauthorizedAccess,
                        onNavigateTab = onTabSelected
                    )
                }

                "products" -> {
                    ProductListScreen(
                        userRole = userRole,
                        onAddProductClick = onAddProductClick
                    )
                }

                "transactions" -> {
                    // ST-1.02.4 & ST-1.02.5: Màn hình Giao dịch với các nút "Duyệt phiếu", "Xem báo cáo"
                    TransactionsScreen(
                        userRole = userRole,
                        onUnauthorizedAccess = onUnauthorizedAccess
                    )
                }

                "settings" -> {
                    SettingsScreen(
                        userName = userName,
                        userRole = userRole,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}

private data class ReceiptSampleItem(
    val id: String,
    val type: String,
    val description: String,
    val quantity: Int,
    val createdBy: String,
    var status: String
)

// =====================================================
// MÀN HÌNH GIAO DỊCH (KIỂM SOÁT DUYỆT PHIẾU & XEM BÁO CÁO)
// ST-1.02.4 & ST-1.02.5
// =====================================================

@Composable
private fun TransactionsScreen(
    userRole: String,
    onUnauthorizedAccess: () -> Unit
) {
    val context = LocalContext.current
    val canApprove = UserRole.canApproveReceipt(userRole)
    val canViewReport = UserRole.canViewReports(userRole)

    var receipts by remember {
        mutableStateOf(
            listOf(
                ReceiptSampleItem("NK-2026-001", "Phiếu nhập kho", "Nhập hàng tươi sống VinEco", 320, "Trần Văn B (Thủ kho)", "PENDING"),
                ReceiptSampleItem("XK-2026-002", "Phiếu xuất kho", "Xuất hàng ra quầy kệ A1-A3", 85, "Lê Thị C (Nhân viên)", "PENDING"),
                ReceiptSampleItem("XK-2026-003", "Phiếu xuất kho", "Xuất kiểm tra hạn sử dụng", 15, "Nguyễn Văn A (Admin)", "APPROVED")
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Giao Dịch Kho",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Quản lý và phê duyệt các phiếu nhập/xuất",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ST-1.02.4 & ST-1.02.5: NÚT "XEM BÁO CÁO"
            // Bị disable (khóa) nếu không phải Admin, khi click sẽ báo "Bạn không có quyền truy cập tính năng này"
            OutlinedButton(
                onClick = {
                    if (canViewReport) {
                        Toast.makeText(context, "Mở báo cáo thống kê giao dịch...", Toast.LENGTH_SHORT).show()
                    } else {
                        onUnauthorizedAccess()
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (canViewReport) MaterialTheme.colorScheme.primary else Color(0xFF94A3B8)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (canViewReport) "▥ Xem báo cáo" else "🔒 Xem báo cáo",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        HorizontalDivider()

        Text(
            text = "Danh sách phiếu cần xử lý",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(receipts, key = { it.id }) { receipt ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${receipt.type} #${receipt.id}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (receipt.status == "APPROVED") Color(0xFFD1FAE5) else Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    text = if (receipt.status == "APPROVED") "ĐÃ DUYỆT" else "CHỜ DUYỆT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (receipt.status == "APPROVED") Color(0xFF0F5132) else Color(0xFF854D0E),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = receipt.description,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Số lượng: ${receipt.quantity} SP",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Tạo bởi: ${receipt.createdBy}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // ST-1.02.4 & ST-1.02.5: NÚT "DUYỆT PHIẾU"
                        // Nếu đã duyệt thì ẩn nút. Nếu đang chờ duyệt:
                        // - Admin: Nút bật (xanh lá cây), click để duyệt thành công
                        // - Manager/Staff: Nút bị vô hiệu hóa (disable/xám), click sẽ báo "Bạn không có quyền truy cập tính năng này"
                        if (receipt.status == "PENDING") {
                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = {
                                    if (canApprove) {
                                        receipts = receipts.map {
                                            if (it.id == receipt.id) it.copy(status = "APPROVED") else it
                                        }
                                        Toast.makeText(context, "Duyệt phiếu #${receipt.id} thành công!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        onUnauthorizedAccess()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (canApprove) Color(0xFF16A34A) else Color(0xFFCBD5E1),
                                    contentColor = if (canApprove) Color.White else Color(0xFF64748B)
                                )
                            ) {
                                Text(
                                    text = if (canApprove) "✔ Duyệt phiếu" else "🔒 Duyệt phiếu (Cần quyền Quản lý)",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    userName: String,
    userRole: String,
    onLogout: () -> Unit
) {
    val displayRole = UserRole.getDisplayName(userRole)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Tài khoản người dùng",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Họ và tên: $userName",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Vai trò: $displayRole ($userRole)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Đăng xuất",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}
