
package appmobile.example.quan_ly_kho.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import appmobile.example.quan_ly_kho.ui.features.dashboard.DashboardScreen

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
    jwtToken: String
) {
    var selectedTab by rememberSaveable {
        mutableStateOf("home")
    }

    Scaffold(
        bottomBar = {
            NavigationBar {

                navigationItems.forEach { item ->

                    NavigationBarItem(
                        selected = selectedTab == item.key,

                        onClick = {
                            selectedTab = item.key
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
                    PlaceholderScreen(
                        title = "Sản phẩm",
                        description = "Khu vực quản lý sản phẩm"
                    )
                }

                "transactions" -> {
                    PlaceholderScreen(
                        title = "Giao dịch",
                        description = "Khu vực quản lý giao dịch"
                    )
                }

                "settings" -> {
                    PlaceholderScreen(
                        title = "Cài đặt",
                        description = "Thiết lập ứng dụng"
                    )
                }
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
