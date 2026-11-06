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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
            FilledTonalIconButton(
                onClick = { actions.onOpenSettings() },
                shapes = IconButtonDefaults.shapes(),
                modifier = Modifier.padding(start = 12.dp, top = 12.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings),
                    contentDescription = stringResource(R.string.action_open_settings)
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
            ) {
                itemsIndexed(state.items) { index, item ->
                    SessionRow(
                        item = item,
                        selected = index == state.currentIndex,
                        index = index,
                        count = state.items.size,
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SessionRow(
    item: SessionItem,
    selected: Boolean,
    index: Int,
    count: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    // A finished session is struck through, and red when it did not exit cleanly.
    val color = if (item.running || item.exitStatus == 0) Color.Unspecified else MaterialTheme.colorScheme.error
    val decoration = if (item.running) null else TextDecoration.LineThrough

    val headline = when {
        !item.hasSession -> "null session"
        item.name.isNotEmpty() -> item.name
        else -> item.title
    }
    val supporting = if (item.hasSession && item.name.isNotEmpty() && item.title.isNotEmpty()) item.title else null

    SegmentedListItem(
        checked = selected,
        onCheckedChange = { onClick() },
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        onLongClick = onLongClick,
        leadingContent = { NumberBadge(item.number) },
        supportingContent = if (supporting != null) {
            {
                Text(
                    text = supporting,
                    color = color,
                    fontStyle = FontStyle.Italic,
                    textDecoration = decoration
                )
            }
        } else {
            null
        }
    ) {
        Text(text = headline, color = color, textDecoration = decoration)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun NumberBadge(number: Int) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, MaterialShapes.Cookie9Sided.toShape()),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$number",
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.labelLargeEmphasized
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
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
        MaterialTheme.colorScheme.secondaryContainer
    }
    val content = if (filled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLargeIncreased)
            .background(container)
            .combinedClickable(role = Role.Button, onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 8.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(text),
            color = content,
            style = MaterialTheme.typography.labelLargeEmphasized,
            textAlign = TextAlign.Center
        )
    }
}
