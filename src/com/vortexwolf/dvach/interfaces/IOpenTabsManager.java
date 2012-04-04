package com.vortexwolf.dvach.interfaces;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;

import com.vortexwolf.dvach.presentation.models.OpenTabModel;

public interface IOpenTabsManager {
	public ArrayList<OpenTabModel> getOpenTabs();

	public abstract void remove(OpenTabModel tab);

	public abstract void navigate(OpenTabModel tab, Activity activity);

	public abstract void add(OpenTabModel tab);

}