package ua.in.quireg.chan.models.presentation;

import android.content.res.Resources;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import ua.in.quireg.chan.R;

public class AddAttachmentViewBag {
    public View container;
    public ImageView thumbnailView;
    public ImageButton removeButton;
    public TextView fileName;
    public TextView fileSize;

    public static AddAttachmentViewBag fromView(View container) {
        AddAttachmentViewBag vb = new AddAttachmentViewBag();
        vb.container = container;
        vb.thumbnailView = container.findViewById(R.id.addpost_thumbnail);
        vb.removeButton = container.findViewById(R.id.addpost_attachment_remove);
        vb.fileName = container.findViewById(R.id.addpost_attachment_name);
        vb.fileSize = container.findViewById(R.id.addpost_attachment_size);

        return vb;
    }

    public void hide() {
        container.setVisibility(View.GONE);
        thumbnailView.setImageResource(android.R.color.transparent);
    }

    public void show(FileModel model, Resources resources) {
        container.setVisibility(View.VISIBLE);
        fileName.setText(model.file.getName());

        if (model instanceof ImageFileModel) {
            ImageFileModel imageModel = (ImageFileModel) model;
            thumbnailView.setImageBitmap(imageModel.getBitmap(100)); // max 100x100

            String info = String.format(Locale.getDefault(),
                    resources.getString(
                            R.string.data_add_post_attachment_info), model.getFileSize(), imageModel.imageWidth, imageModel.imageHeight
            );
            fileSize.setText(info);
        } else {
            thumbnailView.setImageResource(R.drawable.ic_email_attachment);
            String info = String.format(Locale.getDefault(),
                    "%d%s", model.getFileSize(), resources.getString(R.string.data_file_size_measure)
            );
            fileSize.setText(info);
        }
    }
}
