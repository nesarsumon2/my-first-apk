package com.example.data.model

enum class AutomationType {
    WIFI_TIMER,
    WIFI_SCHEDULE,
    BLUETOOTH_TIMER,
    BLUETOOTH_SCHEDULE;

    fun isWifi(): Boolean = this == WIFI_TIMER || this == WIFI_SCHEDULE
    fun isBluetooth(): Boolean = this == BLUETOOTH_TIMER || this == BLUETOOTH_SCHEDULE
    fun isTimer(): Boolean = this == WIFI_TIMER || this == BLUETOOTH_TIMER
    fun isSchedule(): Boolean = this == WIFI_SCHEDULE || this == BLUETOOTH_SCHEDULE
}

enum class TargetAction {
    TURN_OFF,
    TURN_ON,
    TOGGLE;

    fun displayString(): String = when (this) {
        TURN_OFF -> "Turn OFF"
        TURN_ON -> "Turn ON"
        TOGGLE -> "Toggle State"
    }
}
