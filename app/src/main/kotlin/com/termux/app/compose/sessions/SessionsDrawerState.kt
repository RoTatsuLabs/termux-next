package com.termux.app.compose.sessions

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.termux.shared.termux.shell.command.runner.terminal.TermuxSession

/** What the drawer shows for one terminal session. */
internal class SessionItem(
    val number: Int,
    val name: String,
    val title: String,
    val running: Boolean,
    val exitStatus: Int,
    val hasSession: Boolean
)

/** The sessions shown in the drawer. The activity updates it and the drawer reads it. */
class SessionsDrawerState {

    internal var items by mutableStateOf<List<SessionItem>>(emptyList())
        private set

    internal var currentIndex by mutableIntStateOf(-1)
        private set

    fun update(sessions: List<TermuxSession>) {
        items = sessions.mapIndexed { index, termuxSession ->
            val session = termuxSession.getTerminalSession()
            if (session == null) {
                SessionItem(index + 1, "", "", running = false, exitStatus = 0, hasSession = false)
            } else {
                SessionItem(
                    number = index + 1,
                    name = session.mSessionName.orEmpty(),
                    title = session.getTitle().orEmpty(),
                    running = session.isRunning(),
                    exitStatus = session.getExitStatus(),
                    hasSession = true
                )
            }
        }
    }

    fun setCurrent(index: Int) {
        currentIndex = index
    }
}
