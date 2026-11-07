@file:JvmName("DialogHostUi")

package com.termux.app.compose.dialogs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.termux.R
import com.termux.app.compose.theme.TermuxTheme

/** Sets the dialogs requested through [state] as the content of [view]. The view itself draws nothing. */
fun setDialogHostContent(view: ComposeView, state: DialogHostState) {
    view.setContent {
        TermuxTheme {
            DialogHost(state)
        }
    }
}

@Composable
private fun DialogHost(state: DialogHostState) {
    val request = state.request ?: return
    val dismiss: () -> Unit = { state.dismiss() }

    when (request) {
        is TextInputRequest -> TextInputDialog(request, dismiss)
        is ConfirmRequest -> ConfirmDialog(request, dismiss)
        is MessageRequest -> MessageDialog(request, dismiss)
        is UrlsRequest -> UrlsDialog(request, dismiss)
        is ContextMenuRequest -> ContextMenuSheet(request, state)
    }
}

@Composable
private fun TextInputDialog(request: TextInputRequest, onDismiss: () -> Unit) {
    val initial = request.initialText.orEmpty()
    var value by remember(request) { mutableStateOf(TextFieldValue(initial, TextRange(initial.length))) }
    val focusRequester = remember { FocusRequester() }
    val neutralAction = request.onNeutral

    val submit: () -> Unit = {
        request.onPositive.onTextSet(value.text)
        onDismiss()
    }

    LaunchedEffect(request) { focusRequester.requestFocus() }

    AlertDialog(
        onDismissRequest = onDismiss,
        // Back still closes it, a tap outside does not, so typed text is not lost by accident.
        properties = DialogProperties(dismissOnClickOutside = false),
        title = { Text(stringResource(request.title)) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
        },
        confirmButton = {
            TextButton(onClick = submit) { Text(stringResource(request.positive)) }
        },
        dismissButton = {
            Row {
                if (neutralAction != null) {
                    TextButton(
                        onClick = {
                            neutralAction.onTextSet(value.text)
                            onDismiss()
                        }
                    ) { Text(stringResource(request.neutral)) }
                }
                TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
            }
        }
    )
}

@Composable
private fun ConfirmDialog(request: ConfirmRequest, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(stringResource(request.message)) },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    request.onConfirm.run()
                }
            ) { Text(stringResource(android.R.string.yes)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.no)) }
        }
    )
}

@Composable
private fun MessageDialog(request: MessageRequest, onDismiss: () -> Unit) {
    val title: (@Composable () -> Unit)? = if (request.title != 0) {
        { Text(stringResource(request.title)) }
    } else {
        null
    }
    val cancel: (@Composable () -> Unit)? = if (request.showCancel) {
        { TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) } }
    } else {
        null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = title,
        text = { Text(stringResource(request.message)) },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    request.onPositive?.run()
                }
            ) {
                Text(stringResource(if (request.positive != 0) request.positive else android.R.string.ok))
            }
        },
        dismissButton = cancel
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UrlsDialog(request: UrlsRequest, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.title_select_url_dialog)) },
        text = {
            LazyColumn(Modifier.heightIn(max = 400.dp)) {
                items(request.urls) { url ->
                    Text(
                        text = url,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                role = Role.Button,
                                onClick = {
                                    onDismiss()
                                    request.onCopy.onUrl(url)
                                },
                                onLongClick = {
                                    onDismiss()
                                    request.onOpen.onUrl(url)
                                }
                            )
                            .padding(vertical = 12.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ContextMenuSheet(request: ContextMenuRequest, state: DialogHostState) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    val count = request.entries.size

    // The action runs before the menu closes, as it needs state the closing clears.
    val pick: (ContextMenuEntry) -> Unit = { entry ->
        request.onSelect.onId(entry.id)
        state.dismiss(request)
    }

    ModalBottomSheet(
        onDismissRequest = { state.dismiss(request) },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            request.entries.forEachIndexed { index, entry ->
                val shapes = ListItemDefaults.segmentedShapes(index = index, count = count)
                val checked = entry.checked

                if (checked != null) {
                    SegmentedListItem(
                        checked = checked,
                        onCheckedChange = { pick(entry) },
                        enabled = entry.enabled,
                        shapes = shapes,
                        colors = colors,
                        trailingContent = { Switch(checked = checked, onCheckedChange = null) }
                    ) {
                        Text(entry.label)
                    }
                } else {
                    SegmentedListItem(
                        onClick = { pick(entry) },
                        enabled = entry.enabled,
                        shapes = shapes,
                        colors = colors
                    ) {
                        Text(entry.label)
                    }
                }
            }
        }
    }
}
