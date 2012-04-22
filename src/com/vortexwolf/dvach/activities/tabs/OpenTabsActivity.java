package com.vortexwolf.dvach.activities.tabs;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.interfaces.IOpenTabsManager;
import com.vortexwolf.dvach.presentation.models.OpenTabModel;
import android.app.ListActivity;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
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
		
		Bundle extras = this.getIntent().getExtras();
		if(extras != null){
			this.mCurrentUri = Uri.parse(extras.getString(Constants.EXTRA_CURRENT_URL));
		}
		
		this.resetUI();
		
		this.mAdapter = new OpenTabsAdapter(this, mTabsManager.getOpenTabs(), mTabsManager);
		this.getListView().setAdapter(this.mAdapter);
	}
	
	private void resetUI(){
		this.setTheme(this.mApplication.getSettings().getTheme());
		TypedArray a = this.getTheme().obtainStyledAttributes(R.styleable.Theme);
		this.getListView().setBackgroundColor(a.getColor(R.styleable.Theme_activityRootBackground, -1));
		
		this.registerForContextMenu(this.getListView());
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

	@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
    	super.onCreateContextMenu(menu, v, menuInfo);

    	menu.add(Menu.NONE, Constants.CONTEXT_MENU_COPY_URL, 0, this.getString(R.string.cmenu_copy_url));
	}
	
	@Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    	OpenTabModel model = mAdapter.getItem(menuInfo.position);

        switch(item.getItemId()){
	        case Constants.CONTEXT_MENU_COPY_URL:{
	        	ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
	        	clipboard.setText(model.getUri().toString());
	        	
	        	AppearanceUtils.showToastMessage(this, model.getUri().toString());
				return true;
	        }
        }
        
        return false;
    }
}
