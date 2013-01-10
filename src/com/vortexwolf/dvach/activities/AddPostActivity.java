package com.vortexwolf.dvach.activities;

import java.io.File;
import org.apache.http.impl.client.DefaultHttpClient;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.asynctasks.DownloadCaptchaTask;
import com.vortexwolf.dvach.asynctasks.SendPostTask;
import com.vortexwolf.dvach.common.*;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.common.utils.HtmlUtils;
import com.vortexwolf.dvach.common.utils.StringUtils;
import com.vortexwolf.dvach.common.utils.ThreadPostUtils;
import com.vortexwolf.dvach.common.utils.UriUtils;
import com.vortexwolf.dvach.interfaces.ICaptchaView;
import com.vortexwolf.dvach.interfaces.IDraftPostsStorage;
import com.vortexwolf.dvach.interfaces.IHtmlCaptchaChecker;
import com.vortexwolf.dvach.interfaces.IJsonApiReader;
import com.vortexwolf.dvach.interfaces.IPostSendView;
import com.vortexwolf.dvach.interfaces.IPostSender;
import com.vortexwolf.dvach.models.domain.CaptchaEntity;
import com.vortexwolf.dvach.models.domain.PostEntity;
import com.vortexwolf.dvach.models.presentation.CaptchaViewType;
import com.vortexwolf.dvach.models.presentation.DraftPostModel;
import com.vortexwolf.dvach.models.presentation.ImageFileModel;
import com.vortexwolf.dvach.models.presentation.SerializableFileModel;
import com.vortexwolf.dvach.services.Tracker;
import com.vortexwolf.dvach.services.domain.HtmlCaptchaChecker;
import com.vortexwolf.dvach.services.domain.HttpStringReader;
import com.vortexwolf.dvach.services.presentation.DvachUriBuilder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.httpimage.NetworkResourceLoader;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class AddPostActivity extends Activity implements IPostSendView, ICaptchaView {
    public static final String TAG = "AddPostActivity";

    private MainApplication mApplication;
    private IJsonApiReader mJsonReader;
    private IPostSender mPostSender;
    private IHtmlCaptchaChecker mHtmlCaptchaChecker;
    private NetworkResourceLoader mNetworkResourceLoader;
    private IDraftPostsStorage mDraftPostsStorage;
    private Tracker mTracker;

    private ImageFileModel mAttachedFile;
    private String mAttachedVideo;
    private CaptchaEntity mCaptcha;
    private String mBoardName;
    private String mThreadNumber;
    private Uri mRefererUri;
    private CaptchaViewType mCurrentCaptchaView = null;
    private Bitmap mCaptchaBitmap;

    private SendPostTask mCurrentPostSendTask = null;
    private DownloadCaptchaTask mCurrentDownloadCaptchaTask = null;

    private View mCaptchaSkipView = null;
    private View mCaptchaLoadingView = null;
    private ImageView mCaptchaImageView = null;
    private EditText mCaptchaAnswerView = null;
    private CheckBox mSageCheckBox;
    private EditText mCommentView;
    private View mAttachmentView;
    private ProgressDialog mProgressDialog;
    private Button mSendButton;
    private EditText mSubjectView;
    private Spinner mPoliticsView;

    // Определяет, нужно ли сохранять пост (если не отправлен) или можно удалить
    // (после успешной отправки)
    private boolean mFinishedSuccessfully = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mApplication = (MainApplication) getApplication();
        this.mJsonReader = this.mApplication.getJsonApiReader();
        this.mPostSender = this.mApplication.getPostSender();
        DefaultHttpClient httpClient = MainApplication.getHttpClient();
        this.mHtmlCaptchaChecker = new HtmlCaptchaChecker(new HttpStringReader(httpClient), Factory.getContainer().resolve(DvachUriBuilder.class));
        this.mNetworkResourceLoader = new NetworkResourceLoader(httpClient);
        this.mDraftPostsStorage = this.mApplication.getDraftPostsStorage();
        this.mTracker = this.mApplication.getTracker();
        DvachUriBuilder uriBuilder = Factory.getContainer().resolve(DvachUriBuilder.class);

        // Парсим название борды и номер треда
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            this.mBoardName = extras.getString(Constants.EXTRA_BOARD_NAME);
            this.mThreadNumber = extras.getString(Constants.EXTRA_THREAD_NUMBER);
            this.mRefererUri = this.mThreadNumber == Constants.ADD_THREAD_PARENT
                    ? uriBuilder.create2chBoardUri(this.mBoardName, 0)
                    : Uri.parse(uriBuilder.create2chThreadUrl(this.mBoardName, this.mThreadNumber));
        }

        this.resetUI();

        StringBuilder commentBuilder = new StringBuilder();

        // Восстанавливаем состояние, если было сохранено
        DraftPostModel draft = this.mDraftPostsStorage.getDraft(this.mBoardName, this.mThreadNumber);
        if (draft != null) {
            if (!StringUtils.isEmpty(draft.getComment())) {
                commentBuilder.append(draft.getComment() + "\n");
            }

            if (draft.getAttachedFile() != null) {
                this.setAttachment(draft.getAttachedFile());
            }

            this.mSageCheckBox.setChecked(draft.isSage());

            if (draft.getCaptchaType() == CaptchaViewType.SKIP) {
                this.skipCaptcha();
            } else if (draft.getCaptchaType() == CaptchaViewType.IMAGE) {
                this.showCaptcha(draft.getCaptcha(), draft.getCaptchaImage());
            }
        }

        if (extras != null) {
            String postNumber = extras.getString(Constants.EXTRA_POST_NUMBER);
            String postComment = extras.getString(Constants.EXTRA_POST_COMMENT);
            if (postNumber != null) {
                commentBuilder.append(">>" + postNumber + "\n");
            }

            if (!StringUtils.isEmpty(postComment)) {
                postComment = ThreadPostUtils.removeLinksFromComment(postComment);

                postComment = postComment.replaceAll("(\n+)", "$1>");
                commentBuilder.append(">" + postComment + "\n");
            }
        }

        // Сохраняем коммент
        this.mCommentView.setText(commentBuilder.toString());
        // Ставим курсор в конце текстового поля
        this.mCommentView.setSelection(commentBuilder.length());

        // Загружаем и показываем капчу
        if (this.mCurrentCaptchaView == null) {
            this.refreshCaptcha();
        }

        if (Constants.ADD_THREAD_PARENT.equals(this.mThreadNumber)) {
            this.setTitle(String.format(getString(R.string.data_add_thread_title), mBoardName));
            this.mSubjectView.setVisibility(View.VISIBLE);
        } else {
            this.setTitle(String.format(getString(R.string.data_add_post_title), mBoardName, mThreadNumber));
        }

        this.mTracker.setBoardVar(mBoardName);
        this.mTracker.trackActivityView(TAG);
    }

    @Override
    protected void onPause() {
        MyLog.v(TAG, "save state");
        if (!this.mFinishedSuccessfully) {
            DraftPostModel draft = new DraftPostModel(this.mCommentView.getText().toString(), this.mAttachedFile, this.mSageCheckBox.isChecked(), this.mCurrentCaptchaView, this.mCaptcha, this.mCaptchaBitmap);

            this.mDraftPostsStorage.saveDraft(this.mBoardName, this.mThreadNumber, draft);
        } else {
            this.mDraftPostsStorage.clearDraft(this.mBoardName, this.mThreadNumber);
        }

        super.onPause();
    }

    private void resetUI() {
        this.setTheme(this.mApplication.getSettings().getTheme());
        this.setContentView(R.layout.add_post_view);

        this.mCaptchaImageView = (ImageView) findViewById(R.id.addpost_captcha_image);
        this.mCaptchaLoadingView = findViewById(R.id.addpost_captcha_loading);
        this.mCaptchaSkipView = findViewById(R.id.addpost_captcha_skip_text);
        this.mCaptchaAnswerView = (EditText) this.findViewById(R.id.addpost_captcha_input);
        this.mCommentView = (EditText) findViewById(R.id.addpost_comment_input);
        this.mSageCheckBox = (CheckBox) this.findViewById(R.id.addpost_sage_checkbox);
        this.mAttachmentView = findViewById(R.id.addpost_attachment_view);
        this.mSendButton = (Button) this.findViewById(R.id.addpost_send_button);
        this.mSubjectView = (EditText) this.findViewById(R.id.addpost_subject);
        this.mPoliticsView = (Spinner) this.findViewById(R.id.addpost_politics);
        final ImageButton removeAttachmentButton = (ImageButton) this.findViewById(R.id.addpost_attachment_remove);
        final ImageButton refreshCaptchaButton = (ImageButton) this.findViewById(R.id.addpost_refresh_button);
        final LinearLayout textFormatView = (LinearLayout) this.findViewById(R.id.addpost_textformat_view);

        if (ThreadPostUtils.isPoliticsBoard(mBoardName)) {
            mPoliticsView.setVisibility(View.VISIBLE);
        }

        View.OnClickListener formatButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.addpost_textformat_b:
                        formatSelectedText("b");
                        break;
                    case R.id.addpost_textformat_i:
                        formatSelectedText("i");
                        break;
                    case R.id.addpost_textformat_s:
                        formatSelectedText("s");
                        break;
                    case R.id.addpost_textformat_spoiler:
                        formatSelectedText("spoiler");
                        break;
                    case R.id.addpost_textformat_u:
                        formatSelectedText("u");
                        break;
                    case R.id.addpost_textformat_quote:
                        formatQuote();
                        break;
                }
            }
        };

        for (int i = 0; i < textFormatView.getChildCount(); i++) {
            ImageButton b = (ImageButton) textFormatView.getChildAt(i);
            if (b != null) {
                b.setOnClickListener(formatButtonListener);
            }
        }

        // Обрабатываем нажатие на кнопку "Отправить"
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddPostActivity.this.onSend();
            }
        });
        // Удаляем прикрепленный файл
        removeAttachmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddPostActivity.this.removeAttachment();
            }
        });
        // Обновляем нажатие кнопки Refresh для капчи
        refreshCaptchaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddPostActivity.this.refreshCaptcha();
            }
        });
    }

    private void onSend() {
        // Собираем все заполненные поля
        String captchaAnswer = this.mCaptchaAnswerView.getText().toString();
        // if(this.mCurrentCaptchaView == CaptchaViewType.LOADING ||
        // (this.mCurrentCaptchaView == CaptchaViewType.IMAGE &&
        // StringUtils.isEmpty(captchaAnswer))){
        // AppearanceUtils.showToastMessage(this,
        // getString(R.string.warning_enter_captcha));
        // return;
        // }

        String comment = this.mCommentView.getText().toString();
        HtmlUtils.trimBr(comment);
        if (StringUtils.isEmpty(comment) && this.mAttachedFile == null) {
            AppearanceUtils.showToastMessage(this, getString(R.string.warning_write_comment));
            return;
        }

        boolean isSage = this.mSageCheckBox.isChecked();

        File attachment = this.mAttachedFile != null
                ? this.mAttachedFile.file
                : null;
        if (this.mThreadNumber.equals(Constants.ADD_THREAD_PARENT)
                && attachment == null) {
            AppearanceUtils.showToastMessage(this, getString(R.string.warning_attach_file_new_thread));
            return;
        }

        String subject = StringUtils.nullIfEmpty(this.mSubjectView.getText().toString());

        String politics = null;
        if (ThreadPostUtils.isPoliticsBoard(mBoardName)) {
            int itemIndex = mPoliticsView.getSelectedItemPosition();
            politics = Math.max(itemIndex - 1, -1) + ""; // the list starts from
                                                         // -1
        }

        String name = this.mApplication.getSettings().getName();
        String captchaKey = mCaptcha != null ? mCaptcha.getKey() : null;
        PostEntity pe = new PostEntity(captchaKey, captchaAnswer, comment, isSage, attachment, subject, politics, name, this.mAttachedVideo);

        // Отправляем
        sendPost(pe);
    }

    private void sendPost(PostEntity pe) {

        if (this.mCurrentPostSendTask != null) {
            this.mCurrentPostSendTask.cancel(true);
        }

        this.mCurrentPostSendTask = new SendPostTask(this.mPostSender, this, mBoardName, mThreadNumber, pe);
        this.mCurrentPostSendTask.execute();
    }

    @Override
    public void showSuccess(String redirectedPage) {
        AppearanceUtils.showToastMessage(this, getString(R.string.notification_send_post_success));
        // return back to the list of posts
        String redirectedThreadNumber = null;
        if (redirectedPage != null) {
            redirectedThreadNumber = UriUtils.getThreadNumber(Uri.parse(redirectedPage));
        }

        this.mFinishedSuccessfully = true;

        // Завершаем с успешным результатом
        Intent intent = new Intent();
        intent.putExtra(Constants.EXTRA_REDIRECTED_THREAD_NUMBER, redirectedThreadNumber);
        this.setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void showError(String error) {
        error = error != null
                ? error
                : this.getString(R.string.error_send_post);
        AppearanceUtils.showToastMessage(this, error);

        if (error.startsWith("Ошибка: Неверный код подтверждения.")) {
            this.mCaptchaAnswerView.setText("");
            this.refreshCaptcha();
        }
    }

    @Override
    public void showPostLoading() {
        mSendButton.setEnabled(false);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(getString(R.string.loading));
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
    }

    @Override
    public void hidePostLoading() {
        mSendButton.setEnabled(true);
        mProgressDialog.dismiss();
        mCurrentPostSendTask = null;
    }

    @Override
    public void showCaptchaLoading() {
        switchToCaptchaView(CaptchaViewType.LOADING);
    }

    @Override
    public void skipCaptcha() {
        switchToCaptchaView(CaptchaViewType.SKIP);
    }

    @Override
    public void showCaptcha(CaptchaEntity captcha, Bitmap captchaImage) {
        this.mCaptchaImageView.setImageResource(android.R.color.transparent);
        if (this.mCaptchaBitmap != null) {
            this.mCaptchaBitmap.recycle();
        }

        this.mCaptcha = captcha;
        this.mCaptchaBitmap = captchaImage;
        this.mCaptchaImageView.setImageBitmap(captchaImage);

        switchToCaptchaView(CaptchaViewType.IMAGE);
    }

    @Override
    public void showCaptchaError(String errorMessage) {
        this.mCaptchaImageView.setImageResource(android.R.color.transparent);
        AppearanceUtils.showToastMessage(this, errorMessage);

        switchToCaptchaView(CaptchaViewType.ERROR);
    }

    private void switchToCaptchaView(CaptchaViewType vt) {
        this.mCurrentCaptchaView = vt;
        switch (vt) {
            case LOADING:
                mCaptchaImageView.setVisibility(View.GONE);
                mCaptchaImageView.setImageResource(android.R.color.transparent);
                mCaptchaLoadingView.setVisibility(View.VISIBLE);
                mCaptchaSkipView.setVisibility(View.GONE);
                break;
            case ERROR:
            case IMAGE:
                this.mCurrentDownloadCaptchaTask = null;
                mCaptchaAnswerView.setVisibility(View.VISIBLE);
                mCaptchaImageView.setVisibility(View.VISIBLE);
                mCaptchaLoadingView.setVisibility(View.GONE);
                mCaptchaSkipView.setVisibility(View.GONE);
                break;
            case SKIP:
                this.mCurrentDownloadCaptchaTask = null;
                mCaptchaAnswerView.setVisibility(View.GONE);
                mCaptchaImageView.setVisibility(View.GONE);
                mCaptchaLoadingView.setVisibility(View.GONE);
                mCaptchaSkipView.setVisibility(View.VISIBLE);
                break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.addpost, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_attach_file_id:
                Intent intent = new Intent(this, FilesListActivity.class);
                intent.putExtra(FilesListActivity.EXTRA_CURRENT_FILE, this.mAttachedFile != null
                        ? this.mAttachedFile.file.getAbsolutePath()
                        : null);
                startActivityForResult(intent, Constants.REQUEST_CODE_FILE_LIST_ACTIVITY);
                break;
            case R.id.menu_gallery_id:
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("image/*");
                startActivityForResult(i, Constants.REQUEST_CODE_GALLERY);
                break;
            case R.id.menu_attach_youtube_id:
                // A dialog with a text input
                final EditText linkTextView = new EditText(this);
                linkTextView.setHint(this.getString(R.string.attach_youtube_hint));
                FrameLayout rootLayout = new FrameLayout(this);
                rootLayout.setPadding(10, 0, 10, 0);
                rootLayout.addView(linkTextView);

                final AlertDialog d = new AlertDialog.Builder(this).setTitle(this.getString(R.string.attach_youtube_title)).setView(rootLayout).setNegativeButton(this.getString(R.string.cancel), null).setPositiveButton(this.getString(R.string.ok), null).create();

                // don't hide the dialog if there was an error
                d.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                        b.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                boolean success = AddPostActivity.this.setVideoAttachment(linkTextView.getText().toString());
                                if (success) {
                                    d.dismiss();
                                }
                            }
                        });
                    }
                });

                d.show();
                break;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constants.REQUEST_CODE_FILE_LIST_ACTIVITY:

                    SerializableFileModel fileModel = data.getParcelableExtra(getPackageName()
                            + Constants.EXTRA_SELECTED_FILE);

                    this.setAttachment(fileModel);

                    break;
                case Constants.REQUEST_CODE_GALLERY:

                    Uri selectedImage = data.getData();
                    String filePath = null;

                    String[] columns = { MediaStore.Images.Media.DATA };
                    Cursor cursor = this.getContentResolver().query(selectedImage, columns, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();

                        int columnIndex = cursor.getColumnIndex(columns[0]);
                        filePath = cursor.getString(columnIndex);

                        if (!cursor.isClosed()) {
                            cursor.close();
                        }
                    } else {
                        filePath = selectedImage.getPath();
                    }

                    // Почему-то было 2 error reports с NullReferenceException
                    // из-за метода File.fixSlashes, добавлю проверку
                    if (filePath != null) {
                        ImageFileModel image = new ImageFileModel(filePath);
                        this.setAttachment(image);
                    } else {
                        AppearanceUtils.showToastMessage(this, this.getString(R.string.error_image_cannot_be_attached));
                    }

                    break;
            }
        }
    }

    private boolean setVideoAttachment(String videoEditText) {
        if (StringUtils.isEmpty(videoEditText)) {
            AppearanceUtils.showToastMessage(this, this.getString(R.string.error_incorrect_youtube_link));
            return false;
        }

        String code = null;
        if (videoEditText.length() == Constants.YOUTUBE_CODE_LENGTH) {
            code = videoEditText;
        } else if (UriUtils.isYoutubeUri(Uri.parse(videoEditText))) {
            code = UriUtils.getYouTubeCode(videoEditText);
        }

        if (code != null) {
            this.mAttachedVideo = UriUtils.formatYoutubeUriFromCode(code);
            this.mAttachedFile = null;
            this.displayAttachmentView(this.getString(R.string.data_add_post_video_attachment_name, code), "youtube.com");
            return true;
        } else {
            AppearanceUtils.showToastMessage(this, this.getString(R.string.error_incorrect_youtube_link));
            return false;
        }
    }

    private void setAttachment(ImageFileModel fileModel) {
        this.mAttachedFile = fileModel;
        this.mAttachedVideo = null;

        String infoFormat = this.getResources().getString(R.string.data_add_post_attachment_info);
        String info = String.format(infoFormat, fileModel.getFileSize(), fileModel.imageWidth, fileModel.imageHeight);
        this.displayAttachmentView(fileModel.file.getName(), info);
    }

    private void displayAttachmentView(String name, String info) {
        this.mAttachmentView.setVisibility(View.VISIBLE);
        TextView fileNameView = (TextView) findViewById(R.id.addpost_attachment_name);
        TextView fileSizeView = (TextView) findViewById(R.id.addpost_attachment_size);

        fileNameView.setText(name);
        fileSizeView.setText(info);
    }

    private void removeAttachment() {
        this.mAttachedFile = null;
        this.mAttachedVideo = null;

        this.mAttachmentView.setVisibility(View.GONE);
    }

    private void refreshCaptcha() {
        if (mCurrentDownloadCaptchaTask != null) {
            mCurrentDownloadCaptchaTask.cancel(true);
        }

        mCurrentDownloadCaptchaTask = new DownloadCaptchaTask(this, this.mRefererUri, mJsonReader, mNetworkResourceLoader, mHtmlCaptchaChecker, MainApplication.getHttpClient());
        mCurrentDownloadCaptchaTask.execute();
    }

    private void formatSelectedText(String code) {
        Editable editable = mCommentView.getEditableText();
        String text = editable.toString();

        String startTag = "[" + code + "]";
        String endTag = "[/" + code + "]";

        int selectionStart = mCommentView.getSelectionStart();
        int selectionEnd = mCommentView.getSelectionEnd();
        String selectedText = text.substring(selectionStart, selectionEnd);

        // Проверяем текст на краях выделенной области, на случай если уже была
        // добавлена разметка
        String textBeforeSelection = text.substring(Math.max(0, selectionStart
                - startTag.length()), selectionStart);
        String textAfterSelection = text.substring(selectionEnd, Math.min(text.length(), selectionEnd
                + endTag.length()));

        // Удаляем теги форматирования если есть, добавляем если нет
        if (textBeforeSelection.equalsIgnoreCase(startTag)
                && textAfterSelection.equalsIgnoreCase(endTag)) {
            editable.replace(selectionStart - startTag.length(), selectionEnd
                    + endTag.length(), selectedText);
            mCommentView.setSelection(selectionStart - startTag.length(), selectionEnd
                    - startTag.length());
        } else {
            editable.replace(selectionStart, selectionEnd, startTag
                    + selectedText + endTag);
            mCommentView.setSelection(selectionStart + startTag.length(), selectionEnd
                    + startTag.length());
        }
    }

    private void formatQuote() {
        Editable editable = mCommentView.getEditableText();
        String text = editable.toString();

        int selectionStart = mCommentView.getSelectionStart();
        int selectionEnd = mCommentView.getSelectionEnd();
        String selectedText = text.substring(selectionStart, selectionEnd);
        String oneSymbolBefore = text.substring(Math.max(selectionStart - 1, 0), selectionStart);

        if (selectedText.startsWith(">")) {
            String unQuotedText = selectedText.replaceFirst(">", "").replaceAll("(\n+)>", "$1");
            int diff = selectedText.length() - unQuotedText.length();

            editable.replace(selectionStart, selectionEnd, unQuotedText);
            mCommentView.setSelection(selectionStart, selectionEnd - diff);
        } else {
            String firstSymbol = oneSymbolBefore.length() == 0
                    || oneSymbolBefore.equals("\n") ? "" : "\n";
            String quotedText = firstSymbol + ">"
                    + selectedText.replaceAll("(\n+)", "$1>");
            int diff = quotedText.length() - selectedText.length();

            editable.replace(selectionStart, selectionEnd, quotedText);
            mCommentView.setSelection(selectionStart + firstSymbol.length(), selectionEnd
                    + diff);
        }
    }
}
