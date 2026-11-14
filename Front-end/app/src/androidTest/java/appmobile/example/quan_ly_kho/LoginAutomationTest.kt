package appmobile.example.quan_ly_kho

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.*
import org.junit.Rule
import org.junit.Test

class LoginAutomationTest {

    // Lệnh này tương đương với việc bật máy ảo và mở app lên
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testLogin_WithInvisibleWhitespace() {
        // 1. Tìm ô nhập liệu và tự động gõ Email có chứa khoảng trắng 2 đầu
        composeTestRule.onNodeWithText("Số điện thoại / Email")
            .performTextInput(" thuy.tran@megamart.vn ")

        // 2. Tìm ô Mật khẩu và gõ pass
        composeTestRule.onNodeWithText("Mật khẩu")
            .performTextInput("123456")

        // 3. Tìm nút và tự động bấm click
        composeTestRule.onNodeWithText("ĐĂNG NHẬP")
            .performClick()

        // 4. Nghiệm thu (Assert): Đảm bảo rằng thông báo lỗi đỏ KHÔNG xuất hiện trên màn hình
        composeTestRule.onNodeWithText("Tài khoản hoặc mật khẩu không chính xác")
            .assertDoesNotExist()

        // (Tùy chọn) Có thể thêm lệnh kiểm tra xem app đã nhảy sang giao diện Dashboard chưa
    }
    // 1. Luồng thành công (Happy Path) - Điền đúng chuẩn mọi thứ
    @Test
    fun testLogin_Successful() {
        composeTestRule.onNodeWithText("Số điện thoại / Email")
            .performTextInput("thuy.tran@megamart.vn")
        composeTestRule.onNodeWithText("Mật khẩu")
            .performTextInput("123")

        composeTestRule.onNodeWithText("ĐĂNG NHẬP").performClick()

        // Nghiệm thu: Không còn nút Đăng nhập trên màn hình nữa (vì đã chuyển sang Dashboard)
        // Hoặc có thể assert theo Text của trang Dashboard nếu có
        composeTestRule.onNodeWithText("ĐĂNG NHẬP").assertDoesNotExist()
    }

    // 2. Luồng thất bại (Negative Path) - Sai mật khẩu
    @Test
    fun testLogin_WrongPassword_ShowsErrorMessage() {
        composeTestRule.onNodeWithText("Số điện thoại / Email")
            .performTextInput("thuy.tran@megamart.vn")
        composeTestRule.onNodeWithText("Mật khẩu")
            .performTextInput("123456")

        composeTestRule.onNodeWithText("ĐĂNG NHẬP").performClick()
        Thread.sleep(3000)
        // Nghiệm thu: Phải xuất hiện dòng chữ báo lỗi trên UI (kiểm tra lại Text thực tế trong app của Thư/Thảo)
        composeTestRule.onNodeWithText("Tài khoản hoặc mật khẩu không chính xác!")
            .assertExists()
    }

    // 3. Kiểm tra Validate - Bấm Đăng nhập khi bỏ trống thông tin
    @Test
    fun testLogin_EmptyFields_CannotClick() {
        // Không nhập gì cả, bấm nút luôn
        composeTestRule.onNodeWithText("ĐĂNG NHẬP").performClick()

        // Tùy theo logic code UI: Nút có thể bị Disable (mờ đi) hoặc hiện thông báo "Vui lòng nhập Email"
        // Ví dụ: Bắt lỗi báo đỏ trên ô nhập liệu
        composeTestRule.onNodeWithText("Vui lòng điền đầy đủ thông tin!")
            .assertExists()
    }
}