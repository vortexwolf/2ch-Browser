package ua.in.quireg.chan.services.presentation;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources.Theme;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
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

import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.views.controls.ClickableLinksTextView;
import ua.in.quireg.chan.views.controls.MyLinkMovementMethod;
import ua.in.quireg.chan.common.library.MyHtml;
import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.common.utils.CompatibilityUtils;
import ua.in.quireg.chan.common.utils.CompatibilityUtilsImpl;
import ua.in.quireg.chan.common.utils.HtmlUtils;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.common.utils.ThreadPostUtils;
import ua.in.quireg.chan.interfaces.IUrlBuilder;
import ua.in.quireg.chan.interfaces.IWebsite;
import ua.in.quireg.chan.models.domain.BadgeModel;
import ua.in.quireg.chan.models.presentation.FloatImageModel;
import ua.in.quireg.chan.models.presentation.PostItemViewModel;
import ua.in.quireg.chan.models.presentation.ThumbnailViewBag;
import ua.in.quireg.chan.services.BitmapManager;
import ua.in.quireg.chan.settings.ApplicationSettings;

public class PostItemViewBuilder {
    private final LayoutInflater mInflater;
    private final BitmapManager mBitmapManager = Factory.resolve(BitmapManager.class);
    private final IWebsite mWebsite;
    private final String mBoardName;
    private final String mThreadNumber;
    private final Context mAppContext;
    private final ApplicationSettings mSettings;
    private final WindowManager mWindowManager;
    private final IUrlBuilder mUrlBuilder;
    private static Boolean isTablet = null;

    public PostItemViewBuilder(Context context, IWebsite website, String boardName, String threadNumber, ApplicationSettings settings) {
        this.mAppContext = context.getApplicationContext();
        this.mInflater = LayoutInflater.from(context);
        this.mWebsite = website;
        this.mBoardName = boardName;
        this.mThreadNumber = threadNumber;
        this.mSettings = settings;
        this.mWindowManager = ((WindowManager) this.mAppContext.getSystemService(Context.WINDOW_SERVICE));
        this.mUrlBuilder = this.mWebsite.getUrlBuilder();
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
            vb.thumbnailViews = new ThumbnailViewBag[Constants.MAX_ATTACHMENTS];
            vb.thumbnailViews[0] = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view_1));
            vb.thumbnailViews[1] = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view_2));
            vb.thumbnailViews[2] = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view_3));
            vb.thumbnailViews[3] = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view_4));
            vb.thumbnailViews[4] = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view_5));
            vb.thumbnailViews[5] = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view_6));
            vb.thumbnailViews[6] = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view_7));
            vb.thumbnailViews[7] = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view_8));

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
        String trip = item.getTrip();
        String subject = item.getSubject();

        if (isTablet == null) isTablet = CompatibilityUtils.isTablet(mAppContext);
        if (this.mSettings.isDisplayNames() && !StringUtils.isEmptyOrWhiteSpace(name) && (isTablet || !name.equals(ThreadPostUtils.getDefaultName(mBoardName)))) {
            if (!isTablet && name.startsWith(ThreadPostUtils.getDefaultName(mBoardName))) {
                name = name.substring(ThreadPostUtils.getDefaultName(mBoardName).length());
            }
            vb.postNameView.setText(MyHtml.fromHtml(name, null, null));
            vb.postNameView.setVisibility(View.VISIBLE);
        } else {
            vb.postNameView.setVisibility(View.GONE);
        }
        if (this.mSettings.isDisplayNames() && !StringUtils.isEmptyOrWhiteSpace(trip)) {
            vb.postTripView.setText(MyHtml.fromHtml(trip, null, null));
            vb.postTripView.setVisibility(View.VISIBLE);
        } else {
            vb.postTripView.setVisibility(View.GONE);
        }
        if (!StringUtils.isEmptyOrWhiteSpace(subject)) {
            vb.postSubjectView.setText(MyHtml.fromHtml(subject, null, null));
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

            for (int i = 0; i < Constants.MAX_ATTACHMENTS; ++i) {
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
        if (badge != null && mSettings.isDisplayIcons()) {
            vb.badgeView.setVisibility(View.VISIBLE);
            vb.badgeTitle.setText(badge.title);

            String protectedUri = Uri.encode(badge.source);

            Uri uri = Uri.parse(this.mUrlBuilder.getIconUrl(protectedUri));
            this.mBitmapManager.fetchBitmapOnThread(uri, vb.badgeImage, false, null, null);
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

    public void displayPopupDialog(final PostItemViewModel model, final Activity activity, Theme theme, final Point coordinates) {
        if (model == null) return;
        final View view = this.getView(model, null, false);

        // убираем фон в виде рамки с закругленными краями и ставим обычный
        int backColor = AppearanceUtils.getThemeColor(theme, R.styleable.Theme_activityRootBackground);
        view.setBackgroundColor(backColor);

        // контекстное меню
        ImageView menuButton = (ImageView) view.findViewById(R.id.post_item_menu);
        if (Constants.SDK_VERSION >= 11) {
            menuButton.setVisibility(View.VISIBLE);
            menuButton.setOnClickListener(CompatibilityUtilsImpl.createClickListenerShowPostMenu(activity, model, view));
        }

        // Перемещаем текст в ScrollView
        ScrollView scrollView = (ScrollView) view.findViewById(R.id.post_item_scroll);
        RelativeLayout contentLayout = (RelativeLayout) view.findViewById(R.id.post_item_content_layout);

        ((ViewGroup) contentLayout.getParent()).removeView(contentLayout);
        scrollView.addView(contentLayout);
        scrollView.setVisibility(View.VISIBLE);

        // Отображаем созданное view в диалоге
        final Dialog currentDialog = new Dialog(activity);
        currentDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        currentDialog.setCanceledOnTouchOutside(true);
        currentDialog.setContentView(view);
        currentDialog.show();

        if (coordinates != null)
            AppearanceUtils.callWhenLoaded(view, new Runnable(){
                @Override
                public void run() {
                    final float dimAmount = 0.1f;
                    boolean gravityLeft = true;
                    boolean gravityTop = true;
                    int gravity = 0;
                    int x = coordinates.x;
                    int y = coordinates.y;
                    Rect windowRect = new Rect();
                    activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(windowRect);
                    if (x + view.getWidth() > windowRect.width() && x >= view.getWidth()) {
                        gravityLeft = false;
                        x = windowRect.width() - x;
                    }
                    if (y + view.getHeight() > windowRect.height() && y >= view.getHeight()) {
                        gravityTop = false;
                        y = windowRect.height() - y;
                    }
                    gravity |= gravityLeft ? Gravity.LEFT : Gravity.RIGHT;
                    gravity |= gravityTop ? Gravity.TOP : Gravity.BOTTOM;

                    //нужен новый диалог, т.к. в противном случае неправильно определяются координаты следующей ссылки
                    ((ViewGroup) view.getParent()).removeView(view);
                    currentDialog.hide();
                    currentDialog.cancel();
                    Dialog currentDialog = new Dialog(activity);
                    WindowManager.LayoutParams params = currentDialog.getWindow().getAttributes();
                    params.x = x;
                    params.y = y;
                    params.gravity = gravity;
                    currentDialog.getWindow().setAttributes(params);
                    if (Constants.SDK_VERSION >= 14) CompatibilityUtilsImpl.setDimAmount(currentDialog.getWindow(), dimAmount);
                    else currentDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    currentDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    currentDialog.setCanceledOnTouchOutside(true);
                    currentDialog.setContentView(view);
                    currentDialog.show();
                }
            });
    }

    public void displayThumbnail(final View v, final PostItemViewModel item) {
        Object tag = v.getTag();
        if (item == null || tag == null || !(tag instanceof ViewBag)) {
            return;
        }

        ViewBag vb = (ViewBag) tag;
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
