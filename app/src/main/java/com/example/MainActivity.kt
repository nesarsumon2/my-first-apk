package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.AutomationType
import com.example.ui.screens.AutomationFormScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PermissionsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.SettingsManagerTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SettingsManagerTheme {
                SettingsManagerAppNavHost(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshHardwareStates()
    }
}

@Composable
fun SettingsManagerAppNavHost(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val wifiState by viewModel.wifiState.collectAsStateWithLifecycle()
    val bluetoothState by viewModel.bluetoothState.collectAsStateWithLifecycle()
    val currentTimeMillis by viewModel.currentTimeMillis.collectAsStateWithLifecycle()
    val uiEventMessage by viewModel.uiEventMessage.collectAsStateWithLifecycle()

    LaunchedEffect(uiEventMessage) {
        uiEventMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUiMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Dashboard Screen
            composable("dashboard") {
                DashboardScreen(
                    viewModel = viewModel,
                    wifiState = wifiState,
                    bluetoothState = bluetoothState,
                    tasks = tasks,
                    currentTimeMillis = currentTimeMillis,
                    onNavigateToAdd = { type ->
                        navController.navigate("form/${type.name}/-1")
                    },
                    onNavigateToEdit = { taskId ->
                        val task = tasks.find { it.id == taskId }
                        val typeName = task?.type?.name ?: AutomationType.WIFI_TIMER.name
                        navController.navigate("form/$typeName/$taskId")
                    },
                    onNavigateToPermissions = {
                        navController.navigate("permissions")
                    },
                    onNavigateToSettings = {
                        navController.navigate("settings")
                    }
                )
            }

            // Automation Form Screen (Create / Edit)
            composable(
                route = "form/{typeName}/{taskId}",
                arguments = listOf(
                    navArgument("typeName") { type = NavType.StringType },
                    navArgument("taskId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val typeName = backStackEntry.arguments?.getString("typeName") ?: AutomationType.WIFI_TIMER.name
                val taskId = backStackEntry.arguments?.getLong("taskId") ?: -1L

                val initialType = try {
                    AutomationType.valueOf(typeName)
                } catch (e: Exception) {
                    AutomationType.WIFI_TIMER
                }

                val existingTask = if (taskId != -1L) tasks.find { it.id == taskId } else null

                AutomationFormScreen(
                    initialType = initialType,
                    existingTask = existingTask,
                    onSave = { task ->
                        viewModel.saveTask(task)
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Permissions Onboarding Screen
            composable("permissions") {
                PermissionsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Settings & Logs History Screen
            composable("settings") {
                SettingsScreen(
                    logs = logs,
                    onClearLogs = { viewModel.clearLogs() },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
