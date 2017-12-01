package ua.in.quireg.chan.services;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.ncapdevi.fragnav.FragNavController;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.ui.fragments.ImageGalleryFragment;
import ua.in.quireg.chan.ui.fragments.PostsListFragment;
import ua.in.quireg.chan.ui.fragments.ThreadsListFragment;

public class NavigationService {

    private static NavigationService mNavigationService;

    private FragNavController mNavController;

    private NavigationService(FragNavController fragNavController) {
        mNavController = fragNavController;
    }

    public static void init(FragNavController fragNavController){
        mNavigationService = new NavigationService(fragNavController);
    }

    public static NavigationService getInstance(){
        if(mNavigationService != null){
            return mNavigationService;
        }else{
            throw new IllegalStateException("Must call init() first");
        }
    }



}
