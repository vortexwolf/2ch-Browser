package com.vortexwolf.chan.common.controls;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.text.Layout;
import android.text.style.LeadingMarginSpan.LeadingMarginSpan2;

import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.library.MyLog;

@TargetApi(Build.VERSION_CODES.FROYO)
public class MyLeadingMarginSpan2 implements LeadingMarginSpan2 {
    private int margin;
    private int lines;
    private boolean wasDrawCalled = false;
    private int drawLineCount = 0;

    public MyLeadingMarginSpan2(int lines, int margin) {
        //MyLog.v("MyLeadingMarginSpan2", "constructor");
        this.margin = margin;
        this.lines = lines;
    }

    public void resetDrawState() {
        //MyLog.v("MyLeadingMarginSpan2", "resetDrawState");
        this.wasDrawCalled = false;
        this.drawLineCount = 0;
    }

    /* Возвращает значение, на которе должен быть добавлен отступ */
    @Override
    public int getLeadingMargin(boolean first) {
        //MyLog.v("MyLeadingMarginSpan2", "getLeadingMargin, first=" + first);
        boolean isFirstMargin = first;
        if (Constants.SDK_VERSION >= 21) {
            this.drawLineCount = this.wasDrawCalled ? this.drawLineCount + 1 : 0;
            this.wasDrawCalled = false;
            isFirstMargin = this.drawLineCount <= this.lines - 1;
        }

        return isFirstMargin ? this.margin : 0;
    }

    @Override
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom, CharSequence text, int start, int end, boolean first, Layout layout) {
        //MyLog.v("MyLeadingMarginSpan2", "drawLeadingMargin");
        this.wasDrawCalled = true;
    }

    /* Возвращает количество строк, к которым должен быть применен отступ
     * возвращаемый методом getLeadingMargin(true) Замечание. Отступ применяется
     * только к N строкам одного параграфа. */
    @Override
    public int getLeadingMarginLineCount() {
        //MyLog.v("MyLeadingMarginSpan2", "getLeadingMarginLineCount");
        return this.lines;
    }
}
