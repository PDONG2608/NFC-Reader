package com.example.ui

import android.app.Application
import android.nfc.NfcAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.NfcLog
import com.example.data.NfcLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

enum class NfcHardwareState {
    NOT_SUPPORTED,
    DISABLED,
    ENABLED
}

class NfcLogViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val repository = NfcLogRepository(database.nfcLogDao())
    
    val allLogs: StateFlow<List<NfcLog>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val _nfcState = MutableStateFlow(NfcHardwareState.NOT_SUPPORTED)
    val nfcState: StateFlow<NfcHardwareState> = _nfcState.asStateFlow()

    init {
        checkNfcHardware()
    }
    
    fun checkNfcHardware() {
        val adapter = NfcAdapter.getDefaultAdapter(getApplication())
        _nfcState.value = when {
            adapter == null -> NfcHardwareState.NOT_SUPPORTED
            !adapter.isEnabled -> NfcHardwareState.DISABLED
            else -> NfcHardwareState.ENABLED
        }
    }
    
    fun insertScanLog(tagId: String, techList: String, payload: String, tagType: String, isSimulated: Boolean = false) {
        viewModelScope.launch {
            val log = NfcLog(
                tagId = tagId,
                timestamp = System.currentTimeMillis(),
                techList = techList,
                payload = payload,
                tagType = tagType,
                isSimulated = isSimulated
            )
            repository.insertLog(log)
        }
    }
    
    fun deleteLog(id: Int) {
        viewModelScope.launch {
            repository.deleteLogById(id)
        }
    }
    
    fun clearLogs() {
        viewModelScope.launch {
            repository.clearAllLogs()
        }
    }
    
    fun simulateRandomNfcScan() {
        val simulatedCards = listOf(
            Triple(
                "04:E8:A1:3C:9B:6D:80",
                "NfcA, MifareClassic, NdefFormatable",
                "Employee ID: EMP-103982\nPhòng Ban: An Ninh Thông Tin\nQuyền hạn: Cấp 4 (VIP)\nHọ và Tên: Nguyễn Văn Hùng"
            ) to "Mifare Classic 1K",
            Triple(
                "08:35:C3:FA:E6:B8",
                "NfcB, IsoDep, Ndef",
                "Mã vé xe điện: TX-88319\nSố dư ví: 56,000 VND\nGa xuất phát: Cát Linh - Hà Nội\nHạn dùng: 31/12/2026"
            ) to "ISO 14443-4",
            Triple(
                "E1:04:12:DF:D3:A2",
                "Ndef, NfcV, NfcA",
                "Sự kiện: Google Developer Festival 2026\nĐịa điểm: Trung tâm Hội nghị Quốc gia\nTrạng thái: Đã Check-in thành công\nThời gian: 22-05-2026"
            ) to "NFC Forum Type 2",
            Triple(
                "04:A2:CC:8F:B1:05:7E",
                "NfcF, Ndef",
                "Website: https://ai.studio/build\nTiêu đề: Google AI Studio Build Tool\nMô tả: Click chuột từ ứng dụng để mở liên kết trong trình duyệt"
            ) to "NFC NDEF Tag",
            Triple(
                "B4:9C:1D:C7",
                "IsoDep, NfcA",
                "Loại thẻ: Contactless Visa Card\nNhà phát hành: Vietcombank\nSố thẻ: 4000 12xx xxxx 9012\nTần số: 13.56 MHz (An toàn bảo mật)"
            ) to "EMV Payment Card",
            Triple(
                "37:5F:8A:23",
                "NdefFormatable",
                "Tag rỗng / Chưa được định dạng\nThông tin: Thẻ RFID trắng tần số 13.56MHz dùng để ghi dữ liệu"
            ) to "Generic RFID Tag"
        )
        
        val chosen = simulatedCards[Random.nextInt(simulatedCards.size)]
        val (details, type) = chosen
        val (tagId, techList, payload) = details
        
        insertScanLog(
            tagId = tagId,
            techList = techList,
            payload = payload,
            tagType = type,
            isSimulated = true
        )
    }
}
