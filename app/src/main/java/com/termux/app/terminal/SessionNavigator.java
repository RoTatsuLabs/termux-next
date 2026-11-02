package com.termux.app.terminal;

/**
 * Opens, closes and locks the sessions list that is shown next to the terminal.
 */
public interface SessionNavigator {

    void open();

    void close();

    boolean isOpen();

    /** Locked means the list stays closed and cannot be opened, for example while copying text. */
    void setLocked(boolean locked);

}
