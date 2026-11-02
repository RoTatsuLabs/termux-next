package com.termux.app.compose.settings

import android.content.Context
import com.termux.shared.termux.settings.preferences.TermuxAPIAppSharedPreferences
import com.termux.shared.termux.settings.preferences.TermuxAppSharedPreferences
import com.termux.shared.termux.settings.preferences.TermuxFloatAppSharedPreferences
import com.termux.shared.termux.settings.preferences.TermuxTaskerAppSharedPreferences
import com.termux.shared.termux.settings.preferences.TermuxWidgetAppSharedPreferences

/*
 * Read and write access to the existing Termux and plugin app shared preferences.
 * Each factory mirrors the calls of the preference data stores it replaces, using the same
 * shared preferences classes, so stored values and their side effects do not change.
 */

internal class BooleanSetting(val get: () -> Boolean, val set: (Boolean) -> Unit)

internal class DebugSettings(
    val logLevel: () -> Int,
    val setLogLevel: (Int) -> Unit,
    val keyLogging: BooleanSetting? = null,
    val pluginErrorNotifications: BooleanSetting? = null,
    val crashReportNotifications: BooleanSetting? = null
)

internal class TerminalIoSettings(
    val softKeyboard: BooleanSetting,
    val softKeyboardOnlyIfNoHardware: BooleanSetting
)

internal fun termuxDebugSettings(context: Context): DebugSettings? {
    val p = TermuxAppSharedPreferences.build(context, true) ?: return null
    return DebugSettings(
        logLevel = { p.getLogLevel() },
        setLogLevel = { p.setLogLevel(context, it) },
        keyLogging = BooleanSetting(
            { p.isTerminalViewKeyLoggingEnabled() },
            { p.setTerminalViewKeyLoggingEnabled(it) }
        ),
        pluginErrorNotifications = BooleanSetting(
            { p.arePluginErrorNotificationsEnabled(false) },
            { p.setPluginErrorNotificationsEnabled(it) }
        ),
        crashReportNotifications = BooleanSetting(
            { p.areCrashReportNotificationsEnabled(false) },
            { p.setCrashReportNotificationsEnabled(it) }
        )
    )
}

internal fun terminalIoSettings(context: Context): TerminalIoSettings? {
    val p = TermuxAppSharedPreferences.build(context, true) ?: return null
    return TerminalIoSettings(
        softKeyboard = BooleanSetting(
            { p.isSoftKeyboardEnabled() },
            { p.setSoftKeyboardEnabled(it) }
        ),
        softKeyboardOnlyIfNoHardware = BooleanSetting(
            { p.isSoftKeyboardEnabledOnlyIfNoHardware() },
            { p.setSoftKeyboardEnabledOnlyIfNoHardware(it) }
        )
    )
}

internal fun terminalMarginAdjustmentSetting(context: Context): BooleanSetting? {
    val p = TermuxAppSharedPreferences.build(context, true) ?: return null
    return BooleanSetting(
        { p.isTerminalMarginAdjustmentEnabled() },
        { p.setTerminalMarginAdjustment(it) }
    )
}

internal fun apiDebugSettings(context: Context): DebugSettings? {
    val p = TermuxAPIAppSharedPreferences.build(context, true) ?: return null
    return DebugSettings(
        logLevel = { p.getLogLevel(true) },
        setLogLevel = { p.setLogLevel(context, it, true) }
    )
}

internal fun floatDebugSettings(context: Context): DebugSettings? {
    val p = TermuxFloatAppSharedPreferences.build(context, true) ?: return null
    return DebugSettings(
        logLevel = { p.getLogLevel(true) },
        setLogLevel = { p.setLogLevel(context, it, true) },
        keyLogging = BooleanSetting(
            { p.isTerminalViewKeyLoggingEnabled(true) },
            { p.setTerminalViewKeyLoggingEnabled(it, true) }
        )
    )
}

internal fun taskerDebugSettings(context: Context): DebugSettings? {
    val p = TermuxTaskerAppSharedPreferences.build(context, true) ?: return null
    return DebugSettings(
        logLevel = { p.getLogLevel(true) },
        setLogLevel = { p.setLogLevel(context, it, true) }
    )
}

internal fun widgetDebugSettings(context: Context): DebugSettings? {
    val p = TermuxWidgetAppSharedPreferences.build(context, true) ?: return null
    return DebugSettings(
        logLevel = { p.getLogLevel(true) },
        setLogLevel = { p.setLogLevel(context, it, true) }
    )
}
