package ua.in.quireg.chan.mvp.views;

import android.support.annotation.StringRes;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

/**
 * Created by Arcturus Mengsk on 12/1/2017, 6:47 AM.
 * 2ch-Browser
 */
@StateStrategyType(SkipStrategy.class)
public interface MainActivityView extends MvpView {

    void onBackPressed();

    void updateToolbarTitle(String title);

    void updateToolbarControls(boolean isRootFrag);

    void updateTabSelection(int currentTab);

    void showSystemMessage(@StringRes int i);

    void exitApplication();

}
