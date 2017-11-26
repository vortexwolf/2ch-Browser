package ua.in.quireg.chan.mvp.views;

import com.arellomobile.mvp.MvpView;

/**
 * Created by Arcturus Mengsk on 11/21/2017, 2:31 PM.
 * 2ch-Browser
 */

public interface BoardsListView extends MvpView {

    void showUnrecognizedBoardError(String board);

}
