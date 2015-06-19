package com.vortexwolf.chan.activities;

import java.util.List;

import android.content.Context;
import android.opengl.Visibility;
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
import android.widget.ListView;
import android.widget.TextView;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.adapters.HistoryAdapter;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.utils.AppearanceUtils;
import com.vortexwolf.chan.common.utils.CompatibilityUtils;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.db.FavoritesDataSource;
import com.vortexwolf.chan.db.HistoryDataSource;
import com.vortexwolf.chan.db.HistoryEntity;
import com.vortexwolf.chan.services.NavigationService;

public class HistoryFragment extends BaseListFragment {
    private HistoryDataSource mDatasource;
    private FavoritesDataSource mFavoritesDatasource;
    private NavigationService mNavigationService;

    private View mSearchContainer;
    private EditText mSearchInput;

    private HistoryAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mDatasource = Factory.getContainer().resolve(HistoryDataSource.class);
        this.mFavoritesDatasource = Factory.getContainer().resolve(FavoritesDataSource.class);
        this.mNavigationService = Factory.getContainer().resolve(NavigationService.class);

        this.setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.history_list_view, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.mAdapter = new HistoryAdapter(this.getActivity(), this.mFavoritesDatasource);
        this.setListAdapter(this.mAdapter);

        this.registerForContextMenu(this.getListView());

        new OpenDataSourceTask().execute();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.mSearchContainer = view.findViewById(R.id.history_search_container);
        this.mSearchInput = (EditText) view.findViewById(R.id.history_search_input);

        this.mSearchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchHistory();
                    return true;
                }
                return false;
            }
        });

        ImageButton searchButton = (ImageButton) view.findViewById(R.id.history_search_button);
        searchButton.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchHistory();
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && this.mAdapter != null) {
            // update favorites icons
            this.mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        HistoryEntity item = this.mAdapter.getItem(position);
        if (StringUtils.isEmpty(item.getThread())) {
            this.mNavigationService.navigateBoardPage(this.getActivity(), null, item.getWebsite(), item.getBoard(), 0, true);
        } else {
            this.mNavigationService.navigateThread(this.getActivity(), null, item.getWebsite(), item.getBoard(), item.getThread(), item.getTitle(), null, true);
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
                this.mDatasource.deleteAllHistory();
                this.mAdapter.clear();
                break;
        }

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(Menu.NONE, Constants.CONTEXT_MENU_COPY_URL, 0, this.getString(R.string.cmenu_copy_url));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (!this.getUserVisibleHint()) {
            return false;
        }

        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        HistoryEntity model = this.mAdapter.getItem(menuInfo.position);

        switch (item.getItemId()) {
            case Constants.CONTEXT_MENU_COPY_URL: {
                String uri = model.buildUrl();
                CompatibilityUtils.copyText(this.getActivity(), uri, uri);

                AppearanceUtils.showToastMessage(this.getActivity(), uri);
                return true;
            }
        }

        return false;
    }

    private void searchHistory() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.mSearchInput.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

        String query = this.mSearchInput.getText().toString();
        mAdapter.searchItems(query);
    }

    private class OpenDataSourceTask extends AsyncTask<Void, Void, List<HistoryEntity>> {
        @Override
        protected List<HistoryEntity> doInBackground(Void... arg0) {
            List<HistoryEntity> historyItems = mDatasource.getAllHistory();
            return historyItems;
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
