package com.vortexwolf.chan.asynctasks;

import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.exceptions.HttpRequestException;
import com.vortexwolf.chan.services.http.HttpBitmapReader;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

public class DisplayImageUriTask extends AsyncTask<Void, Void, Bitmap> {
    public static final String TAG = "DisplayImageUriTask";
    private static final HttpBitmapReader mHttpBitmapReader = Factory.resolve(HttpBitmapReader.class);

    private final String mUrl;
    private final ImageView mImage;

    public DisplayImageUriTask(String url, ImageView image) {
        this.mUrl = url;
        this.mImage = image;
    }

    @Override
    public void onPreExecute() {
        this.mImage.setTag(this.mUrl);
        this.mImage.setImageResource(android.R.color.transparent);
    }

    @Override
    public void onPostExecute(Bitmap bitmap) {
        if (bitmap != null && this.mImage.getTag() == this.mUrl) {
            this.mImage.setImageBitmap(bitmap);
        }
    }

    @Override
    protected Bitmap doInBackground(Void... arg0) {
        Bitmap bitmap = null;
        try {
            bitmap = this.mHttpBitmapReader.fromUri(this.mUrl);
        } catch (HttpRequestException e) {
            MyLog.e(TAG, e);
        }

        return bitmap;
    }
}
