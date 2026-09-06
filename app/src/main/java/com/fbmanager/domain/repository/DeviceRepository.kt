package com.fbmanager.domain.repository

import com.fbmanager.domain.model.DeviceNode
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun observeDevices(): Flow<List<DeviceNode>>
    suspend fun updateDevice(deviceId: String, updates: Map<String, Any?>)
    suspend fun deleteDevice(deviceId: String)
}
