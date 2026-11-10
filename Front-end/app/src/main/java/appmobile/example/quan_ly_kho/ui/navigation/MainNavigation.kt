
package appmobile.example.quan_ly_kho.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                        jwtToken = jwtToken
                    )
                }

                "products" -> {
                    ProductListScreen(
                        userRole = userRole,
                        onAddProductClick = onAddProductClick
                    )
                }

                "transactions" -> {
                    PlaceholderScreen(
                        title = "Giao dịch",
                        description = "Khu vực quản lý giao dịch"
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

// Màn hình tạm thời cho các tab do thành viên khác phụ trách
@Composable
private fun PlaceholderScreen(
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
