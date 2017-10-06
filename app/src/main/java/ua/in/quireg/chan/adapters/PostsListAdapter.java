package ua.in.quireg.chan.adapters;

import android.app.Activity;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.library.MyLog;
import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.common.utils.CompatibilityUtils;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.interfaces.IBusyAdapter;
import ua.in.quireg.chan.interfaces.IURLSpanClickListener;
import ua.in.quireg.chan.interfaces.IUrlBuilder;
import ua.in.quireg.chan.interfaces.IUrlParser;
import ua.in.quireg.chan.interfaces.IWebsite;
import ua.in.quireg.chan.models.domain.PostModel;
import ua.in.quireg.chan.models.presentation.AttachmentInfo;
import ua.in.quireg.chan.models.presentation.IPostListEntity;
import ua.in.quireg.chan.models.presentation.PostItemViewModel;
import ua.in.quireg.chan.models.presentation.PostsViewModel;
import ua.in.quireg.chan.models.presentation.StatusIndicatorEntity;
import ua.in.quireg.chan.services.BrowserLauncher;
import ua.in.quireg.chan.services.ThreadImagesService;
import ua.in.quireg.chan.services.presentation.PostItemViewBuilder;
import ua.in.quireg.chan.settings.ApplicationSettings;
import ua.in.quireg.chan.ui.controls.ClickableURLSpan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PostsListAdapter extends ArrayAdapter<IPostListEntity> implements IURLSpanClickListener, IBusyAdapter {
    private static final String TAG = "PostsListAdapter";

    private static final int ITEM_VIEW_TYPE_POST = 0;
    private static final int ITEM_VIEW_TYPE_STATUS = 1;

    private final LayoutInflater mInflater;
    private final IWebsite mWebsite;
    private final String mBoardName;
    private final String mThreadNumber;
    private final String mUri;
    private final PostsViewModel mPostsViewModel;
    private final StatusIndicatorEntity mStatusViewModel;
    private final Theme mTheme;
    private final Activity mActivity;
    private final ApplicationSettings mSettings;
    private final ListView mListView;
    private final PostItemViewBuilder mPostItemViewBuilder;
    private final IUrlBuilder mUrlBuilder;
    private final Timer mLoadImagesTimer;
    private final ThreadImagesService mThreadImagesService;
    private final IUrlParser mUrlParser;
    private final ArrayList<PostModel> mOriginalPosts = new ArrayList<PostModel>();

    private StatusItemViewBag mStatusView;
    private boolean mIsUpdating = false;
    private boolean mIsBusy = false;
    private LoadImagesTimerTask mCurrentLoadImagesTask;

    public PostsListAdapter(Activity activity, IWebsite website, String boardName, String threadNumber, ListView listView) {
        super(activity, 0);

        mTheme = activity.getTheme();

        mWebsite = website;
        mActivity = activity;
        mListView = listView;
        mBoardName = boardName;
        mThreadNumber = threadNumber;

        mSettings = Factory.resolve(ApplicationSettings.class);
        mThreadImagesService = Factory.resolve(ThreadImagesService.class);

        mInflater = LayoutInflater.from(activity);
        mPostsViewModel = new PostsViewModel(website, boardName, threadNumber);
        mStatusViewModel = new StatusIndicatorEntity();

        mUrlBuilder = mWebsite.getUrlBuilder();
        mLoadImagesTimer = new Timer();
        mUrlParser = mWebsite.getUrlParser();
        mUri = mUrlBuilder.getThreadUrlHtml(mBoardName, mThreadNumber);
        mPostItemViewBuilder = new PostItemViewBuilder(activity, mWebsite, mBoardName, mThreadNumber, mSettings);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final IPostListEntity item = getItem(position);

        if (item instanceof StatusIndicatorEntity) {

            if(convertView == null){
                convertView = mInflater.inflate(R.layout.posts_list_status_item, null);
            }

            StatusItemViewBag vb = (StatusItemViewBag)convertView.getTag();
            if (vb == null) {
                vb = new StatusItemViewBag();
                vb.model = (StatusIndicatorEntity)item;
                vb.hintView = convertView.findViewById(R.id.statusHintView);
                vb.loadingView = convertView.findViewById(R.id.statusLoadingView);
                convertView.setTag(vb);
            }
            mStatusView = vb;

            mStatusView.setLoading(mIsUpdating);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO figure out what it was
                    //mActivity.refresh();
                }
            });
        }
        else {
            PostItemViewModel model = (PostItemViewModel)item;
            convertView = mPostItemViewBuilder.getView(model, convertView, mIsBusy);

            // cut long posts if necessary
            int maxPostHeight = mSettings.getLongPostsMaxHeight();
            if (maxPostHeight == 0 || model.isLongTextExpanded()) {
                mPostItemViewBuilder.removeMaxHeight(convertView);
            } else {
                mPostItemViewBuilder.setMaxHeight(convertView, maxPostHeight, mTheme);
            }
        }

        return convertView;
    }

    @Override
    public void onClick(View v, ClickableURLSpan span, String url) {

        Uri uri = Uri.parse(url);
        String pageName = mUrlParser.getThreadNumber(uri);

        // Если ссылка указывает на этот тред - перескакиваем на нужный пост,
        // иначе открываем в браузере
        if (mThreadNumber.equals(pageName)) {
            String postNumber = uri.getFragment();
            // Переходим на тот пост, куда указывает ссылка
            int position = postNumber != null ? findPostByNumber(postNumber) : Constants.OP_POST_POSITION;
            if (position == -1) {
                AppearanceUtils.showToastMessage(getContext(), getContext().getString(R.string.notification_post_not_found));
                return;
            }

            IPostListEntity item = getItem(position);
            if (!(item instanceof PostItemViewModel)) {
                return;
            }

            if (mSettings.isLinksInPopup()) {
                mPostItemViewBuilder.displayPopupDialog(
                        (PostItemViewModel) item,
                        mActivity, mTheme,
                        CompatibilityUtils.isTablet(mActivity) ? getSpanCoordinates(v, span) : null);
            } else {
                mListView.setSelection(position);
            }
        } else {
            BrowserLauncher.launchExternalBrowser(v.getContext(), mUrlBuilder.makeAbsolute(url));
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

        Rect windowRect = new Rect();
        mActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(windowRect);
        parentTextViewTopAndBottomOffset -= windowRect.top;

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
        PostItemViewModel vm = mPostsViewModel.getModel(postNumber);
        if (vm != null) {
            return vm.getPosition();
        }
        return -1;
    }

    /** Возвращает номер последнего сообщения */
    public int getLastPostNumber() {
        return mPostsViewModel.getLastPostNumber();
    }

    /** Обновляет адаптер полностью */
    public void setAdapterData(PostModel[] posts) {
        clear();
        mOriginalPosts.clear();
        mThreadImagesService.clearThreadImages(mUri);

        List<PostItemViewModel> models = mPostsViewModel.addModels(Arrays.asList(posts), mTheme, this, mActivity.getResources());
        for (PostItemViewModel model : models) {
            for (int i=0; i < Constants.MAX_ATTACHMENTS; i++) {
                AttachmentInfo attachment = model.getAttachment(i);
                mThreadImagesService.addThreadImage(mUri, attachment);
            }

            add(model);
        }

        mOriginalPosts.addAll(Arrays.asList(posts));

        add(mStatusViewModel);
    }

    public void scrollToPost(String postNumber) {
        if (StringUtils.isEmpty(postNumber)) {
            return;
        }

        int position = findPostByNumber(postNumber);
        if (position == -1) {
            AppearanceUtils.showToastMessage(getContext(), getContext().getString(R.string.notification_post_not_found));
            return;
        }

        mListView.setSelection(position);
    }

    public int updateAdapterData(int from, PostModel[] posts) {
        ArrayList<PostModel> newPosts = new ArrayList<PostModel>();
        for (PostModel pi : posts) {
            Integer currentNumber = !StringUtils.isEmpty(pi.getNumber()) ? Integer.parseInt(pi.getNumber()) : 0;
            if (currentNumber > from) {
                newPosts.add(pi);
            }
        }

        List<PostItemViewModel> newModels = mPostsViewModel.addModels(newPosts, mTheme, this, mActivity.getResources());
        for (PostItemViewModel model : newModels) {
            for (int i=0; i < Constants.MAX_ATTACHMENTS; i++) {
                AttachmentInfo attachment = model.getAttachment(i);
                mThreadImagesService.addThreadImage(mUri, attachment);
            }

            insert(model, getCount() - 1);
        }

        mOriginalPosts.addAll(newPosts);

        // обновить все видимые элементы, чтобы правильно перерисовался список
        // ссылок replies
        if (newPosts.size() > 0) {
            notifyDataSetChanged();
        }

        return newPosts.size();
    }

    @Override
    public void setBusy(boolean value, AbsListView listView) {
        if (mCurrentLoadImagesTask != null) {
            mCurrentLoadImagesTask.cancel();
        }

        if (mIsBusy == true && value == false) {
            mCurrentLoadImagesTask = new LoadImagesTimerTask();
            mLoadImagesTimer.schedule(mCurrentLoadImagesTask, 500);
        }

        mIsBusy = value;
    }

    private void loadListImages() {
        int count = mListView.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = mListView.getChildAt(i);
            int position = mListView.getPositionForView(v);

            IPostListEntity item = getItem(position);
            if (item instanceof PostItemViewModel) {
                mPostItemViewBuilder.displayThumbnail(v, (PostItemViewModel)item);
            }
        }
    }

    public void setUpdating(boolean isUpdating) {
        mIsUpdating = isUpdating;
        if (mStatusView != null) {
            mStatusView.setLoading(isUpdating);
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position) instanceof StatusIndicatorEntity
                ? ITEM_VIEW_TYPE_STATUS
                : ITEM_VIEW_TYPE_POST;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItem(position).isListItemEnabled();
    }

    public PostModel[] getOriginalPosts(){
        return mOriginalPosts.toArray(new PostModel[mOriginalPosts.size()]);
    }

    public List<String> getAllPostFiles() {
        ArrayList<String> filePaths = new ArrayList<String>();
        for (PostItemViewModel model : mPostsViewModel.getAllModels()) {
            for (int i = 0; i < model.getAttachmentsNumber(); i++) {
                AttachmentInfo attachment = model.getAttachment(i);
                filePaths.add(attachment.getSourceUrl());
            }
        }

        return filePaths;
    }

    private class LoadImagesTimerTask extends TimerTask {
        @Override
        public void run() {
            MyLog.d(TAG, "LoadImagesTimerTask");
            mListView.post(new LoadImagesRunnable());
        }
    }

    private class LoadImagesRunnable implements Runnable {
        @Override
        public void run() {
            loadListImages();
        }
    }

    private class StatusItemViewBag {
        public StatusIndicatorEntity model;

        public View hintView;
        public View loadingView;

        public void setLoading(boolean isLoading) {
            model.setLoading(isLoading);
            hintView.setVisibility(!isLoading ? View.VISIBLE : View.GONE);
            loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }
}
