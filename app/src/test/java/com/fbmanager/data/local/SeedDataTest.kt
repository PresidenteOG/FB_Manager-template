package com.fbmanager.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeedDataTest {

    private val now = 1_800_000_000_000L
    private val devices = SeedData.build(now)

    @Test
    fun `seed has between 15 and 25 devices`() {
        assertTrue("got ${devices.size}", devices.size in 15..25)
    }

    @Test
    fun `device ids are unique`() {
        assertEquals(devices.size, devices.map { it.id }.toSet().size)
    }

    @Test
    fun `seed covers allowed, blocked and pending states`() {
        assertTrue(devices.any { it.allowed && !it.blocked })
        assertTrue(devices.any { it.blocked })
        assertTrue(devices.any { !it.allowed && !it.blocked })
    }

    @Test
    fun `seed covers active, overdue and unconfigured payment`() {
        assertTrue(devices.any { it.paymentExpiry != null && it.paymentExpiry!! > now })
        assertTrue(devices.any { it.paymentExpiry != null && it.paymentExpiry!! < now })
        assertTrue(devices.any { it.paymentExpiry == null })
    }

    @Test
    fun `extras blobs carry stats and favourites`() {
        val blobs = devices.mapNotNull { it.extrasJson }
        assertTrue(blobs.isNotEmpty())
        blobs.forEach { raw ->
            assertTrue(raw.contains("\"stats\""))
            assertTrue(raw.contains("\"favorites\""))
            assertTrue(raw.contains("\"totalListenedMs\""))
        }
    }
}
