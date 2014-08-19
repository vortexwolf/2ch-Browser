package com.vortexwolf.chan.services.presentation;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources.Theme;
import android.graphics.Color;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.boards.dvach.DvachUriBuilder;
import com.vortexwolf.chan.common.controls.ClickableLinksTextView;
import com.vortexwolf.chan.common.controls.MyLinkMovementMethod;
import com.vortexwolf.chan.common.library.MyHtml;
import com.vortexwolf.chan.common.utils.AppearanceUtils;
import com.vortexwolf.chan.common.utils.CompatibilityUtils;
import com.vortexwolf.chan.common.utils.HtmlUtils;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.common.utils.ThreadPostUtils;
import com.vortexwolf.chan.interfaces.IBitmapManager;
import com.vortexwolf.chan.models.presentation.AttachmentInfo;
import com.vortexwolf.chan.models.presentation.BadgeModel;
import com.vortexwolf.chan.models.presentation.FloatImageModel;
import com.vortexwolf.chan.models.presentation.PostItemViewModel;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class PostItemViewBuilder {
    private final LayoutInflater mInflater;
    private final IBitmapManager mBitmapManager;
    private final String mBoardName;
    private final String mThreadNumber;
    private final Context mAppContext;
    private final ApplicationSettings mSettings;
    private final WindowManager mWindowManager;
    private final DvachUriBuilder mDvachUriBuilder;

    public PostItemViewBuilder(Context context, String boardName, String threadNumber, IBitmapManager bitmapManager, ApplicationSettings settings, DvachUriBuilder dvachUriBuilder) {
        this.mAppContext = context.getApplicationContext();
        this.mInflater = LayoutInflater.from(context);
        this.mBitmapManager = bitmapManager;
        this.mBoardName = boardName;
        this.mThreadNumber = threadNumber;
        this.mSettings = settings;
        this.mWindowManager = ((WindowManager) this.mAppContext.getSystemService(Context.WINDOW_SERVICE));
        this.mDvachUriBuilder = dvachUriBuilder;
    }

    public View getView(final PostItemViewModel item, final View convertView, final boolean isBusy) {
        final View view = convertView == null ? this.mInflater.inflate(R.layout.posts_list_item, null) : convertView;

        // Get inner controls
        ViewBag vb = (ViewBag) view.getTag();
        if (vb == null) {
            vb = new ViewBag();
            vb.postIdView = (TextView) view.findViewById(R.id.post_id);
            vb.postNameView = (TextView) view.findViewById(R.id.post_name);
            vb.postIndexView = (TextView) view.findViewById(R.id.post_index);
            vb.postDateView = (TextView) view.findViewById(R.id.post_item_date_id);
            vb.postSageView = (TextView) view.findViewById(R.id.post_sage);
            vb.commentView = (ClickableLinksTextView) view.findViewById(R.id.comment);
            vb.attachmentInfoView = (TextView) view.findViewById(R.id.attachment_info);
            vb.postRepliesView = (TextView) view.findViewById(R.id.post_replies);
            vb.fullThumbnailView = view.findViewById(R.id.thumbnail_view);
            vb.imageView = (ImageView) view.findViewById(R.id.thumbnail);
            vb.showFullTextView = (TextView) view.findViewById(R.id.show_full_text);
            vb.badgeView = view.findViewById(R.id.badge_view);
            vb.badgeImage = (ImageView) view.findViewById(R.id.badge_image);
            vb.badgeTitle = (TextView) view.findViewById(R.id.badge_title);
            view.setTag(vb);
        }

        vb.currentModel = item;

        if (item.canMakeCommentFloat() || item.isCommentFloat()) {
            FlowTextHelper.setFloatLayoutPosition(vb.fullThumbnailView, vb.commentView);
        } else {
            FlowTextHelper.setDefaultLayoutPosition(vb.fullThumbnailView, vb.commentView);
        }

        // Apply info from the data item
        // Номер поста
        String postNumber = item.getNumber();
        vb.postIdView.setText(postNumber);

        // Номер по порядку
        int postIndex = item.getPosition() + 1;
        vb.postIndexView.setText(String.valueOf(postIndex));
        if (postIndex >= ThreadPostUtils.getBumpLimitNumber(this.mBoardName)) {
            vb.postIndexView.setTextColor(Color.parseColor("#C41E3A"));
        } else {
            vb.postIndexView.setTextColor(Color.parseColor("#4F7942"));
        }

        // Имя
        String name = item.getName();
        if (this.mSettings.isDisplayNames() && !StringUtils.isEmptyOrWhiteSpace(name) && !name.equals("Аноним")) {
            vb.postNameView.setText(MyHtml.fromHtml(name, HtmlUtils.sImageGetter, null));
            vb.postNameView.setVisibility(View.VISIBLE);
        } else {
            vb.postNameView.setVisibility(View.GONE);
        }
        
        if (item.isSage()) {
            vb.postSageView.setVisibility(View.VISIBLE);
        } else {
            vb.postSageView.setVisibility(View.GONE);
        }

        // Дата поста
        if (this.mSettings.isDisplayPostItemDate()) {
            vb.postDateView.setVisibility(View.VISIBLE);
            vb.postDateView.setText(item.getPostDate(this.mAppContext));
        } else {
            vb.postDateView.setVisibility(View.GONE);
        }

        // Обрабатываем прикрепленный файл
        AttachmentInfo attachment = item.getAttachment(this.mBoardName);
        String threadUrl = this.mDvachUriBuilder.createThreadUri(this.mBoardName, this.mThreadNumber);
        ThreadPostUtils.handleAttachmentImage(isBusy, attachment, vb.imageView, vb.fullThumbnailView, this.mBitmapManager, this.mSettings, this.mAppContext, threadUrl);
        ThreadPostUtils.handleAttachmentDescription(attachment, this.mAppContext.getResources(), vb.attachmentInfoView);

        // Комментарий (обновляем после файла)
        if (item.canMakeCommentFloat()) {
            FloatImageModel floatModel = new FloatImageModel(vb.fullThumbnailView, vb.commentView.getPaint(), this.mWindowManager.getDefaultDisplay(), this.mAppContext.getResources());
            item.makeCommentFloat(floatModel);
        }

        vb.commentView.setText(item.getSpannedComment());
        if (item.hasUrls() && !CompatibilityUtils.isTextSelectable(vb.commentView)) {
            vb.commentView.setMovementMethod(MyLinkMovementMethod.getInstance());
        }

        // Ответы на сообщение
        if (this.mThreadNumber != null && item.hasReferencesFrom()) {
            SpannableStringBuilder replies = item.getReferencesFromAsSpannableString(this.mAppContext.getResources(), this.mBoardName, this.mThreadNumber);
            vb.postRepliesView.setText(replies);
            vb.postRepliesView.setMovementMethod(MyLinkMovementMethod.getInstance());
            vb.postRepliesView.setVisibility(View.VISIBLE);
        } else {
            vb.postRepliesView.setVisibility(View.GONE);
        }

        // badge
        BadgeModel badge = item.getBadge();
        if (badge != null) {
            vb.badgeView.setVisibility(View.VISIBLE);
            vb.badgeTitle.setText(badge.title);

            Uri uri = this.mDvachUriBuilder.adjustRelativeUri(Uri.parse(badge.source));
            this.mBitmapManager.fetchBitmapOnThread(uri.toString(), vb.badgeImage, null, null);
        } else {
            vb.badgeView.setVisibility(View.GONE);
        }

        // Почему-то LinkMovementMethod отменяет контекстное меню. Пустой
        // listener вроде решает проблему
        view.setOnLongClickListener(ClickListenersFactory.sIgnoreOnLongClickListener);

        return view;
    }

    public void setMaxHeight(final View view, final int maxHeight, final Theme theme) {
        final ViewBag vb = (ViewBag) view.getTag();

        // set max height anyway, it will not affect views with small height
        vb.commentView.setMaxHeight(maxHeight);
        vb.showFullTextView.setVisibility(View.GONE);

        SpannableStringBuilder builder = new SpannableStringBuilder(vb.showFullTextView.getText());
        HtmlUtils.replaceUrls(builder, null, theme);
        vb.showFullTextView.setText(builder);

        // wait until the text is drawn
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);

                int textHeight = vb.commentView.getHeight();
                //MyLog.v("PostItemViewBuilder", "onPreDraw Text height: " + textHeight);
                if (textHeight >= maxHeight) {
                    vb.showFullTextView.setVisibility(View.VISIBLE);
                    vb.showFullTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            vb.currentModel.setLongTextExpanded(true);
                            PostItemViewBuilder.this.removeMaxHeight(view);
                        }
                    });
                    return false;
                }

                return true;
            }
        });
    }

    public void removeMaxHeight(View view) {
        ViewBag vb = (ViewBag) view.getTag();
        vb.commentView.setMaxHeight(Integer.MAX_VALUE);
        vb.showFullTextView.setVisibility(View.GONE);
    }

    public void displayPopupDialog(final PostItemViewModel item, Context activityContext, Theme theme) {
        View view = this.getView(item, null, false);

        // убираем фон в виде рамки с закругленными краями и ставим обычный
        int backColor = AppearanceUtils.getThemeColor(theme, R.styleable.Theme_activityRootBackground);
        view.setBackgroundColor(backColor);

        // Перемещаем текст в ScrollView
        ScrollView scrollView = (ScrollView) view.findViewById(R.id.post_item_scroll);
        RelativeLayout contentLayout = (RelativeLayout) view.findViewById(R.id.post_item_content_layout);

        ((ViewGroup) contentLayout.getParent()).removeView(contentLayout);
        scrollView.addView(contentLayout);
        scrollView.setVisibility(View.VISIBLE);

        // Отображаем созданное view в диалоге
        Dialog currentDialog = new Dialog(activityContext);
        currentDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        currentDialog.setCanceledOnTouchOutside(true);
        currentDialog.setContentView(view);
        currentDialog.show();
    }

    public void displayThumbnail(final View v, final PostItemViewModel item) {
        if (item != null) {
            ViewBag vb = (ViewBag) v.getTag();
            AttachmentInfo attachment = item.getAttachment(this.mBoardName);

            if (vb != null && !ThreadPostUtils.isImageHandledWhenWasBusy(attachment, this.mSettings, this.mBitmapManager)) {
                String threadUrl = this.mDvachUriBuilder.createThreadUri(this.mBoardName, this.mThreadNumber);
                ThreadPostUtils.handleAttachmentImage(false, attachment, vb.imageView, vb.fullThumbnailView, this.mBitmapManager, this.mSettings, this.mAppContext, threadUrl);
            }
        }
    }

    public static class ViewBag {
        public PostItemViewModel currentModel;

        public TextView postIdView;
        public TextView postNameView;
        public TextView postIndexView;
        public TextView postDateView;
        public TextView postSageView;
        public ClickableLinksTextView commentView;
        public TextView attachmentInfoView;
        public TextView postRepliesView;
        public TextView showFullTextView;
        public View fullThumbnailView;
        public ImageView imageView;
        public View badgeView;
        public ImageView badgeImage;
        public TextView badgeTitle;
    }
}
