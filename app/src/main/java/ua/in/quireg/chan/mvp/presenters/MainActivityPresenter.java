package ua.in.quireg.chan.mvp.presenters;

import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;

import javax.inject.Inject;

import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.mvp.routing.MainRouter;
import ua.in.quireg.chan.mvp.routing.commands.NavigateBackwards;
import ua.in.quireg.chan.mvp.routing.commands.SwitchTab;
import ua.in.quireg.chan.mvp.views.MainActivityView;

/**
 * Created by Arcturus Mengsk on 12/1/2017, 6:36 AM.
 * 2ch-Browser
 */

@InjectViewState
public class MainActivityPresenter extends MvpPresenter<MainActivityView> {

    @Inject MainRouter mMainRouter;

    public MainActivityPresenter() {
        MainApplication.getAppComponent().inject(this);
    }

    public void onTabSelected(TabLayout.Tab tab) {

        getViewState().updateTabSelection(tab.getPosition());
        mMainRouter.execute(new SwitchTab(tab.getPosition()));
    }

    public void onBackPressed() {
        mMainRouter.execute(new NavigateBackwards());
    }

    public void updateToolbarControls(boolean isRootFrag) {
        getViewState().updateToolbarControls(isRootFrag);
    }

    public void updateTabSelection(int currentTab) {
        getViewState().updateTabSelection(currentTab);
    }

    public void exitApplication() {
        getViewState().exitApplication();
    }

    public void sendShortToast(@StringRes int stringId) {
        getViewState().showSystemMessage(stringId);
    }


}
