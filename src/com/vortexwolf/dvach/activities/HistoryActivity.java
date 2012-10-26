package com.vortexwolf.dvach.activities;

import java.util.List;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.adapters.HistoryAdapter;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.Factory;
import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.db.FavoritesDataSource;
import com.vortexwolf.dvach.db.HistoryDataSource;
import com.vortexwolf.dvach.db.HistoryEntity;
import com.vortexwolf.dvach.interfaces.INavigationService;
import com.vortexwolf.dvach.settings.ApplicationSettings;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;

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
		
		new OpenDataSourceTask().execute();
	}
	
	@Override
	protected int getLayoutId() {
		return R.layout.history_list_view;
	}

	@Override
	protected void resetUI(){
		super.resetUI();
		
		this.registerForContextMenu(this.getListView());
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		HistoryEntity item = mAdapter.getItem(position);
		
		mNavigationService.navigate(Uri.parse(item.getUrl()), this);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.history, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.menu_clear_history_id:
    			mDatasource.deleteAllHistory();
    			mAdapter.clear();
    			mAdapter.notifyDataSetChanged();
    			break;
    	}
    	
    	return true;
    }
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
    	super.onCreateContextMenu(menu, v, menuInfo);

    	menu.add(Menu.NONE, Constants.CONTEXT_MENU_COPY_URL, 0, this.getString(R.string.cmenu_copy_url));
	}
	
	@Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    	HistoryEntity model = mAdapter.getItem(menuInfo.position);

        switch(item.getItemId()){
	        case Constants.CONTEXT_MENU_COPY_URL:{
	        	ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
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
