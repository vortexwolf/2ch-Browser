package ua.in.quireg.chan.mvp.views;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

import java.util.List;

import ua.in.quireg.chan.models.presentation.BoardEntity;

/**
 * Created by Arcturus Mengsk on 11/21/2017, 2:31 PM.
 * 2ch-Browser
 */

@StateStrategyType(SkipStrategy.class)
public interface BoardsListView extends MvpView {

    void setBoards(List<BoardEntity> boardModels);

}
