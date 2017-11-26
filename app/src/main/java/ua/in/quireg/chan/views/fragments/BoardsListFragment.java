package ua.in.quireg.chan.views.fragments;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.Websites;
import ua.in.quireg.chan.models.presentation.BoardEntity;
import ua.in.quireg.chan.mvp.presenters.BoardsListPresenter;
import ua.in.quireg.chan.mvp.views.BoardsListView;
import ua.in.quireg.chan.services.presentation.EditTextDialog;


public class BoardsListFragment extends MvpAppCompatFragment implements BoardsListView {


    @Inject MainApplication mMainApplication;
    @InjectPresenter BoardsListPresenter mBoardsListPresenter;

    @BindView(R.id.pick_board_button) protected Button mPickBoardButton;
    @BindView(R.id.pick_board_input) protected EditText mPickBoardInput;

    protected ListView mListView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainApplication.getComponent().inject(this);

        setHasOptionsMenu(true);

    }

    @Override
    @SuppressLint("InflateParams")
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

        mListView.setAdapter(mBoardsListPresenter.getBoardListAdapter());

        mListView.setOnItemClickListener((adapterView, view1, i, l) -> {
            BoardEntity item = (BoardEntity) mListView.getItemAtPosition(i);
            mBoardsListPresenter.checkAndNavigateBoard(item.getCode());

        });

        registerForContextMenu(mListView);


        //TODO move to navigation controller
        ActionBar mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setTitle(getString(R.string.app_name));
        }


        mPickBoardButton.setOnClickListener(v -> {
            String enteredBoard = mPickBoardInput.getText().toString().trim();
            mBoardsListPresenter.checkAndNavigateBoard(enteredBoard);
        });

        mPickBoardInput.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                String enteredBoard = mPickBoardInput.getText().toString().trim();
                mBoardsListPresenter.checkAndNavigateBoard(enteredBoard);
                return true;
            }
            return false;
        });

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        BoardEntity item = (BoardEntity) mListView.getItemAtPosition(info.position);

        menu.add(Menu.NONE, Constants.CONTEXT_MENU_COPY_URL, 0, getString(R.string.cmenu_copy_url));

        if (mBoardsListPresenter.isFavouriteBoard(item)) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_ADD_FAVORITES, 0, getString(R.string.cmenu_add_to_favorites));
        } else {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_REMOVE_FAVORITES, 0, getString(R.string.cmenu_remove_from_favorites));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        BoardEntity model = (BoardEntity) mListView.getItemAtPosition(menuInfo.position);

        switch (item.getItemId()) {
            case Constants.CONTEXT_MENU_COPY_URL: {
                String uri = Websites.getDefault().getUrlBuilder().getPageUrlHtml(model.getCode(), 0);

                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(uri, uri);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                }
                mMainApplication.showToastShort(uri);

                return true;
            }
            case Constants.CONTEXT_MENU_ADD_FAVORITES: {
                mBoardsListPresenter.addToFavorites(model.getCode());
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
            case R.id.menu_add_board_id:

                EditTextDialog dialog = new EditTextDialog(getContext());
                dialog.setTitle(getString(R.string.menu_add_favorites));
                dialog.setHint(getString(R.string.pick_board_input_hint));
                dialog.setPositiveButtonListener((d, which) -> {
                    mBoardsListPresenter.addToFavorites(dialog.getText());
                });
                dialog.show();
                break;

            case R.id.refresh_menu_id:
                mBoardsListPresenter.requestBoardsListFromServer();
                break;
        }

        return true;
    }

    @Override public void showUnrecognizedBoardError(String board) {
        mMainApplication.showToastShort(getString(R.string.warning_enter_board));
    }

}
