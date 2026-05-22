package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nfc_logs")
data class NfcLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tagId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val techList: String,
    val payload: String,
    val tagType: String, // e.g., "Mifare Classic", "NDEF Text", "NDEF URL", "ISO 14443-4"
    val isSimulated: Boolean = false
)
