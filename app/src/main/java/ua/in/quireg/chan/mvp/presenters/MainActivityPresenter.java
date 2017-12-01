package ua.in.quireg.chan.mvp.presenters;

import android.support.v4.app.FragmentManager;

/**
 * Created by Arcturus Mengsk on 12/1/2017, 10:33 AM.
 * 2ch-Browser
 */

public interface MainActivityPresenter {

    void updateTabSelection(int position);

    void onBackPressed();

    void showToast(String message);

    void setFragmentManager(FragmentManager fragmentManager);
}
