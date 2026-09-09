package com.fbmanager.domain.repository

import com.fbmanager.domain.model.DeviceNode
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun observeDevices(): Flow<List<DeviceNode>>
    suspend fun updateDevice(deviceId: String, updates: Map<String, Any?>)
    suspend fun deleteDevice(deviceId: String)

    /** Raw JSON blob with the device's favourites and listening stats, or null if it has none. */
    suspend fun getExtras(deviceId: String): String?
}
