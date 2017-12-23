package ua.in.quireg.chan.mvp.routing.commands;

import android.support.v4.app.Fragment;

import ru.terrakok.cicerone.commands.Command;

/**
 * Created by Arcturus Mengsk on 12/21/2017, 12:09 AM.
 * 2ch-Browser
 */

public class PushFragment implements Command {

    private Fragment fragment;

    public PushFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public Fragment getFragment() {
        return fragment;
    }
}
