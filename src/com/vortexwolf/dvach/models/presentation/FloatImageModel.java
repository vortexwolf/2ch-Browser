package com.vortexwolf.dvach.models.presentation;

import com.vortexwolf.dvach.R;

import android.content.res.Resources;
import android.text.TextPaint;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;

public class FloatImageModel {

	private int mHeight;
	private int mWidth;
	private float mTextLineHeight;
	private int mRightMargin;
	
	public FloatImageModel(View thumbnailView, TextPaint textPaint, Display display, Resources res) {
		thumbnailView.measure(display.getWidth(), display.getHeight());
		int defaultSize = (int)res.getDimension(R.dimen.thumbnail_size);
		
		mHeight = Math.max(thumbnailView.getMeasuredHeight(), defaultSize);
		mWidth = Math.max(thumbnailView.getMeasuredWidth(), defaultSize);
		mTextLineHeight = Math.max(textPaint.getTextSize(), 1);
		mRightMargin = Math.max(((MarginLayoutParams)thumbnailView.getLayoutParams()).rightMargin, 0);
	}

	public int getHeight() {
		return mHeight;
	}

	public int getWidth() {
		return mWidth;
	}

	public float getTextLineHeight() {
		return mTextLineHeight;
	}

	public int getRightMargin() {
		return mRightMargin;
	}

}
