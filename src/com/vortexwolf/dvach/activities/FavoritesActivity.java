package com.vortexwolf.dvach.activities;

import java.util.List;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.adapters.FavoritesAdapter;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.Factory;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.db.FavoritesDataSource;
import com.vortexwolf.dvach.db.FavoritesEntity;
import com.vortexwolf.dvach.interfaces.INavigationService;

public class FavoritesActivity extends BaseListActivity {
	private FavoritesDataSource mDatasource;
    private INavigationService mNavigationService;
    
    private FavoritesAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.mDatasource = Factory.getContainer().resolve(FavoritesDataSource.class);
		this.mNavigationService = Factory.getContainer().resolve(INavigationService.class);
		
		this.resetUI();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		// refresh favorites in any case
		new OpenDataSourceTask().execute();
	}
	
	@Override
	protected int getLayoutId() {
		return R.layout.favorites_list_view;
	}

	@Override
	protected void resetUI(){
		super.resetUI();
		
		this.registerForContextMenu(this.getListView());
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		FavoritesEntity item = mAdapter.getItem(position);
		
		mNavigationService.navigate(Uri.parse(item.getUrl()), this);
	}
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
    	super.onCreateContextMenu(menu, v, menuInfo);

    	menu.add(Menu.NONE, Constants.CONTEXT_MENU_COPY_URL, 0, this.getString(R.string.cmenu_copy_url));
    	menu.add(Menu.NONE, Constants.CONTEXT_MENU_REMOVE_FAVORITES, 0, this.getString(R.string.cmenu_remove_from_favorites));
	}
	
	@Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    	FavoritesEntity model = mAdapter.getItem(menuInfo.position);

        switch(item.getItemId()){
	        case Constants.CONTEXT_MENU_COPY_URL:{
	        	ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
	        	clipboard.setText(model.getUrl());
	        	
	        	AppearanceUtils.showToastMessage(this, model.getUrl());
				return true;
	        }
	        case Constants.CONTEXT_MENU_REMOVE_FAVORITES: {
	        	mDatasource.removeFromFavorites(model.getUrl());
	        	mAdapter.remove(model);
	        	return true;
	        }
        }
        
        return false;
    }

	private class OpenDataSourceTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			List<FavoritesEntity> favorites = mDatasource.getAllFavorites();

			mAdapter = new FavoritesAdapter(FavoritesActivity.this, favorites);
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			FavoritesActivity.this.setListAdapter(mAdapter);
			FavoritesActivity.this.switchToListView();
		}

		@Override
		protected void onPreExecute() {
			FavoritesActivity.this.switchToLoadingView();
		}
	}
}
