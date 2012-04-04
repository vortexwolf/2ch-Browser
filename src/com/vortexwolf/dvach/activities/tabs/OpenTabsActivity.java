package com.vortexwolf.dvach.activities.tabs;

import java.util.List;

import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.interfaces.IOpenTabsManager;
import com.vortexwolf.dvach.presentation.models.OpenTabModel;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ListActivity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class OpenTabsActivity extends ListActivity {

    private MainApplication mApplication;
    private OpenTabsAdapter mAdapter;
    private IOpenTabsManager mTabsManager;
    
    private Uri mCurrentUri;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        this.mApplication = (MainApplication) this.getApplication();
        this.mTabsManager = this.mApplication.getOpenTabsManager();
		this.mAdapter = new OpenTabsAdapter(this, mTabsManager.getOpenTabs(), mTabsManager);
		
		//this.setTheme(this.mApplication.getSettings().getTheme());
		this.getListView().setAdapter(this.mAdapter);
		
		Bundle extras = this.getIntent().getExtras();
		if(extras != null){
			this.mCurrentUri = Uri.parse(extras.getString(Constants.EXTRA_CURRENT_URL));
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		OpenTabModel item = this.mAdapter.getItem(position);
		if(item.getUri().equals(this.mCurrentUri)){
			this.finish();
		}
		else {			
			this.mTabsManager.navigate(item, this);
		}
	}

	
}
