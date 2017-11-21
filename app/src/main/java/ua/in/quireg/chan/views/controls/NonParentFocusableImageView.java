package ua.in.quireg.chan.views.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class NonParentFocusableImageView extends ImageView {

    public NonParentFocusableImageView(Context context, AttributeSet paramAttributeSet) {
        super(context, paramAttributeSet);
    }

    @Override
    public void setPressed(boolean pressed) {
        if (!pressed || !(((View) this.getParent()).isPressed())) {
            super.setPressed(pressed);
        }
    }
}
