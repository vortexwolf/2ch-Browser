package com.vortexwolf.dvach.presentation.services;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.vortexwolf.dvach.activities.posts.PostsListActivity;
import com.vortexwolf.dvach.activities.threads.ThreadsListActivity;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.db.HistoryDataSource;
import com.vortexwolf.dvach.interfaces.INavigationService;
import com.vortexwolf.dvach.interfaces.IOpenTabsManager;
import com.vortexwolf.dvach.presentation.models.OpenTabModel;
import com.vortexwolf.dvach.presentation.models.OpenTabType;

public class OpenTabsManager implements IOpenTabsManager {
	private final ArrayList<OpenTabModel> mTabs = new ArrayList<OpenTabModel>();
	
	private final HistoryDataSource mDataSource;
	private final INavigationService mNavigationService;
	
	public OpenTabsManager(HistoryDataSource dataSource, INavigationService navigationService){
		mDataSource = dataSource;
		mNavigationService = navigationService;
	}
	
	@Override
	public OpenTabModel add(OpenTabModel newTab){
		//Не добавляем, если уже добавлено
		for(OpenTabModel openTab : mTabs){
			if(openTab.getUri().equals(newTab.getUri())){
				return openTab;
			}
		}
		
		mTabs.add(0, newTab);
		mDataSource.addHistory(newTab.getTitle(), newTab.getUri().toString());
		
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
		Bundle extras = new Bundle();
		extras.putBoolean(Constants.EXTRA_PREFER_DESERIALIZED, true);
		
		mNavigationService.navigate(tab.getUri(), activity, extras);
	}
}
