@file:JvmName("SettingsUi")

package com.termux.app.compose.settings

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.termux.R
import com.termux.app.activities.SettingsActivity
import com.termux.app.compose.theme.TermuxTheme
import com.termux.shared.logger.Logger
import com.termux.shared.termux.settings.preferences.TermuxAPIAppSharedPreferences
import com.termux.shared.termux.settings.preferences.TermuxFloatAppSharedPreferences
import com.termux.shared.termux.settings.preferences.TermuxTaskerAppSharedPreferences
import com.termux.shared.termux.settings.preferences.TermuxWidgetAppSharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val ROOT = "root"
private const val TERMUX = "termux"
private const val TERMUX_DEBUGGING = "termux/debugging"
private const val TERMUX_IO = "termux/io"
private const val TERMUX_VIEW = "termux/view"
private const val API = "api"
private const val API_DEBUGGING = "api/debugging"
private const val FLOAT = "float"
private const val FLOAT_DEBUGGING = "float/debugging"
private const val TASKER = "tasker"
private const val TASKER_DEBUGGING = "tasker/debugging"
private const val WIDGET = "widget"
private const val WIDGET_DEBUGGING = "widget/debugging"

/** Sets the Termux settings screen as the content of [view]. [onFinish] closes the hosting activity. */
fun setSettingsContent(view: ComposeView, title: String, onFinish: Runnable) {
    view.setContent {
        TermuxTheme {
            SettingsApp(rootTitle = title, onFinish = { onFinish.run() })
        }
    }
}

private class PluginVisibility(
    val termuxApi: Boolean = false,
    val termuxFloat: Boolean = false,
    val termuxTasker: Boolean = false,
    val termuxWidget: Boolean = false
)

private fun parentOf(route: String): String? = when {
    route == ROOT -> null
    '/' in route -> route.substringBeforeLast('/')
    else -> ROOT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsApp(rootTitle: String, onFinish: () -> Unit) {
    val context = LocalContext.current
    var route by rememberSaveable { mutableStateOf(ROOT) }
    var plugins by remember { mutableStateOf(PluginVisibility()) }
    var donateVisible by remember { mutableStateOf(false) }

    // Plugin apps and the donate link are only shown when available, as before.
    LaunchedEffect(Unit) {
        val result = withContext(Dispatchers.IO) {
            PluginVisibility(
                termuxApi = TermuxAPIAppSharedPreferences.build(context, false) != null,
                termuxFloat = TermuxFloatAppSharedPreferences.build(context, false) != null,
                termuxTasker = TermuxTaskerAppSharedPreferences.build(context, false) != null,
                termuxWidget = TermuxWidgetAppSharedPreferences.build(context, false) != null
            ) to SettingsActivity.isDonateVisible(context)
        }
        plugins = result.first
        donateVisible = result.second
    }

    val goUp: () -> Unit = {
        val parent = parentOf(route)
        if (parent == null) onFinish() else route = parent
    }
    BackHandler(enabled = route != ROOT) { goUp() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titleFor(route, rootTitle)) },
                navigationIcon = {
                    IconButton(onClick = goUp) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(androidx.appcompat.R.string.abc_action_bar_up_description)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            when (route) {
                TERMUX -> TermuxScreen(onNavigate = { route = it })
                TERMUX_DEBUGGING -> DebugScreen(remember { termuxDebugSettings(context) })
                TERMUX_IO -> TerminalIoScreen()
                TERMUX_VIEW -> TerminalViewScreen()
                API -> PluginScreen { route = API_DEBUGGING }
                API_DEBUGGING -> DebugScreen(remember { apiDebugSettings(context) })
                FLOAT -> PluginScreen { route = FLOAT_DEBUGGING }
                FLOAT_DEBUGGING -> DebugScreen(remember { floatDebugSettings(context) })
                TASKER -> PluginScreen { route = TASKER_DEBUGGING }
                TASKER_DEBUGGING -> DebugScreen(remember { taskerDebugSettings(context) })
                WIDGET -> PluginScreen { route = WIDGET_DEBUGGING }
                WIDGET_DEBUGGING -> DebugScreen(remember { widgetDebugSettings(context) })
                else -> RootScreen(plugins, donateVisible, onNavigate = { route = it })
            }
        }
    }
}

@Composable
private fun titleFor(route: String, rootTitle: String): String = when (route) {
    TERMUX -> stringResource(R.string.termux_preferences_title)
    TERMUX_IO -> stringResource(R.string.termux_terminal_io_preferences_title)
    TERMUX_VIEW -> stringResource(R.string.termux_terminal_view_preferences_title)
    API -> stringResource(R.string.termux_api_preferences_title)
    FLOAT -> stringResource(R.string.termux_float_preferences_title)
    TASKER -> stringResource(R.string.termux_tasker_preferences_title)
    WIDGET -> stringResource(R.string.termux_widget_preferences_title)
    TERMUX_DEBUGGING, API_DEBUGGING, FLOAT_DEBUGGING, TASKER_DEBUGGING, WIDGET_DEBUGGING ->
        stringResource(R.string.termux_debugging_preferences_title)
    else -> rootTitle
}

@Composable
private fun RootScreen(plugins: PluginVisibility, donateVisible: Boolean, onNavigate: (String) -> Unit) {
    val context = LocalContext.current

    NavItem(R.string.termux_preferences_title, R.string.termux_preferences_summary) { onNavigate(TERMUX) }
    if (plugins.termuxApi) {
        NavItem(R.string.termux_api_preferences_title, R.string.termux_api_preferences_summary) { onNavigate(API) }
    }
    if (plugins.termuxFloat) {
        NavItem(R.string.termux_float_preferences_title, R.string.termux_float_preferences_summary) { onNavigate(FLOAT) }
    }
    if (plugins.termuxTasker) {
        NavItem(R.string.termux_tasker_preferences_title, R.string.termux_tasker_preferences_summary) { onNavigate(TASKER) }
    }
    if (plugins.termuxWidget) {
        NavItem(R.string.termux_widget_preferences_title, R.string.termux_widget_preferences_summary) { onNavigate(WIDGET) }
    }

    ListItem(
        headlineContent = { Text(stringResource(R.string.about_preference_title)) },
        modifier = Modifier.clickable { SettingsActivity.showAbout(context) }
    )
    if (donateVisible) {
        ListItem(
            headlineContent = { Text(stringResource(R.string.donate_preference_title)) },
            modifier = Modifier.clickable { SettingsActivity.openDonate(context) }
        )
    }
}

@Composable
private fun TermuxScreen(onNavigate: (String) -> Unit) {
    NavItem(R.string.termux_debugging_preferences_title, R.string.termux_debugging_preferences_summary) {
        onNavigate(TERMUX_DEBUGGING)
    }
    NavItem(R.string.termux_terminal_io_preferences_title, R.string.termux_terminal_io_preferences_summary) {
        onNavigate(TERMUX_IO)
    }
    NavItem(R.string.termux_terminal_view_preferences_title, R.string.termux_terminal_view_preferences_summary) {
        onNavigate(TERMUX_VIEW)
    }
}

/** Plugin apps only have a debugging screen. */
@Composable
private fun PluginScreen(onOpenDebugging: () -> Unit) {
    NavItem(R.string.termux_debugging_preferences_title, R.string.termux_debugging_preferences_summary, onOpenDebugging)
}

@Composable
private fun TerminalIoScreen() {
    val context = LocalContext.current
    val settings = remember { terminalIoSettings(context) } ?: return

    CategoryHeader(R.string.termux_keyboard_header)
    SwitchItem(
        R.string.termux_soft_keyboard_enabled_title,
        R.string.termux_soft_keyboard_enabled_on,
        R.string.termux_soft_keyboard_enabled_off,
        settings.softKeyboard
    )
    SwitchItem(
        R.string.termux_soft_keyboard_enabled_only_if_no_hardware_title,
        R.string.termux_soft_keyboard_enabled_only_if_no_hardware_on,
        R.string.termux_soft_keyboard_enabled_only_if_no_hardware_off,
        settings.softKeyboardOnlyIfNoHardware
    )
}

@Composable
private fun TerminalViewScreen() {
    val context = LocalContext.current
    val setting = remember { terminalMarginAdjustmentSetting(context) } ?: return

    CategoryHeader(R.string.termux_terminal_view_view_header)
    SwitchItem(
        R.string.termux_terminal_view_terminal_margin_adjustment_title,
        R.string.termux_terminal_view_terminal_margin_adjustment_on,
        R.string.termux_terminal_view_terminal_margin_adjustment_off,
        setting
    )
}

@Composable
private fun DebugScreen(settings: DebugSettings?) {
    if (settings == null) return

    CategoryHeader(R.string.termux_logging_header)
    LogLevelItem(settings)
    settings.keyLogging?.let {
        SwitchItem(
            R.string.termux_terminal_view_key_logging_enabled_title,
            R.string.termux_terminal_view_key_logging_enabled_on,
            R.string.termux_terminal_view_key_logging_enabled_off,
            it
        )
    }
    settings.pluginErrorNotifications?.let {
        SwitchItem(
            R.string.termux_plugin_error_notifications_enabled_title,
            R.string.termux_plugin_error_notifications_enabled_on,
            R.string.termux_plugin_error_notifications_enabled_off,
            it
        )
    }
    settings.crashReportNotifications?.let {
        SwitchItem(
            R.string.termux_crash_report_notifications_enabled_title,
            R.string.termux_crash_report_notifications_enabled_on,
            R.string.termux_crash_report_notifications_enabled_off,
            it
        )
    }
}

@Composable
private fun NavItem(@StringRes title: Int, @StringRes summary: Int, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(title)) },
        supportingContent = { Text(stringResource(summary)) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun CategoryHeader(@StringRes text: Int) {
    Text(
        text = stringResource(text),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SwitchItem(
    @StringRes title: Int,
    @StringRes summaryOn: Int,
    @StringRes summaryOff: Int,
    setting: BooleanSetting
) {
    var checked by remember { mutableStateOf(setting.get()) }

    ListItem(
        headlineContent = { Text(stringResource(title)) },
        supportingContent = { Text(stringResource(if (checked) summaryOn else summaryOff)) },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
        modifier = Modifier.toggleable(
            value = checked,
            role = Role.Switch,
            onValueChange = {
                checked = it
                setting.set(it)
            }
        )
    )
}

@Composable
private fun LogLevelItem(settings: DebugSettings) {
    val context = LocalContext.current
    val levels = remember { Logger.getLogLevelsArray() }
    val labels = remember { Logger.getLogLevelLabelsArray(context, levels, true) }
    var current by remember { mutableStateOf(settings.logLevel().toString()) }
    var dialogOpen by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(stringResource(R.string.termux_log_level_title)) },
        supportingContent = {
            val index = levels.indexOfFirst { it.toString() == current }
            Text(labels.getOrNull(index)?.toString().orEmpty())
        },
        modifier = Modifier.clickable { dialogOpen = true }
    )

    if (dialogOpen) {
        AlertDialog(
            onDismissRequest = { dialogOpen = false },
            title = { Text(stringResource(R.string.termux_log_level_title)) },
            text = {
                Column {
                    levels.forEachIndexed { index, value ->
                        val selected = value.toString() == current
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selected,
                                    role = Role.RadioButton,
                                    onClick = {
                                        current = value.toString()
                                        settings.setLogLevel(value.toString().toInt())
                                        dialogOpen = false
                                    }
                                )
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selected, onClick = null)
                            Spacer(Modifier.width(16.dp))
                            Text(labels[index].toString())
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { dialogOpen = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
}
