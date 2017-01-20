package com.vortexwolf.chan.interfaces;

import com.vortexwolf.chan.models.domain.BoardModel;

import java.util.List;

public interface IBoardsListCallback {
    void listUpdated(List<BoardModel> newBoards);
}
