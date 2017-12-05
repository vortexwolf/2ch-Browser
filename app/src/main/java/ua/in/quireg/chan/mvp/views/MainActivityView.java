package ua.in.quireg.chan.mvp.views;

import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;

/**
 * Created by Arcturus Mengsk on 12/1/2017, 6:47 AM.
 * 2ch-Browser
 */

public interface MainActivityView {

    FragmentManager getSupportFragmentManager();

    void registerOnTabSelectedListener(TabLayout.OnTabSelectedListener listener);

    void updateToolbar(boolean isRootFrag);

    void updateTabSelection(int currentTab);

    void showToast(String message);

}
