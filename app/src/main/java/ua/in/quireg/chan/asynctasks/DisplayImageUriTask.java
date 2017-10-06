package ua.in.quireg.chan.asynctasks;

import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.library.MyLog;
import ua.in.quireg.chan.exceptions.HttpRequestException;
import ua.in.quireg.chan.services.http.HttpBitmapReader;

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
