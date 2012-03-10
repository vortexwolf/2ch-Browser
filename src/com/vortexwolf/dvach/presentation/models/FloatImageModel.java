package com.vortexwolf.dvach.presentation.models;

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
		mHeight = thumbnailView.getMeasuredHeight();
		mWidth = thumbnailView.getMeasuredWidth();
		
		mTextLineHeight = textPaint.getTextSize();
		mRightMargin = ((MarginLayoutParams)thumbnailView.getLayoutParams()).rightMargin;
		
		if(mHeight == 0 || mWidth == 0){
			mHeight = mWidth = (int)res.getDimension(R.dimen.thumbnail_size);
		}
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
