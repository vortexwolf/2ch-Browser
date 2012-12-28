package com.vortexwolf.dvach.activities;

import java.util.ArrayList;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.adapters.BoardsListAdapter;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.common.utils.StringUtils;
import com.vortexwolf.dvach.models.presentation.BoardEntity;
import com.vortexwolf.dvach.models.presentation.IBoardListEntity;
import com.vortexwolf.dvach.models.presentation.SectionEntity;
import com.vortexwolf.dvach.services.Tracker;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class PickBoardActivity extends ListActivity {

    public static final String TAG = "PickBoardActivity";

    private MainApplication mApplication;
    private Tracker mTracker;
    private BoardsListAdapter mAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mApplication = (MainApplication) this.getApplication();
        this.mTracker = this.mApplication.getTracker();

        this.mTracker.clearBoardVar();
        this.mTracker.trackActivityView(TAG);

        this.resetUI();

        ArrayList<IBoardListEntity> boards = new ArrayList<IBoardListEntity>();
        String[] entities = this.getResources().getStringArray(R.array.pickboard_boards);
        for (String entity : entities) {
            String[] parts = entity.split(";\\s?");
            if (parts.length == 1) {
                boards.add(new SectionEntity(parts[0]));
            } else if (parts.length == 2) {
                boards.add(new BoardEntity(parts[0], parts[1]));
            }
        }

        this.mAdapter = new BoardsListAdapter(this, boards.toArray(new IBoardListEntity[boards.size()]));
        this.setListAdapter(this.mAdapter);

        this.setTitle(this.getString(R.string.pick_board_title));
    }

    private void resetUI() {
        this.setTheme(this.mApplication.getSettings().getTheme());
        this.setContentView(R.layout.pick_board_view);

        final Button pickBoardButton = (Button) findViewById(R.id.pick_board_button);
        final EditText pickBoardInput = (EditText) findViewById(R.id.pick_board_input);

        pickBoardButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredBoard = pickBoardInput.getText().toString().trim();
                returnBoard(enteredBoard);
            }
        });

        pickBoardInput.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String enteredBoard = pickBoardInput.getText().toString().trim();
                    returnBoard(enteredBoard);
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        BoardEntity item = (BoardEntity) this.mAdapter.getItem(position);
        returnBoard(item.getCode());
    }

    private void returnBoard(String boardCode) {
        if (StringUtils.isEmpty(boardCode)) {
            AppearanceUtils.showToastMessage(this, getString(R.string.warning_enter_board));
            return;
        }

        // remove the slash if it was entered
        if (boardCode.charAt(0) == '/') {
            boardCode = boardCode.substring(1);
        }
        boardCode = boardCode.toLowerCase();

        Intent intent = new Intent();
        intent.putExtra(Constants.EXTRA_SELECTED_BOARD, boardCode);
        setResult(RESULT_OK, intent);
        finish();
    }

}
