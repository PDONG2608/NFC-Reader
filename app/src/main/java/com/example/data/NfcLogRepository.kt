package com.example.data

import kotlinx.coroutines.flow.Flow

class NfcLogRepository(private val nfcLogDao: NfcLogDao) {
    val allLogs: Flow<List<NfcLog>> = nfcLogDao.getAllLogs()

    suspend fun insertLog(log: NfcLog) {
        nfcLogDao.insertLog(log)
    }

    suspend fun deleteLogById(id: Int) {
        nfcLogDao.deleteLogById(id)
    }

    suspend fun clearAllLogs() {
        nfcLogDao.clearAllLogs()
    }
}
