package com.vortexwolf.chan.common.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.vortexwolf.chan.services.presentation.ClickListenersFactory;

public class ExtendedImageView extends ImageView {

    public ExtendedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.setOnLongClickListener(ClickListenersFactory.sIgnoreOnLongClickListener);
    }
}
