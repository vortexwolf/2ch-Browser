package com.vortexwolf.chan.boards.makaba;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.models.domain.CaptchaType;
import com.vortexwolf.chan.models.domain.SendPostModel;
import com.vortexwolf.chan.services.RecaptchaService;
import com.wildflyforcer.utils.Digest;

public class MakabaSendPostMapper {
    private static final String TAG = "MakabaSendPostMapper";

    private static final Charset utf = Constants.UTF8_CHARSET;
    private static final String TASK = "task";
    private static final String BOARD = "board";
    private static final String THREAD = "thread";
    private static final String USERCODE = "usercode";
    private static final String COMMENT = "comment";
    private static final String EMAIL = "email";
    private static final String NAME = "name";
    private static final String SUBJECT = "subject";
    private static final String[] IMAGES = new String[] { "image1", "image2", "image3", "image4" };

    public HttpEntity mapModelToHttpEntity(String boardName, String userCode, SendPostModel model) {
        MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, Constants.MULTIPART_BOUNDARY, Constants.UTF8_CHARSET);

        this.addStringValue(multipartEntity, TASK, "post");
        this.addStringValue(multipartEntity, BOARD, boardName);
        this.addStringValue(multipartEntity, THREAD, model.getParentThread());
        this.addStringValue(multipartEntity, USERCODE, userCode);
        this.addStringValue(multipartEntity, COMMENT, StringUtils.emptyIfNull(model.getComment()));

        if (model.getCaptchaType() == CaptchaType.MAILRU) {
            this.addStringValue(multipartEntity, "captcha_type", "mailru");
            this.addStringValue(multipartEntity, "captcha_id", model.getCaptchaKey());
            this.addStringValue(multipartEntity, "captcha_value", model.getCaptchaAnswer());
        } else if (model.getCaptchaType() == CaptchaType.RECAPTCHA_V1) {
            this.addStringValue(multipartEntity, "captcha_type", "recaptchav1");
            this.addStringValue(multipartEntity, "recaptcha_challenge_field", model.getCaptchaKey());
            this.addStringValue(multipartEntity, "recaptcha_response_field", model.getCaptchaAnswer());
        } else if (model.getCaptchaType() == CaptchaType.RECAPTCHA_V2) {
            try {
                this.addStringValue(multipartEntity, "captcha_type", "recaptcha");
                if (model.getRecaptchaHash() != null) {
                    this.addStringValue(multipartEntity, "g-recaptcha-response", model.getRecaptchaHash());
                } else {
                    this.addStringValue(multipartEntity, "g-recaptcha-response", RecaptchaService.getHash(model.getCaptchaKey(), model.getCaptchaAnswer()));
                }
            } catch (Exception e) {
                MyLog.d(TAG, "can't check get recaptcha hash");
                MyLog.e(TAG, e);
            }
        } else if (model.getCaptchaType() == CaptchaType.DVACH) {
            this.addStringValue(multipartEntity, "captcha_type", "2chaptcha");
            this.addStringValue(multipartEntity, "2chaptcha_id", model.getCaptchaKey());
            this.addStringValue(multipartEntity, "2chaptcha_value", model.getCaptchaAnswer());
        } else if (model.getCaptchaType() == CaptchaType.APP) {
            this.addStringValue(multipartEntity, "captcha_type", "app");
            this.addStringValue(multipartEntity, "app_response_id", model.getCaptchaKey());
            this.addStringValue(multipartEntity, "app_response", Digest.sha256(model.getCaptchaKey() + "|" + com.wildflyforcer.utils.Constants.PRIVATE_API_KEY));
        }

        this.addStringValue(multipartEntity, SUBJECT, model.getSubject());
        this.addStringValue(multipartEntity, NAME, model.getName());
        this.addStringValue(multipartEntity, EMAIL, model.isSage() ? Constants.SAGE_EMAIL : null);

        List<File> files = model.getAttachedFiles();
        for (int i = 0; i < files.size(); i++) {
            multipartEntity.addPart(IMAGES[i], new FileBody(files.get(i)));
        }

        // Only for /po and /test
        if (model.getPolitics() != null) {
            this.addStringValue(multipartEntity, "icon", model.getPolitics());
        }

        return multipartEntity;
    }

    private void addStringValue(MultipartEntity entity, String key, String value) {
        try {
            if (value != null) {
                entity.addPart(key, new StringBody(value, utf));
            }
        } catch (Exception ignored) {
            // ignore
        }
    }
}
