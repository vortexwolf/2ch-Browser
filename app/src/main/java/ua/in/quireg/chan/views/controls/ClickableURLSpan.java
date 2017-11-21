package ua.in.quireg.chan.views.controls;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.View;

import ua.in.quireg.chan.interfaces.IURLSpanClickListener;

public class ClickableURLSpan extends ClickableSpan {
    private final String mURL;
    private IURLSpanClickListener mListener;

    public ClickableURLSpan(String url) {
        this.mURL = url;
    }

    @Override
    public void onClick(View widget) {
        if (this.mListener != null) {
            this.mListener.onClick(widget, this, this.mURL);
        }
    }

    public void setOnClickListener(IURLSpanClickListener listener) {
        this.mListener = listener;
    }

    public static ClickableURLSpan replaceURLSpan(SpannableStringBuilder builder, URLSpan span, int color) {
        int start = builder.getSpanStart(span);
        int end = builder.getSpanEnd(span);
        String url = span.getURL();

        builder.removeSpan(span);

        ClickableURLSpan newSpan = new ClickableURLSpan(url);
        builder.setSpan(newSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return newSpan;
    }
}
