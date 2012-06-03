package com.vortexwolf.dvach.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

public class NonParentFocusableImageButton extends ImageButton {

	public NonParentFocusableImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void setPressed(boolean pressed) {
		if (!pressed || !(((View) this.getParent()).isPressed())) {
			super.setPressed(pressed);
		}
	}	
}
