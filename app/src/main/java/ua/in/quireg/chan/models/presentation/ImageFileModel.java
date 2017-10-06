package ua.in.quireg.chan.models.presentation;

import java.io.File;

import android.graphics.Bitmap;
import android.graphics.Point;

import ua.in.quireg.chan.common.utils.IoUtils;

public class ImageFileModel extends FileModel {
    public int imageHeight;
    public int imageWidth;

    private Bitmap mBitmap;

    public ImageFileModel() { }

    public ImageFileModel(File file) {
        super(file);

        Point size = IoUtils.getImageSize(file);
        this.imageHeight = size.y;
        this.imageWidth = size.x;
    }

    public Bitmap getBitmap(double maxSize) {
        if (this.mBitmap == null) {
            this.mBitmap = IoUtils.readBitmapFromFile(file, maxSize);
        }

        return this.mBitmap;
    }
}
