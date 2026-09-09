package com.fbmanager.ui.screens.devices

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fbmanager.domain.model.DeviceNode
import com.fbmanager.domain.model.PaymentStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceBottomSheet(
    device: DeviceNode,
    onDismiss: () -> Unit,
    onUpdate: (Map<String, Any?>) -> Unit,
    onDelete: () -> Unit,
    onSetPaymentExpiry: (Long) -> Unit,
) {
    var nombre by remember { mutableStateOf(device.nombre ?: "") }
    var allowed by remember { mutableStateOf(device.allowed) }
    var blocked by remember { mutableStateOf(device.blocked) }
    var offlineMode by remember { mutableStateOf(device.offlineMode) }
    var priceText by remember { mutableStateOf(if (device.price > 0) "%.2f".format(device.price) else "") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val expiryLabel = device.paymentExpiry?.let { dateFormat.format(Date(it)) } ?: "No configurado"

    val daysUntilExpiry = device.paymentExpiry?.let {
        val diff = it - System.currentTimeMillis()
        (diff / 86_400_000L).toInt()
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Editar dispositivo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (!device.model.isNullOrBlank()) {
                    Text(device.model, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("ID: ${device.id}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }

            HorizontalDivider()

            // Nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del cliente") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )

            // Precio
            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it },
                label = { Text("Precio mensual (€)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("€ ") },
                shape = RoundedCornerShape(12.dp),
            )

            HorizontalDivider()

            // Estado switches
            Text("Estado", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

            SwitchRow(
                label = "Permitido",
                checked = allowed,
                onCheckedChange = { allowed = it },
                activeColor = Color(0xFF66BB6A),
            )
            SwitchRow(
                label = "Bloqueado",
                checked = blocked,
                onCheckedChange = { blocked = it },
                activeColor = Color(0xFFEF5350),
            )
            SwitchRow(
                label = "Modo offline",
                checked = offlineMode,
                onCheckedChange = { offlineMode = it },
                activeColor = MaterialTheme.colorScheme.secondary,
            )

            HorizontalDivider()

            // Pago
            Text("Mantenimiento", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

            val payColor = when (device.paymentStatus) {
                PaymentStatus.Active -> Color(0xFF66BB6A)
                PaymentStatus.ExpiringSoon -> Color(0xFFFFA726)
                PaymentStatus.Overdue -> Color(0xFFEF5350)
                PaymentStatus.NotConfigured -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Surface(color = payColor.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        when (device.paymentStatus) {
                            PaymentStatus.Active -> "Pago activo"
                            PaymentStatus.ExpiringSoon -> "Expira pronto"
                            PaymentStatus.Overdue -> "Pago vencido"
                            PaymentStatus.NotConfigured -> "Sin configurar"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = payColor,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Expiración: $expiryLabel" + (daysUntilExpiry?.let {
                            if (it >= 0) " ($it días)" else " (hace ${-it} días)"
                        } ?: ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Button(
                onClick = {
                    val thirtyDays = 30L * 24 * 60 * 60 * 1000
                    onSetPaymentExpiry(System.currentTimeMillis() + thirtyDays)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A)),
            ) {
                Text("Renovar pago (+30 días)")
            }

            HorizontalDivider()

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text("Eliminar")
                }
                Button(
                    onClick = {
                        val price = (priceText.toDoubleOrNull() ?: device.price).coerceIn(0.0, 99_999.0)
                        onUpdate(
                            mapOf(
                                "nombre" to nombre.trim().ifBlank { null },
                                "allowed" to allowed,
                                "blocked" to blocked,
                                "offlineMode" to offlineMode,
                                "price" to price,
                            )
                        )
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Guardar")
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("¿Eliminar dispositivo?") },
            text = { Text("Se eliminará '${device.displayName}' del listado. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteConfirm = false }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    activeColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = activeColor,
            ),
        )
    }
}
