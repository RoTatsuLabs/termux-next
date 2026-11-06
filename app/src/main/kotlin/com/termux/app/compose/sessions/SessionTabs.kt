@file:JvmName("SessionTabsUi")

package com.termux.app.compose.sessions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.termux.R
import com.termux.app.compose.theme.TermuxTheme
import com.termux.app.terminal.SessionsDrawerActions

/**
 * Sets a bar that switches between sessions as the content of [view]. It has no height while there
 * is only one session, so a single session keeps all of the screen.
 */
fun setSessionTabsContent(view: ComposeView, state: SessionsDrawerState, actions: SessionsDrawerActions) {
    view.setContent {
        TermuxTheme {
            SessionTabs(state, actions)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SessionTabs(state: SessionsDrawerState, actions: SessionsDrawerActions) {
    if (state.items.size < 2) return

    val listState = rememberLazyListState()

    LaunchedEffect(state.currentIndex, state.items.size) {
        val index = state.currentIndex
        if (index in state.items.indices) listState.animateScrollToItem(index)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LazyRow(
                modifier = Modifier.weight(1f),
                state = listState,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(state.items) { index, item ->
                    SessionTab(
                        item = item,
                        selected = index == state.currentIndex,
                        onClick = { actions.onSessionClick(index) },
                        onLongClick = { actions.onSessionLongClick(index) }
                    )
                }
            }

            FilledTonalIconButton(
                onClick = { actions.onNewSession() },
                shapes = IconButtonDefaults.shapes(),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.action_new_session)
                )
            }
        }
    }
}

private fun SessionItem.tabLabel(): String {
    if (!hasSession) return "null"

    val text = name.ifEmpty { title }
    return if (text.isEmpty()) "$number" else "$number: $text"
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SessionTab(item: SessionItem, selected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val container = if (selected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        Color.Transparent
    }
    val defaultColor = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    // Same as the drawer: struck through when finished, red when it did not exit cleanly.
    val color = if (item.running || item.exitStatus == 0) defaultColor else MaterialTheme.colorScheme.error

    Text(
        text = item.tabLabel(),
        color = color,
        style = if (selected) MaterialTheme.typography.labelLargeEmphasized else MaterialTheme.typography.labelLarge,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textDecoration = if (item.running) null else TextDecoration.LineThrough,
        modifier = Modifier
            .widthIn(max = 160.dp)
            .clip(MaterialTheme.shapes.extraLargeIncreased)
            .background(container)
            .semantics { this.selected = selected }
            .combinedClickable(role = Role.Tab, onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    )
}
