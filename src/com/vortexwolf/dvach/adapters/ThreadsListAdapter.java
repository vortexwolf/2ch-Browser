package com.vortexwolf.dvach.adapters;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.controls.EllipsizingTextView;
import com.vortexwolf.dvach.common.utils.StringUtils;
import com.vortexwolf.dvach.common.utils.ThreadPostUtils;
import com.vortexwolf.dvach.db.HiddenThreadsDataSource;
import com.vortexwolf.dvach.interfaces.IBitmapManager;
import com.vortexwolf.dvach.interfaces.IBusyAdapter;
import com.vortexwolf.dvach.models.domain.ThreadInfo;
import com.vortexwolf.dvach.models.presentation.AttachmentInfo;
import com.vortexwolf.dvach.models.presentation.ThreadItemViewModel;
import com.vortexwolf.dvach.services.presentation.DvachUriBuilder;
import com.vortexwolf.dvach.settings.ApplicationSettings;

public class ThreadsListAdapter extends ArrayAdapter<ThreadItemViewModel> implements IBusyAdapter {
    private static final int ITEM_VIEW_TYPE_THREAD = 0;
    private static final int ITEM_VIEW_TYPE_HIDDEN_THREAD = 1;

    private final LayoutInflater mInflater;
    private final IBitmapManager mBitmapManager;
    private final Theme mTheme;
    private final ApplicationSettings mSettings;
    private final HiddenThreadsDataSource mHiddenThreadsDataSource;
    private final DvachUriBuilder mDvachUriBuilder;

    private final String mBoardName;

    private boolean mIsBusy = false;

    public ThreadsListAdapter(Context context, String boardName, IBitmapManager bitmapManager, ApplicationSettings settings, Theme theme, HiddenThreadsDataSource hiddenThreadsDataSource, DvachUriBuilder dvachUriBuilder) {
        super(context.getApplicationContext(), 0);

        this.mBoardName = boardName;
        this.mBitmapManager = bitmapManager;
        this.mTheme = theme;
        this.mInflater = LayoutInflater.from(context);
        this.mSettings = settings;
        this.mHiddenThreadsDataSource = hiddenThreadsDataSource;
        this.mDvachUriBuilder = dvachUriBuilder;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return this.getItem(position).isHidden() ? ITEM_VIEW_TYPE_HIDDEN_THREAD : ITEM_VIEW_TYPE_THREAD;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        ThreadItemViewModel item = this.getItem(position);

        if (!item.isHidden()) {
            view = convertView != null ? convertView : this.mInflater.inflate(R.layout.threads_list_item, null);

            this.fillItemView(view, item);
        } else {
            view = convertView != null ? convertView : this.mInflater.inflate(R.layout.threads_list_hidden_item, null);

            this.fillHiddenThreadView(view, item);
        }

        return view;
    }

    private void fillHiddenThreadView(final View view, final ThreadItemViewModel item) {
        TextView threadNumberView = (TextView) view.findViewById(R.id.thread_number);
        TextView threadDescriptionView = (TextView) view.findViewById(R.id.thread_description);

        threadNumberView.setText("№" + item.getNumber());

        CharSequence description = item.getSubjectOrText();
        threadDescriptionView.setText(description);
    }

    private void fillItemView(final View view, final ThreadItemViewModel item) {
        // Get inner controls
        ViewBag vb = (ViewBag) view.getTag();
        if (vb == null) {
            vb = new ViewBag();
            vb.titleView = (TextView) view.findViewById(R.id.title);
            vb.commentView = (EllipsizingTextView) view.findViewById(R.id.comment);
            vb.repliesNumberView = (TextView) view.findViewById(R.id.repliesNumber);
            vb.attachmentInfoView = (TextView) view.findViewById(R.id.attachment_info);
            vb.fullThumbnailView = view.findViewById(R.id.thumbnail_view);
            vb.thumbnailView = (ImageView) view.findViewById(R.id.thumbnail);

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
        vb.commentView.setEllipsizeListener(new EllipsizingTextView.EllipsizeListener() {
            @Override
            public void ellipsizeStateChanged(EllipsizingTextView view, boolean ellipsized) {
                ThreadItemViewModel boundItem = (ThreadItemViewModel) view.getTag();
                boundItem.setEllipsized(ellipsized);
            }
        });
        
        // Количество ответов
        String postsQuantity = this.getContext().getResources().getQuantityString(R.plurals.data_posts_quantity, item.getReplyCount(), item.getReplyCount());
        String imagesQuantity = this.getContext().getResources().getQuantityString(R.plurals.data_files_quantity, item.getImageCount(), item.getImageCount());
        String repliesFormat = this.getContext().getString(R.string.data_posts_files);
        String repliesText = String.format(repliesFormat, postsQuantity, imagesQuantity);
        vb.repliesNumberView.setText(repliesText);
        // Обрабатываем прикрепленный файл
        AttachmentInfo attachment = item.getAttachment(this.mBoardName);
        ThreadPostUtils.handleAttachmentImage(this.mIsBusy, attachment, vb.thumbnailView, null, vb.fullThumbnailView, this.mBitmapManager, this.mSettings, this.getContext());
        ThreadPostUtils.handleAttachmentDescription(attachment, this.getContext().getResources(), vb.attachmentInfoView);
    }

    /** Обновляет адаптер полностью */
    public void setAdapterData(ThreadInfo[] threads) {
        this.clear();

        if (threads == null) {
            return;
        }
        
        for (ThreadInfo ti : threads) {
            ThreadItemViewModel model = new ThreadItemViewModel(ti, this.mTheme, this.mDvachUriBuilder);
            boolean isHidden = this.mHiddenThreadsDataSource.isHidden(this.mBoardName, model.getNumber());
            model.setHidden(isHidden);

            this.add(model);
        }
    }

    @Override
    public void setBusy(boolean isBusy, AbsListView view) {
        boolean prevBusy = this.mIsBusy;
        this.mIsBusy = isBusy;

        if (prevBusy == true && isBusy == false) {
            int count = view.getChildCount();
            for (int i = 0; i < count; i++) {
                View v = view.getChildAt(i);
                int position = view.getPositionForView(v);

                if (this.getItemViewType(position) == ITEM_VIEW_TYPE_HIDDEN_THREAD) {
                    continue;
                }

                ViewBag vb = (ViewBag) v.getTag();

                AttachmentInfo attachment = this.getItem(position).getAttachment(this.mBoardName);
                if (!ThreadPostUtils.isImageHandledWhenWasBusy(attachment, this.mSettings, this.mBitmapManager)) {
                    ThreadPostUtils.handleAttachmentImage(isBusy, attachment, vb.thumbnailView, null, vb.fullThumbnailView, this.mBitmapManager, this.mSettings, this.getContext());
                }
            }
        }
    }

    static class ViewBag {
        TextView titleView;
        EllipsizingTextView commentView;
        TextView repliesNumberView;
        TextView attachmentInfoView;
        ImageView thumbnailView;
        View fullThumbnailView;
    }
}
