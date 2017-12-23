package ua.in.quireg.chan.mvp.routing;

import ru.terrakok.cicerone.BaseRouter;
import ru.terrakok.cicerone.commands.Command;

/**
 * Created by Arcturus Mengsk on 12/20/2017, 6:14 AM.
 * 2ch-Browser
 */

public class MainRouter extends BaseRouter {

    public void execute(Command command) {
        executeCommand(command);
    }

    @Override
    protected void executeCommand(Command command) {
        super.executeCommand(command);
    }

}
