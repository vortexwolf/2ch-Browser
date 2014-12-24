package com.vortexwolf.chan.models.presentation;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.services.TimerService;

public class GalleryItemViewBag {
    public FrameLayout layout;
    public View loading;
    public View error;
    public TimerService timer;

    public void switchToErrorView(String errorMessage) {
        this.layout.setVisibility(View.GONE);
        this.loading.setVisibility(View.GONE);
        this.error.setVisibility(View.VISIBLE);

        TextView errorTextView = (TextView) this.error.findViewById(R.id.error_text);
        errorTextView.setText(errorMessage);
    }

    public void clear() {
        layout.removeAllViews();

        if (timer != null) {
            timer.stop();
        }
    }
}
