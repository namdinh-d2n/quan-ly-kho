package appmobile.example.quan_ly_kho.ui.features.product


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class BarcodeScannerUiState(
    val isScanning: Boolean = true,
    val isFlashOn: Boolean = false,
    val scannedCode: String? = null,
    val errorMessage: String? = null
)

class BarcodeScannerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BarcodeScannerUiState())
    val uiState: StateFlow<BarcodeScannerUiState> = _uiState.asStateFlow()

    fun onBarcodeDetected(code: String) {
        if (!_uiState.value.isScanning) return // Tránh quét trùng liên tục
        _uiState.update {
            it.copy(
                isScanning = false,
                scannedCode = code
            )
        }
    }

    fun toggleFlash() {
        _uiState.update { it.copy(isFlashOn = !it.isFlashOn) }
    }

    fun resetScanner() {
        _uiState.update {
            it.copy(
                isScanning = true,
                scannedCode = null,
                errorMessage = null
            )
        }
    }
}