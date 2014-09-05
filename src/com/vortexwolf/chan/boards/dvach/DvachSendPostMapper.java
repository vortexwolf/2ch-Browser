package com.vortexwolf.chan.boards.dvach;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.models.domain.SendPostModel;

public class DvachSendPostMapper {
    private static final Charset utf = Constants.UTF8_CHARSET;
    private static final String COMMENT = "shampoo";
    private static final String EMAIL = "nabiki";
    private static final String NAME = "akane";
    private static final String SUBJECT = "kasumi";
    private static final String PARENT = "parent";
    private static final String CAPTCHA_KEY = "captcha";
    private static final String CAPTCHA_ANSWER = "captcha_value_id_06";
    private static final String FILE = "file";
    
    public HttpEntity mapModelToHttpEntity(SendPostModel model, HashMap<String, String> customValues) {       
        MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, Constants.MULTIPART_BOUNDARY, Constants.UTF8_CHARSET);
        
        if (customValues != null) {
            for (Entry<String, String> entry : customValues.entrySet()) {
                this.addStringValue(multipartEntity, entry.getKey(), entry.getValue());
            }
        }
        
        this.addStringValue(multipartEntity, PARENT, model.getParentThread());
        this.addStringValue(multipartEntity, COMMENT, StringUtils.emptyIfNull(model.getComment()));
        this.addStringValue(multipartEntity, CAPTCHA_KEY, model.getCaptchaKey());
        this.addStringValue(multipartEntity, CAPTCHA_ANSWER, model.getCaptchaAnswer());
        this.addStringValue(multipartEntity, SUBJECT, model.getSubject());
        this.addStringValue(multipartEntity, NAME, model.getName());
        this.addStringValue(multipartEntity, EMAIL, model.isSage() ? Constants.SAGE_EMAIL : null);
        
        List<File> files = model.getAttachedFiles();
        if (files.size() > 0) {
            multipartEntity.addPart(FILE, new FileBody(files.get(0)));
        }
        
        // Only for /po and /test
        if (model.getPolitics() != null) {
            this.addStringValue(multipartEntity, "anon_icon", model.getPolitics());
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
