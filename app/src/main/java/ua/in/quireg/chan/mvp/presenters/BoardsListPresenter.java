package ua.in.quireg.chan.mvp.presenters;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;

import ua.in.quireg.chan.mvp.views.BoardsListView;

/**
 * Created by Arcturus on 11/21/2017.
 */

@InjectViewState
public class BoardsListPresenter extends MvpPresenter<BoardsListView> {

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();

    }
}
