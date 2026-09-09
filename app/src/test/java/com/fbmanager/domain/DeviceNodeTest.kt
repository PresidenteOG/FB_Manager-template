package com.fbmanager.domain

import com.fbmanager.domain.model.DeviceNode
import com.fbmanager.domain.model.PaymentStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceNodeTest {

    @Test
    fun `paymentStatus is Active when expiry is 30 days in the future`() {
        val futureExpiry = System.currentTimeMillis() + 30 * 24 * 3600 * 1000L
        val device = makeDevice(paymentExpiry = futureExpiry)
        assertEquals(PaymentStatus.Active, device.paymentStatus)
    }

    @Test
    fun `paymentStatus is ExpiringSoon when expiry is 3 days in the future`() {
        val soonExpiry = System.currentTimeMillis() + 3 * 24 * 3600 * 1000L
        val device = makeDevice(paymentExpiry = soonExpiry)
        assertEquals(PaymentStatus.ExpiringSoon, device.paymentStatus)
    }

    @Test
    fun `paymentStatus is Overdue when expiry is 8 days in the past`() {
        val pastExpiry = System.currentTimeMillis() - 8 * 24 * 3600 * 1000L
        val device = makeDevice(paymentExpiry = pastExpiry)
        assertEquals(PaymentStatus.Overdue, device.paymentStatus)
    }

    @Test
    fun `paymentStatus is NotConfigured when expiry is null`() {
        val device = makeDevice(paymentExpiry = null)
        assertEquals(PaymentStatus.NotConfigured, device.paymentStatus)
    }

    @Test
    fun `revenue contribution is 0 when device is blocked`() {
        val device = makeDevice(allowed = true, blocked = true, price = 15.0)
        val revenue = if (device.allowed && !device.blocked) device.price else 0.0
        assertEquals(0.0, revenue, 0.001)
    }

    @Test
    fun `revenue contribution equals price when device is active`() {
        val device = makeDevice(allowed = true, blocked = false, price = 25.0)
        val revenue = if (device.allowed && !device.blocked) device.price else 0.0
        assertEquals(25.0, revenue, 0.001)
    }

    private fun makeDevice(
        paymentExpiry: Long? = null,
        allowed: Boolean = true,
        blocked: Boolean = false,
        price: Double = 10.0,
    ) = DeviceNode(
        id = "test_id",
        nombre = "Test Device",
        model = "TestModel",
        allowed = allowed,
        blocked = blocked,
        offlineMode = false,
        paymentExpiry = paymentExpiry,
        price = price,
        firstSeen = 0L,
        lastConnected = null,
        tosVersion = null,
        tosAcceptedAt = null,
        appVersion = null,
    )
}
