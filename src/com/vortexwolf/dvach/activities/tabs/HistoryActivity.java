package com.vortexwolf.dvach.activities.tabs;

import java.util.Arrays;
import java.util.List;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.BaseListActivity;
import com.vortexwolf.dvach.common.Factory;
import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.db.FavoritesDataSource;
import com.vortexwolf.dvach.db.HistoryDataSource;
import com.vortexwolf.dvach.db.HistoryEntity;
import com.vortexwolf.dvach.interfaces.INavigationService;
import com.vortexwolf.dvach.settings.ApplicationSettings;

import android.app.ListActivity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class HistoryActivity extends BaseListActivity {

	private HistoryDataSource mDatasource;
	private FavoritesDataSource mFavoritesDatasource;
    private MainApplication mApplication;
    private ApplicationSettings mSettings;
    private INavigationService mNavigationService;
    
    private HistoryAdapter mAdapter;

		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.mApplication = (MainApplication)this.getApplication();
		this.mSettings = this.mApplication.getSettings();
		this.mDatasource = this.mApplication.getHistoryDataSource();
		this.mFavoritesDatasource = Factory.getContainer().resolve(FavoritesDataSource.class);
		this.mNavigationService = Factory.getContainer().resolve(INavigationService.class);
		
		this.resetUI();
		
		new OpenDataSourceTask().execute();
	}
	
	@Override
	protected int getLayoutId() {
		return R.layout.history_list_view;
	}

	@Override
	protected void resetUI(){
		super.resetUI();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		HistoryEntity item = mAdapter.getItem(position);
		
		mNavigationService.navigate(Uri.parse(item.getUrl()), this);
	}

	private class OpenDataSourceTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			List<HistoryEntity> historyItems = mDatasource.getAllHistory();

			mAdapter = new HistoryAdapter(HistoryActivity.this, historyItems, mFavoritesDatasource);
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			HistoryActivity.this.setListAdapter(mAdapter);
			HistoryActivity.this.switchToListView();
		}

		@Override
		protected void onPreExecute() {
			HistoryActivity.this.switchToLoadingView();
		}
	}
}
