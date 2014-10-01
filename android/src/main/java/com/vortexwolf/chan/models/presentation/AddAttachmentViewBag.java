package com.vortexwolf.chan.models.presentation;

import com.vortexwolf.chan.R;

import android.content.res.Resources;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class AddAttachmentViewBag {
    public View container;
    public ImageView thumbnailView;
    public ImageButton removeButton;
    public TextView fileName;
    public TextView fileSize;
    
    public static AddAttachmentViewBag fromView(View container) {
        AddAttachmentViewBag vb = new AddAttachmentViewBag();
        vb.container = container;
        vb.thumbnailView = (ImageView) container.findViewById(R.id.addpost_thumbnail);
        vb.removeButton = (ImageButton) container.findViewById(R.id.addpost_attachment_remove);
        vb.fileName = (TextView) container.findViewById(R.id.addpost_attachment_name);
        vb.fileSize = (TextView) container.findViewById(R.id.addpost_attachment_size);

        return vb;
    }
    
    public void hide() {
        this.container.setVisibility(View.GONE);
        this.thumbnailView.setImageResource(android.R.color.transparent);
    }
    
    public void show(ImageFileModel model, Resources resources) {
        this.container.setVisibility(View.VISIBLE);
        this.thumbnailView.setImageBitmap(model.getBitmap(100)); // max 100x100
        
        this.fileName.setText(model.file.getName());
        
        String info = String.format(resources.getString(R.string.data_add_post_attachment_info), model.getFileSize(), model.imageWidth, model.imageHeight);
        this.fileSize.setText(info);
    }
}
