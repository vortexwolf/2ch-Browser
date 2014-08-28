package com.vortexwolf.chan.adapters;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.RelativeLayout;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.boards.dvach.DvachUriBuilder;
import com.vortexwolf.chan.boards.dvach.models.DvachThreadInfo;
import com.vortexwolf.chan.common.controls.EllipsizingTextView;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.common.utils.ThreadPostUtils;
import com.vortexwolf.chan.db.HiddenThreadsDataSource;
import com.vortexwolf.chan.interfaces.IBitmapManager;
import com.vortexwolf.chan.interfaces.IBusyAdapter;
import com.vortexwolf.chan.models.domain.ThreadModel;
import com.vortexwolf.chan.models.presentation.AttachmentInfo;
import com.vortexwolf.chan.models.presentation.ThreadItemViewModel;
import com.vortexwolf.chan.settings.ApplicationSettings;

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
            vb.attachmentInfoView[0] = (TextView) view.findViewById(R.id.attachment_info_1);
            vb.attachmentInfoView[1] = (TextView) view.findViewById(R.id.attachment_info_2);
            vb.attachmentInfoView[2] = (TextView) view.findViewById(R.id.attachment_info_3);
            vb.attachmentInfoView[3] = (TextView) view.findViewById(R.id.attachment_info_4);
            vb.fullThumbnailView[0] = view.findViewById(R.id.thumbnail_view_1);
            vb.fullThumbnailView[1] = view.findViewById(R.id.thumbnail_view_2);
            vb.fullThumbnailView[2] = view.findViewById(R.id.thumbnail_view_3);
            vb.fullThumbnailView[3] = view.findViewById(R.id.thumbnail_view_4);
            vb.thumbnailView[0] = (ImageView) view.findViewById(R.id.thumbnail_1);
            vb.thumbnailView[1] = (ImageView) view.findViewById(R.id.thumbnail_2);
            vb.thumbnailView[2] = (ImageView) view.findViewById(R.id.thumbnail_3);
            vb.thumbnailView[3] = (ImageView) view.findViewById(R.id.thumbnail_4);

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

        // Обрабатываем прикрепленные файлы
        for (int i=0; i<4; ++i) {
            AttachmentInfo attachment = item.getAttachment(this.mBoardName, i);
            ThreadPostUtils.handleAttachmentImage(this.mIsBusy, attachment, vb.thumbnailView[i], vb.fullThumbnailView[i], this.mBitmapManager, this.mSettings, this.getContext(), null);
            ThreadPostUtils.handleAttachmentDescription(attachment, this.getContext().getResources(), vb.attachmentInfoView[i]);
        }
        
        // изменение расположения текста оп-поста относительно картинок, если их больше 1
        if (item.getAttachmentsNumber()>1) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) vb.titleView.getLayoutParams();
            int[] rules = layoutParams.getRules();
            rules[RelativeLayout.BELOW] = R.id.thumbnail_view_1;
            rules[RelativeLayout.RIGHT_OF] = 0;
            
            layoutParams = (RelativeLayout.LayoutParams) vb.commentView.getLayoutParams();
            rules = layoutParams.getRules();
            rules[RelativeLayout.RIGHT_OF] = 0;
            
            layoutParams = (RelativeLayout.LayoutParams) vb.repliesNumberView.getLayoutParams();
            rules = layoutParams.getRules();
            rules[RelativeLayout.RIGHT_OF] = 0;
        } else { // без этого разметка почему-то ломается
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) vb.titleView.getLayoutParams();
            int[] rules = layoutParams.getRules();
            rules[RelativeLayout.BELOW] = 0;
            rules[RelativeLayout.RIGHT_OF] = R.id.thumbnail_view_1;
            
            layoutParams = (RelativeLayout.LayoutParams) vb.commentView.getLayoutParams();
            rules = layoutParams.getRules();
            rules[RelativeLayout.RIGHT_OF] = R.id.thumbnail_view_1;
            
            layoutParams = (RelativeLayout.LayoutParams) vb.repliesNumberView.getLayoutParams();
            rules = layoutParams.getRules();
            rules[RelativeLayout.RIGHT_OF] = R.id.thumbnail_view_1;
        }
    }

    /** Обновляет адаптер полностью */
    public void setAdapterData(ThreadModel[] threads) {
        this.clear();

        for (ThreadModel ti : threads) {
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

                for (int j=0; j<4; ++j) {
                    AttachmentInfo attachment = this.getItem(position).getAttachment(this.mBoardName, j);
                    if (!ThreadPostUtils.isImageHandledWhenWasBusy(attachment, this.mSettings, this.mBitmapManager)) {
                        ThreadPostUtils.handleAttachmentImage(isBusy, attachment, vb.thumbnailView[j], vb.fullThumbnailView[j], this.mBitmapManager, this.mSettings, this.getContext(), null);
                    }
                }
            }
        }
    }

    static class ViewBag {
        TextView titleView;
        EllipsizingTextView commentView;
        TextView repliesNumberView;
        TextView[] attachmentInfoView = new TextView[4];
        ImageView[] thumbnailView = new ImageView[4];
        View[] fullThumbnailView = new View[4];
    }
}
