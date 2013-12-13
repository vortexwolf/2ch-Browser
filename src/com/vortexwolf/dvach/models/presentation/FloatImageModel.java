package com.vortexwolf.dvach.models.presentation;

import android.content.res.Resources;
import android.text.TextPaint;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;

import com.vortexwolf.dvach.R;

public class FloatImageModel {

    private int mHeight;
    private int mWidth;
    private float mTextLineHeight;
    private int mRightMargin;

    public FloatImageModel(View thumbnailView, TextPaint textPaint, Display display, Resources res) {
        thumbnailView.measure(display.getWidth(), display.getHeight());
        int defaultSize = (int) res.getDimension(R.dimen.thumbnail_size);

        this.mHeight = Math.max(thumbnailView.getMeasuredHeight(), defaultSize);
        this.mWidth = Math.max(thumbnailView.getMeasuredWidth(), defaultSize);
        this.mTextLineHeight = Math.max(textPaint.getTextSize(), 1);
        this.mRightMargin = Math.max(((MarginLayoutParams) thumbnailView.getLayoutParams()).rightMargin, 0);
    }

    public int getHeight() {
        return this.mHeight;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public float getTextLineHeight() {
        return this.mTextLineHeight;
    }

    public int getRightMargin() {
        return this.mRightMargin;
    }

}
