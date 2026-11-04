package com.termux.app.compose.dialogs

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.termux.shared.termux.interact.TextInputDialogUtils

/** Called with a URL picked in the URL dialog. */
fun interface UrlAction {
    fun onUrl(url: String)
}

/** Called with the id of an item picked in the context menu. */
fun interface IdAction {
    fun onId(id: Int)
}

/** One row of the context menu. [checked] is null for a plain row, and set for a row with a switch. */
class ContextMenuEntry(
    val id: Int,
    val label: String,
    val enabled: Boolean,
    val checked: Boolean?
)

internal sealed interface DialogRequest

internal class TextInputRequest(
    @StringRes val title: Int,
    val initialText: String?,
    @StringRes val positive: Int,
    val onPositive: TextInputDialogUtils.TextSetListener,
    @StringRes val neutral: Int,
    val onNeutral: TextInputDialogUtils.TextSetListener?
) : DialogRequest

internal class ConfirmRequest(
    @StringRes val message: Int,
    val onConfirm: Runnable
) : DialogRequest

internal class MessageRequest(
    @StringRes val title: Int,
    @StringRes val message: Int,
    @StringRes val positive: Int,
    val onPositive: Runnable?,
    val showCancel: Boolean
) : DialogRequest

internal class UrlsRequest(
    val urls: List<String>,
    val onCopy: UrlAction,
    val onOpen: UrlAction
) : DialogRequest

internal class ContextMenuRequest(
    val entries: List<ContextMenuEntry>,
    val onSelect: IdAction,
    val onClosed: Runnable
) : DialogRequest

/**
 * Dialogs requested by the activity and its clients. One dialog is shown at a time, a new request
 * replaces the current one. A resource id of 0 means the text is not used.
 */
class DialogHostState {

    internal var request by mutableStateOf<DialogRequest?>(null)
        private set

    /** A text field with a confirm button, an optional neutral button and cancel. */
    fun showTextInput(
        @StringRes title: Int,
        initialText: String?,
        @StringRes positive: Int,
        onPositive: TextInputDialogUtils.TextSetListener,
        @StringRes neutral: Int,
        onNeutral: TextInputDialogUtils.TextSetListener?
    ) {
        replace(TextInputRequest(title, initialText, positive, onPositive, neutral, onNeutral))
    }

    /** A yes or no question. */
    fun showConfirm(@StringRes message: Int, onConfirm: Runnable) {
        replace(ConfirmRequest(message, onConfirm))
    }

    /** A message with a confirm button, which defaults to OK, and an optional cancel button. */
    fun showMessage(
        @StringRes title: Int,
        @StringRes message: Int,
        @StringRes positive: Int,
        onPositive: Runnable?,
        showCancel: Boolean
    ) {
        replace(MessageRequest(title, message, positive, onPositive, showCancel))
    }

    /** URLs to pick from. A tap calls [onCopy], a long press calls [onOpen]. */
    fun showUrls(urls: List<String>, onCopy: UrlAction, onOpen: UrlAction) {
        replace(UrlsRequest(urls, onCopy, onOpen))
    }

    /**
     * A list of actions. [onSelect] runs first for the picked item, then the menu closes. [onClosed]
     * always runs once when the menu is gone, whether it was picked from, dismissed or replaced.
     */
    fun showContextMenu(entries: List<ContextMenuEntry>, onSelect: IdAction, onClosed: Runnable) {
        replace(ContextMenuRequest(entries, onSelect, onClosed))
    }

    internal fun dismiss() {
        replace(null)
    }

    /** Closes [expected] unless something else was shown in the meantime. */
    internal fun dismiss(expected: DialogRequest) {
        if (request === expected) replace(null)
    }

    private fun replace(next: DialogRequest?) {
        val previous = request
        request = next
        if (previous is ContextMenuRequest && previous !== next) previous.onClosed.run()
    }
}
