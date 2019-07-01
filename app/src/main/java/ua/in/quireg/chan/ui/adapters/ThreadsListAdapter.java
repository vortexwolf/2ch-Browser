package ua.in.quireg.chan.ui.adapters;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import javax.inject.Inject;

import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.GlideApp;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.db.HiddenThreadsDataSource;
import ua.in.quireg.chan.models.presentation.ThreadItemViewModel;
import ua.in.quireg.chan.models.presentation.ThumbnailViewBag;
import ua.in.quireg.chan.settings.ApplicationSettings;

public class ThreadsListAdapter extends ArrayAdapter<ThreadItemViewModel> {

    private static final int ITEM_VIEW_TYPE_THREAD = 0;
    private static final int ITEM_VIEW_TYPE_HIDDEN_THREAD = 1;

    @Inject ApplicationSettings mSettings;
    @Inject HiddenThreadsDataSource mHiddenThreadsDataSource;

    public ThreadsListAdapter(Context context) {
        super(context.getApplicationContext(), 0);
        MainApplication.getAppComponent().inject(this);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        ThreadItemViewModel item = getItem(position);

        if (item != null) {
            return item.isHidden() ? ITEM_VIEW_TYPE_HIDDEN_THREAD : ITEM_VIEW_TYPE_THREAD;
        } else {
            return -1;
        }
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        View view;
        ThreadItemViewModel item = getItem(position);

        if (!item.isHidden()) {
            view = convertView != null ?
                    convertView :
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.threads_list_item, parent, false);
            fillItemView(view, item);
        } else {
            view = convertView != null ?
                    convertView :
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.threads_list_hidden_item, parent, false);
            fillHiddenThreadView(view, item);
        }

        return view;
    }

    private void fillHiddenThreadView(final View view, final ThreadItemViewModel item) {
        TextView threadNumberView = view.findViewById(R.id.thread_number);
        TextView threadDescriptionView = view.findViewById(R.id.thread_description);

        threadNumberView.setText(String.format("№%s", item.getNumber()));

        CharSequence description = item.getSubjectOrText();
        threadDescriptionView.setText(description);
    }

    private void fillItemView(final View view, final ThreadItemViewModel item) {
        // Get inner controls
        ViewBag vb = (ViewBag) view.getTag();
        if (vb == null) {
            vb = new ViewBag();
            vb.titleView = view.findViewById(R.id.title);
            vb.commentView = view.findViewById(R.id.comment);
            vb.repliesNumberView = view.findViewById(R.id.repliesNumber);
            vb.singleThumbnailView = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view));

            vb.multiThumbnailsView = view.findViewById(R.id.multi_thumbnails_view);
            vb.thumbnailViews = new ThumbnailViewBag[4];
            vb.thumbnailViews[0] = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view_1));
            vb.thumbnailViews[1] = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view_2));
            vb.thumbnailViews[2] = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view_3));
            vb.thumbnailViews[3] = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view_4));

            view.setTag(vb);
        }

        // Apply info from the data item
        String subject = item.getSubject();
        if (!StringUtils.isEmpty(subject)) {
            vb.titleView.setVisibility(View.VISIBLE);
            vb.titleView.setText(subject);
        } else {
            vb.titleView.setVisibility(View.GONE);
        }

        // Комментарий
        vb.commentView.setText(item.getSpannedComment());
        vb.commentView.setTag(item);
//        vb.commentView.setEllipsizeListener(new TextView.EllipsizeListener() {
//            @Override
//            public void ellipsizeStateChanged(TextView view, boolean ellipsized) {
//                ThreadItemViewModel boundItem = (ThreadItemViewModel) view.getTag();
//                boundItem.setEllipsized(ellipsized);
//            }
//        });

        // Количество ответов
        String postsQuantity = getContext().getResources()
                .getQuantityString(R.plurals.data_posts_quantity,
                        item.getReplyCount(), item.getReplyCount());
        String imagesQuantity = getContext().getResources()
                .getQuantityString(R.plurals.data_files_quantity,
                        item.getImageCount(), item.getImageCount());

        String repliesFormat = getContext().getString(R.string.data_posts_files);
        String repliesText = String.format(repliesFormat, postsQuantity, imagesQuantity);
        vb.repliesNumberView.setText(repliesText);

        if (mSettings.isMultiThumbnailsInThreads() && item.getAttachmentsNumber() > 1) {
            vb.multiThumbnailsView.setVisibility(View.VISIBLE);
            vb.singleThumbnailView.hide();
            for (int i = 0; i < 4; ++i) {

                if (item.getAttachment(i) == null || item.getAttachment(i).isEmpty()) {
                    vb.thumbnailViews[i].hide();
                    break;
                }
                GlideApp.with(getContext())
                        .load(item.getAttachment(i).getImageUrlIfImage())
                        .into(vb.thumbnailViews[i].image)
                        .onLoadFailed(getContext().getDrawable(R.drawable.error_image));

                vb.thumbnailViews[i].container.setVisibility(View.VISIBLE);
                vb.thumbnailViews[i].info.setText(item.getAttachment(i).getDescription());
            }
        } else if (item.getAttachmentsNumber() >= 1) {
            if (item.getAttachment(0) == null || item.getAttachment(0).isEmpty()) {
                vb.singleThumbnailView.hide();
                vb.multiThumbnailsView.setVisibility(View.GONE);
                return;
            }

            GlideApp.with(getContext())
                    .load(item.getAttachment(0).getImageUrlIfImage())
                    .into(vb.singleThumbnailView.image)
                    .onLoadFailed(getContext().getDrawable(R.drawable.error_image));

            vb.singleThumbnailView.container.setVisibility(View.VISIBLE);
            vb.singleThumbnailView.info.setText(item.getAttachment(0).getDescription());

        } else {
            vb.multiThumbnailsView.setVisibility(View.GONE);
            vb.singleThumbnailView.hide();
        }
    }

//    private void loadListImages() {
//        int count = mListView.getChildCount();
//        for (int i = 0; i < count; i++) {
//            View v = mListView.getChildAt(i);
//            int position = mListView.getPositionForView(v);
//
//            if (getItemViewType(position) == ITEM_VIEW_TYPE_HIDDEN_THREAD) {
//                continue;
//            }
//
//            ViewBag vb = (ViewBag) v.getTag();
//            ThreadItemViewModel item = getItem(position);
//            if (mSettings.isMultiThumbnailsInThreads() && item.getAttachmentsNumber() > 1) {
//                for (int j = 0; j < 4; ++j) {
//                    ThreadPostUtils.setNonBusyAttachment(item.getAttachment(j), vb.thumbnailViews[j].image);
//                }
//            } else if (item.getAttachmentsNumber() >= 1) {
//                ThreadPostUtils.setNonBusyAttachment(item.getAttachment(0), vb.singleThumbnailView.image);
//            }
//        }
//    }

    private class ViewBag {
        TextView titleView;
        TextView commentView;
        TextView repliesNumberView;
        ThumbnailViewBag singleThumbnailView;
        View multiThumbnailsView;
        ThumbnailViewBag[] thumbnailViews;
    }
}
