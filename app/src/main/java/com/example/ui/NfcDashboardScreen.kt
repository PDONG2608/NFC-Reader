package com.example.ui.theme

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.NfcLog
import com.example.ui.NfcHardwareState
import com.example.ui.NfcLogViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcDashboardScreen(
    viewModel: NfcLogViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    val logs by viewModel.allLogs.collectAsStateWithLifecycle()
    val nfcState by viewModel.nfcState.collectAsStateWithLifecycle()
    
    var showClearDialog by remember { mutableStateOf(false) }
    var expandedLogId by remember { mutableStateOf<Int?>(null) }

    val lastScanText = remember(logs) {
        if (logs.isNotEmpty()) {
            val diffMs = System.currentTimeMillis() - logs.first().timestamp
            val diffSecs = diffMs / 1000
            val diffMins = diffSecs / 60
            when {
                diffSecs < 10 -> "Vừa xong"
                diffSecs < 60 -> "$diffSecs giây trước"
                diffMins < 60 -> "$diffMins phút trước"
                else -> {
                    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                    formatter.format(Date(logs.first().timestamp))
                }
            }
        } else {
            "Trực tuyến"
        }
    }
    
    Scaffold(
        bottomBar = {
            // Elegant Dark bottom navigation block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2B2930))
                    .border(0.5.dp, Color(0xFF49454F), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logs icon
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { }
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Logs",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    // Stats icon
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { 
                            Toast.makeText(context, "Thống kê chi tiết đang hoạt động tự động trong Log!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFCAC4D0).copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Stats",
                            fontSize = 11.sp,
                            color = Color(0xFFCAC4D0).copy(alpha = 0.6f)
                        )
                    }
                    // Writer icon
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { 
                            Toast.makeText(context, "Tính năng Ghi Thẻ NFC sẽ xuất hiện trong bản cập nhật tới!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color(0xFFCAC4D0).copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Writer",
                            fontSize = 11.sp,
                            color = Color(0xFFCAC4D0).copy(alpha = 0.6f)
                        )
                    }
                }
                
                // System gesture navigation bar simulator height fallback
                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            
            // ELEGANT DARK HEADER ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF49454F)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh, // NFC style antenna representation
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "NFC Log",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "BACKGROUND SCANNING ACTIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = Color(0xFF938F99)
                        )
                    }
                }
                Row {
                    IconButton(
                        onClick = { viewModel.checkNfcHardware() },
                        modifier = Modifier.testTag("refresh_hardware_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Kiểm tra phần cứng",
                            tint = Color(0xFFCAC4D0)
                        )
                    }
                    if (logs.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearDialog = true },
                            modifier = Modifier.testTag("clear_all_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Xóa tất cả nhật ký",
                                tint = Color(0xFFF2B8B5)
                            )
                        }
                    }
                }
            }

            // ELEGANT STATS CARD
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2B2930)
                ),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Scans Today",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${logs.size}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "LAST SCAN",
                            color = Color(0xFF938F99),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = lastScanText,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            // NFC STATUS HARDWARE ACCORDION/CARD
            NfcStatusWidget(nfcState = nfcState, onOpenSettings = {
                try {
                    val intent = Intent(Settings.ACTION_NFC_SETTINGS)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    try {
                        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                        context.startActivity(intent)
                    } catch (ex: Exception) {
                        Toast.makeText(context, "Không thể mở cài đặt NFC lúc này", Toast.LENGTH_SHORT).show()
                    }
                }
            })
            
            Spacer(modifier = Modifier.height(14.dp))
            
            // SIMULATE TRIGGER BUTTON
            Button(
                onClick = { 
                    viewModel.simulateRandomNfcScan()
                    Toast.makeText(context, "Giả lập quẹt thẻ thành công!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("simulate_scan_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Giả Lập Quẹt Thẻ NFC",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            
            Spacer(modifier = Modifier.height(18.dp))
            
            // RECENT EVENTS SECTION HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT EVENTS (${logs.size})",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    color = Color(0xFFCAC4D0)
                )
                
                if (logs.isNotEmpty()) {
                    Text(
                        text = "Chọn dòng để xem chi tiết",
                        fontSize = 11.sp,
                        color = Color(0xFF938F99),
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // LAZY COLUMN LOG ENTRIES
            if (logs.isEmpty()) {
                EmptyStateView(
                    onSimulateClick = {
                        viewModel.simulateRandomNfcScan()
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(logs, key = { it.id }) { log ->
                        val isExpanded = expandedLogId == log.id
                        NfcLogItem(
                            log = log,
                            isExpanded = isExpanded,
                            onToggleExpand = {
                                expandedLogId = if (isExpanded) null else log.id
                            },
                            onDelete = {
                                if (isExpanded) expandedLogId = null
                                viewModel.deleteLog(log.id)
                            },
                            onCopy = { text ->
                                clipboardManager.setText(AnnotatedString(text))
                                Toast.makeText(context, "Đã sao chép vào bộ nhớ tạm", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
    
    // CONFIRM DIALOG FOR CLEAR ALL
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(
                    text = "Xóa toàn bộ dữ liệu?",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Hành động này sẽ xóa vĩnh viễn tất cả nhật ký quẹt thẻ nfc hiện tại. Bạn không thể khôi phục lại dữ liệu này.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearLogs()
                        showClearDialog = false
                    },
                    modifier = Modifier.testTag("confirm_clear_button")
                ) {
                    Text("Xác Nhận Xóa", color = ErrorRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Hủy", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun NfcStatusWidget(
    nfcState: NfcHardwareState,
    onOpenSettings: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = when (nfcState) {
                NfcHardwareState.ENABLED -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Glowing pulses indicator or static error based on hardware condition
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                if (nfcState == NfcHardwareState.ENABLED) {
                    NfcRadarAnimation(modifier = Modifier.fillMaxSize())
                } else {
                    Icon(
                        imageVector = if (nfcState == NfcHardwareState.DISABLED) Icons.Default.Warning else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (nfcState == NfcHardwareState.DISABLED) SimulateOrange else ErrorRed,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = when (nfcState) {
                        NfcHardwareState.ENABLED -> "ĂNG TEN NFC HOẠT ĐỘNG"
                        NfcHardwareState.DISABLED -> "NFC ĐANG TẮT"
                        NfcHardwareState.NOT_SUPPORTED -> "KHÔNG HỖ TRỢ NFC"
                    },
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = when (nfcState) {
                        NfcHardwareState.ENABLED -> ScanActiveGreen
                        else -> SimulateOrange
                    }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = when (nfcState) {
                        NfcHardwareState.ENABLED -> "Thiết bị đang lắng nghe tín hiệu ở tần số 13.56 MHz. Hãy chạm thẻ RF/NFC để ghi nhật ký."
                        NfcHardwareState.DISABLED -> "Cần bật tính năng NFC trong cài đặt hệ thống để có thể quét thẻ thực tế."
                        NfcHardwareState.NOT_SUPPORTED -> "Điện thoại này không trang bị mô-đun phần cứng NFC. Bạn hãy sử dụng tính năng giả lập."
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
                
                if (nfcState == NfcHardwareState.DISABLED) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onOpenSettings,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SimulateOrange,
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("enable_nfc_settings_button")
                    ) {
                        Text(text = "Mở Cài Đặt NFC", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(
    onSimulateClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "TRẠM CHUYỂN LOG TRỐNG",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Chưa nhận thấy bất kỳ hoạt động quét NFC nào gần đây từ thiết bị phần cứng.",
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = onSimulateClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("empty_simulate_button")
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Chạy quẹt thử thẻ giả lập ngay",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun NfcLogItem(
    log: NfcLog,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onDelete: () -> Unit,
    onCopy: (String) -> Unit
) {
    val dateStr = remember(log.timestamp) {
        val format = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
        format.format(Date(log.timestamp))
    }
    
    val tagIcon = when {
        log.tagType.contains("Mifare", ignoreCase = true) -> Icons.Default.Home
        log.tagType.contains("Payment", ignoreCase = true) || log.tagType.contains("EMV", ignoreCase = true) -> Icons.Default.Check
        log.tagType.contains("Link", ignoreCase = true) || log.tagType.contains("URL", ignoreCase = true) -> Icons.Default.Share
        else -> Icons.Default.Info
    }

    val iconColor = when {
        log.isSimulated -> SimulateOrange
        else -> CyanPrimary
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isExpanded) iconColor else ElegantBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleExpand)
            .animateContentSize()
            .testTag("nfc_log_item_${log.id}")
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            // Elegant left vertical colored accent bar representing 'border-l-4'
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(iconColor)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header symbol indicator
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tagIcon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = log.tagType,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        // Origin label badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(iconColor.copy(alpha = 0.15f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (log.isSimulated) "GIẢ LẬP" else "THIẾT BỊ",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = iconColor
                            )
                        }
                    }
                    
                    Text(
                        text = "UID: ${log.tagId}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column(
                    modifier = Modifier,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = dateStr.substringBefore(" "),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dateStr.substringAfter(" "),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Expanded content section showing parsed technology specifications and log text details
            if (isExpanded) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))
                
                Text(
                    text = "SPECIFICATION & PROTOCOLS",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = log.techList,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
                
                Spacer(modifier = Modifier.height(14.dp))
                
                Text(
                    text = "DECODED PAYLOAD DATA",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Beautiful retro terminal body console styling
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = log.payload,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Copy & Delete operational triggers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onCopy("Mã thẻ UID: ${log.tagId}\nLoại thẻ: ${log.tagType}\nCác công nghệ: ${log.techList}\nDữ liệu giải mã:\n${log.payload}") },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Sao chép Log", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ErrorRed
                        ),
                        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(38.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Xóa", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
}

@Composable
fun NfcRadarAnimation(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "terminal_radar")
    val scale1 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_scale_1"
    )
    val opacity1 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_opacity_1"
    )
    
    val scale2 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing, delayMillis = 1100),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_scale_2"
    )
    val opacity2 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing, delayMillis = 1100),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_opacity_2"
    )

    val waveColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 5f
        
        // Dynamic pulse 1
        drawCircle(
            color = waveColor.copy(alpha = opacity1),
            radius = radius * scale1,
            style = Stroke(width = 2.dp.toPx())
        )
        
        // Dynamic pulse 2
        drawCircle(
            color = waveColor.copy(alpha = opacity2),
            radius = radius * scale2,
            style = Stroke(width = 2.dp.toPx())
        )
        
        // Core static glowing signal point
        drawCircle(
            color = waveColor,
            radius = radius * 0.7f
        )
    }
}
