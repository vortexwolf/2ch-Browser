package ua.in.quireg.chan.views.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
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
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.adapters.HistoryAdapter;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.common.utils.CompatibilityUtils;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.db.FavoritesDataSource;
import ua.in.quireg.chan.db.HistoryDataSource;
import ua.in.quireg.chan.db.HistoryEntity;
import ua.in.quireg.chan.services.NavigationService;

import java.util.List;

import javax.inject.Inject;

public class HistoryFragment extends BaseListFragment {

    @Inject protected HistoryDataSource mHistoryDataSource;
    @Inject protected FavoritesDataSource mFavoritesDatasource;


    @BindView(R.id.history_search_container) protected View mSearchContainer;
    @BindView(R.id.history_search_input) protected EditText mSearchInput;

    private HistoryAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainApplication.getComponent().inject(this);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history_list_view, container, false);
        ButterKnife.bind(this, view);

        mAdapter = new HistoryAdapter(getContext());
        new OpenDataSourceTask().execute();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView.setAdapter(mAdapter);
        registerForContextMenu(mListView);
        setTitle(getString(R.string.tabs_history));

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

            if (StringUtils.isEmpty(item.getThread())) {
                NavigationService.getInstance().navigateBoard(item.getWebsite(), item.getBoard());

            } else {
                NavigationService.getInstance().navigateThread(item.getWebsite(), item.getBoard(), item.getThread(), item.getTitle(), null, false);
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
        menu.clear();
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

        switch (item.getItemId()) {
            case Constants.CONTEXT_MENU_COPY_URL: {
                String uri = model.buildUrl();
                CompatibilityUtils.copyText(getActivity(), uri, uri);

                AppearanceUtils.showToastMessage(getActivity(), uri);
                return true;
            }
        }

        return false;
    }

    private void searchHistory() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchInput.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

        String query = mSearchInput.getText().toString();
        mAdapter.searchItems(query);
    }

    @Override
    public void onRefresh() {

    }

    private class OpenDataSourceTask extends AsyncTask<Void, Void, List<HistoryEntity>> {
        @Override
        protected List<HistoryEntity> doInBackground(Void... arg0) {
            return mHistoryDataSource.getAllHistory();
        }

        @Override
        protected void onPostExecute(List<HistoryEntity> result) {
            mAdapter.setItems(result);
            switchToListView();
        }

        @Override
        protected void onPreExecute() {
            switchToLoadingView();
        }
    }
}
