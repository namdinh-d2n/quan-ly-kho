package appmobile.example.quan_ly_kho.presentation.authorization

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import appmobile.example.quan_ly_kho.data.local.JwtUtils
import appmobile.example.quan_ly_kho.data.local.SessionManager
import appmobile.example.quan_ly_kho.data.model.AuthResponse
import appmobile.example.quan_ly_kho.data.remote.ApiService
import appmobile.example.quan_ly_kho.ui.features.auth.LoginScreen
import appmobile.example.quan_ly_kho.ui.features.auth.LoginViewModel

@Composable
fun MainAppRouting(apiService: ApiService) {
    val context = LocalContext.current
    var currentUser by remember { mutableStateOf<AuthResponse?>(null) }

    // ST-1.02.3: Khi khởi chạy ứng dụng, nếu có token đã lưu thì giải mã JWT
    // và gọi API GET /api/v1/auth/me để lấy vai trò mới nhất
    LaunchedEffect(Unit) {
        val savedToken = SessionManager.token
        if (!savedToken.isNullOrBlank()) {
            if (JwtUtils.isExpired(savedToken)) {
                Log.w("AUTH_ME", "Token đã hết hạn, yêu cầu đăng nhập lại")
                SessionManager.clear()
                return@LaunchedEffect
            }

            try {
                val response = apiService.getCurrentUser("Bearer $savedToken")
                if (response.isSuccessful && response.body() != null) {
                    val userInfo = response.body()!!
                    SessionManager.updateUserInfo(userInfo)
                    currentUser = AuthResponse(
                        token = savedToken,
                        userID = userInfo.userID,
                        fullName = userInfo.fullName,
                        role = userInfo.role
                    )
                    Log.d("AUTH_ME", "Khôi phục phiên đăng nhập: ${userInfo.fullName} - ${userInfo.role}")
                } else if (response.code() == 401 || response.code() == 403) {
                    Log.w("AUTH_ME", "Token hết hạn hoặc tài khoản bị khóa")
                    SessionManager.clear()
                    Toast.makeText(
                        context,
                        "Phiên đăng nhập đã hết hạn hoặc tài khoản bị khóa!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("AUTH_ME", "Lỗi kết nối khi gọi /api/v1/auth/me: ${e.message}")
                val decoded = JwtUtils.decodeJwt(savedToken)
                if (decoded != null && !decoded.role.isNullOrBlank()) {
                    currentUser = AuthResponse(
                        token = savedToken,
                        userID = decoded.userId ?: SessionManager.userId,
                        fullName = decoded.fullName ?: SessionManager.userName ?: "",
                        role = decoded.role
                    )
                    Log.d("AUTH_ME", "Fallback: dùng thông tin từ JWT decode - Role: ${decoded.role}")
                }
            }
        }
    }

    if (currentUser == null) {
        val viewModel: LoginViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
        LoginScreen(viewModel = viewModel, onLoginSuccess = { user ->
            SessionManager.saveAuth(user)
            currentUser = user
        })
    } else {
        // Phân quyền màn hình sau khi Login thành công
        // ST-1.02.4: Hiển thị Dashboard phù hợp với vai trò người dùng
        when (currentUser?.role?.trim()?.lowercase()) {
            "admin" -> AdminDashboardScreen(
                user = currentUser!!,
                onLogout = {
                    SessionManager.clear()
                    currentUser = null
                    Toast.makeText(context, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show()
                }
            )
            "manager" -> ManagerDashboardScreen(
                user = currentUser!!,
                onLogout = {
                    SessionManager.clear()
                    currentUser = null
                    Toast.makeText(context, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show()
                }
            )
            "staff" -> StaffDashboardScreen(
                user = currentUser!!,
                onLogout = {
                    SessionManager.clear()
                    currentUser = null
                    Toast.makeText(context, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show()
                }
            )
            else -> {
                // ST-1.02.5: Hiển thị thông báo khi không có quyền truy cập
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Tài khoản không có quyền truy cập hợp lệ!")
                }
            }
        }
    }
}

// Màn hình cho Admin (Quản lý)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(user: AuthResponse, onLogout: () -> Unit) {
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
            Text("Quyền hạn: Duyệt phiếu nhập/xuất, Xem báo cáo, Tạo kỳ kiểm kê, Quản lý tài khoản.")
        }
    }
}

// Màn hình cho Thủ kho (Manager)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerDashboardScreen(user: AuthResponse, onLogout: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thủ Kho - Quản lý") },
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
            Text("Quyền hạn: Thêm sản phẩm, Lập phiếu nhập/xuất, Quét mã kiểm kê thực tế.")
            Spacer(modifier = Modifier.height(16.dp))

            // ST-1.02.4 & ST-1.02.5: Nút Duyệt phiếu bị vô hiệu hóa (Disable) với Thủ kho
            Button(
                onClick = {
                    Toast.makeText(
                        context,
                        "Bạn không có quyền truy cập tính năng này",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("🔒 Duyệt phiếu (Cần quyền Quản lý)")
            }
        }
    }
}

// Màn hình cho Nhân viên siêu thị (Staff)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDashboardScreen(user: AuthResponse, onLogout: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nhân Viên Siêu Thị") },
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
            Text("Xin chào Nhân viên: ${user.fullName}")
            Text("Quyền hạn: Lập phiếu xuất kho.")
            Spacer(modifier = Modifier.height(16.dp))

            // ST-1.02.4 & ST-1.02.5: Nút Duyệt phiếu bị vô hiệu hóa (Disable) với Nhân viên
            Button(
                onClick = {
                    Toast.makeText(
                        context,
                        "Bạn không có quyền truy cập tính năng này",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("🔒 Duyệt phiếu (Cần quyền Quản lý)")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ST-1.02.4 & ST-1.02.5: Nút Xem báo cáo bị vô hiệu hóa (Disable) với Nhân viên
            Button(
                onClick = {
                    Toast.makeText(
                        context,
                        "Bạn không có quyền truy cập tính năng này",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("🔒 Xem báo cáo (Cần quyền Quản lý)")
            }
        }
    }
}
