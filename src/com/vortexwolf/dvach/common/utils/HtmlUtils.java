package com.vortexwolf.dvach.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.impl.client.DefaultHttpClient;

import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.httpimage.HttpImageManager;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.style.URLSpan;

import com.vortexwolf.chan.R;
import com.vortexwolf.dvach.common.Factory;
import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.common.controls.ClickableURLSpan;
import com.vortexwolf.dvach.common.library.MyHtml;
import com.vortexwolf.dvach.common.library.UnknownTagsHandler;
import com.vortexwolf.dvach.interfaces.IURLSpanClickListener;
import com.vortexwolf.dvach.services.presentation.DvachUriBuilder;

public class HtmlUtils {
    private static final Pattern styleColorPattern = Pattern.compile(".*?color: rgb\\((\\d+), (\\d+), (\\d+)\\);.*");

    private static final DefaultHttpClient httpClient = MainApplication.getHttpClient();
    // Картинки со смайликами во время всяких праздников
    public static final MyHtml.ImageGetter sImageGetter = new MyHtml.ImageGetter() {
        @Override
        public Drawable getDrawable(String ref) {
            Uri uri = Factory.getContainer().resolve(DvachUriBuilder.class).adjust2chRelativeUri(Uri.parse(ref));

            HttpImageManager imageManager = Factory.getContainer().resolve(HttpImageManager.class);

            Bitmap cached = imageManager.loadImage(uri);
            if (cached != null) {
                Bitmap bmp = cached;
                Drawable d = new BitmapDrawable(bmp);
                d.setBounds(0, 0, Math.max(d.getIntrinsicWidth(), bmp.getWidth()), Math.max(d.getIntrinsicHeight(), bmp.getHeight()));
                return d;
            }

            return Factory.getContainer().resolve(Resources.class).getDrawable(R.drawable.a_empty);
        }
    };

    public static SpannableStringBuilder createSpannedFromHtml(String htmlText, Theme theme) {
        SpannableStringBuilder builder = (SpannableStringBuilder) MyHtml.fromHtml(StringUtils.emptyIfNull(htmlText), sImageGetter, new UnknownTagsHandler(theme));

        return builder;
    }

    /** Добавляет обработчики событий к ссылкам */
    public static SpannableStringBuilder replaceUrls(SpannableStringBuilder builder, IURLSpanClickListener listener, Theme theme) {
        URLSpan[] spans = builder.getSpans(0, builder.length(), URLSpan.class);

        if (spans.length > 0) {
            TypedArray a = theme.obtainStyledAttributes(R.styleable.Theme);
            int urlColor = a.getColor(R.styleable.Theme_urlLinkForeground, Color.BLUE);
            a.recycle();
            
            for (URLSpan span : spans) {
                ClickableURLSpan newSpan = ClickableURLSpan.replaceURLSpan(builder, span, urlColor);
                newSpan.setOnClickListener(listener);
            }
        }

        return builder;
    }

    public static String fixHtmlTags(String htmlText) {
        if (htmlText == null) {
            return null;
        }

        String result = htmlText;
        // Убираем абзацы
        if (result.startsWith("<p>") && result.endsWith("</p>")) {
            String newHtml = result.substring(3, result.length() - 4);
            result = "<span>" + trimBr(newHtml) + "</span>"; // except <p>
        }

        return result;
    }

    public static String trimBr(String htmlText) {
        if (htmlText == null) {
            return null;
        }

        String result = htmlText;
        if (htmlText.startsWith("<br />") || htmlText.endsWith("<br />")) {
            result = htmlText.replaceAll("^(?:<br />)*(.*?)(?:<br />)*$", "$1");
        }

        return result;
    }

    public static Integer getIntFontColor(String htmlText) {
        if (htmlText == null) {
            return null;
        }

        Matcher m = styleColorPattern.matcher(htmlText);
        while (m.find() && m.groupCount() == 3) {
            Integer n1 = Integer.valueOf(m.group(1));
            Integer n2 = Integer.valueOf(m.group(2));
            Integer n3 = Integer.valueOf(m.group(3));
            int c = Color.rgb(n1, n2, n3);

            return c;
        }
        return null;
    }

    public static String getStringFontColor(String htmlText) {
        Integer color = getIntFontColor(htmlText);

        if (color != null) {
            return String.format("#%06X", (0xFFFFFF & color));
        }

        return null;
    }
}
