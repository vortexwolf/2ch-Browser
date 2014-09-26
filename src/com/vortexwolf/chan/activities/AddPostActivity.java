package com.vortexwolf.chan.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.asynctasks.CheckCloudflareTask;
import com.vortexwolf.chan.asynctasks.DownloadCaptchaTask;
import com.vortexwolf.chan.asynctasks.SendPostTask;
import com.vortexwolf.chan.boards.dvach.DvachUriBuilder;
import com.vortexwolf.chan.boards.dvach.DvachUriParser;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.AppearanceUtils;
import com.vortexwolf.chan.common.utils.IoUtils;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.common.utils.ThreadPostUtils;
import com.vortexwolf.chan.common.utils.UriUtils;
import com.vortexwolf.chan.interfaces.ICaptchaView;
import com.vortexwolf.chan.interfaces.ICheckCaptchaView;
import com.vortexwolf.chan.interfaces.ICloudflareCheckListener;
import com.vortexwolf.chan.interfaces.IDraftPostsStorage;
import com.vortexwolf.chan.interfaces.IPostSendView;
import com.vortexwolf.chan.interfaces.IPostSender;
import com.vortexwolf.chan.models.domain.CaptchaEntity;
import com.vortexwolf.chan.models.domain.SendPostModel;
import com.vortexwolf.chan.models.presentation.AddAttachmentViewBag;
import com.vortexwolf.chan.models.presentation.CaptchaViewType;
import com.vortexwolf.chan.models.presentation.DraftPostModel;
import com.vortexwolf.chan.models.presentation.ImageFileModel;
import com.vortexwolf.chan.models.presentation.SerializableFileModel;
import com.vortexwolf.chan.services.CloudflareCheckService;
import com.vortexwolf.chan.services.IconsList;
import com.vortexwolf.chan.services.MyTracker;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class AddPostActivity extends Activity implements IPostSendView, ICaptchaView {
    public static final String TAG = "AddPostActivity";

    private final IPostSender mPostSender = Factory.resolve(IPostSender.class);
    private final ApplicationSettings mSettings = Factory.resolve(ApplicationSettings.class);
    private final IDraftPostsStorage mDraftPostsStorage = Factory.resolve(IDraftPostsStorage.class);
    private final MyTracker mTracker = Factory.resolve(MyTracker.class);
    private final DvachUriBuilder mUriBuilder = Factory.resolve(DvachUriBuilder.class);
    private final DvachUriParser mUriParser = Factory.resolve(DvachUriParser.class);
    
    private ImageFileModel[] mAttachedFiles = new ImageFileModel[4];
    private CaptchaEntity mCaptcha;
    private String mBoardName;
    private String mThreadNumber;
    private Uri mRefererUri;
    private CaptchaViewType mCurrentCaptchaView = null;
    private Bitmap mCaptchaBitmap;
    private boolean mCaptchaPasscodeSuccess;
    private boolean mCaptchaPasscodeFail;

    private SendPostTask mCurrentPostSendTask = null;
    private DownloadCaptchaTask mCurrentDownloadCaptchaTask = null;

    private TextView mCaptchaSkipView = null;
    private View mCaptchaLoadingView = null;
    private ImageView mCaptchaImageView = null;
    private EditText mCaptchaAnswerView = null;
    private CheckBox mSageCheckBox;
    private EditText mCommentView;
    private AddAttachmentViewBag[] mAttachmentViews = new AddAttachmentViewBag[4];
    private ProgressDialog mProgressDialog;
    private Button mSendButton;
    private EditText mSubjectView;
    private Spinner mPoliticsView;
    private boolean isPoliticsBoard = false;

    // Определяет, нужно ли сохранять пост (если не отправлен) или можно удалить
    // (после успешной отправки)
    private boolean mFinishedSuccessfully = false;
    
    private boolean isRecaptcha = false;
    private CheckCloudflareTask mCheckCloudflareTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Парсим название борды и номер треда
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            this.mBoardName = extras.getString(Constants.EXTRA_BOARD_NAME);
            this.mThreadNumber = extras.getString(Constants.EXTRA_THREAD_NUMBER);
            this.mRefererUri = UriUtils.createRefererUri(this.mBoardName, this.mThreadNumber);
        }

        this.resetUI();

        StringBuilder commentBuilder = new StringBuilder();

        // Восстанавливаем состояние, если было сохранено
        DraftPostModel draft = this.mDraftPostsStorage.getDraft(this.mBoardName, this.mThreadNumber);
        if (draft != null) {
            if (!StringUtils.isEmpty(draft.getComment())) {
                commentBuilder.append(draft.getComment() + "\n");
            }

            for (ImageFileModel file : draft.getAttachedFiles()) {
                this.setAttachment(file);
            }

            this.mSageCheckBox.setChecked(draft.isSage());

            if (draft.getCaptchaType() == CaptchaViewType.SKIP) {
                this.skipCaptcha(draft.isCaptchaPasscodeSuccess(), draft.isCaptchaPasscodeFail());
            } else if (draft.getCaptchaType() == CaptchaViewType.IMAGE && draft.getCaptchaImage() != null && draft.getCaptchaImage().isRecycled() == false) {
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
            this.setTitle(String.format(this.getString(R.string.data_add_thread_title), this.mBoardName));
            this.mSubjectView.setVisibility(View.VISIBLE);
        } else {
            this.setTitle(String.format(this.getString(R.string.data_add_post_title), this.mBoardName, this.mThreadNumber));
        }

        this.mTracker.setBoardVar(this.mBoardName);
        this.mTracker.trackActivityView(TAG);
    }

    @Override
    protected void onPause() {
        MyLog.v(TAG, "save state");
        if (!this.mFinishedSuccessfully) {
            DraftPostModel draft = new DraftPostModel(this.mCommentView.getText().toString(), this.getAttachments(), this.mSageCheckBox.isChecked(), this.mCurrentCaptchaView, this.mCaptcha, this.mCaptchaBitmap, this.mCaptchaPasscodeSuccess, this.mCaptchaPasscodeFail);

            this.mDraftPostsStorage.saveDraft(this.mBoardName, this.mThreadNumber, draft);
        } else {
            this.mDraftPostsStorage.clearDraft(this.mBoardName, this.mThreadNumber);
        }

        super.onPause();
    }

    private void resetUI() {
        this.setTheme(this.mSettings.getTheme());
        this.setContentView(R.layout.add_post_view);

        this.mCaptchaImageView = (ImageView) this.findViewById(R.id.addpost_captcha_image);
        this.mCaptchaLoadingView = this.findViewById(R.id.addpost_captcha_loading);
        this.mCaptchaSkipView = (TextView) this.findViewById(R.id.addpost_captcha_skip_text);
        this.mCaptchaAnswerView = (EditText) this.findViewById(R.id.addpost_captcha_input);
        this.mCommentView = (EditText) this.findViewById(R.id.addpost_comment_input);
        this.mSageCheckBox = (CheckBox) this.findViewById(R.id.addpost_sage_checkbox);
        this.mAttachmentViews[0] = AddAttachmentViewBag.fromView(this.findViewById(R.id.addpost_attachment_view_1));
        this.mAttachmentViews[1] = AddAttachmentViewBag.fromView(this.findViewById(R.id.addpost_attachment_view_2));
        this.mAttachmentViews[2] = AddAttachmentViewBag.fromView(this.findViewById(R.id.addpost_attachment_view_3));
        this.mAttachmentViews[3] = AddAttachmentViewBag.fromView(this.findViewById(R.id.addpost_attachment_view_4));
        this.mSendButton = (Button) this.findViewById(R.id.addpost_send_button);
        this.mSubjectView = (EditText) this.findViewById(R.id.addpost_subject);
        this.mPoliticsView = (Spinner) this.findViewById(R.id.addpost_politics);

        final ImageButton refreshCaptchaButton = (ImageButton) this.findViewById(R.id.addpost_refresh_button);
        final LinearLayout textFormatView = (LinearLayout) this.findViewById(R.id.addpost_textformat_view);

        try {
            if (Factory.resolve(IconsList.class).getData(mBoardName) != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Factory.resolve(IconsList.class).getData(mBoardName));
                this.mPoliticsView.setAdapter(adapter);
                this.mPoliticsView.setVisibility(View.VISIBLE);
                isPoliticsBoard = true;
            }
        } catch (Exception e) { MyLog.e(TAG, e); }

        View.OnClickListener formatButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.addpost_textformat_b:
                        AddPostActivity.this.formatSelectedText("b");
                        break;
                    case R.id.addpost_textformat_i:
                        AddPostActivity.this.formatSelectedText("i");
                        break;
                    case R.id.addpost_textformat_s:
                        AddPostActivity.this.formatSelectedText("s");
                        break;
                    case R.id.addpost_textformat_spoiler:
                        AddPostActivity.this.formatSelectedText("spoiler");
                        break;
                    case R.id.addpost_textformat_u:
                        AddPostActivity.this.formatSelectedText("u");
                        break;
                    case R.id.addpost_textformat_quote:
                        AddPostActivity.this.formatQuote();
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
        this.mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddPostActivity.this.onSend();
            }
        });
        // Удаляем прикрепленный файл
        for (int i = 0; i < this.mAttachmentViews.length; i++) {
            final int index = i;
            this.mAttachmentViews[i].removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AddPostActivity.this.removeAttachment(index);
                }
            });
        }
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
        
        if (this.isRecaptcha) {
            this.showPostLoading();
            if (this.mCheckCloudflareTask != null) {
                this.mCheckCloudflareTask.cancel(true);
            }
            mCheckCloudflareTask = new CheckCloudflareTask(this.mCaptcha, captchaAnswer, new ICheckCaptchaView(){
                @Override
                public void showSuccess() {
                    AddPostActivity.this.hidePostLoading();
                    AddPostActivity.this.setRecaptcha(false);
                    AddPostActivity.this.refreshCaptcha();
                }

                @Override
                public void showError(String message) {
                    AddPostActivity.this.hidePostLoading();
                    AddPostActivity.this.showError(message != null ? message : AddPostActivity.this.getString(R.string.error_cloudflare_recaptcha));
                    AddPostActivity.this.refreshCaptcha();
                }  
            });
            mCheckCloudflareTask.execute();
            return;
        }

        String comment = this.mCommentView.getText().toString();

        if (StringUtils.isEmpty(comment) && !this.hasAttachments()) {
            AppearanceUtils.showToastMessage(this, this.getString(R.string.warning_write_comment));
            return;
        }

        boolean isSage = this.mSageCheckBox.isChecked();

        if (this.mThreadNumber.equals(Constants.ADD_THREAD_PARENT) && !this.hasAttachments()) {
            AppearanceUtils.showToastMessage(this, this.getString(R.string.warning_attach_file_new_thread));
            return;
        }

        String subject = StringUtils.nullIfEmpty(this.mSubjectView.getText().toString());

        String politics = null;
        if (isPoliticsBoard) {
            int itemIndex = this.mPoliticsView.getSelectedItemPosition();
            politics = Math.max(itemIndex, 0) + "";
        }

        String name = this.mSettings.getName();
        String captchaKey = this.mCaptcha != null ? this.mCaptcha.getKey() : null;
        SendPostModel pe = new SendPostModel(captchaKey, captchaAnswer, comment, isSage, this.getAttachedFiles(), subject, politics, name);
        pe.setParentThread(this.mThreadNumber);

        // Отправляем
        this.sendPost(pe);
    }

    private void setRecaptcha(boolean isRecaptcha) {
        this.isRecaptcha = isRecaptcha;
        this.mCaptchaAnswerView.setInputType(isRecaptcha ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_CLASS_NUMBER);
        
    }

    private void sendPost(SendPostModel pe) {

        if (this.mCurrentPostSendTask != null) {
            this.mCurrentPostSendTask.cancel(true);
        }

        this.mCurrentPostSendTask = new SendPostTask(this.mPostSender, this, this, this.mBoardName, this.mThreadNumber, pe);
        this.mCurrentPostSendTask.execute();
    }
    
    @Override
    public void showSuccess(String redirectedPage) {
        AppearanceUtils.showToastMessage(this, this.getString(R.string.notification_send_post_success));
        // return back to the list of posts
        String redirectedThreadNumber = null;
        if (redirectedPage != null) {
            redirectedThreadNumber = this.mUriParser.getThreadNumber(Uri.parse(redirectedPage));
        }

        this.mFinishedSuccessfully = true;

        // Завершаем с успешным результатом
        Intent intent = new Intent();
        intent.putExtra(Constants.EXTRA_REDIRECTED_THREAD_NUMBER, redirectedThreadNumber);
        this.setResult(RESULT_OK, intent);
        this.finish();
    }

    @Override
    public void showError(String error) {
        error = error != null ? error : this.getString(R.string.error_send_post);
        AppearanceUtils.showToastMessage(this, error);

        if (error.startsWith("Ошибка: Неверный код подтверждения.") || error.startsWith("Капча невалидна") || error.startsWith("Вы постите слишком быстро")) {
            this.refreshCaptcha();
        }
        
        if (error.startsWith("503")) {
            String url = Factory.resolve(DvachUriBuilder.class).createUri("/makaba/posting.fcgi").toString();
            new CloudflareCheckService(url, this, new ICloudflareCheckListener() {
                public void onSuccess() {
                    refreshCaptcha();
                    AppearanceUtils.showToastMessage(AddPostActivity.this, getString(R.string.notification_cloudflare_check_finished));
                }
                public void onStart() {
                    AppearanceUtils.showToastMessage(AddPostActivity.this, getString(R.string.notification_cloudflare_check_started));
                }
                public void onTimeout() {
                    AppearanceUtils.showToastMessage(AddPostActivity.this, getString(R.string.error_cloudflare_check_timeout));
                }
            }).start();
        }
        if (error.equals(this.getString(R.string.notification_cloudflare_recaptcha))) {
            this.setRecaptcha(true);
            this.refreshCaptcha();
        }
        
    }

    @Override
    public void showPostLoading() {
        this.mSendButton.setEnabled(false);
        this.mProgressDialog = new ProgressDialog(this);
        this.mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.mProgressDialog.setMessage(this.getString(R.string.loading));
        this.mProgressDialog.setCancelable(true);
        this.mProgressDialog.setCanceledOnTouchOutside(false);
        this.mProgressDialog.show();
    }

    @Override
    public void hidePostLoading() {
        this.mSendButton.setEnabled(true);
        this.mProgressDialog.dismiss();
        this.mCurrentPostSendTask = null;
    }

    @Override
    public void showCaptchaLoading() {
        this.switchToCaptchaView(CaptchaViewType.LOADING);
    }

    @Override
    public void skipCaptcha(boolean successPasscode, boolean failPasscode) {
        if (successPasscode) {
            this.mCaptchaSkipView.setText(this.getString(R.string.addpost_captcha_can_skip_passcode));
        } else if (failPasscode) {
            this.mCaptchaSkipView.setText(this.getString(R.string.addpost_captcha_fail_passcode));
        } else {
            this.mCaptchaSkipView.setText(this.getString(R.string.addpost_captcha_can_skip));
        }

        this.switchToCaptchaView(CaptchaViewType.SKIP);
        this.mCaptchaPasscodeSuccess = successPasscode;
        this.mCaptchaPasscodeFail = failPasscode;
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

        this.switchToCaptchaView(CaptchaViewType.IMAGE);
    }

    @Override
    public void showCaptchaError(String errorMessage) {
        this.mCaptchaImageView.setImageResource(android.R.color.transparent);
        AppearanceUtils.showToastMessage(this, !StringUtils.isEmpty(errorMessage)
                ? errorMessage
                : this.getResources().getString(R.string.error_read_response));

        this.switchToCaptchaView(CaptchaViewType.ERROR);
    }

    private void switchToCaptchaView(CaptchaViewType vt) {
        this.mCurrentCaptchaView = vt;
        switch (vt) {
            case LOADING:
                this.mCaptchaImageView.setVisibility(View.GONE);
                this.mCaptchaImageView.setImageResource(android.R.color.transparent);
                this.mCaptchaLoadingView.setVisibility(View.VISIBLE);
                this.mCaptchaSkipView.setVisibility(View.GONE);
                break;
            case ERROR:
            case IMAGE:
                this.mCurrentDownloadCaptchaTask = null;
                this.mCaptchaAnswerView.setVisibility(View.VISIBLE);
                this.mCaptchaImageView.setVisibility(View.VISIBLE);
                this.mCaptchaLoadingView.setVisibility(View.GONE);
                this.mCaptchaSkipView.setVisibility(View.GONE);
                break;
            case SKIP:
                this.mCurrentDownloadCaptchaTask = null;
                this.mCaptchaAnswerView.setVisibility(View.GONE);
                this.mCaptchaImageView.setVisibility(View.GONE);
                this.mCaptchaLoadingView.setVisibility(View.GONE);
                this.mCaptchaSkipView.setVisibility(View.VISIBLE);
                break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.addpost, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_attach_file_id:
                if (this.getAttachments().size() >= ThreadPostUtils.getMaximumAttachments(this.mBoardName)) {
                    AppearanceUtils.showToastMessage(this, this.getString(R.string.warning_maximum_attachments));
                    break;
                }
                
                Intent intent = new Intent(this, FilesListActivity.class);
                if (this.hasAttachments()) {
                    intent.putExtra(FilesListActivity.EXTRA_CURRENT_FILE, this.getAttachments().get(0).file.getAbsolutePath());
                }
                this.startActivityForResult(intent, Constants.REQUEST_CODE_FILE_LIST_ACTIVITY);
                break;
            case R.id.menu_gallery_id:
                if (this.getAttachments().size() >= ThreadPostUtils.getMaximumAttachments(this.mBoardName)) {
                    AppearanceUtils.showToastMessage(this, this.getString(R.string.warning_maximum_attachments));
                    break;
                }
                
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("image/*");
                this.startActivityForResult(i, Constants.REQUEST_CODE_GALLERY);
                break;
        }

        return true;
    }

    //@SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constants.REQUEST_CODE_FILE_LIST_ACTIVITY:

                    SerializableFileModel fileModel = data.getParcelableExtra(this.getPackageName() + Constants.EXTRA_SELECTED_FILE);

                    this.setAttachment(fileModel);

                    break;
                case Constants.REQUEST_CODE_GALLERY:
                    Uri imageUri = data.getData();
                    File imageFile = IoUtils.getFile(this, imageUri);

                    // Почему-то было 2 error reports с NullReferenceException
                    // из-за метода File.fixSlashes, добавлю проверку
                    if (imageFile != null) {
                        ImageFileModel image = new ImageFileModel(imageFile);
                        this.setAttachment(image);
                    } else {
                        AppearanceUtils.showToastMessage(this, this.getString(R.string.error_image_cannot_be_attached));
                    }

                    break;
            }
        }
    }

    private void setAttachment(ImageFileModel fileModel) {
        int index = 0;
        for (int i = 0; i < this.mAttachedFiles.length; i++) {
            if (this.mAttachedFiles[i] == null) {
                index = i;
                break;
            }
        }
        
        this.mAttachedFiles[index] = fileModel;
        this.mAttachmentViews[index].show(fileModel, this.getResources());
    }

    private void removeAttachment(int index) {
        this.mAttachedFiles[index] = null;
        this.mAttachmentViews[index].hide();
    }
    
    private boolean hasAttachments() {
        return this.getAttachments().size() > 0;
    }
    
    private List<ImageFileModel> getAttachments() {
        ArrayList<ImageFileModel> attachments = new ArrayList<ImageFileModel>(4);
        for (ImageFileModel file : this.mAttachedFiles) {
            if (file != null) {
                attachments.add(file);
            }
        }
        
        return attachments;
    }
    
    private List<File> getAttachedFiles() {
        ArrayList<File> files = new ArrayList<File>(4);
        for (ImageFileModel file : this.getAttachments()) {
            files.add(file.file);
        }
        
        return files;
    }

    private void refreshCaptcha() {
        if (this.mCurrentDownloadCaptchaTask != null) {
            this.mCurrentDownloadCaptchaTask.cancel(true);
        }

        this.mCaptchaAnswerView.setText("");

        if (!this.isRecaptcha) {
            this.mCurrentDownloadCaptchaTask = new DownloadCaptchaTask(this, this.mRefererUri);
        } else {
            this.mCurrentDownloadCaptchaTask = new DownloadCaptchaTask(this, true);
        }
        this.mCurrentDownloadCaptchaTask.execute();
    }

    private void formatSelectedText(String code) {
        Editable editable = this.mCommentView.getEditableText();
        String text = editable.toString();

        String startTag = "[" + code + "]";
        String endTag = "[/" + code + "]";

        int selectionStart = Math.max(0, this.mCommentView.getSelectionStart());
        int selectionEnd = Math.min(text.length(), this.mCommentView.getSelectionEnd());
        if (selectionStart < 0 || selectionEnd > text.length() || selectionStart > selectionEnd) {
            return;
        }

        String selectedText = text.substring(selectionStart, selectionEnd);

        // Проверяем текст на краях выделенной области, на случай если уже была
        // добавлена разметка
        String textBeforeSelection = text.substring(Math.max(0, selectionStart - startTag.length()), selectionStart);
        String textAfterSelection = text.substring(selectionEnd, Math.min(text.length(), selectionEnd + endTag.length()));

        // Удаляем теги форматирования если есть, добавляем если нет
        if (textBeforeSelection.equalsIgnoreCase(startTag) && textAfterSelection.equalsIgnoreCase(endTag)) {
            editable.replace(selectionStart - startTag.length(), selectionEnd + endTag.length(), selectedText);
            this.mCommentView.setSelection(selectionStart - startTag.length(), selectionEnd - startTag.length());
        } else {
            editable.replace(selectionStart, selectionEnd, startTag + selectedText + endTag);
            this.mCommentView.setSelection(selectionStart + startTag.length(), selectionEnd + startTag.length());
        }
    }

    private void formatQuote() {
        Editable editable = this.mCommentView.getEditableText();
        String text = editable.toString();

        int selectionStart = this.mCommentView.getSelectionStart();
        int selectionEnd = this.mCommentView.getSelectionEnd();
        String selectedText = text.substring(selectionStart, selectionEnd);
        String oneSymbolBefore = text.substring(Math.max(selectionStart - 1, 0), selectionStart);

        if (selectedText.startsWith(">")) {
            String unQuotedText = selectedText.replaceFirst(">", "").replaceAll("(\n+)>", "$1");
            int diff = selectedText.length() - unQuotedText.length();

            editable.replace(selectionStart, selectionEnd, unQuotedText);
            this.mCommentView.setSelection(selectionStart, selectionEnd - diff);
        } else {
            String firstSymbol = oneSymbolBefore.length() == 0 || oneSymbolBefore.equals("\n") ? "" : "\n";
            String quotedText = firstSymbol + ">" + selectedText.replaceAll("(\n+)", "$1>");
            int diff = quotedText.length() - selectedText.length();

            editable.replace(selectionStart, selectionEnd, quotedText);
            this.mCommentView.setSelection(selectionStart + firstSymbol.length(), selectionEnd + diff);
        }
    }
}
