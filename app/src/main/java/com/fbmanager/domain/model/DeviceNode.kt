package com.fbmanager.domain.model

data class DeviceNode(
    val id: String,
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
    /** Versión de SoundWave instalada en el dispositivo (ej. "2.0"). Puede ser null en dispositivos anteriores. */
    val appVersion: String?,
) {
    val paymentStatus: PaymentStatus
        get() {
            val expiry = paymentExpiry ?: return PaymentStatus.NotConfigured
            val now = System.currentTimeMillis()
            return when {
                expiry > now -> {
                    val daysLeft = (expiry - now) / 86_400_000L
                    if (daysLeft <= 7) PaymentStatus.ExpiringSoon else PaymentStatus.Active
                }
                else -> {
                    val daysOverdue = (now - expiry) / 86_400_000L
                    if (daysOverdue > 7) PaymentStatus.Overdue else PaymentStatus.ExpiringSoon
                }
            }
        }

    val displayName: String
        get() = nombre?.takeIf { it.isNotBlank() } ?: model ?: id
}

enum class PaymentStatus { Active, ExpiringSoon, Overdue, NotConfigured }
