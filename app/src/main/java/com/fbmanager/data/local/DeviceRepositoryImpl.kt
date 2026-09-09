package com.fbmanager.data.local

import com.fbmanager.domain.model.DeviceNode
import com.fbmanager.domain.repository.DeviceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed device store. The fleet lives entirely on the device — there is no
 * network call anywhere in this class. On first run the table is empty, so it is
 * filled from [SeedData] before the first list is emitted.
 */
@Singleton
class DeviceRepositoryImpl @Inject constructor(
    private val dao: DeviceDao,
) : DeviceRepository {

    override fun observeDevices(): Flow<List<DeviceNode>> = flow {
        if (dao.count() == 0) {
            dao.insertAll(SeedData.build())
        }
        emitAll(dao.observeAll().map { list -> list.map(DeviceEntity::toDomain) })
    }.flowOn(Dispatchers.IO)

    override suspend fun updateDevice(deviceId: String, updates: Map<String, Any?>) {
        val current = dao.getById(deviceId) ?: return
        val patched = current.copy(
            nombre        = if (updates.containsKey("nombre")) updates["nombre"] as? String else current.nombre,
            allowed       = updates["allowed"] as? Boolean ?: current.allowed,
            blocked       = updates["blocked"] as? Boolean ?: current.blocked,
            offlineMode   = updates["offlineMode"] as? Boolean ?: current.offlineMode,
            price         = (updates["price"] as? Number)?.toDouble() ?: current.price,
            paymentExpiry = if (updates.containsKey("paymentExpiry"))
                (updates["paymentExpiry"] as? Number)?.toLong() else current.paymentExpiry,
        )
        dao.update(patched)
    }

    override suspend fun deleteDevice(deviceId: String) = dao.deleteById(deviceId)

    override suspend fun getExtras(deviceId: String): String? = dao.getExtras(deviceId)
}
