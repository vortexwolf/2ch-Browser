package ua.in.quireg.chan.mvp.routing.commands;

import android.support.annotation.StringRes;

import ru.terrakok.cicerone.commands.Command;

/**
 * Created by Arcturus Mengsk on 12/21/2017, 12:55 AM.
 * 2ch-Browser
 */

public class SendShortToast implements Command {

    private int stringResource;

    public SendShortToast(@StringRes int i) {
        this.stringResource = i;
    }

    public int getToast() {
        return stringResource;
    }
}
