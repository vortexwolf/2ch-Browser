package ua.in.quireg.chan.mvp.views;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.AddToEndStrategy;
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

import java.util.List;

import ua.in.quireg.chan.models.presentation.ThreadItemViewModel;

/**
 * Created by Arcturus Mengsk on 3/17/2018, 4:33 AM.
 * 2ch-Browser
 */

public interface ThreadsListView extends MvpView {

    @StateStrategyType(AddToEndStrategy.class)
    void showThreads(List<ThreadItemViewModel> threads);

    @StateStrategyType(SkipStrategy.class)
    void clearList();

    @StateStrategyType(AddToEndSingleStrategy.class)
    void setRecyclerViewPosition(int position);

}
