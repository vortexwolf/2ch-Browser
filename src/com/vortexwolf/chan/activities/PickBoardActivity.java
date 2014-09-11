package com.vortexwolf.chan.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import com.vortexwolf.chan.R;
import com.vortexwolf.chan.adapters.BoardsListAdapter;
import com.vortexwolf.chan.boards.dvach.DvachUriBuilder;
import com.vortexwolf.chan.boards.dvach.DvachUriParser;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.MainApplication;
import com.vortexwolf.chan.common.utils.AppearanceUtils;
import com.vortexwolf.chan.common.utils.CompatibilityUtils;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.db.FavoritesDataSource;
import com.vortexwolf.chan.db.FavoritesEntity;
import com.vortexwolf.chan.models.presentation.BoardEntity;
import com.vortexwolf.chan.models.presentation.BoardModel;
import com.vortexwolf.chan.models.presentation.SectionEntity;
import com.vortexwolf.chan.services.MyTracker;
import com.vortexwolf.chan.services.presentation.EditTextDialog;
import com.vortexwolf.chan.settings.ApplicationPreferencesActivity;
import com.vortexwolf.chan.settings.ApplicationSettings;
import com.vortexwolf.chan.settings.SettingsEntity;

public class PickBoardActivity extends ListActivity {

    public static final String TAG = "PickBoardActivity";

    private static final Pattern boardCodePattern = Pattern.compile("^/?\\w+/?$");

    private MyTracker mTracker = Factory.resolve(MyTracker.class);
    private FavoritesDataSource mFavoritesDatasource = Factory.resolve(FavoritesDataSource.class);
    private ApplicationSettings mSettings = Factory.resolve(ApplicationSettings.class);
    private DvachUriBuilder mDvachUriBuilder = Factory.resolve(DvachUriBuilder.class);
    private DvachUriParser mUriParser = Factory.resolve(DvachUriParser.class);

    private BoardsListAdapter mAdapter = null;
    private SettingsEntity mCurrentSettings = null;

    private final ArrayList<BoardModel> mBoards = new ArrayList<BoardModel>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mCurrentSettings = this.mSettings.getCurrentSettings();

        if (this.mSettings.getStartPage() != null && Intent.ACTION_MAIN.equals(this.getIntent().getAction())) {
            Intent openBoard = new Intent(this.getApplicationContext(), ThreadsListActivity.class);
            openBoard.setData(this.mDvachUriBuilder.createBoardUri(this.mSettings.getStartPage()));
            openBoard.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(openBoard);
            this.finish();
            return;
        }

        this.resetUI();

        this.parseAllBoards();

        this.mAdapter = new BoardsListAdapter(this);
        this.updateVisibleBoards(this.mAdapter);
        this.setListAdapter(this.mAdapter);

        this.setTitle(this.getString(R.string.pick_board_title));

        this.mTracker.trackActivityView(TAG);
    }

    @Override
    protected void onStart() {
        super.onStart();

        SettingsEntity prevSettings = this.mCurrentSettings;
        this.mCurrentSettings = this.mSettings.getCurrentSettings();

        if (this.mCurrentSettings.theme != prevSettings.theme) {
            this.finish();
            Intent i = new Intent(this.getIntent());
            this.startActivity(i);
            return;
        }

        if (this.mCurrentSettings.isDisplayAllBoards != prevSettings.isDisplayAllBoards) {
            this.updateVisibleBoards(this.mAdapter);
        }
    }

    private void parseAllBoards() {
        //TODO: загружать /makaba/mobile.fcgi?task=get_boards
        this.mBoards.addAll(this.parseBoardsList(R.array.pickboard_boards));
        this.mBoards.addAll(this.parseBoardsList(R.array.pickboard_boards_hidden));
    }

    private void updateVisibleBoards(BoardsListAdapter adapter) {
        adapter.clear();

        String currentGroup = null;
        for (BoardModel board : this.mBoards) {
            if (!board.isVisible && !this.mSettings.isDisplayAllBoards()) {
                continue; // ignore hidden boards
            }

            // add group header if necessary
            if (board.group != null && !board.group.equals(currentGroup)) {
                currentGroup = board.group;
                adapter.add(new SectionEntity(currentGroup));
            }

            // add item
            adapter.add(new BoardEntity(board.code, board.title));
        }

        // add favorite boards
        List<FavoritesEntity> favoriteBoards = this.mFavoritesDatasource.getFavoriteBoards(this.mUriParser);
        for (FavoritesEntity f : favoriteBoards) {
            Uri uri = Uri.parse(f.getUrl());
            String boardName = this.mUriParser.getBoardName(uri);
            adapter.addItemToFavoritesSection(boardName, this.findBoardByCode(boardName));
        }
    }

    private BoardModel findBoardByCode(String code) {
        for (BoardModel board : this.mBoards) {
            if (board.code.equals(code)) {
                return board;
            }
        }

        return null;
    }

    private ArrayList<BoardModel> parseBoardsList(int arrayId) {
        ArrayList<BoardModel> boards = new ArrayList<BoardModel>();

        String[] entities = this.getResources().getStringArray(arrayId);
        String currentGroup = null;
        for (String entity : entities) {
            String[] parts = entity.split(";\\s?");
            if (parts.length == 1) {
                currentGroup = parts[0];
            } else if (parts.length >= 2) {
                boolean isVisible = parts.length >= 3 && parts[2].equals("1");
                boards.add(new BoardModel(parts[0], parts[1], isVisible, currentGroup));
            }
        }

        return boards;
    }

    private void resetUI() {
        this.setTheme(this.mSettings.getTheme());
        this.setContentView(R.layout.pick_board_view);

        this.registerForContextMenu(this.getListView());

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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        BoardEntity item = (BoardEntity) this.mAdapter.getItem(info.position);

        menu.add(Menu.NONE, Constants.CONTEXT_MENU_COPY_URL, 0, this.getString(R.string.cmenu_copy_url));

        if (!this.mFavoritesDatasource.hasFavorites(this.mDvachUriBuilder.createBoardUri(item.getCode()).toString())) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_ADD_FAVORITES, 0, this.getString(R.string.cmenu_add_to_favorites));
        } else {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_REMOVE_FAVORITES, 0, this.getString(R.string.cmenu_remove_from_favorites));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        BoardEntity model = (BoardEntity) this.mAdapter.getItem(menuInfo.position);

        switch (item.getItemId()) {
            case Constants.CONTEXT_MENU_COPY_URL: {
                String uri = this.mDvachUriBuilder.createBoardUri(model.getCode()).toString();

                CompatibilityUtils.copyText(this, uri, uri);

                AppearanceUtils.showToastMessage(this, uri);
                return true;
            }
            case Constants.CONTEXT_MENU_ADD_FAVORITES: {
                this.addToFavorites(model.getCode());
                return true;
            }
            case Constants.CONTEXT_MENU_REMOVE_FAVORITES: {
                this.removeFromFavorites(model);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.pickboard, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_add_board_id:
                final EditTextDialog dialog = new EditTextDialog(this);
                dialog.setTitle(this.getString(R.string.menu_add_favorites));
                dialog.setHint(this.getString(R.string.pick_board_input_hint));

                dialog.setPositiveButtonListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        String boardCode = dialog.getText();
                        boolean success = PickBoardActivity.this.validateBoardCode(boardCode);

                        if (success) {
                            PickBoardActivity.this.addToFavorites(boardCode);
                            dialog.dismiss();
                        } else {
                            AppearanceUtils.showToastMessage(PickBoardActivity.this, PickBoardActivity.this.getString(R.string.warning_enter_board));
                        }
                    }
                });

                dialog.show();
                break;
            case R.id.preferences_menu_id:
                Intent preferencesIntent = new Intent(this.getApplicationContext(), ApplicationPreferencesActivity.class);
                this.startActivity(preferencesIntent);
                break;
            case R.id.tabs_menu_id:
                Intent openTabsIntent = new Intent(this.getApplicationContext(), 
                        Constants.SDK_VERSION >= 4 ? TabsHistoryBookmarksActivity.class : TabsHistoryBookmarksCompActivity.class);
                this.startActivity(openTabsIntent);
                break;
        }

        return true;
    }

    private void returnBoard(String boardCode) {
        if (!this.validateBoardCode(boardCode)) {
            AppearanceUtils.showToastMessage(this, this.getString(R.string.warning_enter_board));
            return;
        }

        boardCode = this.fixSlashes(boardCode);

        Intent intent = new Intent(this.getApplicationContext(), ThreadsListActivity.class);
        intent.setData(this.mDvachUriBuilder.createBoardUri(boardCode));
        this.startActivity(intent);
    }

    private void addToFavorites(String boardCode) {
        String uri = this.mDvachUriBuilder.createBoardUri(boardCode).toString();
        this.mFavoritesDatasource.addToFavorites(boardCode, uri);
        this.mAdapter.addItemToFavoritesSection(boardCode, this.findBoardByCode(boardCode));
    }

    private void removeFromFavorites(BoardEntity model) {
        String uri = this.mDvachUriBuilder.createBoardUri(model.getCode()).toString();
        this.mFavoritesDatasource.removeFromFavorites(uri);
        this.mAdapter.removeItemFromFavoritesSection(model);
    }

    private boolean validateBoardCode(String boardCode) {
        if (!StringUtils.isEmpty(boardCode) && boardCodePattern.matcher(boardCode).matches()) {
            return true;
        }

        return false;
    }

    private String fixSlashes(String boardCode) {
        String result = boardCode.replaceAll("/", "").toLowerCase();

        return result;
    }

}
