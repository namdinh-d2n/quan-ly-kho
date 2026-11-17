package appmobile.example.quan_ly_kho.data.model

enum class UserRole(val code: String, val displayName: String) {
    ADMIN("Admin", "Quản lý"),
    MANAGER("Manager", "Thủ kho"),
    STAFF("Staff", "Nhân viên siêu thị");

    companion object {
        fun fromString(role: String?): UserRole? {
            return entries.firstOrNull { it.code.equals(role?.trim(), ignoreCase = true) }
        }

        // ST-1.02.4: Phân quyền Thêm sản phẩm (Quản lý, Thủ kho)
        fun canAddProduct(role: String?): Boolean {
            val userRole = fromString(role)
            return userRole == ADMIN || userRole == MANAGER
        }

        // ST-1.02.4: Phân quyền Xem báo cáo / Thống kê (Chỉ Quản lý - Admin)
        fun canViewReports(role: String?): Boolean {
            val userRole = fromString(role)
            return userRole == ADMIN
        }

        fun canViewStatistics(role: String?): Boolean {
            return canViewReports(role)
        }

        // ST-1.02.4: Phân quyền Duyệt phiếu xuất/nhập (Chỉ Quản lý - Admin)
        fun canApproveReceipt(role: String?): Boolean {
            val userRole = fromString(role)
            return userRole == ADMIN
        }

        // Phân quyền Kiểm kho (Quản lý, Thủ kho)
        fun canStocktake(role: String?): Boolean {
            val userRole = fromString(role)
            return userRole == ADMIN || userRole == MANAGER
        }

        // Phân quyền Nhập kho (Quản lý, Thủ kho)
        fun canImportWarehouse(role: String?): Boolean {
            val userRole = fromString(role)
            return userRole == ADMIN || userRole == MANAGER
        }

        // Phân quyền Xuất kho (Tất cả vai trò)
        fun canExportWarehouse(role: String?): Boolean {
            val userRole = fromString(role)
            return userRole != null
        }

        // Phân quyền Quản lý tài khoản (Chỉ Quản lý - Admin)
        fun canManageUsers(role: String?): Boolean {
            val userRole = fromString(role)
            return userRole == ADMIN
        }

        fun getDisplayName(role: String?): String {
            return fromString(role)?.displayName ?: (role ?: "")
        }
    }
}
