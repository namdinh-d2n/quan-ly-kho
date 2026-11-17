package appmobile.example.quan_ly_kho
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.*
import org.junit.Rule
import org.junit.Test
import appmobile.example.quan_ly_kho.ui.features.product.AddProductScreen
class ProductAutomationTest {
    // Khởi tạo Rule cho Compose Test
    // Thay vì createComposeRule, ta dùng createAndroidComposeRule để ép app khởi động MainActivity (Màn hình đầu tiên)
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

//    @Test
//    fun testAddProduct_FullFlow_WithRealData() {
//        // BƯỚC 1: ĐĂNG NHẬP
//        // Giả định bạn đã bảo Thư/Thảo gắn tag cho màn hình Login
//        composeTestRule.onNodeWithText("Số điện thoại / Email")
//            .performTextInput("thuy.tran@megamart.vn")
//
//        // 2. Tìm ô Mật khẩu và gõ pass
//        composeTestRule.onNodeWithText("Mật khẩu")
//            .performTextInput("123")
//
//        // 3. Tìm nút và tự động bấm click
//        composeTestRule.onNodeWithText("ĐĂNG NHẬP")
//            .performClick()
//        // Đợi 2-3 giây để API Login của Nam trả về Token và App nhảy vào Dashboard
//        Thread.sleep(4000)
//        composeTestRule.waitUntil(timeoutMillis = 10000) {
//            composeTestRule
//                .onAllNodesWithText("Sản phẩm", substring = true, ignoreCase = true, useUnmergedTree = true)
//                .fetchSemanticsNodes().isNotEmpty()
//        }
//        // BƯỚC 2: ĐIỀU HƯỚNG SANG KHO SẢN PHẨM
//        composeTestRule.onNodeWithText("Sản phẩm", substring = true, ignoreCase = true, useUnmergedTree = true)
//            .performClick()
//        Thread.sleep(1000)
//
//        // BƯỚC 3: BẤM NÚT THÊM (+)
//        composeTestRule.onNodeWithContentDescription("Thêm sản phẩm", substring = true, ignoreCase = true, useUnmergedTree = true)
//
//            .performClick()
//
//        // Quan trọng: Đợi 1.5 - 2 giây để ViewModel gọi API lấy danh sách Categories thật từ Backend về
//        Thread.sleep(2000)
//
//        // BƯỚC 4: TEST FORM THÊM SẢN PHẨM (Đã có dữ liệu thật)
//
//        // Nhập tên, ĐVT, SKU...
//        composeTestRule.onNodeWithTag("input_ten_sp").performScrollTo().performTextInput("Bia Heineken")
//
//        // Click Dropdown Danh mục
//        composeTestRule.onNodeWithTag("dropdown_danh_muc").performScrollTo().performClick()
//        Thread.sleep(1000) // Đợi menu bung ra
//
//        // Bấm chọn một danh mục CÓ THẬT trong DB của Nam (Sương phải hỏi Nam xem trong SQL Server đang có những chữ gì nhé)
//        composeTestRule.onAllNodesWithText("Đồ uống & Giải khát ", substring = true, ignoreCase = true, useUnmergedTree = true)
//            .onFirst()
//            .performClick()
//
//        // Tiếp tục nhập giá, tồn kho...
//        composeTestRule.onNodeWithTag("input_gia_ban").performScrollTo().performTextInput("360000")
//
//        // Bấm Lưu
//        composeTestRule.onNodeWithText("Lưu", substring = true, ignoreCase = true).performClick()
//
//        // Nghiệm thu: Đợi API thêm sản phẩm chạy xong và quay ra ngoài
//        Thread.sleep(2000)
//        composeTestRule.onAllNodesWithText("Bia Heineken", substring = true).onFirst().assertExists()
//    }

}