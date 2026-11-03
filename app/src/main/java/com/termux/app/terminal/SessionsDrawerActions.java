package com.termux.app.terminal;

/**
 * What the sessions drawer can ask the activity to do. Indexes are positions in the sessions list.
 */
public interface SessionsDrawerActions {

    void onSessionClick(int index);

    void onSessionLongClick(int index);

    void onNewSession();

    /** Asks for a session name or a failsafe session. */
    void onNewSessionLongClick();

    void onOpenSettings();

    void onToggleKeyboard();

    /** Shows or hides the extra keys toolbar. */
    void onToggleKeyboardLongClick();

}
