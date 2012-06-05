package com.vortexwolf.dvach.common.controls;

import com.vortexwolf.dvach.services.presentation.ClickListenersFactory;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ExtendedImageView extends ImageView {

    public ExtendedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.setOnLongClickListener(ClickListenersFactory.sIgnoreOnLongClickListener);
    }    
}
