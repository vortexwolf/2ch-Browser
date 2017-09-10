package ua.in.quireg.chan.models.presentation;

import ua.in.quireg.chan.R;

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
    
    public void show(FileModel model, Resources resources) {
        this.container.setVisibility(View.VISIBLE);
        this.fileName.setText(model.file.getName());

        if (model instanceof ImageFileModel) {
            ImageFileModel imageModel = (ImageFileModel)model;
            this.thumbnailView.setImageBitmap(imageModel.getBitmap(100)); // max 100x100

            String info = String.format(resources.getString(R.string.data_add_post_attachment_info), model.getFileSize(), imageModel.imageWidth, imageModel.imageHeight);
            this.fileSize.setText(info);
        } else {
            this.thumbnailView.setImageResource(R.drawable.ic_email_attachment);
            this.fileSize.setText(model.getFileSize() + resources.getString(R.string.data_file_size_measure));
        }
    }
}
