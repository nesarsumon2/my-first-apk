package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.AutomationTask
import com.example.data.model.AutomationType
import com.example.data.model.TargetAction
import com.example.ui.theme.IndigoSecondary
import com.example.ui.theme.TealPrimary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationFormScreen(
    initialType: AutomationType,
    existingTask: AutomationTask?,
    onSave: (AutomationTask) -> Unit,
    onBack: () -> Unit
) {
    var selectedType by remember { mutableStateOf(existingTask?.type ?: initialType) }
    var targetAction by remember { mutableStateOf(existingTask?.targetAction ?: TargetAction.TURN_OFF) }

    // Timer state
    var durationMinutes by remember { mutableIntStateOf(existingTask?.durationMinutes ?: 30) }

    // Schedule state
    var hourOfDay by remember { mutableIntStateOf(existingTask?.hourOfDay ?: 7) }
    var minuteOfHour by remember { mutableIntStateOf(existingTask?.minuteOfHour ?: 0) }
    var repeatDaily by remember { mutableStateOf(existingTask?.repeatDaily ?: true) }

    // Title state
    var customTitle by remember { mutableStateOf(existingTask?.title ?: "") }

    val isWifi = selectedType.isWifi()
    val isTimer = selectedType.isTimer()
    val accentColor = if (isWifi) TealPrimary else IndigoSecondary

    // Auto-update default title when inputs change if custom title wasn't manually edited
    LaunchedEffect(selectedType, targetAction, durationMinutes, hourOfDay, minuteOfHour) {
        if (existingTask == null || customTitle.isBlank()) {
            val device = if (isWifi) "Wi-Fi" else "Bluetooth"
            val action = when (targetAction) {
                TargetAction.TURN_OFF -> "Turn OFF"
                TargetAction.TURN_ON -> "Turn ON"
                TargetAction.TOGGLE -> "Toggle"
            }
            customTitle = if (isTimer) {
                "$device $action in $durationMinutes mins"
            } else {
                val formattedTime = String.format(
                    Locale.US,
                    "%02d:%02d %s",
                    if (hourOfDay % 12 == 0) 12 else hourOfDay % 12,
                    minuteOfHour,
                    if (hourOfDay >= 12) "PM" else "AM"
                )
                "$device $action at $formattedTime"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existingTask == null) "New Automation" else "Edit Automation",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("form_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // 1. Hardware & Automation Type Selector
            Text(
                text = "1. Automation Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TypeChoiceCard(
                    title = "Wi-Fi Timer",
                    subtitle = "Countdown OFF/ON",
                    icon = Icons.Default.Wifi,
                    isSelected = selectedType == AutomationType.WIFI_TIMER,
                    color = TealPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedType = AutomationType.WIFI_TIMER }
                )
                TypeChoiceCard(
                    title = "Wi-Fi Schedule",
                    subtitle = "Time-based trigger",
                    icon = Icons.Default.Schedule,
                    isSelected = selectedType == AutomationType.WIFI_SCHEDULE,
                    color = TealPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedType = AutomationType.WIFI_SCHEDULE }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TypeChoiceCard(
                    title = "Bluetooth Timer",
                    subtitle = "Countdown OFF/ON",
                    icon = Icons.Default.Bluetooth,
                    isSelected = selectedType == AutomationType.BLUETOOTH_TIMER,
                    color = IndigoSecondary,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedType = AutomationType.BLUETOOTH_TIMER }
                )
                TypeChoiceCard(
                    title = "Bluetooth Schedule",
                    subtitle = "Time-based trigger",
                    icon = Icons.Default.Schedule,
                    isSelected = selectedType == AutomationType.BLUETOOTH_SCHEDULE,
                    color = IndigoSecondary,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedType = AutomationType.BLUETOOTH_SCHEDULE }
                )
            }

            // 2. Target Action Picker
            Text(
                text = "2. Target Action",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val actions = listOf(TargetAction.TURN_OFF, TargetAction.TURN_ON, TargetAction.TOGGLE)
                actions.forEachIndexed { index, action ->
                    SegmentedButton(
                        selected = targetAction == action,
                        onClick = { targetAction = action },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = actions.size)
                    ) {
                        Text(action.displayString(), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 3. Time / Duration Configuration
            Text(
                text = if (isTimer) "3. Countdown Duration" else "3. Scheduled Time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (isTimer) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Duration",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (durationMinutes >= 60) {
                                    val hrs = durationMinutes / 60
                                    val mins = durationMinutes % 60
                                    if (mins > 0) "$hrs hr $mins mins" else "$hrs hr"
                                } else {
                                    "$durationMinutes mins"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Slider(
                            value = durationMinutes.toFloat(),
                            onValueChange = { durationMinutes = it.toInt().coerceAtLeast(1) },
                            valueRange = 1f..480f, // 1 min up to 8 hours
                            steps = 0,
                            modifier = Modifier.testTag("duration_slider")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Quick Presets:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(5, 15, 30, 60, 120, 480).forEach { preset ->
                                val label = if (preset >= 60) "${preset / 60}h" else "${preset}m"
                                Surface(
                                    onClick = { durationMinutes = preset },
                                    color = if (durationMinutes == preset) accentColor else MaterialTheme.colorScheme.surface,
                                    contentColor = if (durationMinutes == preset) Color.White else MaterialTheme.colorScheme.onSurface,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Scheduled Time selector
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Set Execution Time",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Hour & Minute sliders/inputs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Hour: ${String.format(Locale.US, "%02d", hourOfDay)}:00",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Slider(
                                    value = hourOfDay.toFloat(),
                                    onValueChange = { hourOfDay = it.toInt() },
                                    valueRange = 0f..23f
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Minute: ${String.format(Locale.US, "%02d", minuteOfHour)}",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Slider(
                                    value = minuteOfHour.toFloat(),
                                    onValueChange = { minuteOfHour = it.toInt() },
                                    valueRange = 0f..59f
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { repeatDaily = !repeatDaily }
                        ) {
                            Checkbox(
                                checked = repeatDaily,
                                onCheckedChange = { repeatDaily = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Repeat Daily at this time",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 4. Custom Title Input
            Text(
                text = "4. Title & Description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = customTitle,
                onValueChange = { customTitle = it },
                label = { Text("Automation Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("automation_title_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Save Button
            Button(
                onClick = {
                    val task = AutomationTask(
                        id = existingTask?.id ?: 0L,
                        title = customTitle.ifBlank { "Automated Settings Rule" },
                        type = selectedType,
                        targetAction = targetAction,
                        durationMinutes = durationMinutes,
                        hourOfDay = hourOfDay,
                        minuteOfHour = minuteOfHour,
                        repeatDaily = repeatDaily,
                        isEnabled = true,
                        isPaused = false
                    )
                    onSave(task)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_automation_button"),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (existingTask == null) "Create & Activate Automation" else "Update Automation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TypeChoiceCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, color) else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
