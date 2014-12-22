package com.vortexwolf.chan.services;

import java.io.InputStream;
import java.util.regex.Matcher;
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
import com.vortexwolf.chan.services.http.HttpBitmapReader;
import com.vortexwolf.chan.services.http.HttpStreamModel;
import com.vortexwolf.chan.services.http.HttpStreamReader;
import com.vortexwolf.chan.services.http.HttpStringReader;

import android.graphics.Bitmap;

public class Recaptcha2 {
    private final static String recaptchaChallengeUrl = "http://www.google.com/recaptcha/api/challenge?k=6LcM2P4SAAAAAD97nF449oigatS5hPCIgt8AQanz";
    private final static String recaptchaFallbackUrl = "http://www.google.com/recaptcha/api/fallback?k=6LcM2P4SAAAAAD97nF449oigatS5hPCIgt8AQanz";
    private final static String recaptchaImageUrl = "http://www.google.com/recaptcha/api2/payload?c=";
    
    public final Bitmap bitmap;
    private final String challenge;
    
    public Recaptcha2() throws Exception {
        String response = Factory.resolve(HttpStringReader.class).fromUri(recaptchaChallengeUrl);
        Matcher matcher = Pattern.compile("challenge.?:.?'([\\w-]+)'").matcher(response);
        if (matcher.find() && matcher.groupCount() == 1) {
            challenge = matcher.group(1);
            bitmap = Factory.resolve(HttpBitmapReader.class).fromUri(recaptchaImageUrl+challenge);
        } else throw new Exception("can't get recaptcha");
    }
    
    public String getHash(String answer) throws Exception {
        MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, Constants.MULTIPART_BOUNDARY, Constants.UTF8_CHARSET);
        entity.addPart("c", new StringBody(challenge, Constants.UTF8_CHARSET));
        entity.addPart("response", new StringBody(answer, Constants.UTF8_CHARSET));
        HttpStreamModel httpStreamModel = null;
        try {
            httpStreamModel = Factory.resolve(HttpStreamReader.class).fromUri(recaptchaFallbackUrl, null, entity, null, null);
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
