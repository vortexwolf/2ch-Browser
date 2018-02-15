package ua.in.quireg.chan.ui.fragments;

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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.PresenterType;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.Websites;
import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.common.utils.CompatibilityUtils;
import ua.in.quireg.chan.models.presentation.BoardEntity;
import ua.in.quireg.chan.models.presentation.SectionEntity;
import ua.in.quireg.chan.mvp.presenters.BoardsListPresenter;
import ua.in.quireg.chan.mvp.views.BoardsListView;
import ua.in.quireg.chan.ui.adapters.BoardsListAdapter;

public class BoardsListFragment extends MvpAppCompatFragment implements BoardsListView {

    @InjectPresenter(type = PresenterType.WEAK)
    BoardsListPresenter mBoardsListPresenter;

    @BindView(R.id.pick_board_button) protected Button mPickBoardButton;
    @BindView(R.id.pick_board_input) protected EditText mPickBoardInput;

    protected ListView mListView;
    protected BoardsListAdapter mBoardsListAdapter;
    protected AppearanceUtils.ListViewPosition mListViewPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MainApplication.getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

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

        if (mBoardsListAdapter == null) {
            mBoardsListAdapter = new BoardsListAdapter(getContext());
        }

        mListView.setAdapter(mBoardsListAdapter);

        if (savedInstanceState != null) {
            mListViewPosition = (AppearanceUtils.ListViewPosition) savedInstanceState.getSerializable(Constants.EXTRA_LIST_VIEW_POSITION);

        }

        mListView.setOnItemClickListener((adapterView, view1, i, l) -> {
            BoardEntity boardEntity = (BoardEntity) mListView.getItemAtPosition(i);
            mBoardsListPresenter.onBoardClick(boardEntity);

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

                mPickBoardInput.clearFocus();
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
        BoardEntity boardEntity = (BoardEntity) mListView.getItemAtPosition(info.position);

        menu.add(Menu.NONE, Constants.CONTEXT_MENU_COPY_URL, 0, getString(R.string.cmenu_copy_url));

        if (!boardEntity.isFavorite) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_ADD_FAVORITES, 0, getString(R.string.cmenu_add_to_favorites));
        } else {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_REMOVE_FAVORITES, 0, getString(R.string.cmenu_remove_from_favorites));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        BoardEntity boardEntity = (BoardEntity) mListView.getItemAtPosition(menuInfo.position);

        if (boardEntity == null) {
            Timber.e("ListView returned null at position %d!", menuInfo.position);
            return false;
        }

        switch (item.getItemId()) {
            case Constants.CONTEXT_MENU_COPY_URL: {
                String uri = Websites.getDefault().getUrlBuilder().getPageUrlHtml(boardEntity.id, 0);

                CompatibilityUtils.copyText(getActivity(), uri, uri);

                Toast.makeText(getContext(), uri, Toast.LENGTH_SHORT).show();

                return true;
            }
            case Constants.CONTEXT_MENU_ADD_FAVORITES: {
                mBoardsListPresenter.addToFavorites(boardEntity);
                return true;
            }
            case Constants.CONTEXT_MENU_REMOVE_FAVORITES: {
                mBoardsListPresenter.removeFromFavorites(boardEntity);
                return true;
            }
        }

        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.pickboard, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.refresh_menu_id:
                mBoardsListPresenter.updateBoardsList(true);
                return true;
        }

        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        AppearanceUtils.ListViewPosition position = AppearanceUtils.getCurrentListPosition(mListView);
        outState.putSerializable(Constants.EXTRA_LIST_VIEW_POSITION, position);
    }

    @Override
    public void setBoards(List<BoardEntity> boardEntities) {

        if (mBoardsListAdapter == null) {
            Timber.e("No adapter registered");
            return;
        }

        mBoardsListAdapter.clear();

        String currentCategory = null;

        for (BoardEntity boardEntity : boardEntities) {

            if(boardEntity.isFavorite) {
                mBoardsListAdapter.addItemToFavoritesSection(boardEntity);
            }

            if(!boardEntity.isVisible) {
                continue;
            }

            // add group header
            if(boardEntity.category != null &&
                    (currentCategory == null || !boardEntity.category.equals(currentCategory))) {
                currentCategory = boardEntity.category;
                mBoardsListAdapter.add(new SectionEntity(currentCategory));
            }

            // add item
            mBoardsListAdapter.add(boardEntity);
        }

        mBoardsListAdapter.notifyDataSetChanged();

        //Restore listview position in case fragment was re-created.
        if (mListViewPosition != null) {
            mListView.setSelectionFromTop(mListViewPosition.position, mListViewPosition.top);
            mListViewPosition = null;
        }

    }

}
