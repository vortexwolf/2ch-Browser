package ua.in.quireg.chan.mvp.views;

import com.arellomobile.mvp.MvpView;

import ua.in.quireg.chan.models.presentation.OpenTabModel;

/**
 * Created by Arcturus Mengsk on 2/15/2018, 2:52 PM.
 * 2ch-Browser
 */

public interface OpenTabsView extends MvpView {

    void add(OpenTabModel model);

    void remove(OpenTabModel model);

    void clearAll();
}
