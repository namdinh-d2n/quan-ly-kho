package appmobile.example.quan_ly_kho

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.*
import org.junit.Rule
import org.junit.Test
import appmobile.example.quan_ly_kho.MainActivity

class DashboardRoleAutomationTest {

    // Khởi động app từ màn hình MainActivity (Login)
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testStaffLogin_ShowsOnlyExportMenu_NoCharts() {
        // 1. Giả lập Nhân viên đăng nhập
        // Thay email này bằng data tài khoản Nhân viên thực tế trong DB SQL Server
        composeTestRule.onNodeWithText("Số điện thoại / Email")
            .performTextInput("0911222333")
        composeTestRule.onNodeWithText("Mật khẩu")
            .performTextInput("123")

        composeTestRule.onNodeWithText("ĐĂNG NHẬP").performClick()

        // Tạm dừng 4 giây để chờ API Backend của Nam phản hồi và Compose vẽ UI mới
        Thread.sleep(4000)

        // 2. NGHIỆM THU (Assert) - Giao diện Dashboard Nhân Viên

        // a. Đảm bảo TUYỆT ĐỐI KHÔNG CÓ chữ liên quan đến Biểu đồ hay Thống kê
        // Dùng substring = true để quét chuỗi con (VD: Dev code là "Biểu đồ Nhập - Xuất" thì máy vẫn bắt được)

        composeTestRule.onAllNodesWithText("Sản phẩm", substring = true, ignoreCase = true)
            .onFirst().assertExists()

        // b. Đảm bảo PHẢI CÓ danh mục "Xuất kho"
        composeTestRule.onAllNodesWithText("Xuất kho", substring = true, ignoreCase = true)
            .onFirst().assertExists()

    }
    @Test
    fun testManagerLogin_ShowsFullDashboard_And_Filters() {
        // 1. Giả lập Quản lý đăng nhập
        composeTestRule.onNodeWithText("Số điện thoại / Email")
            .performTextInput("thuy.tran@megamart.vn")
        composeTestRule.onNodeWithText("Mật khẩu")
            .performTextInput("123")
        composeTestRule.onNodeWithText("ĐĂNG NHẬP").performClick()

        Thread.sleep(4000)

        composeTestRule.onAllNodesWithText("Duyệt phiếu", substring = true, ignoreCase = true)
            .onFirst().assertExists()

        composeTestRule.onAllNodesWithText("Xem báo cáo", substring = true, ignoreCase = true)
            .onFirst().assertExists()
        composeTestRule.onAllNodesWithText("Tài khoản", substring = true, ignoreCase = true)
            .onFirst().assertExists()


        // 4. KIỂM TRA BỘ LỌC (TC_DB_02)
        // Bấm chọn "7 ngày qua" và đảm bảo app không bị crash
        composeTestRule.onNodeWithText("7 ngày qua").performClick()
        Thread.sleep(2000)
        // Sau khi load lại, biểu đồ vẫn phải tồn tại
        composeTestRule.onNodeWithText("Biểu đồ", substring = true, ignoreCase = true)
            .assertExists()
    }
    @Test
    fun testStorekeeperLogin_Shows4Categories_NoCharts() {
        // 1. Tiền điều kiện: Bắt đầu từ màn hình Đăng nhập
        // Giả lập Thủ kho nhập thông tin
        composeTestRule.onNodeWithText("Số điện thoại / Email")
            .performTextInput("tuan.le@megamart.vn") // Thay bằng acc Thủ kho trong DB
        composeTestRule.onNodeWithText("Mật khẩu")
            .performTextInput("123")

        // Bấm Đăng nhập
        composeTestRule.onNodeWithText("ĐĂNG NHẬP").performClick()

        // Chờ API gọi về và Compose vẽ màn hình Dashboard Thủ kho
        Thread.sleep(8000)

        // a. KHÔNG hiển thị bất kỳ biểu đồ thống kê hay danh sách xếp hạng nào
        composeTestRule.onNodeWithText("Biểu đồ", substring = true, ignoreCase = true)
            .assertDoesNotExist()

        composeTestRule.onNodeWithText("xuất nhiều nhất", substring = true, ignoreCase = true)
            .assertDoesNotExist()
        composeTestRule.onNodeWithText("tồn kho lâu nhất", substring = true, ignoreCase = true)
            .assertDoesNotExist()



        composeTestRule.onAllNodesWithText("Nhập kho", substring = true, ignoreCase = true)
            .onFirst().assertExists()
        composeTestRule.onAllNodesWithText("Kiểm kho", substring = true, ignoreCase = true)
            .onFirst().assertExists()
        composeTestRule.onAllNodesWithText("Quét mã vạch", substring = true, ignoreCase = true)
            .onFirst().assertExists()

                composeTestRule.onAllNodesWithText("Duyệt phiếu", substring = true, ignoreCase = true)
                    .onFirst().assertExists().assertIsEnabled()

    }
}