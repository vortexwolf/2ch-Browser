package ua.in.quireg.chan.mvp.views;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

import java.util.List;

import ua.in.quireg.chan.models.domain.BoardModel;

/**
 * Created by Arcturus Mengsk on 11/21/2017, 2:31 PM.
 * 2ch-Browser
 */
@StateStrategyType(SkipStrategy.class)
public interface BoardsListView extends MvpView {

    void setBoards(List<BoardModel> boardModels);

    void setFavBoards(List<BoardModel> boardModels);

    void clearBoards();

    void addFavoriteBoard(BoardModel boardModel);

    void removeFavoriteBoard(BoardModel boardModel);

    void hideSoftKeyboard();

    void showBoardError(String board);

}
