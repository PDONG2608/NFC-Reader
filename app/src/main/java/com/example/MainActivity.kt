package com.example

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.ui.NfcLogViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NfcDashboardScreen

class MainActivity : ComponentActivity() {

  private var nfcAdapter: NfcAdapter? = null
  private val viewModel: NfcLogViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    nfcAdapter = NfcAdapter.getDefaultAdapter(this)
    
    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          NfcDashboardScreen(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }

    // Process any intent that launched the activity with NFC data
    intent?.let { handleNfcIntent(it) }
  }

  override fun onResume() {
    super.onResume()
    viewModel.checkNfcHardware()
    setupForegroundDispatch()
  }

  override fun onPause() {
    super.onPause()
    disableForegroundDispatch()
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleNfcIntent(intent)
  }

  private fun setupForegroundDispatch() {
    val adapter = nfcAdapter ?: return
    if (!adapter.isEnabled) return

    try {
      val intent = Intent(this, javaClass).apply {
        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
      }
      
      val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
      } else {
        PendingIntent.FLAG_UPDATE_CURRENT
      }
      
      val pendingIntent = PendingIntent.getActivity(this, 0, intent, flags)

      val intentFilters = arrayOf(
        IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
          addCategory(Intent.CATEGORY_DEFAULT)
          try {
            addDataType("*/*")
          } catch (e: IntentFilter.MalformedMimeTypeException) {
            // fallback
          }
        },
        IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
        IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
      )

      adapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun disableForegroundDispatch() {
    try {
      nfcAdapter?.disableForegroundDispatch(this)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun handleNfcIntent(intent: Intent) {
    val action = intent.action
    if (NfcAdapter.ACTION_NDEF_DISCOVERED == action ||
        NfcAdapter.ACTION_TECH_DISCOVERED == action ||
        NfcAdapter.ACTION_TAG_DISCOVERED == action
    ) {
      val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
      } else {
        @Suppress("DEPRECATION")
        intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
      }

      if (tag != null) {
        val tagIdBytes = tag.id
        val tagIdHex = tagIdBytes?.joinToString(":") { "%02X".format(it) } ?: "UNKNOWN_UID"

        // Map technologies
        val techList = tag.techList.map { it.substringAfterLast(".") }.joinToString(", ")

        // Detect friendly tag type
        val resolvedTagType = when {
          tag.techList.contains("android.nfc.tech.MifareClassic") -> "Mifare Classic"
          tag.techList.contains("android.nfc.tech.MifareUltralight") -> "Mifare Ultralight"
          tag.techList.contains("android.nfc.tech.Ndef") -> "NFC NDEF Tag"
          tag.techList.contains("android.nfc.tech.IsoDep") -> "ISO 14443-4"
          else -> "Generic RFID Tag"
        }

        // Parse NDEF records if available
        var payloadText = ""
        val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        
        if (rawMessages != null) {
          try {
            val messages = rawMessages.map { it as NdefMessage }
            for (message in messages) {
              for (record in message.records) {
                val payload = record.payload
                val payloadStr = try {
                  val type = String(record.type, Charsets.US_ASCII)
                  if (type == "T") { // NDEF Text record handling
                    val langCodeLen = (payload[0].toInt() and 0x3F)
                    String(payload, 1 + langCodeLen, payload.size - 1 - langCodeLen, Charsets.UTF_8)
                  } else if (type == "U") { // NDEF URI record handling
                    val prefixCode = payload[0].toInt()
                    val prefix = when (prefixCode) {
                      1 -> "http://www."
                      2 -> "https://www."
                      3 -> "http://"
                      4 -> "https://"
                      else -> ""
                    }
                    prefix + String(payload, 1, payload.size - 1, Charsets.UTF_8)
                  } else {
                    String(payload, Charsets.UTF_8)
                  }
                } catch (e: Exception) {
                  "Hex representation: " + payload.joinToString(" ") { "%02X".format(it) }
                }
                payloadText += "$payloadStr\n"
              }
            }
          } catch (e: Exception) {
            payloadText = "Lỗi phân giải bản ghi NDEF: ${e.localizedMessage}"
          }
        }

        if (payloadText.isBlank()) {
          val commonUsage = when {
            tag.techList.contains("android.nfc.tech.MifareClassic") -> {
              "Thẻ MIFARE Classic (1K/4K/Mini). Thường dùng làm thẻ từ cư dân chung cư, thẻ gửi xe thông minh, khóa cửa phòng kỹ thuật (Smart Lock), thẻ thang máy, hoặc thẻ tích điểm thành viên."
            }
            tag.techList.contains("android.nfc.tech.MifareUltralight") -> {
              "Thẻ siêu nhẹ MIFARE Ultralight. Phổ biến nhất trong vé di chuyển công cộng từ tính dùng 1 lần (vé tàu điện xe buýt), vé vào cổng sự kiện lễ hội lớn, hoặc tem nhãn dán dán trên hàng hóa thông minh."
            }
            tag.techList.contains("android.nfc.tech.IsoDep") && tag.techList.contains("android.nfc.tech.NfcA") -> {
              "Chuẩn thẻ thông minh cao cấp ISO/IEC 14443-4 Loại A (ví dụ: MIFARE DESFire). Đây là thẻ chip mã hóa bảo mật tối đa, thường dùng làm thẻ ngân hàng thanh toán một chạm (Contactless Visa/Mastercard/Napass), Căn cước công dân gắn chíp (CCCD), Vé điện tử Metro đường sắt đô thị tốc độ cao."
            }
            tag.techList.contains("android.nfc.tech.IsoDep") && tag.techList.contains("android.nfc.tech.NfcB") -> {
              "Chuẩn thẻ thông minh công nghiệp ISO/IEC 14443-4 Loại B. Thường dùng trong hệ thống Căn cước quốc gia ở một số nước, Hộ chiếu điện tử sinh trắc học (E-Passport), hoặc hệ thống thanh toán giao thông liên kết Châu Âu (chuẩn Calypso)."
            }
            tag.techList.contains("android.nfc.tech.NfcV") -> {
              "Chuẩn ISO 15693 (NfcV / Vicinity Card). Ưu thế quét tầm xa cực tốt (có thể quét tới 1 mét với đầu đọc ăng-ten lớn). Thường dùng làm thẻ thư viện tự động, tem dán quản lý sách, nhãn dán dập nổi theo dõi dòng đời sản phẩm và logistic kho bãi."
            }
            tag.techList.contains("android.nfc.tech.NfcF") -> {
              "Chuẩn Sony FeliCa (NfcF). Rất phổ biến tại Nhật Bản và Hồng Kông. Được dùng làm vé tàu điện ngầm siêu nhanh (Thẻ Pasmo, Suica, Octopus), hoặc ví điện tử thanh toán không tiếp xúc nội địa Nhật Bản tích hợp trên smartphone."
            }
            tag.techList.contains("android.nfc.tech.NdefFormatable") -> {
              "Thẻ chip trắng mới xuất xưởng (NdefFormatable). Chưa được phân vùng dữ liệu để lưu trữ văn bản. Bạn có thể sử dụng các app ghi thẻ để tiến hành định dạng NDEF và lưu trữ link Website, thông tin mạng Wi-Fi, hoặc Danh bạ liên hệ."
            }
            tag.techList.contains("android.nfc.tech.NfcA") -> {
              "Chip tiêu chuẩn NFC Forum Type 2 / NTAG Series (ví dụ: NTAG213, NTAG215 dùng làm tượng Amiibo game, NTAG216). Chuyên dùng để tự động hóa kích hoạt phím tắt (automation) trên smartphone, bảng quảng cáo thông minh Smart Poster."
            }
            else -> {
              "Thẻ RFID/NFC tần số cao 13.56 MHz cơ bản. Thường dùng cho các giải pháp kiểm soát ra vào cơ quan, chấm công nhân viên, hoặc mã khóa số định danh duy nhất (UID) trong quản trị nội bộ."
            }
          }

          payloadText = """
            [PHÂN TÍCH CHIP NFC & THẺ BẢO MẬT]
            
            Thẻ này được cài chế độ bảo mật hoặc chưa được định dạng nội dung văn bản chuẩn (NDEF). Ứng dụng đã xử lý và phân tách được cấu trúc kỹ thuật dưới đây:
            
            MÃ ĐỊNH DANH DUY NHẤT (UID):
            👉 $tagIdHex
            
            ỨNG DỤNG THỰC TẾ TIÊU BIỂU:
            • $commonUsage
            
            CHI TIẾT KỸ THUẬT:
            • Các lớp công nghệ tích hợp: $techList
            • Tần số sóng mang: 13.56 MHz (High Frequency - HF)
            • Khả năng mã hóa: Cao (Đọc mã bảo vệ phần cứng nguyên bản)
          """.trimIndent()
        }

        viewModel.insertScanLog(
          tagId = tagIdHex,
          techList = techList,
          payload = payloadText.trim(),
          tagType = resolvedTagType,
          isSimulated = false
        )
      }
    }
  }
}

