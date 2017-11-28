package ua.in.quireg.chan.ui.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.asynctasks.CheckCloudflareTask;
import ua.in.quireg.chan.asynctasks.CheckPasscodeTask;
import ua.in.quireg.chan.asynctasks.DownloadCaptchaTask;
import ua.in.quireg.chan.asynctasks.SendPostTask;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.Websites;
import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.common.utils.IoUtils;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.common.utils.ThreadPostUtils;
import ua.in.quireg.chan.interfaces.ICaptchaView;
import ua.in.quireg.chan.interfaces.ICheckCaptchaView;
import ua.in.quireg.chan.interfaces.ICheckPasscodeView;
import ua.in.quireg.chan.interfaces.ICloudflareCheckListener;
import ua.in.quireg.chan.interfaces.IPostSendView;
import ua.in.quireg.chan.interfaces.IUrlBuilder;
import ua.in.quireg.chan.interfaces.IUrlParser;
import ua.in.quireg.chan.interfaces.IWebsite;
import ua.in.quireg.chan.models.domain.CaptchaEntity;
import ua.in.quireg.chan.models.domain.CaptchaType;
import ua.in.quireg.chan.models.domain.SendPostModel;
import ua.in.quireg.chan.models.presentation.AddAttachmentViewBag;
import ua.in.quireg.chan.models.presentation.CaptchaInfoType;
import ua.in.quireg.chan.models.presentation.CaptchaViewType;
import ua.in.quireg.chan.models.presentation.DraftPostModel;
import ua.in.quireg.chan.models.presentation.FileModel;
import ua.in.quireg.chan.models.presentation.ImageFileModel;
import ua.in.quireg.chan.models.presentation.SerializableFileModel;
import ua.in.quireg.chan.services.CloudflareCheckService;
import ua.in.quireg.chan.services.IconsList;
import ua.in.quireg.chan.services.presentation.DraftPostsStorage;
import ua.in.quireg.chan.settings.ApplicationSettings;

public class AddPostActivity extends Activity implements IPostSendView, ICaptchaView {

    private final ApplicationSettings mSettings = Factory.resolve(ApplicationSettings.class);
    private final DraftPostsStorage mDraftPostsStorage = Factory.resolve(DraftPostsStorage.class);
    private IUrlParser mUrlParser;
    private IUrlBuilder mUrlBuilder;

    private FileModel[] mAttachedFiles = new FileModel[4];
    private CaptchaEntity mCaptcha;
    private IWebsite mWebsite;
    private String mBoardName;
    private String mThreadNumber;
    private CaptchaViewType mCurrentCaptchaView = null;
    private Bitmap mCaptchaBitmap;
    private File instantPhotoTempFile;

    private SendPostTask mCurrentPostSendTask = null;
    private DownloadCaptchaTask mCurrentDownloadCaptchaTask = null;
    private CheckPasscodeTask mCurrentCheckPasscodeTask = null;

    private TextView mCaptchaInfoView = null;
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
    private boolean isAppCaptchedNewPost = false;


    private SendPostModel mCachedSendPostModel;

    // Определяет, нужно ли сохранять пост (если не отправлен) или можно удалить
    // (после успешной отправки)
    private boolean mFinishedSuccessfully = false;

    private boolean mRunCloudflareCheck = false;
    private CheckCloudflareTask mCheckCloudflareTask;
    private CaptchaInfoType mCaptchaInfoType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Парсим название борды и номер треда
        Bundle extras = getIntent().getExtras();
        mWebsite = Websites.fromName(extras.getString(Constants.EXTRA_WEBSITE));
        mBoardName = extras.getString(Constants.EXTRA_BOARD_NAME);
        mThreadNumber = extras.getString(Constants.EXTRA_THREAD_NUMBER);

        mUrlBuilder = mWebsite.getUrlBuilder();
        mUrlParser = mWebsite.getUrlParser();

        resetUI();

        StringBuilder commentBuilder = new StringBuilder();

        // Восстанавливаем состояние, если было сохранено
        DraftPostModel draft = mDraftPostsStorage.getDraft(mBoardName, mThreadNumber);
        if (draft != null) {
            if (!StringUtils.isEmpty(draft.getComment())) {
                commentBuilder.append(draft.getComment()).append("\n");
            }

            for (FileModel file : draft.getAttachedFiles()) {
                setAttachment(file);
            }

            mSageCheckBox.setChecked(draft.isSage());

            if (draft.getCaptchaType() == CaptchaViewType.INFO) {
                showCaptchaInfo(draft.getCaptchaInfoType());
            } else if (draft.getCaptchaType() == CaptchaViewType.IMAGE && draft.getCaptchaImage() != null && !draft.getCaptchaImage().isRecycled()) {
                showCaptcha(draft.getCaptcha(), draft.getCaptchaImage());
            }
        }

        String postNumber = extras.getString(Constants.EXTRA_POST_NUMBER);
        String postComment = extras.getString(Constants.EXTRA_POST_COMMENT);
        if (postNumber != null) {
            commentBuilder.append(">>").append(postNumber).append("\n");
        }

        if (!StringUtils.isEmpty(postComment)) {
            postComment = ThreadPostUtils.removeLinksFromComment(postComment);

            postComment = postComment.replaceAll("(\n+)", "$1>");
            commentBuilder.append(">").append(postComment).append("\n");
        }

        // Сохраняем коммент
        mCommentView.setText(commentBuilder.toString());
        // Ставим курсор в конце текстового поля
        mCommentView.setSelection(commentBuilder.length());

        // Загружаем и показываем капчу
        Timber.d("Captcha view: %s", mCurrentCaptchaView);
        if (mCurrentCaptchaView == null) {
            refreshCaptcha();
        }

        if (StringUtils.isEmpty(mThreadNumber)) {
            setTitle(String.format(getString(R.string.data_add_thread_title), mBoardName));
            mSubjectView.setVisibility(View.VISIBLE);
        } else {
            setTitle(String.format(getString(R.string.data_add_post_title), mBoardName, mThreadNumber));
        }
    }

    @Override
    protected void onDestroy() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Timber.v("save state");
        if (!mFinishedSuccessfully) {
            DraftPostModel draft = new DraftPostModel(mCommentView.getText().toString(), getAttachments(), mSageCheckBox.isChecked(), mCurrentCaptchaView, mCaptcha, mCaptchaBitmap, mCaptchaInfoType);

            mDraftPostsStorage.saveDraft(mBoardName, mThreadNumber, draft);
        } else {
            mDraftPostsStorage.clearDraft(mBoardName, mThreadNumber);
        }

        super.onPause();
    }

    private void resetUI() {
        setTheme(mSettings.getTheme());
        setContentView(R.layout.add_post_view);

        mCaptchaImageView = (ImageView) findViewById(R.id.addpost_captcha_image);
        mCaptchaLoadingView = findViewById(R.id.addpost_captcha_loading);
        mCaptchaInfoView = (TextView) findViewById(R.id.addpost_captcha_info_message);
        mCaptchaAnswerView = (EditText) findViewById(R.id.addpost_captcha_input);
        mCommentView = (EditText) findViewById(R.id.addpost_comment_input);
        mSageCheckBox = (CheckBox) findViewById(R.id.addpost_sage_checkbox);
        mAttachmentViews[0] = AddAttachmentViewBag.fromView(findViewById(R.id.addpost_attachment_view_1));
        mAttachmentViews[1] = AddAttachmentViewBag.fromView(findViewById(R.id.addpost_attachment_view_2));
        mAttachmentViews[2] = AddAttachmentViewBag.fromView(findViewById(R.id.addpost_attachment_view_3));
        mAttachmentViews[3] = AddAttachmentViewBag.fromView(findViewById(R.id.addpost_attachment_view_4));
        mSendButton = (Button) findViewById(R.id.addpost_send_button);
        mSubjectView = (EditText) findViewById(R.id.addpost_subject);
        mPoliticsView = (Spinner) findViewById(R.id.addpost_politics);

        final ImageButton refreshCaptchaButton = (ImageButton) findViewById(R.id.addpost_refresh_button);
        final LinearLayout textFormatView = (LinearLayout) findViewById(R.id.addpost_textformat_view);

        try {
            if (Factory.resolve(IconsList.class).getData(mBoardName) != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Factory.resolve(IconsList.class).getData(mBoardName));
                mPoliticsView.setAdapter(adapter);
                mPoliticsView.setVisibility(View.VISIBLE);
                isPoliticsBoard = true;
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        View.OnClickListener formatButtonListener = v -> {
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
        };

        for (int i = 0; i < textFormatView.getChildCount(); i++) {
            ImageButton b = (ImageButton) textFormatView.getChildAt(i);
            if (b != null) {
                b.setOnClickListener(formatButtonListener);
            }
        }

        // Обрабатываем нажатие на кнопку "Отправить"
        mSendButton.setOnClickListener(v -> AddPostActivity.this.onSend());
        // и заодно при нажатии на Send на софтовой клавиатуре при вводе капчи
        mCaptchaAnswerView.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (EditorInfo.IME_ACTION_SEND == actionId) {
                AddPostActivity.this.onSend();
                return true;
            }
            return false;
        });

        // Удаляем прикрепленный файл
        for (int i = 0; i < mAttachmentViews.length; i++) {
            final int index = i;
            mAttachmentViews[i].removeButton.setOnClickListener(v -> AddPostActivity.this.removeAttachment(index));
        }
        // Обновляем нажатие кнопки Refresh для капчи
        refreshCaptchaButton.setOnClickListener(v -> AddPostActivity.this.refreshCaptcha());
    }

    private void onSend() {
        // Собираем все заполненные поля
        String captchaAnswer = mCaptchaAnswerView.getText().toString();

        if (mRunCloudflareCheck) {
            // Check the cloudflare captcha and then show a posting captcha
            showPostLoading();
            if (mCheckCloudflareTask != null) {
                mCheckCloudflareTask.cancel(true);
            }
            mCheckCloudflareTask = new CheckCloudflareTask(mWebsite, mCaptcha, captchaAnswer, new ICheckCaptchaView() {
                @Override
                public void beforeCheck() {
                }

                @Override
                public void showSuccess() {
                    AddPostActivity.this.hidePostLoading();
                    AddPostActivity.this.setCfRecaptcha(false);
                    AddPostActivity.this.refreshCaptcha();
                }

                @Override
                public void showError(String message) {
                    AddPostActivity.this.hidePostLoading();
                    AddPostActivity.this.showError(message != null ? message : AddPostActivity.this.getString(R.string.error_cloudflare_recaptcha), false);
                    AddPostActivity.this.refreshCaptcha();
                }
            });
            mCheckCloudflareTask.execute();
            return;
        }

        String comment = mCommentView.getText().toString();

        if (StringUtils.isEmpty(comment) && !hasAttachments()) {
            AppearanceUtils.showLongToast(this, getString(R.string.warning_write_comment));
            return;
        }

        boolean isSage = mSageCheckBox.isChecked();

        if (StringUtils.isEmpty(mThreadNumber)
                && !hasAttachments()
                && !mBoardName.equals("d")) {
            AppearanceUtils.showLongToast(this, getString(R.string.warning_attach_file_new_thread));
            return;
        }

        String subject = StringUtils.nullIfEmpty(mSubjectView.getText().toString());

        String politics = null;
        if (isPoliticsBoard) {
            int itemIndex = mPoliticsView.getSelectedItemPosition();
            politics = Math.max(itemIndex, 0) + "";
        }

        String name = mSettings.getName();
        SendPostModel pe = new SendPostModel(mCaptcha, captchaAnswer, comment, isSage, getAttachedFiles(), subject, politics, name);
        pe.setParentThread(mThreadNumber);
        mCachedSendPostModel = pe;

        // Отправляем
        sendPost(pe);
    }

    private void setCfRecaptcha(boolean isCfRecaptcha) {
        mRunCloudflareCheck = isCfRecaptcha;
    }

    private void sendPost(SendPostModel pe) {
        if (mCurrentPostSendTask != null) {
            mCurrentPostSendTask.cancel(true);
        }
        if (mCaptcha != null
                && mCaptcha.getCaptchaType() == CaptchaType.RECAPTCHA_V2
                && Constants.SDK_VERSION >= Build.VERSION_CODES.HONEYCOMB) {
            Intent reIntent = new Intent(this, NewRecaptchaActivity.class);
            startActivityForResult(reIntent, Constants.REQUEST_CODE_RECAPTCHA);
        } else {
            mCurrentPostSendTask = new SendPostTask(this, this, mWebsite, mBoardName, mThreadNumber, pe);
            mCurrentPostSendTask.execute();
        }
    }

    @Override
    public void showSuccess(String redirectedPage) {
        AppearanceUtils.showLongToast(this, getString(R.string.notification_send_post_success));
        // return back to the list of posts
        String redirectedThreadNumber = null;
        if (redirectedPage != null) {
            redirectedThreadNumber = mUrlParser.getThreadNumber(Uri.parse(redirectedPage));
        }

        mFinishedSuccessfully = true;

        // Завершаем с успешным результатом
        Intent intent = new Intent();
        intent.putExtra(Constants.EXTRA_REDIRECTED_THREAD_NUMBER, redirectedThreadNumber);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void showError(String error, boolean isRecaptcha) {
        error = error != null ? error : getString(R.string.error_send_post);
        AppearanceUtils.showLongToast(this, error);

        if (isRecaptcha) {
            setCfRecaptcha(true);
            refreshCaptcha();
        }

        if (error.startsWith("Ошибка: Неверный код подтверждения.") || error.startsWith("Капча невалидна") || error.startsWith("Вы постите слишком быстро")) {
            refreshCaptcha();
        }

        if (error.startsWith("503")) {
            String url = mUrlBuilder.getPostingUrlHtml();
            new CloudflareCheckService(url, this, new ICloudflareCheckListener() {
                public void onSuccess() {
                    refreshCaptcha();
                    AppearanceUtils.showLongToast(AddPostActivity.this, getString(R.string.notification_cloudflare_check_finished));
                }

                public void onStart() {
                    AppearanceUtils.showLongToast(AddPostActivity.this, getString(R.string.notification_cloudflare_check_started));
                }

                public void onTimeout() {
                    AppearanceUtils.showLongToast(AddPostActivity.this, getString(R.string.error_cloudflare_check_timeout));
                }
            }).start();
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
    public void skipCaptcha(boolean successPasscode, boolean failPasscode) {
        if (successPasscode) {
            showCaptchaInfo(CaptchaInfoType.PASSCODE_SUCCESS);
        } else if (failPasscode) {
            showCaptchaInfo(CaptchaInfoType.PASSCODE_FAIL);
        } else {
            showCaptchaInfo(CaptchaInfoType.SKIP);
        }
    }

    @Override
    public void showCaptcha(CaptchaEntity captcha, Bitmap captchaImage) {
        mCaptchaImageView.setImageResource(android.R.color.transparent);
        if (mCaptchaBitmap != null) {
            mCaptchaBitmap.recycle();
        }

        mCaptcha = captcha;
        mCaptchaBitmap = captchaImage;
        mCaptchaImageView.setImageBitmap(captchaImage);

        if (captcha.getCaptchaType() == CaptchaType.RECAPTCHA_V2) {
            showCaptchaInfo(CaptchaInfoType.RECAPTCHA_V2);
        } else {
            switchToCaptchaView(CaptchaViewType.IMAGE);
        }

        if (captcha.getCaptchaType() == CaptchaType.DVACH) {
            mCaptchaAnswerView.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
    }

    public void showCaptchaInfo(CaptchaInfoType captchaInfoType) {
        mCaptchaInfoType = captchaInfoType;

        String message;

        if (mCaptchaInfoType == CaptchaInfoType.PASSCODE_SUCCESS) {
            message = getString(R.string.addpost_captcha_can_skip_passcode);

        } else if (mCaptchaInfoType == CaptchaInfoType.PASSCODE_FAIL) {
            message = getString(R.string.addpost_captcha_fail_passcode);

        } else if (mCaptchaInfoType == CaptchaInfoType.SKIP ||
                mCaptchaInfoType == CaptchaInfoType.API) {
            message = getString(R.string.addpost_captcha_can_skip);

        } else if (mCaptchaInfoType == CaptchaInfoType.RECAPTCHA_V2) {
            message = getString(R.string.addpost_captcha_solve_recaptcha);

        } else {
            message = getString(R.string.error_unknown);

        }

        mCaptchaInfoView.setText(message);

        switchToCaptchaView(CaptchaViewType.INFO);
    }

    @Override
    public void appCaptcha(CaptchaEntity captcha) {
        mCaptcha = captcha;
        isAppCaptchedNewPost = true;
        showCaptchaInfo(CaptchaInfoType.API);
    }

    @Override
    public void showCaptchaError(String errorMessage) {
        mCaptchaImageView.setImageResource(android.R.color.transparent);
        AppearanceUtils.showLongToast(this, !StringUtils.isEmpty(errorMessage)
                ? errorMessage
                : getResources().getString(R.string.error_read_response));

        switchToCaptchaView(CaptchaViewType.ERROR);
    }

    private void switchToCaptchaView(CaptchaViewType vt) {
        mCurrentCaptchaView = vt;
        if (vt == null) {
            return;
        }

        switch (vt) {
            case LOADING:
                mCaptchaImageView.setVisibility(View.GONE);
                mCaptchaImageView.setImageResource(android.R.color.transparent);
                mCaptchaLoadingView.setVisibility(View.VISIBLE);
                mCaptchaInfoView.setVisibility(View.GONE);
                break;
            case ERROR:
            case IMAGE:
                mCurrentDownloadCaptchaTask = null;
                mCaptchaAnswerView.setVisibility(View.VISIBLE);
                mCaptchaImageView.setVisibility(View.VISIBLE);
                mCaptchaLoadingView.setVisibility(View.GONE);
                mCaptchaInfoView.setVisibility(View.GONE);
                break;
            case INFO:
                mCurrentDownloadCaptchaTask = null;
                mCaptchaAnswerView.setVisibility(View.GONE);
                mCaptchaImageView.setVisibility(View.GONE);
                mCaptchaLoadingView.setVisibility(View.GONE);
                mCaptchaInfoView.setVisibility(View.VISIBLE);
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
                if (getAttachments().size() >= ThreadPostUtils.getMaximumAttachments(mBoardName)) {
                    AppearanceUtils.showLongToast(this, getString(R.string.warning_maximum_attachments));
                    break;
                }

                Intent intent = new Intent(this, FilesListActivity.class);
                if (hasAttachments()) {
                    intent.putExtra(FilesListActivity.EXTRA_CURRENT_FILE, getAttachments().get(0).file.getAbsolutePath());
                }
                startActivityForResult(intent, Constants.REQUEST_CODE_FILE_LIST_ACTIVITY);
                break;
            case R.id.menu_gallery_id:
                if (getAttachments().size() >= ThreadPostUtils.getMaximumAttachments(mBoardName)) {
                    AppearanceUtils.showLongToast(this, getString(R.string.warning_maximum_attachments));
                    break;
                }

                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("image/*");
                startActivityForResult(i, Constants.REQUEST_CODE_GALLERY);
                break;
            case R.id.menu_attach_instant_photo:
                if (getAttachments().size() >= ThreadPostUtils.getMaximumAttachments(mBoardName)) {
                    AppearanceUtils.showLongToast(this, getString(R.string.warning_maximum_attachments));
                    break;
                }

                Intent instantPhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (instantPhoto.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    try {
                        instantPhotoTempFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Timber.e(ex);
                    }
                    // Continue only if the File was successfully created
                    if (instantPhotoTempFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(this,
                                "ua.in.quireg.chan.fileprovider",
                                instantPhotoTempFile);
                        instantPhoto.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(instantPhoto, Constants.REQUEST_CODE_INSTANT_PHOTO);
                    }
                }
                break;
            case R.id.menu_attach_video_id:
                if (getAttachments().size() >= ThreadPostUtils.getMaximumAttachments(mBoardName)) {
                    AppearanceUtils.showLongToast(this, getString(R.string.warning_maximum_attachments));
                    break;
                }

                Intent videoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                videoIntent.setType("video/*");
                startActivityForResult(videoIntent, Constants.REQUEST_CODE_VIDEO_FILE);
                break;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_RECAPTCHA) {
            if (resultCode == NewRecaptchaActivity.OK) {
                String hash = data.getStringExtra("hash");
                if (hash != null && hash.length() != 0) {
                    if (mCurrentPostSendTask != null) {
                        mCurrentPostSendTask.cancel(true);
                    }
                    mCachedSendPostModel.setRecaptchaHash(hash);
                    mCurrentPostSendTask = new SendPostTask(this, this, mWebsite, mBoardName, mThreadNumber, mCachedSendPostModel);
                    mCachedSendPostModel = null;
                    mCurrentPostSendTask.execute();
                }
            }
            return;
        }
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constants.REQUEST_CODE_FILE_LIST_ACTIVITY:

                    SerializableFileModel fileModel = data.getParcelableExtra(getPackageName() + Constants.EXTRA_SELECTED_FILE);

                    setAttachment(fileModel);

                    break;
                case Constants.REQUEST_CODE_GALLERY:
                    Uri imageUri = data.getData();
                    File imageFile = IoUtils.getFile(this, imageUri);
                    if (imageFile != null) {
                        ImageFileModel image = new ImageFileModel(imageFile);
                        setAttachment(image);
                    } else {
                        AppearanceUtils.showLongToast(this, getString(R.string.error_image_cannot_be_attached));
                    }

                    break;
                case Constants.REQUEST_CODE_INSTANT_PHOTO:
                    if (instantPhotoTempFile != null) {
                        //TODO Remove exif from image file.
                        ImageFileModel image = new ImageFileModel(instantPhotoTempFile);
                        setAttachment(image);
                    } else {
                        AppearanceUtils.showLongToast(this, getString(R.string.error_image_cannot_be_attached));
                    }

                    break;
                case Constants.REQUEST_CODE_VIDEO_FILE:
                    Uri videoUri = data.getData();
                    File videoFile = IoUtils.getFile(this, videoUri);
                    if (videoFile != null) {
                        FileModel attachmentModel = new FileModel(videoFile);
                        setAttachment(attachmentModel);
                    } else {
                        AppearanceUtils.showLongToast(this, getString(R.string.error_image_cannot_be_attached));
                    }
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";


        File storageDir = getApplication().getApplicationContext().getExternalFilesDir(
                Environment.DIRECTORY_PICTURES);
        instantPhotoTempFile = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );
        return instantPhotoTempFile;
    }

    private void setAttachment(FileModel fileModel) {
        int index = -1;
        for (int i = 0; i < mAttachedFiles.length; i++) {
            if (mAttachedFiles[i] == null) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            AppearanceUtils.showLongToast(this, getString(R.string.error_image_cannot_be_attached));
            return;
        }

        mAttachedFiles[index] = fileModel;
        mAttachmentViews[index].show(fileModel, getResources());
    }

    private void removeAttachment(int index) {
        mAttachedFiles[index] = null;
        mAttachmentViews[index].hide();
    }

    private boolean hasAttachments() {
        return getAttachments().size() > 0;
    }

    private List<FileModel> getAttachments() {
        ArrayList<FileModel> attachments = new ArrayList<FileModel>(4);
        for (FileModel file : mAttachedFiles) {
            if (file != null) {
                attachments.add(file);
            }
        }

        return attachments;
    }

    private List<File> getAttachedFiles() {
        ArrayList<File> files = new ArrayList<File>(4);
        for (FileModel file : getAttachments()) {
            files.add(file.file);
        }

        return files;
    }

    private void refreshCaptcha() {
        Timber.d("refreshCaptcha()");
        if (mCurrentCaptchaView == CaptchaViewType.INFO) {
            refreshCaptchaSkipView();
        } else {
            refreshCaptchaImageView();
        }
    }

    private void refreshCaptchaSkipView() {
        Timber.d("refreshCaptchaSkipView()");
        if (mCurrentCheckPasscodeTask != null) {
            mCurrentCheckPasscodeTask.cancel(true);
        }

        mCurrentCheckPasscodeTask = new CheckPasscodeTask(mWebsite, new CheckPasscodeView());
        mCurrentCheckPasscodeTask.execute();
    }

    private void refreshCaptchaImageView() {
        Timber.d("refreshCaptchaImageView()");
        if (mCurrentDownloadCaptchaTask != null) {
            mCurrentDownloadCaptchaTask.cancel(true);
        }

        mCaptchaAnswerView.setText("");

        mCurrentDownloadCaptchaTask = new DownloadCaptchaTask(this, mWebsite, mBoardName, mThreadNumber, mSettings.getCaptchaType());
        mCurrentDownloadCaptchaTask.execute();
    }

    private void formatSelectedText(String code) {
        Editable editable = mCommentView.getEditableText();
        String text = editable.toString();

        String startTag = "[" + code + "]";
        String endTag = "[/" + code + "]";

        int selectionStart = Math.max(0, mCommentView.getSelectionStart());
        int selectionEnd = Math.min(text.length(), mCommentView.getSelectionEnd());
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
            mCommentView.setSelection(selectionStart - startTag.length(), selectionEnd - startTag.length());
        } else {
            editable.replace(selectionStart, selectionEnd, startTag + selectedText + endTag);
            mCommentView.setSelection(selectionStart + startTag.length(), selectionEnd + startTag.length());
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
            String firstSymbol = oneSymbolBefore.length() == 0 || oneSymbolBefore.equals("\n") ? "" : "\n";
            String quotedText = firstSymbol + ">" + selectedText.replaceAll("(\n+)", "$1>");
            int diff = quotedText.length() - selectedText.length();

            editable.replace(selectionStart, selectionEnd, quotedText);
            mCommentView.setSelection(selectionStart + firstSymbol.length(), selectionEnd + diff);
        }
    }

    private class CheckPasscodeView implements ICheckPasscodeView {
        @Override
        public void onPasscodeRemoved() {
            Timber.d("onPasscodeRemoved()");
            AddPostActivity activity = AddPostActivity.this;
            activity.switchToCaptchaView(null);
            activity.refreshCaptcha();
        }

        @Override
        public void onPasscodeChecked(boolean isSuccess, String errorMessage) {
            Timber.d("onPasscodeChecked()");
            AddPostActivity activity = AddPostActivity.this;
            if (isSuccess) {
                activity.showCaptchaInfo(CaptchaInfoType.PASSCODE_SUCCESS);
                AppearanceUtils.showLongToast(activity, activity.getString(R.string.notification_passcode_correct));
            } else {
                String error = !StringUtils.isEmpty(errorMessage) ? errorMessage : activity.getString(R.string.notification_passcode_incorrect);
                AppearanceUtils.showLongToast(activity, error);
                activity.switchToCaptchaView(null);
                activity.refreshCaptcha();
            }
        }
    }
}
