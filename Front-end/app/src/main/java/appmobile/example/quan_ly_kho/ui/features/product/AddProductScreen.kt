@file:OptIn(ExperimentalMaterial3Api::class)

package appmobile.example.quan_ly_kho.ui.features.product

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import appmobile.example.quan_ly_kho.R
import coil.compose.AsyncImage

@Composable
fun AddProductScreen(
    onBackClick: () -> Unit = {},
    onSaveSuccess: () -> Unit = {},
    viewModel: AddProductViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    BackHandler { onBackClick() }

    // Lắng nghe danh mục động từ Backend đẩy sang
    val categories by viewModel.categories.collectAsState()

    // State Form
    var isCategoryExpanded by remember { mutableStateOf(false) } // Thêm state đóng/mở menu
    var productName by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var categoryName by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var importPrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var stockQuantity by remember { mutableStateOf("0") }
    var minStockLevel by remember { mutableStateOf("10") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher chọn ảnh từ thư viện
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // Validation form
    val isFormValid by remember {
        derivedStateOf {
            productName.isNotBlank() &&
                    selectedCategoryId != null &&
                    sku.isNotBlank() &&
                    sellingPrice.isNotBlank() &&
                    selectedImageUri != null
        }
    }

    // Quan sát kết quả từ ViewModel
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AddProductUiState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                onSaveSuccess()
            }
            is AddProductUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    val primaryBlue = Color(0xFF2563EB)
    val bgLightGray = Color(0xFFF8F9FA)
    val textDark = Color(0xFF1E293B)
    val textGray = Color(0xFF475569)

    val isLoading = uiState is AddProductUiState.Loading

    Scaffold(
        bottomBar = {
            Surface(color = Color.White, shadowElevation = 8.dp) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Button(
                        onClick = {
                            selectedImageUri?.let { uri ->
                                viewModel.createProduct(
                                    context = context,
                                    productName = productName,
                                    categoryId = selectedCategoryId ?: 1,
                                    sku = sku,
                                    sellingPrice = sellingPrice,
                                    stockQuantity = stockQuantity,
                                    minStockLevel = minStockLevel,
                                    barcode = barcode,
                                    unit = unit,
                                    importPrice = importPrice,
                                    imageUri = uri
                                )
                            }
                        },
                        enabled = isFormValid && !isLoading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryBlue,
                            disabledContainerColor = Color(0xFF94A3B8)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                        } else {
                            Text("LƯU SẢN PHẨM", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgLightGray)
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // 1. TOP BAR
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).background(Color(0xFFF1F5F9), shape = CircleShape).clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painter = painterResource(id = R.drawable.back), contentDescription = "Back", tint = textDark, modifier = Modifier.size(30.dp))
                }
                Text("Thêm sản phẩm mới", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textDark)
                Box(
                    modifier = Modifier.size(40.dp).background(if (isFormValid) primaryBlue else Color(0xFF94A3B8), shape = CircleShape)
                        .clickable {
                            if (isFormValid && !isLoading && selectedImageUri != null) {
                                viewModel.createProduct(
                                    context = context,
                                    productName = productName,
                                    categoryId = selectedCategoryId ?: 1,
                                    sku = sku,
                                    sellingPrice = sellingPrice,
                                    stockQuantity = stockQuantity,
                                    minStockLevel = minStockLevel,
                                    barcode = barcode,
                                    unit = unit,
                                    importPrice = importPrice,
                                    imageUri = selectedImageUri!!
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Lưu", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. KHỐI TẢI ẢNH SẢN PHẨM
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(Color(0xFFF3F4F6), shape = RoundedCornerShape(12.dp))
                            .dashedBorder(Color(0xFFCBD5E1), radius = 12.dp)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected Image",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(painter = painterResource(id = R.drawable.camera), contentDescription = "Add Photo", tint = Color(0xFF64748B), modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Thêm ảnh *", fontSize = 13.sp, color = textGray, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. KHỐI THÔNG TIN CHUNG
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("THÔNG TIN CHUNG", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textDark)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Tên sản phẩm *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textDark)
                    Spacer(modifier = Modifier.height(6.dp))
                    CustomInputField(value = productName, onValueChange = { productName = it }, placeholder = "Nhập tên sản phẩm...")

                    Spacer(modifier = Modifier.height(12.dp))

                    // Danh mục & Đơn vị tính (Hàng ngang)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = isCategoryExpanded,
                            onExpandedChange = { isCategoryExpanded = !isCategoryExpanded },
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Column {
                                Text("Danh mục *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textDark)
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = categoryName,
                                    onValueChange = {},
                                    readOnly = true,
                                    placeholder = { Text("Chọn danh mục...", fontSize = 13.sp, color = textGray) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded) },
                                    modifier = Modifier.menuAnchor(
                                        type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                        enabled = true
                                    ).fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedContainerColor = Color(0xFFF3F4F6),
                                        focusedContainerColor = Color.White,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedBorderColor = primaryBlue,
                                        focusedTextColor = Color(0xFF0F172A),
                                        unfocusedTextColor = Color(0xFF0F172A)
                                    )
                                )

                                ExposedDropdownMenu(
                                    expanded = isCategoryExpanded,
                                    onDismissRequest = { isCategoryExpanded = false }
                                ) {
                                    categories.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text(category.categoryName, fontSize = 13.sp, color = textDark) },
                                            onClick = {
                                                selectedCategoryId = category.categoryId
                                                categoryName = category.categoryName
                                                isCategoryExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(0.7f)) {
                            Text("Đơn vị tính", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textDark)
                            Spacer(modifier = Modifier.height(6.dp))
                            CustomInputField(
                                value = unit,
                                onValueChange = { unit = it },
                                placeholder = "Chai, kg..."
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mã hàng (SKU) & Mã vạch (Barcode)
                    Text("Mã hàng (SKU) *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textDark)
                    Spacer(modifier = Modifier.height(6.dp))
                    CustomInputField(
                        value = sku,
                        onValueChange = { sku = it },
                        placeholder = "Nhập mã hoặc quét tự động...",
                        trailingIcon = {
                            Box(
                                modifier = Modifier.size(36.dp).background(Color(0xFFF1F5F9), shape = RoundedCornerShape(8.dp)).clickable { },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(painter = painterResource(id = R.drawable.barcode), contentDescription = "Scan SKU", modifier = Modifier.size(18.dp), tint = textDark)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Giá nhập & Giá bán (Hàng ngang)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Giá nhập", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textDark)
                            Spacer(modifier = Modifier.height(6.dp))
                            CustomInputField(
                                value = importPrice,
                                onValueChange = { importPrice = it },
                                placeholder = "0",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                trailingIcon = { Text("đ", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textDark) }
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Giá bán *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textDark)
                            Spacer(modifier = Modifier.height(6.dp))
                            CustomInputField(
                                value = sellingPrice,
                                onValueChange = { sellingPrice = it },
                                placeholder = "0",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                trailingIcon = { Text("đ", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textDark) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. KHỐI QUẢN LÝ TỒN KHO
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("QUẢN LÝ TỒN KHO", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textDark)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tồn kho ban đầu", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textDark)
                            Spacer(modifier = Modifier.height(6.dp))
                            CustomInputField(value = stockQuantity, onValueChange = { stockQuantity = it }, placeholder = "0", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tồn kho tối thiểu", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textDark)
                            Spacer(modifier = Modifier.height(6.dp))
                            CustomInputField(value = minStockLevel, onValueChange = { minStockLevel = it }, placeholder = "10", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// Component con: Input Field dùng chung
@Composable
fun CustomInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, fontSize = 13.sp, color = Color(0xFF475569)) },
        trailingIcon = trailingIcon,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        singleLine = true,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFF3F4F6),
            focusedContainerColor = Color.White,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color(0xFF2563EB),
            focusedTextColor = Color(0xFF0F172A),
            unfocusedTextColor = Color(0xFF0F172A)
        )
    )
}

// Extension viền nét đứt (Dashed Border)
fun Modifier.dashedBorder(color: Color, radius: Dp) = this.drawWithContent {
    drawContent()
    val strokeWidth = 2.dp.toPx()
    val dashWidth = 6.dp.toPx()
    val gapWidth = 4.dp.toPx()
    val cornerRadius = radius.toPx()

    drawRoundRect(
        color = color,
        style = Stroke(
            width = strokeWidth,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashWidth, gapWidth), 0f)
        ),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )
}