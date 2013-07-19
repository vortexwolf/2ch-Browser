package com.vortexwolf.dvach.activities;

import java.util.List;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.adapters.HistoryAdapter;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.Factory;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.db.FavoritesDataSource;
import com.vortexwolf.dvach.db.HistoryDataSource;
import com.vortexwolf.dvach.db.HistoryEntity;
import com.vortexwolf.dvach.interfaces.INavigationService;

public class HistoryActivity extends BaseListActivity {

    private HistoryDataSource mDatasource;
    private FavoritesDataSource mFavoritesDatasource;
    private INavigationService mNavigationService;

    private HistoryAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mDatasource = Factory.getContainer().resolve(HistoryDataSource.class);
        this.mFavoritesDatasource = Factory.getContainer().resolve(FavoritesDataSource.class);
        this.mNavigationService = Factory.getContainer().resolve(INavigationService.class);

        this.resetUI();
        // load history only 1 time
        new OpenDataSourceTask().execute();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // update if favorites were changed
        if (this.mAdapter != null) {
            this.mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.history_list_view;
    }

    @Override
    protected void resetUI() {
        super.resetUI();

        this.registerForContextMenu(this.getListView());
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        HistoryEntity item = this.mAdapter.getItem(position);

        this.mNavigationService.navigate(Uri.parse(item.getUrl()), this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear_history_id:
                this.mDatasource.deleteAllHistory();
                this.mAdapter.clear();
                this.mAdapter.notifyDataSetChanged();
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
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        HistoryEntity model = this.mAdapter.getItem(menuInfo.position);

        switch (item.getItemId()) {
            case Constants.CONTEXT_MENU_COPY_URL: {
                ClipboardManager clipboard = (ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);
                clipboard.setText(model.getUrl());

                AppearanceUtils.showToastMessage(this, model.getUrl());
                return true;
            }
        }

        return false;
    }

    private class OpenDataSourceTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            List<HistoryEntity> historyItems = HistoryActivity.this.mDatasource.getAllHistory();

            HistoryActivity.this.mAdapter = new HistoryAdapter(HistoryActivity.this, historyItems, HistoryActivity.this.mFavoritesDatasource);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            HistoryActivity.this.setListAdapter(HistoryActivity.this.mAdapter);
            HistoryActivity.this.switchToListView();
        }

        @Override
        protected void onPreExecute() {
            HistoryActivity.this.switchToLoadingView();
        }
    }
}
