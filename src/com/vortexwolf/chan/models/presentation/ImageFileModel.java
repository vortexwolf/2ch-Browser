package com.vortexwolf.chan.models.presentation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.vortexwolf.chan.common.library.MyLog;

public class ImageFileModel {
    public File file;
    public int imageHeight;
    public int imageWidth;

    private Bitmap mBitmap;

    protected ImageFileModel() {
    }

    public ImageFileModel(String filePath) {
        this(new File(filePath));
    }

    public ImageFileModel(File file) {
        this.file = file;

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        this.readBitmapFromFile(o);

        this.imageHeight = o.outHeight;
        this.imageWidth = o.outWidth;
    }

    public int getFileSize() {
        return (int) Math.round(this.file.length() / 1024.0);
    }

    public Bitmap getBitmap(double maxSize) {
        if (this.mBitmap == null) {
            int scale = 1;
            if (this.imageHeight > maxSize || this.imageWidth > maxSize) {
                double realScale = Math.max(this.imageHeight, this.imageWidth) / maxSize;
                double roundedScale = Math.pow(2, Math.ceil(Math.log(realScale) / Math.log(2)));
                scale = (int) roundedScale; // 2, 4, 8, 16
            }

            // Decode with inSampleSize
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inSampleSize = scale;

            this.mBitmap = this.readBitmapFromFile(o);
        }

        return this.mBitmap;
    }

    private Bitmap readBitmapFromFile(BitmapFactory.Options o) {
        try {
            FileInputStream fis = new FileInputStream(this.file);
            Bitmap b = BitmapFactory.decodeStream(fis, null, o);
            fis.close();

            return b;
        } catch (IOException e) {
            MyLog.e("FileModel", e);
        }

        return null;
    }
}
