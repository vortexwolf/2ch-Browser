package ua.in.quireg.chan.services;

import java.util.regex.Pattern;

import ua.in.quireg.chan.common.utils.RegexUtils;
import ua.in.quireg.chan.models.domain.SendPostResult;

public class PostResponseParser {
    private static final String TAG = "PostResponseParser";
    private static final Pattern sCenterPattern = Pattern.compile("<center>(.+?)</center>");
    private static final Pattern sErrorPattern = Pattern.compile("(.+?)<a[^>]*>Назад</a>.*?");
    private static final Pattern sMakabaErrorPattern = Pattern.compile("\"Reason\":\\s?\"(.*?)\"");

    public SendPostResult isPostSuccessful(String boardName, String responseText) {
        SendPostResult result = new SendPostResult();

        if (responseText == null) {
            result.isSuccess = true;
            return result;
        }

        String errorText = RegexUtils.getGroupValue(responseText, sMakabaErrorPattern, 1);
        if (errorText != null) {
            result.error = errorText;
            return result;
        }

        // Пусть будет true, т.к. если не нашел ошибку, то скорей всего
        // результат верный
        result.isSuccess = true;
        return result;
    }
}
