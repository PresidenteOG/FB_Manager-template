package com.fbmanager.ui.screens.devices

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fbmanager.data.auth.AuthRepository
import com.fbmanager.data.firebase.DeviceRepositoryImpl
import com.fbmanager.domain.model.DeviceNode
import com.fbmanager.domain.repository.DeviceRepository
import com.fbmanager.notification.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

sealed interface AuthState {
    data object Checking : AuthState
    data object NeedsLogin : AuthState
    data object Authenticated : AuthState
    data class Error(val message: String) : AuthState
}

private const val PREFS_NAME = "fbm_settings"
private const val KEY_NOTIF_NEW_DEVICES = "notif_new_devices"

@HiltViewModel
class DeviceListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceRepository: DeviceRepository,
    private val deviceRepositoryImpl: DeviceRepositoryImpl,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _devices = MutableStateFlow<List<DeviceNode>>(emptyList())
    val devices: StateFlow<List<DeviceNode>> = _devices

    private val _authState = MutableStateFlow<AuthState>(AuthState.Checking)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _showingCache = MutableStateFlow(false)
    val showingCache: StateFlow<Boolean> = _showingCache.asStateFlow()

    /** Whether to fire a local notification when a new device registers. */
    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean(KEY_NOTIF_NEW_DEVICES, true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    /**
     * IDs considered "already known" at session start.
     * Initialized from cache → only truly new registrations during this session trigger alerts.
     */
    private val knownIds: MutableSet<String> =
        deviceRepositoryImpl.loadCache().map { it.id }.toMutableSet()
    private var initialLoadDone = false
    private val notifCounter = AtomicInteger(1_000)

    private var observeJob: Job? = null

    init { checkAuth() }

    fun toggleNotifications() {
        val next = !_notificationsEnabled.value
        _notificationsEnabled.value = next
        prefs.edit().putBoolean(KEY_NOTIF_NEW_DEVICES, next).apply()
    }

    private fun checkAuth() {
        if (authRepository.isSignedIn()) {
            _authState.value = AuthState.Authenticated
            startObserving()
        } else {
            _authState.value = AuthState.NeedsLogin
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Checking
            runCatching { authRepository.signIn(email, password) }
                .onSuccess {
                    _authState.value = AuthState.Authenticated
                    startObserving()
                }
                .onFailure { _authState.value = AuthState.Error(it.message ?: "Error de autenticación") }
        }
    }

    fun signOut() {
        observeJob?.cancel()
        authRepository.signOut()
        _devices.value = emptyList()
        _showingCache.value = false
        _authState.value = AuthState.NeedsLogin
    }

    fun showCachedData() {
        _devices.value = deviceRepositoryImpl.loadCache()
        _showingCache.value = true
    }

    fun refreshLive() {
        _showingCache.value = false
        startObserving()
    }

    private fun startObserving() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            deviceRepository.observeDevices().collect { list ->
                if (initialLoadDone && _notificationsEnabled.value) {
                    // Detect genuinely new devices (not in knownIds from session start).
                    list.filter { it.id !in knownIds }.forEach { newDevice ->
                        NotificationHelper.notifyNewDevice(
                            context,
                            newDevice.id,
                            newDevice.model ?: newDevice.nombre,
                            notifCounter.getAndIncrement(),
                        )
                    }
                }
                list.forEach { knownIds.add(it.id) }
                initialLoadDone = true
                _devices.value = list
                _showingCache.value = false
            }
        }
    }

    fun updateDevice(deviceId: String, updates: Map<String, Any?>) {
        viewModelScope.launch { deviceRepository.updateDevice(deviceId, updates) }
    }

    fun deleteDevice(deviceId: String) {
        viewModelScope.launch { deviceRepository.deleteDevice(deviceId) }
    }

    fun setPaymentExpiry(deviceId: String, expiryMs: Long) {
        viewModelScope.launch {
            deviceRepository.updateDevice(deviceId, mapOf("paymentExpiry" to expiryMs))
        }
    }
}
