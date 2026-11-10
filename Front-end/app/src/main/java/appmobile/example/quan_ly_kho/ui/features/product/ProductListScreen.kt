package appmobile.example.quan_ly_kho.ui.features.product

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import appmobile.example.quan_ly_kho.R
import appmobile.example.quan_ly_kho.data.model.UserRole
import appmobile.example.quan_ly_kho.data.model.product.ProductResponse
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    userRole: String = "",
    onAddProductClick: () -> Unit = {},
    viewModel: ProductListViewModel = viewModel()
) {
    var searchText by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    val primaryBlue = Color(0xFF2563EB)
    val bgLightGray = Color(0xFFF8F9FA)
    val textDark = Color(0xFF1E293B)
    val textGray = Color(0xFF64748B)

    val canAddProduct = remember(userRole) {
        UserRole.canAddProduct(userRole)
    }

    // Gọi tìm kiếm khi người dùng nhập từ khóa
    LaunchedEffect(searchText) {
        viewModel.fetchProducts(searchText)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgLightGray)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 1. HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kho Sản Phẩm",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textDark
            )

            if (canAddProduct) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(primaryBlue, shape = CircleShape)
                        .clickable { onAddProductClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.plus),
                        contentDescription = "Add Product",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Ô TÌM KIẾM & NÚT SCAN
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val interactionSource = remember { MutableInteractionSource() }

            BasicTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp, color = textDark),
                interactionSource = interactionSource,
                decorationBox = { innerTextField ->
                    OutlinedTextFieldDefaults.DecorationBox(
                        value = searchText,
                        innerTextField = innerTextField,
                        enabled = true,
                        singleLine = true,
                        visualTransformation = VisualTransformation.None,
                        interactionSource = interactionSource,
                        placeholder = {
                            Text("Tìm kiếm sản phẩm...", fontSize = 14.sp, color = textGray)
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.search),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = textGray
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        container = {
                            OutlinedTextFieldDefaults.Container(
                                enabled = true,
                                isError = false,
                                interactionSource = interactionSource,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color(0xFFF1F5F9),
                                    focusedContainerColor = Color.White,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = primaryBlue
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    )
                }
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFF1F5F9), shape = RoundedCornerShape(12.dp))
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.barcode),
                    contentDescription = "Scan Barcode",
                    tint = textDark,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. NỘI DUNG DANH SÁCH (TẢI TỪ BACKEND)
        when (val state = uiState) {
            is ProductListUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryBlue)
                }
            }
            is ProductListUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = state.message, color = Color.Red, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.fetchProducts(searchText) }) {
                            Text("Thử lại")
                        }
                    }
                }
            }
            is ProductListUiState.Success -> {
                val products = state.products

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tất cả sản phẩm (${products.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = textDark
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { }
                    ) {
//                        Text(
//                            text = "Sắp xếp",
//                            fontSize = 13.sp,
//                            color = textGray,
//                            fontWeight = FontWeight.Medium
//                        )
//                        Spacer(modifier = Modifier.width(4.dp))
//                        Icon(
//                            painter = painterResource(id = R.drawable.chevron_down),
//                            contentDescription = null,
//                            tint = textGray,
//                            modifier = Modifier.size(14.dp)
//                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductCardItem(product = product)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCardItem(product: ProductResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // HÌNH ẢNH SẢN PHẨM (TẢI QUA COIL HOẶC HIỂN THỊ DỰ PHÒNG)
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                if (!product.imageUrl.isNullOrBlank()) {
                    // Chuyển relative path từ server (ví dụ: uploads/products/abc.jpg) thành full URL cho emulator
                    val fullImageUrl = if (product.imageUrl.startsWith("http")) {
                        product.imageUrl
                    } else {
                        "http://10.0.2.2:5028/${product.imageUrl.trimStart('/')}"
                    }

                    AsyncImage(
                        model = fullImageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.resource_package),
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.weight(1f)
                    )

                    // BADGE TRẠNG THÁI CẢNH BÁO TỒN KHO (TỪ TRƯỜNG isWarning CỦA BACKEND)
                    Row(
                        modifier = Modifier
                            .background(
                                color = if (product.isWarning) Color(0xFFFEE2E2) else Color(0xFFD1FAE5),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (product.isWarning) R.drawable.alert_triangle else R.drawable.check_circle
                            ),
                            contentDescription = null,
                            tint = if (product.isWarning) Color(0xFFDC2626) else Color(0xFF0F5132),
                            modifier = Modifier.size(11.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = if (product.isWarning) "Cảnh báo" else "Tốt",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (product.isWarning) Color(0xFFDC2626) else Color(0xFF0F5132)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "SKU: ${product.sku}",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // GIÁ ĐÃ ĐỊNH DẠNG TỪ BACKEND (priceFormatted)
                Text(
                    text = product.priceFormatted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(2.dp))

                if (product.isWarning) {
                    Text(
                        text = "Kho: ${product.stock} (Dưới mức tối thiểu)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFEF4444)
                    )
                } else {
                    Text(
                        text = "Kho: ${product.stock}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}