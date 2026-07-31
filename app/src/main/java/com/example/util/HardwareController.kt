package com.example.util

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log

class HardwareController(private val context: Context) {

    private val wifiManager: WifiManager? =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private val bluetoothAdapter: BluetoothAdapter? = run {
        val bluetoothManager =
            context.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter()
    }

    fun isWifiEnabled(): Boolean {
        return wifiManager?.isWifiEnabled == true
    }

    /**
     * Attempts to toggle Wi-Fi state.
     * Note: On Android 10 (API 29) and above, WifiManager.setWifiEnabled is deprecated/restricted
     * for third-party apps. We attempt the call, and if it fails or OS restricts it,
     * we return false so caller can notify user or open panel intent.
     */
    @Suppress("DEPRECATION")
    fun setWifiEnabled(enabled: Boolean): ToggleResult {
        if (wifiManager == null) {
            return ToggleResult.Error("Wi-Fi hardware unavailable")
        }

        if (isWifiEnabled() == enabled) {
            return ToggleResult.Success("Wi-Fi is already " + (if (enabled) "ON" else "OFF"))
        }

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // On Android 10+, setWifiEnabled returns false or is ignored for non-system apps.
                val success = wifiManager.setWifiEnabled(enabled)
                if (success && isWifiEnabled() == enabled) {
                    ToggleResult.Success("Wi-Fi turned ${if (enabled) "ON" else "OFF"}")
                } else {
                    ToggleResult.RequiresSystemPanel(
                        "Android 10+ requires user confirmation to change Wi-Fi state.",
                        getWifiPanelIntent()
                    )
                }
            } else {
                val success = wifiManager.setWifiEnabled(enabled)
                if (success) {
                    ToggleResult.Success("Wi-Fi turned ${if (enabled) "ON" else "OFF"}")
                } else {
                    ToggleResult.Error("Failed to change Wi-Fi state")
                }
            }
        } catch (e: SecurityException) {
            Log.e("HardwareController", "Wi-Fi toggle security exception", e)
            ToggleResult.RequiresSystemPanel(
                "Permission needed to toggle Wi-Fi. Tap to open Wi-Fi settings.",
                getWifiPanelIntent()
            )
        } catch (e: Exception) {
            Log.e("HardwareController", "Error toggling Wi-Fi", e)
            ToggleResult.Error("Error: ${e.message}")
        }
    }

    fun isBluetoothEnabled(): Boolean {
        return try {
            bluetoothAdapter?.isEnabled == true
        } catch (e: SecurityException) {
            Log.e("HardwareController", "Bluetooth permission exception on state check", e)
            false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Attempts to toggle Bluetooth state.
     * On Android 12+, requires BLUETOOTH_CONNECT permission.
     * On Android 13+, BluetoothAdapter.enable()/disable() are restricted on some builds,
     * so we fallback to opening Bluetooth Settings Panel if needed.
     */
    @Suppress("DEPRECATION")
    fun setBluetoothEnabled(enabled: Boolean): ToggleResult {
        if (bluetoothAdapter == null) {
            return ToggleResult.Error("Bluetooth hardware unavailable")
        }

        return try {
            if (isBluetoothEnabled() == enabled) {
                return ToggleResult.Success("Bluetooth is already " + (if (enabled) "ON" else "OFF"))
            }

            val success = if (enabled) {
                bluetoothAdapter.enable()
            } else {
                bluetoothAdapter.disable()
            }

            if (success) {
                ToggleResult.Success("Bluetooth turned ${if (enabled) "ON" else "OFF"}")
            } else {
                ToggleResult.RequiresSystemPanel(
                    "System restricted direct Bluetooth toggle. Tap to open Bluetooth Settings.",
                    getBluetoothSettingsIntent()
                )
            }
        } catch (e: SecurityException) {
            Log.e("HardwareController", "Bluetooth permission exception", e)
            ToggleResult.RequiresSystemPanel(
                "Bluetooth permission required. Tap to open Bluetooth Settings.",
                getBluetoothSettingsIntent()
            )
        } catch (e: Exception) {
            Log.e("HardwareController", "Error toggling Bluetooth", e)
            ToggleResult.Error("Error toggling Bluetooth: ${e.message}")
        }
    }

    fun getWifiPanelIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        } else {
            Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun getBluetoothSettingsIntent(): Intent {
        return Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    sealed class ToggleResult {
        data class Success(val message: String) : ToggleResult()
        data class Error(val message: String) : ToggleResult()
        data class RequiresSystemPanel(val explanation: String, val intent: Intent) : ToggleResult()
    }
}
