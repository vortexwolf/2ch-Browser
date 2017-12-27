package ua.in.quireg.chan.mvp.routing;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;

import ru.terrakok.cicerone.BaseRouter;
import ru.terrakok.cicerone.commands.Command;
import ua.in.quireg.chan.mvp.routing.commands.NavigateBackwards;
import ua.in.quireg.chan.mvp.routing.commands.NavigateBoard;
import ua.in.quireg.chan.mvp.routing.commands.NavigateThread;
import ua.in.quireg.chan.mvp.routing.commands.PushFragment;
import ua.in.quireg.chan.mvp.routing.commands.SendShortToast;
import ua.in.quireg.chan.mvp.routing.commands.SwitchTab;

/**
 * Created by Arcturus Mengsk on 12/20/2017, 6:14 AM.
 * 2ch-Browser
 */

public final class MainRouter extends BaseRouter {

    public void showSystemMessage(@StringRes int stringId) {
        executeCommand(new SendShortToast(stringId));
    }

    public void navigateBoard(String mWebsite, String boardCode, boolean preferDeserialized) {
        executeCommand(new NavigateBoard(mWebsite, boardCode, preferDeserialized));
    }

    public void onBackPressed() {
        executeCommand(new NavigateBackwards());
    }

    public void switchTab(int position) {
        executeCommand(new SwitchTab(position));
    }

    public void pushFragment(Fragment f) {
        executeCommand(new PushFragment(f));
    }

    public void navigateThread(String website, String boardName, String thread, @Nullable String subject, @Nullable String post, boolean preferDeserialized) {
        executeCommand(new NavigateThread(website, boardName, thread, subject, post, preferDeserialized));
    }

    @Override
    protected void executeCommand(Command command) {
        super.executeCommand(command);
    }

}
