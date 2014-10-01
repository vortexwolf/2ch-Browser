package com.vortexwolf.chan.services;

import java.util.ArrayList;
import java.util.HashMap;

import com.vortexwolf.chan.models.presentation.AttachmentInfo;
import com.vortexwolf.chan.models.presentation.ThreadImageModel;

public class ThreadImagesService {
    private HashMap<String, ArrayList<ThreadImageModel>> mThreadImages = new HashMap<String, ArrayList<ThreadImageModel>>();

    public void addThreadImage(String threadUrl, String imageUrl, double size, AttachmentInfo attachment) {
        if (imageUrl == null && attachment == null) {
            return;
        }

        ArrayList<ThreadImageModel> imagesList = this.getOrCreateImagesList(threadUrl);

        ThreadImageModel model = new ThreadImageModel();
        model.position = imagesList.size() + 1;
        model.url = imageUrl;
        model.size = size;
        model.attachment = attachment;

        imagesList.add(model);
    }
    
    public void addThreadImage(String threadUrl, String imageUrl, double size) {
        this.addThreadImage(threadUrl, imageUrl, size, null);
    }

    public void clearThreadImages(String threadUrl) {
        if (this.mThreadImages.containsKey(threadUrl)) {
            this.mThreadImages.remove(threadUrl);
        }
    }

    // returns a copy instead of a real list
    public ArrayList<ThreadImageModel> getImagesList(String threadUrl) {
        if (!this.mThreadImages.containsKey(threadUrl)) {
            return new ArrayList<ThreadImageModel>();
        }

        return (ArrayList<ThreadImageModel>) this.mThreadImages.get(threadUrl).clone();
    }

    public boolean hasImage(String threadUrl, String imageUrl) {
        if (this.mThreadImages.containsKey(threadUrl)) {
            ThreadImageModel model = this.getImageByUrl(this.mThreadImages.get(threadUrl), imageUrl);
            return model != null;
        }

        return false;
    }

    public ThreadImageModel getImageByUrl(ArrayList<ThreadImageModel> images, String imageUrl) {
        for (ThreadImageModel image : images) {
            if (image.url != null && image.url.equals(imageUrl)) {
                return image;
            }
        }

        return null;
    }

    private ArrayList<ThreadImageModel> getOrCreateImagesList(String threadUrl) {
        if (!this.mThreadImages.containsKey(threadUrl)) {
            ArrayList<ThreadImageModel> newValue = new ArrayList<ThreadImageModel>();
            this.mThreadImages.put(threadUrl, newValue);
            return newValue;
        }

        return this.mThreadImages.get(threadUrl);
    }
}
