package ua.in.quireg.chan.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.arellomobile.mvp.MvpAppCompatFragment;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.common.utils.CompatibilityUtils;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.db.FavoritesDataSource;
import ua.in.quireg.chan.db.HistoryDataSource;
import ua.in.quireg.chan.db.HistoryEntity;
import ua.in.quireg.chan.mvp.routing.MainRouter;
import ua.in.quireg.chan.ui.adapters.HistoryAdapter;

public class HistoryFragment extends MvpAppCompatFragment {

    @Inject protected HistoryDataSource mHistoryDataSource;
    @Inject protected FavoritesDataSource mFavoritesDatasource;
    @Inject MainRouter mMainRouter;

    @BindView(R.id.history_search_container) protected View mSearchContainer;
    @BindView(R.id.history_search_input) protected EditText mSearchInput;

    private HistoryAdapter mAdapter;
    private ListView mListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MainApplication.getAppComponent().inject(this);
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history_list_view, container, false);

        mListView = view.findViewById(android.R.id.list);
        mListView.setNestedScrollingEnabled(true);

        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if ((getActivity()) != null) {
            ActionBar mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (mActionBar != null) {
                mActionBar.setTitle(getString(R.string.tabs_history));
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mAdapter == null) {
            mAdapter = new HistoryAdapter(view.getContext());
        }

        mListView.setAdapter(mAdapter);
        mAdapter.setItems(mHistoryDataSource.getAllHistory());

        registerForContextMenu(mListView);

        mSearchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchHistory();
                return true;
            }
            return false;
        });

        ImageButton searchButton = view.findViewById(R.id.history_search_button);
        searchButton.setOnClickListener(v -> searchHistory());

        mListView.setOnItemClickListener((adapterView, view1, position, l) -> {
            HistoryEntity item = mAdapter.getItem(position);

            if (item == null) {
                Timber.e("HistoryEntity item == null, position %d", position);
                return;
            }
            if (StringUtils.isEmpty(item.getThread())) {
                mMainRouter.navigateBoard(item.getWebsite(), item.getBoard(), true);
            } else {
                mMainRouter.navigateThread(item.getThread(), false);
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && mAdapter != null) {
            // update favorites icons
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.history, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear_history_id:
                mHistoryDataSource.deleteAllHistory();
                mAdapter.clear();
                break;
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, Constants.CONTEXT_MENU_COPY_URL, 0, getString(R.string.cmenu_copy_url));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (!getUserVisibleHint()) {
            return false;
        }

        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        HistoryEntity model = mAdapter.getItem(menuInfo.position);

        if (model == null) {
            Timber.e("HistoryEntity item == null, position %d", menuInfo.position);
            return false;
        }

        switch (item.getItemId()) {
            case Constants.CONTEXT_MENU_COPY_URL: {
                String uri = model.buildUrl();
                CompatibilityUtils.copyText(getActivity(), uri, uri);

                AppearanceUtils.showLongToast(getActivity(), uri);
                return true;
            }
        }
        return false;
    }

    private void searchHistory() {
        String query = mSearchInput.getText().toString();
        mAdapter.searchItems(query);

        //Hide keyboard:
        @SuppressWarnings("ConstantConditions")
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mSearchInput.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
