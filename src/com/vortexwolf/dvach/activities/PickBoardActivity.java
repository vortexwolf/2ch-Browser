package com.vortexwolf.dvach.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.adapters.BoardsListAdapter;
import com.vortexwolf.dvach.asynctasks.DownloadFileTask;
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
import com.vortexwolf.dvach.models.presentation.OpenTabModel;
import com.vortexwolf.dvach.models.presentation.SectionEntity;
import com.vortexwolf.dvach.services.BrowserLauncher;
import com.vortexwolf.dvach.services.Tracker;
import com.vortexwolf.dvach.services.presentation.DvachUriBuilder;
import com.vortexwolf.dvach.services.presentation.EditTextDialog;
import com.vortexwolf.dvach.settings.ApplicationSettings;

public class PickBoardActivity extends ListActivity {

    public static final String TAG = "PickBoardActivity";

    private static final Pattern boardCodePattern = Pattern.compile("^/?\\w+/?$");
    
    private MainApplication mApplication;
    private Tracker mTracker;
    private FavoritesDataSource mFavoritesDatasource;
    private ApplicationSettings mSettings;
    private DvachUriBuilder mDvachUriBuilder;
    private BoardsListAdapter mAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mApplication = (MainApplication) this.getApplication();
        this.mTracker = this.mApplication.getTracker();
        this.mSettings = this.mApplication.getSettings();
        this.mFavoritesDatasource = Factory.getContainer().resolve(FavoritesDataSource.class);
    	this.mDvachUriBuilder = Factory.getContainer().resolve(DvachUriBuilder.class);
    	
        this.mTracker.clearBoardVar();
        this.mTracker.trackActivityView(TAG);

        this.resetUI();

        ArrayList<IBoardListEntity> boards = this.createBoardList();

        this.mAdapter = new BoardsListAdapter(this, boards);
        this.addFavoritesToAdapter();
        this.setListAdapter(this.mAdapter);

        this.setTitle(this.getString(R.string.pick_board_title));
    }

    private ArrayList<IBoardListEntity> createBoardList() {
        ArrayList<IBoardListEntity> boards = new ArrayList<IBoardListEntity>();

        // default boards
        this.addBoardsToTheList(R.array.pickboard_boards, boards);
        
        // hidden boards
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
    
    private void addFavoritesToAdapter(){
        List<FavoritesEntity> favoriteBoards = this.mFavoritesDatasource.getFavoriteBoards();
        
        if (favoriteBoards.size() > 0) {
            for (FavoritesEntity f : favoriteBoards) {            	
                String boardName = UriUtils.getBoardName(Uri.parse(f.getUrl()));
                this.mAdapter.addItemToFavoritesSection(boardName);
            }
        }
    }

    private void resetUI() {
        this.setTheme(this.mApplication.getSettings().getTheme());
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
        BoardEntity item = (BoardEntity)this.mAdapter.getItem(info.position);

        menu.add(Menu.NONE, Constants.CONTEXT_MENU_COPY_URL, 0, this.getString(R.string.cmenu_copy_url));

        if (!this.mFavoritesDatasource.hasFavorites(this.mDvachUriBuilder.create2chBoardUri(item.getCode()).toString())) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_ADD_FAVORITES, 0, this.getString(R.string.cmenu_add_to_favorites));
        } else {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_REMOVE_FAVORITES, 0, this.getString(R.string.cmenu_remove_from_favorites));
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        BoardEntity model = (BoardEntity)this.mAdapter.getItem(menuInfo.position);

        switch (item.getItemId()) {
            case Constants.CONTEXT_MENU_COPY_URL: {
            	String uri = this.mDvachUriBuilder.create2chBoardUri(model.getCode()).toString();
                ClipboardManager clipboard = (ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);
                clipboard.setText(uri);

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
 
                dialog.setPositiveButtonListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
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
        }

        return true;
    }

    private void returnBoard(String boardCode) {
        if (!this.validateBoardCode(boardCode)) {
            AppearanceUtils.showToastMessage(this, this.getString(R.string.warning_enter_board));
            return;
        }

        boardCode = this.fixSlashes(boardCode);

        Intent intent = new Intent();
        intent.putExtra(Constants.EXTRA_SELECTED_BOARD, boardCode);
        this.setResult(RESULT_OK, intent);
        this.finish();
    }
    
    private void addToFavorites(String boardCode) {
    	String uri = this.mDvachUriBuilder.create2chBoardUri(boardCode).toString();
        this.mFavoritesDatasource.addToFavorites(boardCode, uri);
        this.mAdapter.addItemToFavoritesSection(boardCode);
    }
    
    private void removeFromFavorites(BoardEntity model) {
    	String uri = this.mDvachUriBuilder.create2chBoardUri(model.getCode()).toString();
        this.mFavoritesDatasource.removeFromFavorites(uri);
        this.mAdapter.removeItemFromFavoritesSection(model);
    }
    
    private boolean validateBoardCode(String boardCode) {
    	if(!StringUtils.isEmpty(boardCode) && boardCodePattern.matcher(boardCode).matches()) {
    		return true;
    	}
    	
    	return false;
    }
    
    private String fixSlashes(String boardCode) {
        String result = boardCode.replaceAll("/", "").toLowerCase();
        
        return result;
    }

}
