package com.vortexwolf.chan.services.domain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vortexwolf.chan.common.library.MyHtml;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.exceptions.SendPostException;

public class PostResponseParser {
    private static final String TAG = "PostResponseParser";
    private final Pattern mCenterPattern = Pattern.compile("<center>(.+?)</center>");
    private final Pattern mErrorPattern = Pattern.compile("(.+?)<a[^>]*>Назад</a>.*?");

    public boolean isPostSuccessful(String response) throws SendPostException {
        if (response == null) {
            return true;
        }

        Matcher centerMatcher = this.mCenterPattern.matcher(response);
        while (centerMatcher.find() && centerMatcher.groupCount() > 0) {

            Matcher m = this.mErrorPattern.matcher(centerMatcher.group(1));
            if (m.find() && m.groupCount() > 0) {
                String htmlText = m.group(1);
                String text = MyHtml.fromHtml(htmlText).toString().replaceAll("\n", "");

                MyLog.v(TAG, text);
                throw new SendPostException(text);
            }
        }

        // Пусть будет true, т.к. если не нашел ошибку, то скорей всего
        // результат верный
        return true;
    }
}
