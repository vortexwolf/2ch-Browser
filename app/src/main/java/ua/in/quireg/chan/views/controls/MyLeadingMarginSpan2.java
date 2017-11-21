package ua.in.quireg.chan.views.controls;

import android.annotation.TargetApi;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.text.Layout;
import android.text.style.LeadingMarginSpan.LeadingMarginSpan2;

import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.utils.StringUtils;

@TargetApi(Build.VERSION_CODES.FROYO)
public class MyLeadingMarginSpan2 implements LeadingMarginSpan2 {
    private int margin;
    private int firstMarginLines;
    // some code can be called between TextView.onDraw and this.drawLeadingMargin
    private boolean isWaitingForDraw = false;
    private int drawnLines = 0;
    private int clickedLine = -1;

    public MyLeadingMarginSpan2(int lines, int margin) {
        this.margin = margin;
        this.firstMarginLines = lines;
    }

    public void resetDrawState() {
        this.drawnLines = 0;
        this.isWaitingForDraw = true;
    }

    public void onMeasure() {
        this.drawnLines = 0;
    }

    public void setMyLeadingMarginSpanCurrentLine(int line) {
        this.clickedLine = line;
    }

    @Override
    public int getLeadingMargin(boolean first) {
        boolean isFirstMargin = first;

        if (Constants.SDK_VERSION >= 21) {
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            String method3 = stackTraceElements[3].getMethodName();

            if (StringUtils.areEqual(method3, "generate")) {
                // use the default value
            } else if (StringUtils.areEqual(method3, "drawText")) {
                // margin for visible text after it was drawn
                isFirstMargin = this.drawnLines <= this.firstMarginLines - 1;
            } else if (StringUtils.areEqual(method3, "getParagraphLeadingMargin")
                    && (this.drawnLines > 0 || this.isWaitingForDraw)) {
                // margin for invisible selectable text
                // if you click somewhere - you can select and copy a word from invisible text
                isFirstMargin = this.clickedLine <= this.firstMarginLines - 2;
            }
        }

        return isFirstMargin ? this.margin : 0;
    }

    @Override
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom, CharSequence text, int start, int end, boolean first, Layout layout) {
        this.isWaitingForDraw = false;
        this.drawnLines++;
    }

    @Override
    public int getLeadingMarginLineCount() {
        return this.firstMarginLines;
    }
}
