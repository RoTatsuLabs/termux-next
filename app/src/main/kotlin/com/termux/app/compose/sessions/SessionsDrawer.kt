@file:JvmName("SessionsDrawerUi")

package com.termux.app.compose.sessions

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.termux.R
import com.termux.app.compose.theme.TermuxTheme
import com.termux.app.terminal.SessionsDrawerActions

/** Sets the sessions drawer as the content of [view]. */
fun setSessionsDrawerContent(view: ComposeView, state: SessionsDrawerState, actions: SessionsDrawerActions) {
    view.setContent {
        TermuxTheme {
            SessionsDrawer(state, actions)
        }
    }
}

@Composable
private fun SessionsDrawer(state: SessionsDrawerState, actions: SessionsDrawerActions) {
    val listState = rememberLazyListState()

    // Keep the current session in view, also when a session was just added.
    LaunchedEffect(state.currentIndex, state.items.size) {
        val index = state.currentIndex
        if (index in state.items.indices) listState.animateScrollToItem(index)
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            // Taps on empty drawer space must not reach the terminal underneath.
            .pointerInput(Unit) { detectTapGestures { } },
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(Modifier.fillMaxSize()) {
            IconButton(
                onClick = { actions.onOpenSettings() },
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings),
                    contentDescription = stringResource(R.string.action_open_settings)
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                itemsIndexed(state.items) { index, item ->
                    SessionRow(
                        item = item,
                        selected = index == state.currentIndex,
                        onClick = { actions.onSessionClick(index) },
                        onLongClick = { actions.onSessionLongClick(index) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DrawerButton(
                    text = R.string.action_toggle_soft_keyboard,
                    filled = false,
                    onClick = { actions.onToggleKeyboard() },
                    onLongClick = { actions.onToggleKeyboardLongClick() },
                    modifier = Modifier.weight(1f)
                )
                DrawerButton(
                    text = R.string.action_new_session,
                    filled = true,
                    onClick = { actions.onNewSession() },
                    onLongClick = { actions.onNewSessionLongClick() },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun SessionItem.label(): AnnotatedString {
    if (!hasSession) return AnnotatedString("null session")

    return buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append("[$number] $name")
        }
        if (title.isNotEmpty()) {
            if (name.isNotEmpty()) append("\n")
            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                append(title)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SessionRow(item: SessionItem, selected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val container = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
    val defaultColor = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    // A finished session is struck through, and red when it did not exit cleanly.
    val color = if (item.running || item.exitStatus == 0) defaultColor else MaterialTheme.colorScheme.error

    Text(
        text = item.label(),
        color = color,
        textDecoration = if (item.running) null else TextDecoration.LineThrough,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(MaterialTheme.shapes.large)
            .background(container)
            .semantics { this.selected = selected }
            .combinedClickable(role = Role.Button, onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DrawerButton(
    @StringRes text: Int,
    filled: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = if (filled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val content = if (filled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(container)
            .combinedClickable(role = Role.Button, onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 8.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(text),
            color = content,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center
        )
    }
}
