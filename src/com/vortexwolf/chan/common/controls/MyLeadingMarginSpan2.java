package com.vortexwolf.chan.common.controls;

import android.annotation.TargetApi;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.text.Layout;
import android.text.style.LeadingMarginSpan.LeadingMarginSpan2;

import com.vortexwolf.chan.common.Constants;

@TargetApi(Build.VERSION_CODES.FROYO)
public class MyLeadingMarginSpan2 implements LeadingMarginSpan2 {
    private int margin;
    private int firstMarginLines;
    private boolean isDrawnAtLeastOnce = false;
    private boolean isDrawCall = false;
    private int drawnLines = 0;
    private int clickedLine = -1;

    public MyLeadingMarginSpan2(int lines, int margin) {
        this.margin = margin;
        this.firstMarginLines = lines;
    }

    public void resetDrawState() {
        this.isDrawCall = false;
        this.drawnLines = 0;
    }

    public void setMyLeadingMarginSpanCurrentLine(int line) {
        this.clickedLine = line;
    }

    @Override
    public int getLeadingMargin(boolean first) {
        boolean isFirstMargin = first;

        if (Constants.SDK_VERSION >= 21) {
            if (this.isDrawCall) {
                // margin for visible text after it was drawn
                this.isDrawCall = false;
                isFirstMargin = this.drawnLines <= this.firstMarginLines - 1;
            } else if (!this.isDrawCall && this.isDrawnAtLeastOnce) {
                // margin for invisible selectable text
                // if you click somewhere - you can select and copy a word from invisible text
                isFirstMargin = this.clickedLine <= this.firstMarginLines - 2;
            }
        }

        return isFirstMargin ? this.margin : 0;
    }

    @Override
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom, CharSequence text, int start, int end, boolean first, Layout layout) {
        this.isDrawnAtLeastOnce = true;
        this.isDrawCall = true;
        this.drawnLines++;
    }

    @Override
    public int getLeadingMarginLineCount() {
        return this.firstMarginLines;
    }
}
