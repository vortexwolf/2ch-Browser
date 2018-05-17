package ua.in.quireg.chan.mvp.views;

import com.arellomobile.mvp.MvpView;

import java.util.List;

import ua.in.quireg.chan.models.presentation.ThreadItemViewModel;

/**
 * Created by Arcturus Mengsk on 3/17/2018, 4:33 AM.
 * 2ch-Browser
 */

public interface ThreadsListView extends MvpView {

    void showThreads(List<ThreadItemViewModel> threads);

}
