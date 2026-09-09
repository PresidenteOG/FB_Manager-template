package com.fbmanager.data.local

/**
 * Demo fleet used to populate the app on first launch. Every device, name and listening
 * figure here is invented — there is no real customer data in this project.
 *
 * Timestamps are built relative to "now" at insert time so the payment badges (active,
 * expiring soon, overdue) stay meaningful whenever the app is first run.
 */
object SeedData {

    private const val DAY = 24L * 60 * 60 * 1000

    private val musicExtras = """
        {
          "stats": {
            "totalListenedMs": 4830000,
            "totalTracks": 212,
            "topArtists": ["The Paper Lanterns", "Kestrel & Vale", "Ana Ferro", "Low Country Radio", "Marisol Vega"],
            "top10Tracks": [
              {"title": "Nocturne in Blue", "artist": "The Paper Lanterns"},
              {"title": "Slow Tide", "artist": "Kestrel & Vale"},
              {"title": "Cartas sin abrir", "artist": "Ana Ferro"},
              {"title": "Ferrocarril", "artist": "Low Country Radio"}
            ]
          },
          "favorites": [
            {"title": "Nocturne in Blue", "artist": "The Paper Lanterns"},
            {"title": "Cartas sin abrir", "artist": "Ana Ferro"},
            {"title": "Harbour Lights", "artist": "Kestrel & Vale"}
          ],
          "recentlyPlayed": [
            {"title": "Slow Tide", "artist": "Kestrel & Vale"},
            {"title": "Ferrocarril", "artist": "Low Country Radio"},
            {"title": "Media luna", "artist": "Marisol Vega"}
          ]
        }
    """.trimIndent()

    private val lightExtras = """
        {
          "stats": {
            "totalListenedMs": 720000,
            "totalTracks": 34,
            "topArtists": ["Marisol Vega", "Low Country Radio"],
            "top10Tracks": [
              {"title": "Media luna", "artist": "Marisol Vega"}
            ]
          },
          "favorites": [
            {"title": "Media luna", "artist": "Marisol Vega"}
          ],
          "recentlyPlayed": [
            {"title": "Media luna", "artist": "Marisol Vega"},
            {"title": "Harbour Lights", "artist": "Kestrel & Vale"}
          ]
        }
    """.trimIndent()

    fun build(now: Long = System.currentTimeMillis()): List<DeviceEntity> = listOf(
        DeviceEntity("dev_01f3a9", "Laura Méndez", "Pixel 7", allowed = true, blocked = false,
            offlineMode = false, paymentExpiry = now + 24 * DAY, price = 7.50,
            firstSeen = now - 210 * DAY, lastConnected = now - 2 * DAY,
            tosVersion = "2.1", tosAcceptedAt = now - 200 * DAY, appVersion = "2.0", extrasJson = musicExtras),

        DeviceEntity("dev_08b7c2", "Club Vela Poniente", "Galaxy Tab A8", allowed = true, blocked = false,
            offlineMode = true, paymentExpiry = now + 3 * DAY, price = 12.00,
            firstSeen = now - 340 * DAY, lastConnected = now - 1 * DAY,
            tosVersion = "2.1", tosAcceptedAt = now - 330 * DAY, appVersion = "2.0", extrasJson = musicExtras),

        DeviceEntity("dev_1142de", "Sergio Ibáñez", "Redmi Note 12", allowed = true, blocked = false,
            offlineMode = false, paymentExpiry = now - 11 * DAY, price = 6.00,
            firstSeen = now - 95 * DAY, lastConnected = now - 14 * DAY,
            tosVersion = "2.0", tosAcceptedAt = now - 90 * DAY, appVersion = "1.9", extrasJson = lightExtras),

        DeviceEntity("dev_1a5f60", "Recepción Hostal Duna", "Moto G54", allowed = true, blocked = false,
            offlineMode = false, paymentExpiry = now + 46 * DAY, price = 9.00,
            firstSeen = now - 500 * DAY, lastConnected = now - 4 * DAY,
            tosVersion = "2.1", tosAcceptedAt = now - 480 * DAY, appVersion = "2.0", extrasJson = null),

        DeviceEntity("dev_23c081", "Nerea Fuentes", "iPhone 12", allowed = false, blocked = false,
            offlineMode = false, paymentExpiry = null, price = 0.0,
            firstSeen = now - 6 * DAY, lastConnected = now - 6 * DAY,
            tosVersion = null, tosAcceptedAt = null, appVersion = "2.0", extrasJson = null),

        DeviceEntity("dev_2f9ab4", "Taller Marina 4", "Galaxy A34", allowed = true, blocked = false,
            offlineMode = true, paymentExpiry = now + 60 * DAY, price = 15.00,
            firstSeen = now - 400 * DAY, lastConnected = now - 3 * DAY,
            tosVersion = "2.1", tosAcceptedAt = now - 390 * DAY, appVersion = "2.0", extrasJson = musicExtras),

        DeviceEntity("dev_31d7e8", "Óscar Prat", "Pixel 6a", allowed = false, blocked = true,
            offlineMode = false, paymentExpiry = now - 40 * DAY, price = 6.00,
            firstSeen = now - 260 * DAY, lastConnected = now - 38 * DAY,
            tosVersion = "2.0", tosAcceptedAt = now - 250 * DAY, appVersion = "1.9", extrasJson = lightExtras),

        DeviceEntity("dev_3ab512", "Marta Solís", "Galaxy S21", allowed = true, blocked = false,
            offlineMode = false, paymentExpiry = now + 5 * DAY, price = 7.50,
            firstSeen = now - 150 * DAY, lastConnected = now - 1 * DAY,
            tosVersion = "2.1", tosAcceptedAt = now - 140 * DAY, appVersion = "2.0", extrasJson = musicExtras),

        DeviceEntity("dev_44e0c7", "Camping El Faro (oficina)", "Lenovo Tab M10", allowed = true, blocked = false,
            offlineMode = true, paymentExpiry = now + 90 * DAY, price = 18.00,
            firstSeen = now - 620 * DAY, lastConnected = now - 7 * DAY,
            tosVersion = "2.1", tosAcceptedAt = now - 600 * DAY, appVersion = "2.0", extrasJson = null),

        DeviceEntity("dev_4c8b13", "Iván Roldán", "Redmi 12", allowed = true, blocked = false,
            offlineMode = false, paymentExpiry = now - 2 * DAY, price = 6.00,
            firstSeen = now - 70 * DAY, lastConnected = now - 5 * DAY,
            tosVersion = "2.0", tosAcceptedAt = now - 65 * DAY, appVersion = "1.9", extrasJson = lightExtras),

        DeviceEntity("dev_51a9f0", "Bar La Cala", "Galaxy A14", allowed = true, blocked = false,
            offlineMode = false, paymentExpiry = now + 19 * DAY, price = 10.00,
            firstSeen = now - 180 * DAY, lastConnected = now - 2 * DAY,
            tosVersion = "2.1", tosAcceptedAt = now - 175 * DAY, appVersion = "2.0", extrasJson = musicExtras),

        DeviceEntity("dev_5f3d22", "Patricia Gil", "iPhone 13", allowed = false, blocked = false,
            offlineMode = false, paymentExpiry = null, price = 0.0,
            firstSeen = now - 3 * DAY, lastConnected = now - 3 * DAY,
            tosVersion = null, tosAcceptedAt = null, appVersion = "2.0", extrasJson = null),

        DeviceEntity("dev_63b7a1", "Escola Nàutica Tramuntana", "Galaxy Tab S6 Lite", allowed = true, blocked = false,
            offlineMode = true, paymentExpiry = now + 33 * DAY, price = 20.00,
            firstSeen = now - 720 * DAY, lastConnected = now - 6 * DAY,
            tosVersion = "2.1", tosAcceptedAt = now - 700 * DAY, appVersion = "2.0", extrasJson = musicExtras),

        DeviceEntity("dev_6d1e88", "Rubén Castaño", "Pixel 8", allowed = true, blocked = false,
            offlineMode = false, paymentExpiry = now + 1 * DAY, price = 7.50,
            firstSeen = now - 45 * DAY, lastConnected = now - 1 * DAY,
            tosVersion = "2.1", tosAcceptedAt = now - 40 * DAY, appVersion = "2.0", extrasJson = lightExtras),

        DeviceEntity("dev_7a4c09", "Cristina Bravo", "Galaxy A54", allowed = false, blocked = true,
            offlineMode = false, paymentExpiry = now - 75 * DAY, price = 6.00,
            firstSeen = now - 300 * DAY, lastConnected = now - 72 * DAY,
            tosVersion = "1.9", tosAcceptedAt = now - 290 * DAY, appVersion = null, extrasJson = null),

        DeviceEntity("dev_82f5b6", "Kiosco Puerto Norte", "Moto G84", allowed = true, blocked = false,
            offlineMode = false, paymentExpiry = now + 12 * DAY, price = 9.00,
            firstSeen = now - 210 * DAY, lastConnected = now - 3 * DAY,
            tosVersion = "2.1", tosAcceptedAt = now - 205 * DAY, appVersion = "2.0", extrasJson = musicExtras),

        DeviceEntity("dev_8e6a31", "Álvaro Nieto", "Redmi Note 11", allowed = true, blocked = false,
            offlineMode = false, paymentExpiry = now + 27 * DAY, price = 6.00,
            firstSeen = now - 130 * DAY, lastConnected = now - 9 * DAY,
            tosVersion = "2.0", tosAcceptedAt = now - 125 * DAY, appVersion = "1.9", extrasJson = lightExtras),

        DeviceEntity("dev_9b2d74", "Gimnàs Onada", "Galaxy Tab A9+", allowed = true, blocked = false,
            offlineMode = true, paymentExpiry = now - 6 * DAY, price = 16.00,
            firstSeen = now - 380 * DAY, lastConnected = now - 8 * DAY,
            tosVersion = "2.1", tosAcceptedAt = now - 370 * DAY, appVersion = "2.0", extrasJson = musicExtras),

        DeviceEntity("dev_a7c3e2", "Elena Duarte", "iPhone 14", allowed = true, blocked = false,
            offlineMode = false, paymentExpiry = now + 55 * DAY, price = 7.50,
            firstSeen = now - 95 * DAY, lastConnected = now - 2 * DAY,
            tosVersion = "2.1", tosAcceptedAt = now - 90 * DAY, appVersion = "2.0", extrasJson = lightExtras),

        DeviceEntity("dev_b3f8d5", "Autoescuela Litoral", "Lenovo Tab M11", allowed = false, blocked = false,
            offlineMode = false, paymentExpiry = null, price = 0.0,
            firstSeen = now - 1 * DAY, lastConnected = now - 1 * DAY,
            tosVersion = null, tosAcceptedAt = null, appVersion = "2.0", extrasJson = null),

        DeviceEntity("dev_c9a1f7", "Hugo Peralta", "Galaxy S22", allowed = true, blocked = false,
            offlineMode = false, paymentExpiry = now + 8 * DAY, price = 7.50,
            firstSeen = now - 260 * DAY, lastConnected = now - 4 * DAY,
            tosVersion = "2.1", tosAcceptedAt = now - 250 * DAY, appVersion = "2.0", extrasJson = musicExtras),
    )
}
