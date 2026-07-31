package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AutomationTask
import com.example.data.model.AutomationType
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoSecondary
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.MainViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    wifiState: Boolean,
    bluetoothState: Boolean,
    tasks: List<AutomationTask>,
    currentTimeMillis: Long,
    onNavigateToAdd: (AutomationType) -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val snackbarHostState = remember { SnackbarHostState() }
    val uiMessage by viewModel.uiEventMessage.run { remember { mutableStateOf(value) } }

    val filteredTasks = remember(tasks, selectedFilter) {
        when (selectedFilter) {
            "Wi-Fi" -> tasks.filter { it.type.isWifi() }
            "Bluetooth" -> tasks.filter { it.type.isBluetooth() }
            "Timers" -> tasks.filter { it.type.isTimer() }
            "Schedules" -> tasks.filter { it.type.isSchedule() }
            else -> tasks
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Settings Manager",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Automation & Quick Toggles",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToPermissions,
                        modifier = Modifier.testTag("permissions_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Permissions Onboarding",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Settings and Logs",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAdd(AutomationType.WIFI_TIMER) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_automation_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Automation")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("New Automation", fontWeight = FontWeight.Bold)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Hardware Status Header Card
            item {
                HardwareStatusCard(
                    wifiState = wifiState,
                    bluetoothState = bluetoothState,
                    onToggleWifi = { viewModel.toggleWifiDirectly() },
                    onToggleBluetooth = { viewModel.toggleBluetoothDirectly() }
                )
            }

            // Quick Create Automation Shortcuts
            item {
                Text(
                    text = "Quick Automations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionChip(
                        title = "Wi-Fi Timer",
                        icon = Icons.Default.WifiOff,
                        color = TealPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToAdd(AutomationType.WIFI_TIMER) }
                    )
                    QuickActionChip(
                        title = "Wi-Fi Schedule",
                        icon = Icons.Default.Schedule,
                        color = TealPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToAdd(AutomationType.WIFI_SCHEDULE) }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionChip(
                        title = "BT Timer",
                        icon = Icons.Default.BluetoothDisabled,
                        color = IndigoSecondary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToAdd(AutomationType.BLUETOOTH_TIMER) }
                    )
                    QuickActionChip(
                        title = "BT Schedule",
                        icon = Icons.Default.Schedule,
                        color = IndigoSecondary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToAdd(AutomationType.BLUETOOTH_SCHEDULE) }
                    )
                }
            }

            // Filter Chips Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active & Scheduled Tasks (${filteredTasks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val filters = listOf("All", "Wi-Fi", "Bluetooth", "Timers", "Schedules")
                    items(filters) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            // Empty State if no automations match filter
            if (filteredTasks.isEmpty()) {
                item {
                    EmptyAutomationState(
                        filter = selectedFilter,
                        onAddClick = { onNavigateToAdd(AutomationType.WIFI_TIMER) }
                    )
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    AutomationCard(
                        task = task,
                        currentTimeMillis = currentTimeMillis,
                        onToggleEnable = { viewModel.toggleTaskEnabled(task) },
                        onTogglePause = { viewModel.toggleTaskPaused(task) },
                        onTriggerNow = { viewModel.triggerTaskNow(task) },
                        onEdit = { onNavigateToEdit(task.id) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun HardwareStatusCard(
    wifiState: Boolean,
    bluetoothState: Boolean,
    onToggleWifi: () -> Unit,
    onToggleBluetooth: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hardware_status_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "System Status & Toggles",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Wi-Fi Quick Toggle Box
                HardwareToggleBox(
                    title = "Wi-Fi",
                    isEnabled = wifiState,
                    activeIcon = Icons.Default.Wifi,
                    inactiveIcon = Icons.Default.WifiOff,
                    activeColor = TealPrimary,
                    modifier = Modifier.weight(1f),
                    onToggle = onToggleWifi
                )

                // Bluetooth Quick Toggle Box
                HardwareToggleBox(
                    title = "Bluetooth",
                    isEnabled = bluetoothState,
                    activeIcon = Icons.Default.Bluetooth,
                    inactiveIcon = Icons.Default.BluetoothDisabled,
                    activeColor = IndigoSecondary,
                    modifier = Modifier.weight(1f),
                    onToggle = onToggleBluetooth
                )
            }
        }
    }
}

@Composable
fun HardwareToggleBox(
    title: String,
    isEnabled: Boolean,
    activeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    inactiveIcon: androidx.compose.ui.graphics.vector.ImageVector,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit
) {
    val containerColor by animateColorAsState(
        if (isEnabled) activeColor.copy(alpha = 0.15f)
        else MaterialTheme.colorScheme.surface
    )
    val iconColor by animateColorAsState(
        if (isEnabled) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
    )

    Surface(
        onClick = onToggle,
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        color = containerColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isEnabled) activeIcon else inactiveIcon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isEnabled) "ON" else "OFF",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isEnabled) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = activeColor,
                    checkedTrackColor = activeColor.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun QuickActionChip(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AutomationCard(
    task: AutomationTask,
    currentTimeMillis: Long,
    onToggleEnable: () -> Unit,
    onTogglePause: () -> Unit,
    onTriggerNow: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isWifi = task.type.isWifi()
    val accentColor = if (isWifi) TealPrimary else IndigoSecondary

    val remainingMillis = remember(task.triggerTimeMillis, currentTimeMillis, task.isEnabled, task.isPaused) {
        if (task.type.isTimer() && task.isEnabled && !task.isPaused && task.triggerTimeMillis > currentTimeMillis) {
            task.triggerTimeMillis - currentTimeMillis
        } else 0L
    }

    val totalDurationMillis = remember(task.durationMinutes) {
        task.durationMinutes * 60 * 1000L
    }

    val progress = remember(remainingMillis, totalDurationMillis) {
        if (totalDurationMillis > 0 && remainingMillis > 0) {
            remainingMillis.toFloat() / totalDurationMillis.toFloat()
        } else 0f
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("automation_card_${task.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isEnabled) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (task.isEnabled) 2.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Type Badge + Title + Main Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isWifi) Icons.Default.Wifi else Icons.Default.Bluetooth,
                            contentDescription = task.type.name,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (task.isEnabled) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = accentColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = task.targetAction.displayString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            if (task.repeatDaily) {
                                Surface(
                                    color = EmeraldSuccess.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Daily",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldSuccess,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Switch(
                    checked = task.isEnabled,
                    onCheckedChange = { onToggleEnable() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = accentColor,
                        checkedTrackColor = accentColor.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.testTag("task_switch_${task.id}")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-details & Countdown Progress
            if (task.type.isTimer()) {
                if (task.isEnabled && !task.isPaused && remainingMillis > 0) {
                    val seconds = (remainingMillis / 1000) % 60
                    val minutes = (remainingMillis / (1000 * 60)) % 60
                    val hours = remainingMillis / (1000 * 60 * 60)
                    val timeFormatted = if (hours > 0) {
                        String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
                    } else {
                        String.format(Locale.US, "%02d:%02d", minutes, seconds)
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Timer running",
                                    tint = accentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Timer Ending In:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = timeFormatted,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = accentColor
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = accentColor,
                            trackColor = accentColor.copy(alpha = 0.15f),
                        )
                    }
                } else if (task.isPaused) {
                    Text(
                        text = "⏸ Automation Paused (${task.durationMinutes} mins timer)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "⏱ Countdown Timer: ${task.durationMinutes} mins",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Schedule info
                val formattedTime = String.format(
                    Locale.US,
                    "%02d:%02d %s",
                    if (task.hourOfDay % 12 == 0) 12 else task.hourOfDay % 12,
                    task.minuteOfHour,
                    if (task.hourOfDay >= 12) "PM" else "AM"
                )
                Text(
                    text = "⏰ Scheduled for $formattedTime" + (if (task.repeatDaily) " (Repeats Daily)" else ""),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row: Pause/Resume, Run Now, Edit, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Pause/Resume Button
                    if (task.isEnabled) {
                        Surface(
                            onClick = onTogglePause,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (task.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = if (task.isPaused) "Resume" else "Pause",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (task.isPaused) "Resume" else "Pause",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }

                    // Run Now (Test)
                    Surface(
                        onClick = onTriggerNow,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = "Run Now",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Run Now",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Automation",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Automation",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyAutomationState(filter: String, onAddClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No $filter Automations Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Create a timer or scheduled rule to automatically turn Wi-Fi or Bluetooth ON or OFF.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
