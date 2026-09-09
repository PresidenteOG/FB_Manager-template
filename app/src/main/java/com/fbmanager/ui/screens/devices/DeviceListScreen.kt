package com.fbmanager.ui.screens.devices

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fbmanager.domain.model.DeviceNode
import com.fbmanager.domain.model.PaymentStatus
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
private fun Long.toDateStr(): String = dateFormat.format(Date(this))

private fun JSONArray?.toStringList(): List<String> =
    if (this == null) emptyList() else (0 until length()).map { optString(it) }

private fun JSONArray?.toTitleArtistList(): List<Pair<String, String>> {
    if (this == null) return emptyList()
    return (0 until length()).mapNotNull { i ->
        optJSONObject(i)?.let { it.optString("title", "—") to it.optString("artist", "—") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceListScreen(
    viewModel: DeviceListViewModel = hiltViewModel(),
) {
    val authState by viewModel.authState.collectAsState()
    val devices by viewModel.devices.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    var selectedDevice by remember { mutableStateOf<DeviceNode?>(null) }
    var dataDeviceId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dispositivos") },
                actions = {
                    if (authState == AuthState.Authenticated) {
                        // Notification toggle
                        IconButton(onClick = viewModel::toggleNotifications) {
                            Icon(
                                if (notificationsEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                contentDescription = if (notificationsEnabled) "Notificaciones activas" else "Notificaciones desactivadas",
                                tint = if (notificationsEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = viewModel::signOut) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = authState) {
                AuthState.Checking -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            CircularProgressIndicator()
                            Text("Cargando...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                AuthState.NeedsLogin -> LoginContent(onSignIn = viewModel::signIn)
                is AuthState.Error -> LoginContent(error = state.message, onSignIn = viewModel::signIn)
                AuthState.Authenticated -> DevicesContent(
                    devices = devices,
                    onDeviceClick = { selectedDevice = it },
                    onDataClick = { dataDeviceId = it.id }
                )
            }
        }
    }

    dataDeviceId?.let { id ->
        DeviceDataDialog(
            deviceId = id,
            loadExtras = viewModel::deviceExtras,
            onDismiss = { dataDeviceId = null },
        )
    }

    selectedDevice?.let { device ->
        DeviceBottomSheet(
            device = device,
            onDismiss = { selectedDevice = null },
            onUpdate = { updates -> viewModel.updateDevice(device.id, updates) },
            onDelete = { viewModel.deleteDevice(device.id); selectedDevice = null },
            onSetPaymentExpiry = { expiry -> viewModel.setPaymentExpiry(device.id, expiry) },
        )
    }
}

@Composable
private fun LoginContent(
    onSignIn: (String, String) -> Unit,
    error: String? = null,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ElevatedCard(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column {
                    Text("Iniciar sesión", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Panel de administración", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp),
                )
                if (error != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            error,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                Button(
                    onClick = { onSignIn(email.trim(), password) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = email.isNotBlank() && password.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Entrar", modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun DevicesContent(
    devices: List<DeviceNode>,
    onDeviceClick: (DeviceNode) -> Unit,
    onDataClick: (DeviceNode) -> Unit,
) {
    val activeCount = devices.count { it.allowed && !it.blocked }
    val blockedCount = devices.count { it.blocked }
    val overdueCount = devices.count { it.paymentStatus == PaymentStatus.Overdue }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, tonalElevation = 2.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatCard("Total", devices.size.toString(), MaterialTheme.colorScheme.onSurfaceVariant)
                StatDivider()
                StatCard("Activos", activeCount.toString(), MaterialTheme.colorScheme.secondary)
                StatDivider()
                StatCard("Bloqueados", blockedCount.toString(), if (blockedCount > 0) Color(0xFFEF5350) else MaterialTheme.colorScheme.onSurfaceVariant)
                StatDivider()
                StatCard("Vencidos", overdueCount.toString(), if (overdueCount > 0) Color(0xFFFFA726) else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (devices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sin dispositivos registrados.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(devices, key = { it.id }) { device ->
                    DeviceCard(
                        device = device,
                        onClick = { onDeviceClick(device) },
                        onDataClick = { onDataClick(device) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StatDivider() {
    Box(modifier = Modifier.width(1.dp).height(32.dp)) {
        HorizontalDivider(modifier = Modifier.fillMaxWidth().align(Alignment.Center))
    }
}

@Composable
private fun DeviceCard(device: DeviceNode, onClick: () -> Unit, onDataClick: () -> Unit) {
    val (stateBg, stateLabel) = when {
        device.blocked -> Color(0xFFEF5350) to "Bloqueado"
        device.allowed -> Color(0xFF66BB6A) to "Permitido"
        else -> Color(0xFFFFA726) to "Pendiente"
    }

    val (payColor, payLabel) = when (device.paymentStatus) {
        PaymentStatus.Active -> Color(0xFF66BB6A) to "Pago activo"
        PaymentStatus.ExpiringSoon -> Color(0xFFFFA726) to "Expira pronto"
        PaymentStatus.Overdue -> Color(0xFFEF5350) to "Vencido"
        PaymentStatus.NotConfigured -> MaterialTheme.colorScheme.onSurfaceVariant to "Sin pago"
    }

    val borderColor = when (device.paymentStatus) {
        PaymentStatus.Overdue -> Color(0xFFEF5350).copy(alpha = 0.4f)
        PaymentStatus.ExpiringSoon -> Color(0xFFFFA726).copy(alpha = 0.3f)
        else -> Color.Transparent
    }

    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header: name + state badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    device.displayName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Surface(
                    color = stateBg.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(
                        stateLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = stateBg,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Fecha de inicio
            InfoRow("Inicio", if (device.firstSeen > 0) device.firstSeen.toDateStr() else "—")
            
            // Última conexión
            if (device.lastConnected != null && device.lastConnected > 0) {
                InfoRow("Últ. conex.", device.lastConnected.toDateStr())
            }

            // Modelo
            if (!device.model.isNullOrBlank()) {
                InfoRow("Modelo", device.model)
            }

            // Versión SoundWave
            InfoRow(
                label = "SW ver.",
                value = device.appVersion ?: "—",
                valueColor = if (device.appVersion != null)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )

            // Offline + TOS checkboxes
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CheckItem("Offline", device.offlineMode)
                CheckItem(if (device.tosVersion != null) "TOS v${device.tosVersion}" else "Sin TOS", device.tosVersion != null)
            }

            // Fecha TOS
            if (device.tosAcceptedAt != null) {
                InfoRow("Fecha TOS", device.tosAcceptedAt.toDateStr())
            }

            // Pago
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${"%.2f".format(device.price)} €/mes",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Surface(
                    color = payColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text(
                        payLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        color = payColor,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(
                onClick = onDataClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                Text("Ver Datos (Favoritos y Estadísticas)", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: Color? = null) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            modifier = Modifier.width(80.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun CheckItem(label: String, checked: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        val color = if (checked) Color(0xFF66BB6A) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(3.dp))
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}


@Composable
fun DeviceDataDialog(
    deviceId: String,
    loadExtras: suspend (String) -> String?,
    onDismiss: () -> Unit,
) {
    var loading by remember { mutableStateOf(true) }
    var totalMs by remember { mutableStateOf(0L) }
    var totalTracks by remember { mutableStateOf(0) }
    var topArtists by remember { mutableStateOf<List<String>>(emptyList()) }
    var favoritesList by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var recentlyPlayedList by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var topStatsList by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    LaunchedEffect(deviceId) {
        loading = true
        try {
            val raw = loadExtras(deviceId)
            if (!raw.isNullOrBlank()) {
                val json = JSONObject(raw)
                val stats = json.optJSONObject("stats")
                if (stats != null) {
                    totalMs = stats.optLong("totalListenedMs", 0L)
                    totalTracks = stats.optInt("totalTracks", 0)
                    topArtists = stats.optJSONArray("topArtists").toStringList()
                    topStatsList = stats.optJSONArray("top10Tracks").toTitleArtistList()
                }
                favoritesList = json.optJSONArray("favorites").toTitleArtistList()
                recentlyPlayedList = json.optJSONArray("recentlyPlayed").toTitleArtistList().take(10)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        loading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Datos del Usuario", fontWeight = FontWeight.Bold) },
        text = {
            if (loading) {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        val artistsList = topArtists

                        Text("Estadísticas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text("Minutos Escuchados: ${totalMs / 60000} min")
                        LinearProgressIndicator(progress = { ((totalMs / 60000).toFloat() / 1000f).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)))
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Canciones Reproducidas: $totalTracks")
                        LinearProgressIndicator(progress = { (totalTracks.toFloat() / 500f).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)))

                        if (artistsList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Top Artistas:", fontWeight = FontWeight.Bold)
                            artistsList.take(5).forEachIndexed { i, artist ->
                                Text("${i+1}. $artist", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text("Favoritos (${favoritesList.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    if (favoritesList.isEmpty()) {
                        item {
                            Text("No tiene favoritos guardados.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        items(favoritesList) { fav ->
                            Column {
                                Text(fav.first, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(fav.second, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Recientemente Escuchado (${recentlyPlayedList.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    if (recentlyPlayedList.isEmpty()) {
                        item {
                            Text("No tiene reproducciones recientes.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        items(recentlyPlayedList) { rec ->
                            Column {
                                Text(rec.first, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(rec.second, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Top 10 Estadísticas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    if (topStatsList.isEmpty()) {
                        item {
                            Text("No tiene estadísticas destacadas.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        items(topStatsList) { stat ->
                            Column {
                                Text(stat.first, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(stat.second, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}
