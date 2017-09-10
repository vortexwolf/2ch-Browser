package ua.in.quireg.chan.models.presentation;

import ua.in.quireg.chan.R;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ThumbnailViewBag {
    public View container;
    public ImageView image;
    public TextView info;
    
    public static ThumbnailViewBag fromView(View container) {
        ThumbnailViewBag vb = new ThumbnailViewBag();
        vb.container = container;
        vb.image = (ImageView) container.findViewById(R.id.thumbnail);
        vb.info = (TextView) container.findViewById(R.id.attachment_info);
        return vb;
    }
    
    public void hide() {
        container.setVisibility(View.GONE);
        image.setImageResource(android.R.color.transparent);
        image.setOnClickListener(null);
        info.setText("");
    }
}