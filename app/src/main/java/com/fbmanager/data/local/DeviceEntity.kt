package com.fbmanager.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fbmanager.domain.model.DeviceNode

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val id: String,
    val nombre: String?,
    val model: String?,
    val allowed: Boolean,
    val blocked: Boolean,
    val offlineMode: Boolean,
    val paymentExpiry: Long?,
    val price: Double,
    val firstSeen: Long,
    val lastConnected: Long?,
    val tosVersion: String?,
    val tosAcceptedAt: Long?,
    val appVersion: String?,
    /** Favourites + listening stats for this device, stored as a JSON blob. Null when the device has none. */
    val extrasJson: String? = null,
) {
    fun toDomain() = DeviceNode(
        id = id,
        nombre = nombre,
        model = model,
        allowed = allowed,
        blocked = blocked,
        offlineMode = offlineMode,
        paymentExpiry = paymentExpiry,
        price = price,
        firstSeen = firstSeen,
        lastConnected = lastConnected,
        tosVersion = tosVersion,
        tosAcceptedAt = tosAcceptedAt,
        appVersion = appVersion,
    )
}
