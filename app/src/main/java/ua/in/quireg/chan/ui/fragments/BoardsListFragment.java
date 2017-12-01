package ua.in.quireg.chan.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.ui.adapters.BoardsListAdapter;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.Websites;
import ua.in.quireg.chan.common.utils.CompatibilityUtils;
import ua.in.quireg.chan.models.domain.BoardModel;
import ua.in.quireg.chan.models.presentation.SectionEntity;
import ua.in.quireg.chan.mvp.presenters.BoardsListPresenter;
import ua.in.quireg.chan.mvp.views.BoardsListView;


public class BoardsListFragment extends MvpAppCompatFragment implements BoardsListView {

    @Inject MainApplication mMainApplication;
    @InjectPresenter BoardsListPresenter mBoardsListPresenter;

    @BindView(R.id.pick_board_button) protected Button mPickBoardButton;
    @BindView(R.id.pick_board_input) protected EditText mPickBoardInput;

    protected ListView mListView;
    protected BoardsListAdapter mBoardsListAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        MainApplication.getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mBoardsListAdapter = new BoardsListAdapter(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.pick_board_view, container, false);
        mListView = view.findViewById(android.R.id.list);

        View headerView = inflater.inflate(R.layout.pick_board_header, mListView, false);
        mListView.addHeaderView(headerView);

        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListView.setAdapter(mBoardsListAdapter);

        mListView.setOnItemClickListener((adapterView, view1, i, l) -> {
            BoardModel boardModel = (BoardModel) mListView.getItemAtPosition(i);
            mBoardsListPresenter.onBoardClick(boardModel);

        });

        registerForContextMenu(mListView);

        //TODO move to navigation controller
        ActionBar mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setTitle(getString(R.string.app_name));
        }

        mPickBoardButton.setOnClickListener(v -> {
            String enteredBoard = mPickBoardInput.getText().toString().trim();
            mBoardsListPresenter.onBoardClick(enteredBoard);
        });

        mPickBoardInput.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                String enteredBoard = mPickBoardInput.getText().toString().trim();

                mBoardsListPresenter.onBoardClick(enteredBoard);
                return true;
            }
            return false;
        });

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        BoardModel boardModel = (BoardModel) mListView.getItemAtPosition(info.position);

        menu.add(Menu.NONE, Constants.CONTEXT_MENU_COPY_URL, 0, getString(R.string.cmenu_copy_url));

        if (mBoardsListPresenter.isFavoriteBoard(boardModel)) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_ADD_FAVORITES, 0, getString(R.string.cmenu_add_to_favorites));
        } else {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_REMOVE_FAVORITES, 0, getString(R.string.cmenu_remove_from_favorites));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        BoardModel model = (BoardModel) mListView.getItemAtPosition(menuInfo.position);

        if(model == null){
            Timber.e("ListView returned null at position %d!", menuInfo.position);
            return false;
        }

        switch (item.getItemId()) {
            case Constants.CONTEXT_MENU_COPY_URL: {
                String uri = Websites.getDefault().getUrlBuilder().getPageUrlHtml(model.getId(), 0);

                CompatibilityUtils.copyText(getActivity(), uri, uri);

                mMainApplication.showShortToast(uri);

                return true;
            }
            case Constants.CONTEXT_MENU_ADD_FAVORITES: {
                mBoardsListPresenter.addToFavorites(model);
                return true;
            }
            case Constants.CONTEXT_MENU_REMOVE_FAVORITES: {
                mBoardsListPresenter.removeFromFavorites(model);
                return true;
            }
        }

        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.pickboard, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.refresh_menu_id:
                mBoardsListPresenter.requestBoards(false);
                return true;
        }

        return false;
    }

    @Override
    public void setBoards(List<BoardModel> boardModels) {
        Timber.v("setBoards()");

        if(mBoardsListAdapter == null){
            Timber.e("No adapter registered");
            return;
        }

        String[] categorySequenceToBeShown = new String[]{
                "Игры",
                "Политика",
                "Японская культура",
                "Разное",
                "Творчество",
                "Тематика",
                "Техника и софт",
                "Взрослым",
                "Пользовательские"
        };

        String currentCategory = null;

        for (String category : categorySequenceToBeShown) {
            for (BoardModel board : boardModels) {
                // ignore all boards except of matching category.
                if (!board.getCategory().equals(category)) {
                    continue;
                }
                // add group header
                if (board.getCategory() != null && !board.getCategory().equals(currentCategory)) {
                    currentCategory = board.getCategory();
                    mBoardsListAdapter.add(new SectionEntity(currentCategory));
                }
                // add item
                mBoardsListAdapter.add(board);
            }
        }
        mBoardsListAdapter.notifyDataSetChanged();

    }

    @Override
    public void setFavBoards(List<BoardModel> favBoards) {
        Timber.v("setFavBoards()");

        if(mBoardsListAdapter == null){
            Timber.e("No adapter registered");
            return;
        }
        for (BoardModel favBoard:favBoards) {
            mBoardsListAdapter.addItemToFavoritesSection(favBoard);
        }
        mBoardsListAdapter.notifyDataSetChanged();
    }

    @Override
    public void clearBoards() {
        if(mBoardsListAdapter != null){
            mBoardsListAdapter.clear();
        }else {
            Timber.e("Illegal state");
        }
    }

    @Override
    public void addFavoriteBoard(BoardModel boardModel) {
        mBoardsListAdapter.addItemToFavoritesSection(boardModel);

    }

    @Override
    public void removeFavoriteBoard(BoardModel boardModel) {
        mBoardsListAdapter.removeItemFromFavoritesSection(boardModel);
    }

    @Override
    public void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null && getView() != null && getView().getRootView() != null){
            imm.hideSoftInputFromWindow(getView().getRootView().getWindowToken(), 0);
        }
    }

    @Override
    public void showBoardError(String board) {
        mMainApplication.showShortToast(getString(R.string.warning_enter_board));
    }
}
