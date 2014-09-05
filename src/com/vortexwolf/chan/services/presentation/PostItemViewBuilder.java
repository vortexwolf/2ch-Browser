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
import com.vortexwolf.chan.models.presentation.ThumbnailViewBag;
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
            vb.postIconView = (TextView) view.findViewById(R.id.post_icon);
            vb.postOpView = (TextView) view.findViewById(R.id.post_op);
            vb.postTripView = (TextView) view.findViewById(R.id.post_trip);
            vb.postSubjectView = (TextView) view.findViewById(R.id.post_subject);
            vb.commentView = (ClickableLinksTextView) view.findViewById(R.id.comment);
            vb.postRepliesView = (TextView) view.findViewById(R.id.post_replies);
            vb.singleThumbnailView = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view));
            vb.showFullTextView = (TextView) view.findViewById(R.id.show_full_text);
            vb.badgeView = view.findViewById(R.id.badge_view);
            vb.badgeImage = (ImageView) view.findViewById(R.id.badge_image);
            vb.badgeTitle = (TextView) view.findViewById(R.id.badge_title);
            
            vb.multiThumbnailsView = view.findViewById(R.id.multi_thumbnails_view);
            vb.thumbnailViews = new ThumbnailViewBag[4];
            vb.thumbnailViews[0] = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view_1));
            vb.thumbnailViews[1] = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view_2));
            vb.thumbnailViews[2] = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view_3));
            vb.thumbnailViews[3] = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view_4));
            
            view.setTag(vb);
        }

        vb.currentModel = item;

        if (item.canMakeCommentFloat() || item.isCommentFloat()) {
            FlowTextHelper.setFloatLayoutPosition(vb.singleThumbnailView.container, vb.commentView);
        } else {
            FlowTextHelper.setDefaultLayoutPosition(vb.singleThumbnailView.container, vb.commentView);
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

        // Имя, иконка, трип и тема поста
        String name = item.getName();
        String icon = item.getIcon();
        String trip = item.getTrip();
        String subject = item.getSubject();
        
        if (this.mSettings.isDisplayNames() && !StringUtils.isEmptyOrWhiteSpace(name) && !name.equals("Аноним")) {
            vb.postNameView.setText(MyHtml.fromHtml(name, HtmlUtils.sImageGetter, null));
            vb.postNameView.setVisibility(View.VISIBLE);
        } else {
            vb.postNameView.setVisibility(View.GONE);
        }
        if (this.mSettings.isDisplayNames() && !StringUtils.isEmptyOrWhiteSpace(trip)) {
            vb.postTripView.setText(MyHtml.fromHtml(trip, HtmlUtils.sImageGetter, null));
            vb.postTripView.setVisibility(View.VISIBLE);
        } else {
            vb.postTripView.setVisibility(View.GONE);
        }
        if (this.mSettings.isDisplayNames() && !StringUtils.isEmptyOrWhiteSpace(icon)) {
            vb.postIconView.setText(MyHtml.fromHtml(icon, HtmlUtils.sImageGetter, null));
            vb.postIconView.setVisibility(View.VISIBLE);
        } else {
            vb.postIconView.setVisibility(View.GONE);
        }
        if (!StringUtils.isEmptyOrWhiteSpace(subject)) {
            vb.postSubjectView.setText(MyHtml.fromHtml(subject, HtmlUtils.sImageGetter, null));
            vb.postSubjectView.setVisibility(View.VISIBLE);
        } else {
            vb.postSubjectView.setVisibility(View.GONE);
        }
        
        if (item.isSage()) {
            vb.postSageView.setVisibility(View.VISIBLE);
        } else {
            vb.postSageView.setVisibility(View.GONE);
        }
        
        if (item.isOp()) {
            vb.postOpView.setVisibility(View.VISIBLE);
        } else {
            vb.postOpView.setVisibility(View.GONE);
        }

        // Дата поста
        if (this.mSettings.isDisplayPostItemDate()) {
            vb.postDateView.setVisibility(View.VISIBLE);
            vb.postDateView.setText(item.getPostDate(this.mAppContext));
        } else {
            vb.postDateView.setVisibility(View.GONE);
        }

        // Обрабатываем прикрепленные файлы
        if (item.getAttachmentsNumber() == 1) {
            vb.multiThumbnailsView.setVisibility(View.GONE);
            
            ThreadPostUtils.refreshAttachmentView(isBusy, item.getAttachment(0), vb.singleThumbnailView);
        } else if (item.getAttachmentsNumber() > 1) {
            vb.multiThumbnailsView.setVisibility(View.VISIBLE);
            vb.singleThumbnailView.hide();
            
            for (int i = 0; i < 4; ++i) {
                ThreadPostUtils.refreshAttachmentView(isBusy, item.getAttachment(i), vb.thumbnailViews[i]);
            }
        } else {
            vb.multiThumbnailsView.setVisibility(View.GONE);
            vb.singleThumbnailView.hide();
        }


        if (item.canMakeCommentFloat()) {
            FloatImageModel floatModel = new FloatImageModel(vb.singleThumbnailView.container, vb.commentView.getPaint(), this.mWindowManager.getDefaultDisplay(), this.mAppContext.getResources());
            item.makeCommentFloat(floatModel);
        }

        vb.commentView.setText(item.getSpannedComment());
        if (item.hasUrls() && !CompatibilityUtils.isTextSelectable(vb.commentView)) {
            vb.commentView.setMovementMethod(MyLinkMovementMethod.getInstance());
        }

        // Ответы на сообщение
        if (this.mThreadNumber != null && item.hasReferencesFrom()) {
            vb.postRepliesView.setText(item.getReferencesFromAsSpannableString());
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
        ViewBag vb = (ViewBag) v.getTag();
        if (item == null || vb == null) {
            return;
        }
        
        if (item.getAttachmentsNumber() == 1) {
            ThreadPostUtils.setNonBusyAttachment(item.getAttachment(0), vb.singleThumbnailView.image);
        } else if (item.getAttachmentsNumber() > 1) {
            for (int i = 0; i < 4; ++i) {
                ThreadPostUtils.setNonBusyAttachment(item.getAttachment(i), vb.thumbnailViews[i].image);
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
        public TextView postIconView;
        public TextView postOpView;
        public TextView postTripView;
        public TextView postSubjectView;
        public ClickableLinksTextView commentView;
        public TextView postRepliesView;
        public TextView showFullTextView;
        public ThumbnailViewBag singleThumbnailView;
        public View badgeView;
        public ImageView badgeImage;
        public TextView badgeTitle;
        
        public View multiThumbnailsView;
        public ThumbnailViewBag[] thumbnailViews;
    }
}
