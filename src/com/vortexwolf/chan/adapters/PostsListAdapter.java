package com.vortexwolf.chan.adapters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources.Theme;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.text.Layout;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.boards.dvach.DvachUriBuilder;
import com.vortexwolf.chan.boards.dvach.DvachUriParser;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.controls.ClickableURLSpan;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.AppearanceUtils;
import com.vortexwolf.chan.common.utils.CompatibilityUtilsImpl;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.interfaces.IBusyAdapter;
import com.vortexwolf.chan.interfaces.IURLSpanClickListener;
import com.vortexwolf.chan.models.domain.PostModel;
import com.vortexwolf.chan.models.presentation.AttachmentInfo;
import com.vortexwolf.chan.models.presentation.PostItemViewModel;
import com.vortexwolf.chan.models.presentation.PostsViewModel;
import com.vortexwolf.chan.services.ThreadImagesService;
import com.vortexwolf.chan.services.presentation.ClickListenersFactory;
import com.vortexwolf.chan.services.presentation.PostItemViewBuilder;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class PostsListAdapter extends ArrayAdapter<PostItemViewModel> implements IURLSpanClickListener, IBusyAdapter {
    private static final String TAG = "PostsListAdapter";

    private final LayoutInflater mInflater;
    private final String mBoardName;
    private final String mThreadNumber;
    private final String mUri;
    private final PostsViewModel mPostsViewModel;
    private final Theme mTheme;
    private final ApplicationSettings mSettings;
    private final ListView mListView;
    private final Context mActivityContext;
    private final PostItemViewBuilder mPostItemViewBuilder;
    private final DvachUriBuilder mDvachUriBuilder;
    private final Timer mLoadImagesTimer;
    private final ThreadImagesService mThreadImagesService;
    private final DvachUriParser mUriParser;

    private boolean mIsBusy = false;
    private boolean mIsLoadingMore = false;
    private LoadImagesTimerTask mCurrentLoadImagesTask;

    public PostsListAdapter(Context context, String boardName, String threadNumber, ApplicationSettings settings, Theme theme, ListView listView, DvachUriBuilder dvachUriBuilder, ThreadImagesService threadImagesService, DvachUriParser uriParser) {
        super(context.getApplicationContext(), 0);

        this.mBoardName = boardName;
        this.mThreadNumber = threadNumber;
        this.mInflater = LayoutInflater.from(context);
        this.mTheme = theme;
        this.mPostsViewModel = new PostsViewModel(boardName, threadNumber);
        this.mSettings = settings;
        this.mListView = listView;
        this.mActivityContext = context;
        this.mDvachUriBuilder = dvachUriBuilder;
        this.mPostItemViewBuilder = new PostItemViewBuilder(this.mActivityContext, this.mBoardName, this.mThreadNumber, this.mSettings, this.mDvachUriBuilder);
        this.mLoadImagesTimer = new Timer();
        this.mThreadImagesService = threadImagesService;
        this.mUriParser = uriParser;
        this.mUri = this.mDvachUriBuilder.createThreadUri(this.mBoardName, this.mThreadNumber);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (this.isStatusView(position)) {
            return this.mInflater.inflate(R.layout.loading, null);
        }

        PostItemViewModel model = this.getItem(position);
        View view = this.mPostItemViewBuilder.getView(model, convertView, this.mIsBusy);

        // cut long posts if necessary
        int maxPostHeight = this.mSettings.getLongPostsMaxHeight();
        if (maxPostHeight == 0 || model.isLongTextExpanded()) {
            this.mPostItemViewBuilder.removeMaxHeight(view);
        } else {
            this.mPostItemViewBuilder.setMaxHeight(view, maxPostHeight, this.mTheme);
        }

        return view;
    }

    @Override
    public void onClick(View v, ClickableURLSpan span, String url) {

        Uri uri = Uri.parse(url);
        String pageName = this.mUriParser.getThreadNumber(uri);

        // Если ссылка указывает на этот тред - перескакиваем на нужный пост,
        // иначе открываем в браузере
        if (this.mThreadNumber.equals(pageName)) {
            String postNumber = uri.getFragment();
            // Переходим на тот пост, куда указывает ссылка
            int position = postNumber != null ? this.findPostByNumber(postNumber) : Constants.OP_POST_POSITION;
            if (position == -1) {
                AppearanceUtils.showToastMessage(this.getContext(), this.getContext().getString(R.string.notification_post_not_found));
                return;
            }

            if (this.mSettings.isLinksInPopup()) {
                this.mPostItemViewBuilder.displayPopupDialog(this.getItem(position), this.mActivityContext, this.mTheme,
                        Constants.SDK_VERSION >= 4 && CompatibilityUtilsImpl.isTablet(mActivityContext) ? getSpanCoordinates(v, span) : null);
            } else {
                this.mListView.setSelection(position);
            }
        } else {
            ClickListenersFactory.getDefaultSpanClickListener(this.mDvachUriBuilder).onClick(v, span, url);
        }
    }
    
    private Point getSpanCoordinates(View widget, ClickableURLSpan span) {
        TextView parentTextView = (TextView) widget;

        Rect parentTextViewRect = new Rect();

        // Initialize values for the computing of clickedText position
        SpannableString completeText = (SpannableString)(parentTextView).getText();
        Layout textViewLayout = parentTextView.getLayout();

        double startOffsetOfClickedText = completeText.getSpanStart(span);
        double endOffsetOfClickedText = completeText.getSpanEnd(span);
        double startXCoordinatesOfClickedText = textViewLayout.getPrimaryHorizontal((int)startOffsetOfClickedText);
        double endXCoordinatesOfClickedText = textViewLayout.getPrimaryHorizontal((int)endOffsetOfClickedText);


        // Get the rectangle of the clicked text
        int currentLineStartOffset = textViewLayout.getLineForOffset((int)startOffsetOfClickedText);
        int currentLineEndOffset = textViewLayout.getLineForOffset((int)endOffsetOfClickedText);
        boolean keywordIsInMultiLine = currentLineStartOffset != currentLineEndOffset;
        textViewLayout.getLineBounds(currentLineStartOffset, parentTextViewRect);


        // Update the rectangle position to his real position on screen
        int[] parentTextViewLocation = {0,0};
        parentTextView.getLocationOnScreen(parentTextViewLocation);
        
        double parentTextViewTopAndBottomOffset = (
            parentTextViewLocation[1] -
            parentTextView.getScrollY() +
            parentTextView.getCompoundPaddingTop()
        );
        if (mActivityContext instanceof Activity) {
            Rect windowRect = new Rect();
            ((Activity)mActivityContext).getWindow().getDecorView().getWindowVisibleDisplayFrame(windowRect);
            parentTextViewTopAndBottomOffset -= windowRect.top;
        }
        parentTextViewRect.top += parentTextViewTopAndBottomOffset;
        parentTextViewRect.bottom += parentTextViewTopAndBottomOffset;

        parentTextViewRect.left += (
            parentTextViewLocation[0] +
            startXCoordinatesOfClickedText +
            parentTextView.getCompoundPaddingLeft() -
            parentTextView.getScrollX()
        );
        parentTextViewRect.right = (int) (
            parentTextViewRect.left +
            endXCoordinatesOfClickedText -
            startXCoordinatesOfClickedText
        );

        int x = (parentTextViewRect.left + parentTextViewRect.right) / 2;
        int y = (parentTextViewRect.top + parentTextViewRect.bottom) / 2;
        if (keywordIsInMultiLine) {
            x = parentTextViewRect.left;
        }

        return new Point(x, y);
    }

    private int findPostByNumber(String postNumber) {
        PostItemViewModel vm = this.mPostsViewModel.getModel(postNumber);
        if (vm != null) {
            return vm.getPosition();
        }
        return -1;
    }

    /** Возвращает номер последнего сообщения */
    public String getLastPostNumber() {
        return this.mPostsViewModel.getLastPostNumber();
    }

    /** Обновляет адаптер полностью */
    public void setAdapterData(PostModel[] posts) {
        this.clear();

        List<PostItemViewModel> models = this.mPostsViewModel.addModels(Arrays.asList(posts), this.mTheme, this, this.mActivityContext.getResources());
        for (PostItemViewModel model : models) {
            for (int i=0; i<4; ++i) {
                AttachmentInfo attachment = model.getAttachment(i);
                if (attachment != null) {
                    if (attachment.isImage()) {
                        this.mThreadImagesService.addThreadImage(this.mUri, attachment.getImageUrlIfImage(), attachment.getSize());
                    } else {
                        this.mThreadImagesService.addThreadImage(this.mUri, null, attachment.getSize(), attachment);
                    }
                }
            }

            this.add(model);
        }
    }

    public void scrollToPost(String postNumber) {
        if (StringUtils.isEmpty(postNumber)) {
            return;
        }

        int position = this.findPostByNumber(postNumber);
        if (position == -1) {
            AppearanceUtils.showToastMessage(this.getContext(), this.getContext().getString(R.string.notification_post_not_found));
            return;
        }

        this.mListView.setSelection(position);
    }

    public int updateAdapterData(String from, PostModel[] posts) {
        Integer lastPostNumber;
        try {
            lastPostNumber = !StringUtils.isEmpty(from) ? Integer.valueOf(from) : 0;
        } catch (NumberFormatException e) {
            lastPostNumber = 0;
        }

        ArrayList<PostModel> newPosts = new ArrayList<PostModel>();
        for (PostModel pi : posts) {
            Integer currentNumber = !StringUtils.isEmpty(pi.getNumber()) ? Integer.parseInt(pi.getNumber()) : 0;
            if (currentNumber > lastPostNumber) {
                newPosts.add(pi);
            }
        }

        List<PostItemViewModel> newModels = this.mPostsViewModel.addModels(newPosts, this.mTheme, this, this.mActivityContext.getResources());
        for (PostItemViewModel model : newModels) {
            for (int i=0; i<4; ++i) {
                AttachmentInfo attachment = model.getAttachment(i);
                if (attachment != null) {
                    if (attachment.isImage()) {
                        this.mThreadImagesService.addThreadImage(this.mUri, attachment.getImageUrlIfImage(), attachment.getSize());
                    } else {
                        this.mThreadImagesService.addThreadImage(this.mUri, null, attachment.getSize(), attachment);
                    }
                }
            }

            this.add(model);
        }

        // обновить все видимые элементы, чтобы правильно перерисовался список
        // ссылок replies
        if (newPosts.size() > 0) {
            this.notifyDataSetChanged();
        }

        return newPosts.size();
    }

    @Override
    public void setBusy(boolean value, AbsListView listView) {
        if (this.mCurrentLoadImagesTask != null) {
            this.mCurrentLoadImagesTask.cancel();
        }

        if (this.mIsBusy == true && value == false) {
            this.mCurrentLoadImagesTask = new LoadImagesTimerTask();
            this.mLoadImagesTimer.schedule(this.mCurrentLoadImagesTask, 500);
        }

        this.mIsBusy = value;
    }

    private void loadListImages() {
        int count = this.mListView.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = this.mListView.getChildAt(i);
            int position = this.mListView.getPositionForView(v);

            this.mPostItemViewBuilder.displayThumbnail(v, this.getItem(position));
        }
    }

    public void setLoadingMore(boolean isLoadingMore) {
        this.mIsLoadingMore = isLoadingMore;
        this.notifyDataSetChanged();
    }

    private final boolean hasStatusView() {
        return this.mIsLoadingMore;
    }

    private final boolean isStatusView(int position) {
        return this.hasStatusView() && position == this.getCount() - 1;
    }

    @Override
    public int getCount() {
        int i = super.getCount();
        if (this.hasStatusView()) {
            i++;
        }
        return i;
    }

    @Override
    public PostItemViewModel getItem(int position) {
        return (position < super.getCount()) ? super.getItem(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return this.isStatusView(position) ? Long.MIN_VALUE : super.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return this.isStatusView(position) ? Adapter.IGNORE_ITEM_VIEW_TYPE : super.getItemViewType(position);
    }

    @Override
    public boolean isEnabled(int position) {
        return !this.isStatusView(position);
    }

    private class LoadImagesTimerTask extends TimerTask {
        @Override
        public void run() {
            MyLog.d(TAG, "LoadImagesTimerTask");
            PostsListAdapter.this.mListView.post(new LoadImagesRunnable());
        }
    }

    private class LoadImagesRunnable implements Runnable {
        @Override
        public void run() {
            PostsListAdapter.this.loadListImages();
        }
    }
}
