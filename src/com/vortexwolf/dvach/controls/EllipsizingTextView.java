package com.vortexwolf.dvach.controls;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.Editable;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.SpannableStringBuilder;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.widget.TextView;
import android.text.StaticLayout;

public class EllipsizingTextView extends TextView {
    private static final String ELLIPSIS = "...";

    public interface EllipsizeListener {
        void ellipsizeStateChanged(EllipsizingTextView view, boolean ellipsized);
    }

    private EllipsizeListener ellipsizeListener = null;
    private boolean isEllipsized;
    private boolean isStale;
    private boolean programmaticChange;
    private CharSequence fullText;
    private int maxLines = -1;
    private float lineSpacingMultiplier = 1.0f;
    private float lineAdditionalVerticalPadding = 0.0f;

    public EllipsizingTextView(Context context) {
        super(context);
    }

    public EllipsizingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.updateMaxLinesFromAttributes(context, attrs);
    }

    public EllipsizingTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.updateMaxLinesFromAttributes(context, attrs);
    }
    
    private void updateMaxLinesFromAttributes(Context context, AttributeSet attrs){
    	TypedArray a = context.obtainStyledAttributes(attrs, new int[] { android.R.attr.maxLines }); 
    	int maxLines = a.getInt(0, this.maxLines);
    	setMaxLines(maxLines); 
    }

    public void setEllipsizeListener(EllipsizeListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        ellipsizeListener = listener;
    }

    public boolean isEllipsized() {
        return isEllipsized;
    }

    @Override
    public void setMaxLines(int maxLines) {
        super.setMaxLines(maxLines);
        this.maxLines = maxLines;
        isStale = true;
    }

    public int getMaxLines() {
        return maxLines;
    }

    @Override
    public void setLineSpacing(float add, float mult) {
        this.lineAdditionalVerticalPadding = add;
        this.lineSpacingMultiplier = mult;
        super.setLineSpacing(add, mult);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int before, int after) {
        super.onTextChanged(text, start, before, after);
        if (!programmaticChange) {
            fullText = text;
            isStale = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isStale) {
            super.setEllipsize(null);
            resetText();
        }
        super.onDraw(canvas);
    }

    private void resetText() {
        int maxLines = getMaxLines();
        CharSequence workingText = fullText;
        boolean ellipsized = false;
        
        if (maxLines != -1) {
            Layout layout = createWorkingLayout(workingText);
            if (layout.getLineCount() > maxLines) {
                workingText = workingText.subSequence(0, layout.getLineEnd(maxLines - 1));
                while (createWorkingLayout(workingText + ELLIPSIS).getLineCount() > maxLines) {
                    int lastSpace = workingText.toString().lastIndexOf(' ');
                    if (lastSpace == -1) {
                        break;
                    }
                    workingText = workingText.subSequence(0, lastSpace);
                }
                workingText = workingText + ELLIPSIS;
                ellipsized = true;
            }
        }
        if (!workingText.equals(getText())) {
            programmaticChange = true;
            try {
                setText(workingText);
            } finally {
                programmaticChange = false;
            }
        }
        isStale = false;
        isEllipsized = ellipsized;
        if(ellipsizeListener != null){
        	ellipsizeListener.ellipsizeStateChanged(this, ellipsized);
        }
    }
    
    private Layout createWorkingLayout(CharSequence workingText) {
        return new StaticLayout(workingText, getPaint(), getWidth() - getPaddingLeft() - getPaddingRight(), Alignment.ALIGN_NORMAL, lineSpacingMultiplier, lineAdditionalVerticalPadding, false);
    }

    @Override
    public void setEllipsize(TruncateAt where) {
        // Ellipsize settings are not respected
    }
}

