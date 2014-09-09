package com.vortexwolf.chan.models.presentation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.IoUtils;

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

        Point size = IoUtils.getImageSize(file);

        this.imageHeight = size.y;
        this.imageWidth = size.x;
    }

    public int getFileSize() {
        return (int) Math.round(this.file.length() / 1024.0);
    }

    public Bitmap getBitmap(double maxSize) {
        if (this.mBitmap == null) {
            this.mBitmap = IoUtils.readBitmapFromFile(file, maxSize);
        }

        return this.mBitmap;
    }
}
