package com.vortexwolf.chan.services;

import java.util.ArrayList;
import java.util.HashMap;

import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.models.presentation.ThreadImageModel;

public class ThreadImagesService {
    private HashMap<String, ArrayList<ThreadImageModel>> mThreadImages = new HashMap<String, ArrayList<ThreadImageModel>> ();
    
    public void addThreadImage(String threadUrl, String imageUrl, double size) {
        if (imageUrl == null) {
            return;
        }
        
        ArrayList<ThreadImageModel> imagesList = this.getOrCreateImagesList(threadUrl);
        
        ThreadImageModel model = new ThreadImageModel();
        model.position = imagesList.size() + 1;
        model.url = imageUrl;
        model.size = size;
        
        imagesList.add(model);
    }
    
    public void clearThreadImages(String threadUrl){
        if (this.mThreadImages.containsKey(threadUrl)){
            this.mThreadImages.remove(threadUrl);
        }
    }
    
    public ArrayList<ThreadImageModel> getImagesList(String threadUrl) {
        if (!this.mThreadImages.containsKey(threadUrl)){
            return new ArrayList<ThreadImageModel>();
        }
        
        return (ArrayList<ThreadImageModel>)this.mThreadImages.get(threadUrl).clone();
    }
    
    public boolean hasImage(String threadUrl, String imageUrl) {
        if (this.mThreadImages.containsKey(threadUrl)) {
            ThreadImageModel model = getImageByUrl(this.mThreadImages.get(threadUrl), imageUrl);
            return model != null;
        }
        
        return false;
    }
    
    public ThreadImageModel getImageByUrl(ArrayList<ThreadImageModel> images, String imageUrl){
        for (ThreadImageModel image : images) {
            if (image.url.equals(imageUrl)) {
                return image;
            }
        }
        
        return null;
    }
    
    private ArrayList<ThreadImageModel> getOrCreateImagesList(String threadUrl){
        if (!this.mThreadImages.containsKey(threadUrl)){
            ArrayList<ThreadImageModel> newValue = new ArrayList<ThreadImageModel>();
            this.mThreadImages.put(threadUrl, newValue);
            return newValue;
        }
        
        return getImagesList(threadUrl);
    }
}
