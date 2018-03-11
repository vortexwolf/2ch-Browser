package ua.in.quireg.chan.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;

public class TextView extends JellyBeanSpanFixTextView {
    private static final String ELLIPSIS = "...";

    public interface EllipsizeListener {
        void ellipsizeStateChanged(TextView view, boolean ellipsized);
    }

    private EllipsizeListener ellipsizeListener = null;
    private boolean isEllipsized;
    private boolean isStale;
    private boolean programmaticChange;
    private CharSequence fullText;
    private int maxLines = -1;
    private float lineSpacingMultiplier = 1.0f;
    private float lineAdditionalVerticalPadding = 0.0f;

    public TextView(Context context) {
        super(context);
    }

    public TextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.updateMaxLinesFromAttributes(context, attrs);
    }

    public TextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.updateMaxLinesFromAttributes(context, attrs);
    }

    private void updateMaxLinesFromAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, new int[] { android.R.attr.maxLines });
        int maxLines = a.getInt(0, this.maxLines);
        a.recycle();

        this.setMaxLines(maxLines);
    }

    public void setEllipsizeListener(EllipsizeListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        this.ellipsizeListener = listener;
    }

    public boolean isEllipsized() {
        return this.isEllipsized;
    }

    @Override
    public void setMaxLines(int maxLines) {
        super.setMaxLines(maxLines);
        this.maxLines = maxLines;
        this.isStale = true;
    }

    public int getCustomMaxLines() {
        return this.maxLines;
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
        if (!this.programmaticChange) {
            this.fullText = text;
            this.isStale = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (this.isStale) {
            super.setEllipsize(null);
            this.resetText();
        }
        super.onDraw(canvas);
    }

    private void resetText() {
        int maxLines = this.getCustomMaxLines();
        CharSequence workingText = this.fullText;
        boolean ellipsized = false;

        if (maxLines != -1) {
            Layout layout = this.createWorkingLayout(workingText);
            if (layout.getLineCount() > maxLines) {
                workingText = workingText.subSequence(0, layout.getLineEnd(maxLines - 1));
                workingText = new SpannableStringBuilder(workingText).append(ELLIPSIS);
                ellipsized = true;
            }
        }
        if (!workingText.equals(this.getText())) {
            this.programmaticChange = true;
            try {
                this.setText(workingText);
            } finally {
                this.programmaticChange = false;
            }
        }
        this.isStale = false;
        this.isEllipsized = ellipsized;
        if (this.ellipsizeListener != null) {
            this.ellipsizeListener.ellipsizeStateChanged(this, ellipsized);
        }
    }

    private Layout createWorkingLayout(CharSequence workingText) {
        return new StaticLayout(workingText, this.getPaint(), this.getWidth() - this.getPaddingLeft() - this.getPaddingRight(), Alignment.ALIGN_NORMAL, this.lineSpacingMultiplier, this.lineAdditionalVerticalPadding, false);
    }

    @Override
    public void setEllipsize(TruncateAt where) {
        // Ellipsize settings are not respected
    }
}
