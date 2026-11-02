package com.termux.app.terminal;

import android.annotation.SuppressLint;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;

/** A {@link SessionNavigator} backed by the left {@link DrawerLayout} drawer. */
public class DrawerSessionNavigator implements SessionNavigator {

    private final DrawerLayout mDrawer;

    public DrawerSessionNavigator(@NonNull DrawerLayout drawer) {
        mDrawer = drawer;
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public void open() {
        mDrawer.openDrawer(Gravity.LEFT);
    }

    @Override
    public void close() {
        mDrawer.closeDrawers();
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public boolean isOpen() {
        return mDrawer.isDrawerOpen(Gravity.LEFT);
    }

    @Override
    public void setLocked(boolean locked) {
        mDrawer.setDrawerLockMode(locked ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED : DrawerLayout.LOCK_MODE_UNLOCKED);
    }

}
