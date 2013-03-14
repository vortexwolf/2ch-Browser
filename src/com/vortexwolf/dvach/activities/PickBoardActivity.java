package com.vortexwolf.dvach.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.adapters.BoardsListAdapter;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.Factory;
import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.common.utils.StringUtils;
import com.vortexwolf.dvach.common.utils.UriUtils;
import com.vortexwolf.dvach.db.FavoritesDataSource;
import com.vortexwolf.dvach.db.FavoritesEntity;
import com.vortexwolf.dvach.models.presentation.BoardEntity;
import com.vortexwolf.dvach.models.presentation.IBoardListEntity;
import com.vortexwolf.dvach.models.presentation.SectionEntity;
import com.vortexwolf.dvach.services.Tracker;
import com.vortexwolf.dvach.settings.ApplicationSettings;

public class PickBoardActivity extends ListActivity {

    public static final String TAG = "PickBoardActivity";

    private MainApplication mApplication;
    private Tracker mTracker;
    private FavoritesDataSource mFavoritesDataSource;
    private ApplicationSettings mSettings;
    private BoardsListAdapter mAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mApplication = (MainApplication) this.getApplication();
        this.mTracker = this.mApplication.getTracker();
        this.mSettings = this.mApplication.getSettings();
        this.mFavoritesDataSource = Factory.getContainer().resolve(FavoritesDataSource.class);

        this.mTracker.clearBoardVar();
        this.mTracker.trackActivityView(TAG);

        this.resetUI();

        ArrayList<IBoardListEntity> boards = this.createBoardList();

        this.mAdapter = new BoardsListAdapter(this, boards.toArray(new IBoardListEntity[boards.size()]));
        this.setListAdapter(this.mAdapter);

        this.setTitle(this.getString(R.string.pick_board_title));
    }

    private ArrayList<IBoardListEntity> createBoardList() {
        ArrayList<IBoardListEntity> boards = new ArrayList<IBoardListEntity>();

        // favorite boards
        List<FavoritesEntity> favoriteBoards = this.mFavoritesDataSource.getFavoriteBoards();
        if (favoriteBoards.size() > 0) {
            boards.add(new SectionEntity(this.getString(R.string.favorites)));

            for (FavoritesEntity f : favoriteBoards) {
                String boardName = UriUtils.getBoardName(Uri.parse(f.getUrl()));
                boards.add(new BoardEntity(boardName, boardName));
            }
        }

        // default boards
        this.addBoardsToTheList(R.array.pickboard_boards, boards);
        if (this.mSettings.isDisplayAllBoards()) {
            this.addBoardsToTheList(R.array.pickboard_boards_hidden, boards);
        }

        return boards;
    }

    private void addBoardsToTheList(int arrayId, ArrayList<IBoardListEntity> boards) {
        String[] entities = this.getResources().getStringArray(arrayId);
        for (String entity : entities) {
            String[] parts = entity.split(";\\s?");
            if (parts.length == 1) {
                boards.add(new SectionEntity(parts[0]));
            } else if (parts.length == 2) {
                boards.add(new BoardEntity(parts[0], parts[1]));
            }
        }
    }

    private void resetUI() {
        this.setTheme(this.mApplication.getSettings().getTheme());
        this.setContentView(R.layout.pick_board_view);

        final Button pickBoardButton = (Button) this.findViewById(R.id.pick_board_button);
        final EditText pickBoardInput = (EditText) this.findViewById(R.id.pick_board_input);

        pickBoardButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredBoard = pickBoardInput.getText().toString().trim();
                PickBoardActivity.this.returnBoard(enteredBoard);
            }
        });

        pickBoardInput.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String enteredBoard = pickBoardInput.getText().toString().trim();
                    PickBoardActivity.this.returnBoard(enteredBoard);
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        BoardEntity item = (BoardEntity) this.mAdapter.getItem(position);
        this.returnBoard(item.getCode());
    }

    private void returnBoard(String boardCode) {
        if (StringUtils.isEmpty(boardCode)) {
            AppearanceUtils.showToastMessage(this, this.getString(R.string.warning_enter_board));
            return;
        }

        // remove the slash if it was entered
        if (boardCode.charAt(0) == '/') {
            boardCode = boardCode.substring(1);
        }
        boardCode = boardCode.toLowerCase();

        Intent intent = new Intent();
        intent.putExtra(Constants.EXTRA_SELECTED_BOARD, boardCode);
        this.setResult(RESULT_OK, intent);
        this.finish();
    }

}
