package ua.in.quireg.chan.services;

import ua.in.quireg.chan.models.presentation.AttachmentInfo;
import ua.in.quireg.chan.models.presentation.ThreadImageModel;

import java.util.ArrayList;
import java.util.HashMap;

public class ThreadImagesService {

    private HashMap<String, ArrayList<ThreadImageModel>> mThreadImages = new HashMap<>();

    public void addThreadImage(String threadUrl, AttachmentInfo attachment) {
        if (attachment == null || !attachment.isDisplayableInGallery()) {
            return;
        }

        ArrayList<ThreadImageModel> imagesList = getOrCreateImagesList(threadUrl);

        ThreadImageModel model = new ThreadImageModel();
        model.position = imagesList.size() + 1;
        model.url = attachment.getSourceUrl();
        model.size = attachment.getSize();
        model.attachment = attachment;

        imagesList.add(model);
    }

    public void clearThreadImages(String threadUrl) {
        mThreadImages.remove(threadUrl);
    }

    // returns a copy
    public ArrayList<ThreadImageModel> getImagesList(String threadUrl) {

        if (mThreadImages.containsKey(threadUrl)) {
            return new ArrayList<>(mThreadImages.get(threadUrl));
        } else {
            return new ArrayList<>();
        }

    }

    public boolean hasThreadImages(String threadUrl) {
        return mThreadImages.containsKey(threadUrl);
    }

    public boolean hasImage(String threadUrl, String imageUrl) {
        if (mThreadImages.containsKey(threadUrl)) {
            ThreadImageModel model = getImageByUrl(mThreadImages.get(threadUrl), imageUrl);
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
        if (!mThreadImages.containsKey(threadUrl)) {
            mThreadImages.put(threadUrl, new ArrayList<>());
        }

        return mThreadImages.get(threadUrl);
    }
}
