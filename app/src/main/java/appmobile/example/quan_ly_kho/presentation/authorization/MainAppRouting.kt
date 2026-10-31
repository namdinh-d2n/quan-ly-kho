package appmobile.example.quan_ly_kho.presentation.authorization
import LoginScreen
import LoginViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import appmobile.example.quan_ly_kho.data.model.AuthResponse
import appmobile.example.quan_ly_kho.data.remote.ApiService

@Composable
fun MainAppRouting(apiService: ApiService) {
    var currentUser by remember { mutableStateOf<AuthResponse?>(null) }

    if (currentUser == null) {
        val viewModel = remember { LoginViewModel(apiService) }
        LoginScreen(viewModel = viewModel, onLoginSuccess = { user ->
            currentUser = user
        })
    } else {
        // Phân quyền màn hình sau khi Login thành công
        when (currentUser?.role) {
            "Manager" -> ManagerDashboardScreen(user = currentUser!!, onLogout = { currentUser = null })
            "Staff" -> StaffDashboardScreen(user = currentUser!!, onLogout = { currentUser = null })
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Tài khoản không có quyền truy cập hợp lệ!")
                }
            }
        }
    }
}

// Màn hình cho Quản lý (Manager)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerDashboardScreen(user: AuthResponse, onLogout: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản Lý Kho - Admin") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Đăng xuất", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Xin chào Quản Lý: ${user.fullName}")
            Text("Quyền hạn: Duyệt phiếu nhập/xuất, Tạo kỳ kiểm kê, Quản lý tài khoản.")
        }
    }
}

// Màn hình cho Thủ kho (Staff)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDashboardScreen(user: AuthResponse, onLogout: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thủ Kho - Nhập/Xuất") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Đăng xuất", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Xin chào Thủ kho: ${user.fullName}")
            Text("Quyền hạn: Lập phiếu nhập/xuất, Quét mã kiểm kê thực tế.")
        }
    }
}