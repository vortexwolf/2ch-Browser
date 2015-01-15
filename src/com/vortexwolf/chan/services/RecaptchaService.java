package com.vortexwolf.chan.services;

import java.io.InputStream;
import java.util.regex.Pattern;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.library.ExtendedHttpClient;
import com.vortexwolf.chan.common.utils.IoUtils;
import com.vortexwolf.chan.common.utils.RegexUtils;
import com.vortexwolf.chan.exceptions.HttpRequestException;
import com.vortexwolf.chan.models.domain.CaptchaEntity;
import com.vortexwolf.chan.services.http.HttpStreamModel;
import com.vortexwolf.chan.services.http.HttpStreamReader;
import com.vortexwolf.chan.services.http.HttpStringReader;

public class RecaptchaService {
    private static final String CLOUDFLARE_CHECK_KEY = "6LeT6gcAAAAAAAZ_yDmTMqPH57dJQZdQcu6VFqog";
    private static final String SEND_POST_KEY = "6LcM2P4SAAAAAD97nF449oigatS5hPCIgt8AQanz";
    private static final String RECAPTCHA_CHALLENGE_URI = "http://www.google.com/recaptcha/api/challenge?k=";
    private static final String FALLBACK_URI = "http://www.google.com/recaptcha/api/fallback?k=";
    private static final String IMAGE_URI = "http://www.google.com/recaptcha/api2/payload?c=";

    private static final Pattern jsChallengePattern = Pattern.compile("challenge.?:.?'([\\w-]+)'");


    public static boolean isCloudflareCaptchaPage(String html) {
        return html.contains(CLOUDFLARE_CHECK_KEY);
    }

    public static CaptchaEntity loadCloudflareCaptcha() {
        return loadRecaptcha(CLOUDFLARE_CHECK_KEY, CaptchaEntity.Type.RECAPTCHA_CF);
    }

    public static CaptchaEntity loadPostingRecaptcha() {
        return loadRecaptcha(SEND_POST_KEY, CaptchaEntity.Type.RECAPTCHA_POST);
    }

    public static CaptchaEntity loadRecaptcha(String key, CaptchaEntity.Type type) {
        try {
            String response = Factory.resolve(HttpStringReader.class).fromUri(RECAPTCHA_CHALLENGE_URI + key);
            CaptchaEntity captcha = getCaptchaFromJavascript(response, type);
            return captcha;
        } catch (HttpRequestException e) {
            return null;
        }
    }

    private static CaptchaEntity getCaptchaFromJavascript(String js, CaptchaEntity.Type type) {
        String challenge = RegexUtils.getGroupValue(js, jsChallengePattern, 1);
        if (challenge == null) {
            return null;
        }

        CaptchaEntity captcha = new CaptchaEntity();
        captcha.setKey(challenge);
        captcha.setUrl(IMAGE_URI + challenge);
        captcha.setType(type);
        return captcha;
    }

    public static String getHash(String challenge, String answer) throws Exception {
        MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, Constants.MULTIPART_BOUNDARY, Constants.UTF8_CHARSET);
        entity.addPart("c", new StringBody(challenge, Constants.UTF8_CHARSET));
        entity.addPart("response", new StringBody(answer, Constants.UTF8_CHARSET));

        HttpStreamModel httpStreamModel = null;
        try {
            httpStreamModel = Factory.resolve(HttpStreamReader.class).fromUri(FALLBACK_URI + SEND_POST_KEY, null, entity, null, null);
            InputStream stream = httpStreamModel.stream;
            String response = IoUtils.convertStreamToString(stream);
            Document document = Jsoup.parseBodyFragment(response);
            Elements verificationToken = document.select("div.fbc-verification-token textarea");
            String hash = verificationToken.text();
            return hash;
        } finally {
            ExtendedHttpClient.releaseRequestResponse(httpStreamModel.request, httpStreamModel.response);
        }
    }
}
