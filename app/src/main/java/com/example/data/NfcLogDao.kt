package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NfcLogDao {
    @Query("SELECT * FROM nfc_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<NfcLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: NfcLog)

    @Query("DELETE FROM nfc_logs WHERE id = :id")
    suspend fun deleteLogById(id: Int)

    @Query("DELETE FROM nfc_logs")
    suspend fun clearAllLogs()
}
