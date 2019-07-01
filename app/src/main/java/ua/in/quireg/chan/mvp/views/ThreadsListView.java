package ua.in.quireg.chan.mvp.views;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.AddToEndStrategy;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

import java.util.List;

import ua.in.quireg.chan.models.presentation.IThreadListEntity;

/**
 * Created by Arcturus Mengsk on 3/17/2018, 4:33 AM.
 * 2ch-Browser
 */

public interface ThreadsListView extends MvpView {

    @StateStrategyType(AddToEndStrategy.class)
    void showThreads(List<IThreadListEntity> threads);

    @StateStrategyType(SingleStateStrategy.class)
    void setList(List<IThreadListEntity> threads);

    @StateStrategyType(SingleStateStrategy.class)
    void clearList();

    @StateStrategyType(AddToEndSingleStrategy.class)
    void setListPosition(int position);

    @StateStrategyType(SingleStateStrategy.class)
    void startLoadingFirstTime();

    @StateStrategyType(SingleStateStrategy.class)
    void stopLoadingFirstTime();

    @StateStrategyType(SingleStateStrategy.class)
    void startLoadingNewPage();

    @StateStrategyType(SingleStateStrategy.class)
    void stopLoadingNewPage();


}
