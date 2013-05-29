package com.vortexwolf.dvach.interfaces;

import java.util.ArrayList;

import android.app.Activity;

import com.vortexwolf.dvach.models.presentation.OpenTabModel;

public interface IOpenTabsManager {
    public ArrayList<OpenTabModel> getOpenTabs();

    public abstract void remove(OpenTabModel tab);

    public abstract void navigate(OpenTabModel tab, Activity activity);

    public abstract OpenTabModel add(OpenTabModel tab);
    
    void removeAll();

}