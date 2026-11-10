package appmobile.example.quan_ly_kho.data.model

enum class UserRole(val code: String, val displayName: String) {
    ADMIN("Admin", "Quản lý"),
    MANAGER("Manager", "Thủ kho"),
    STAFF("Staff", "Nhân viên siêu thị");

    companion object {
        fun fromString(role: String?): UserRole? {
            return entries.firstOrNull { it.code.equals(role?.trim(), ignoreCase = true) }
        }

        fun canAddProduct(role: String?): Boolean {
            val userRole = fromString(role)
            return userRole == ADMIN || userRole == MANAGER
        }

        fun canViewStatistics(role: String?): Boolean {
            val userRole = fromString(role)
            return userRole == ADMIN
        }

        fun getDisplayName(role: String?): String {
            return fromString(role)?.displayName ?: (role ?: "")
        }
    }
}
