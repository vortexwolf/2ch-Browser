package com.vortexwolf.dvach.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

public class ExtendedImageView extends ImageView {

    public ExtendedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				return false;
			}
		});
    }    
}
