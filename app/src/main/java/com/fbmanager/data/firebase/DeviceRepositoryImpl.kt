package com.fbmanager.data.firebase

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.fbmanager.domain.model.DeviceNode
import com.fbmanager.domain.repository.DeviceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : DeviceRepository {

    private val prefs by lazy {
        val key = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        EncryptedSharedPreferences.create(
            context, "fbm_device_cache", key,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override fun observeDevices(): Flow<List<DeviceNode>> = callbackFlow {
        val ref = FirebaseDatabase.getInstance().getReference("devices")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val devices = snapshot.children.mapNotNull { child ->
                    val id = child.key ?: return@mapNotNull null
                    DeviceNode(
                        id = id,
                        nombre = child.child("nombre").getValue(String::class.java),
                        model = child.child("model").getValue(String::class.java),
                        allowed = child.child("allowed").getValue(Boolean::class.java) ?: false,
                        blocked = child.child("blocked").getValue(Boolean::class.java) ?: false,
                        offlineMode = child.child("offlineMode").getValue(Boolean::class.java) ?: false,
                        paymentExpiry = child.child("paymentExpiry").getValue(Long::class.java),
                        price = child.child("price").getValue(Double::class.java) ?: 0.0,
                        firstSeen = child.child("firstSeen").getValue(Long::class.java) ?: 0L,
                        lastConnected = child.child("lastConnected").getValue(Long::class.java),
                        tosVersion = child.child("tos/version").getValue(Any::class.java)?.toString(),
                        tosAcceptedAt = child.child("tos/acceptedAt").getValue(Long::class.java),
                        appVersion = child.child("appVersion").getValue(String::class.java),
                    )
                }
                saveCache(devices)
                trySend(devices)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DeviceRepo", "Firebase cancelled: ${error.message}")
                trySend(loadCache())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun updateDevice(deviceId: String, updates: Map<String, Any?>) {
        FirebaseDatabase.getInstance().getReference("devices").child(deviceId)
            .updateChildren(updates).await()
    }

    override suspend fun deleteDevice(deviceId: String) {
        FirebaseDatabase.getInstance().getReference("devices").child(deviceId)
            .removeValue().await()
    }

    private fun saveCache(devices: List<DeviceNode>) {
        val arr = JSONArray()
        devices.forEach { d ->
            arr.put(JSONObject().apply {
                put("id", d.id)
                put("nombre", d.nombre ?: "")
                put("model", d.model ?: "")
                put("allowed", d.allowed)
                put("blocked", d.blocked)
                put("offlineMode", d.offlineMode)
                put("paymentExpiry", d.paymentExpiry ?: -1L)
                put("price", d.price)
                put("firstSeen", d.firstSeen)
                put("lastConnected", d.lastConnected ?: -1L)
                put("tosVersion", d.tosVersion ?: "")
                put("tosAcceptedAt", d.tosAcceptedAt ?: -1L)
                put("appVersion", d.appVersion ?: "")
            })
        }
        prefs.edit().putString("devices_json", arr.toString()).apply()
    }

    fun loadCache(): List<DeviceNode> {
        val json = prefs.getString("devices_json", null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                DeviceNode(
                    id = o.getString("id"),
                    nombre = o.getString("nombre").takeIf { it.isNotBlank() },
                    model = o.getString("model").takeIf { it.isNotBlank() },
                    allowed = o.getBoolean("allowed"),
                    blocked = o.getBoolean("blocked"),
                    offlineMode = o.getBoolean("offlineMode"),
                    paymentExpiry = o.getLong("paymentExpiry").takeIf { it >= 0 },
                    price = o.getDouble("price"),
                    firstSeen = o.getLong("firstSeen"),
                    lastConnected = o.optLong("lastConnected", -1L).takeIf { it >= 0 },
                    tosVersion = o.getString("tosVersion").takeIf { it.isNotBlank() },
                    tosAcceptedAt = o.getLong("tosAcceptedAt").takeIf { it >= 0 },
                    appVersion = o.optString("appVersion").takeIf { it.isNotBlank() },
                )
            }
        } catch (e: Exception) {
            Log.e("DeviceRepo", "Cache parse error", e)
            emptyList()
        }
    }
}
