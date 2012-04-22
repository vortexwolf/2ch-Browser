package com.vortexwolf.dvach.presentation.services;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import com.vortexwolf.dvach.activities.posts.PostsListActivity;
import com.vortexwolf.dvach.activities.threads.ThreadsListActivity;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.interfaces.IOpenTabsManager;
import com.vortexwolf.dvach.presentation.models.OpenTabModel;
import com.vortexwolf.dvach.presentation.models.OpenTabType;

public class OpenTabsManager implements IOpenTabsManager {
	private final ArrayList<OpenTabModel> mTabs = new ArrayList<OpenTabModel>();
	
	@Override
	public OpenTabModel add(OpenTabModel newTab){
		//Не добавляем, если уже добавлено
		for(OpenTabModel openTab : mTabs){
			if(openTab.getUri().equals(newTab.getUri())){
				return openTab;
			}
		}
		
		mTabs.add(0, newTab);
		
		return newTab;
	}
	
	@Override
	public ArrayList<OpenTabModel> getOpenTabs(){
		return mTabs;
	}
	
	@Override
	public void remove(OpenTabModel tab){
		this.mTabs.remove(tab);
	}

	@Override
	public void navigate(OpenTabModel tab, Activity activity){
		OpenTabType tabType = tab.getTabType();
		
		if(tabType == OpenTabType.BOARD){
			Intent i = new Intent(activity.getApplicationContext(), ThreadsListActivity.class);
			i.setData(tab.getUri());
			i.putExtra(Constants.EXTRA_PREFER_DESERIALIZED, true);
			activity.startActivity(i);
		}
		else if(tabType == OpenTabType.THREAD){
			Intent i = new Intent(activity.getApplicationContext(), PostsListActivity.class);
			i.setData(tab.getUri());
			i.putExtra(Constants.EXTRA_PREFER_DESERIALIZED, true);
			activity.startActivity(i);
		}
	}
}
