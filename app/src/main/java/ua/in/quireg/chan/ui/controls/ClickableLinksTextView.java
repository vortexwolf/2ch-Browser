package ua.in.quireg.chan.ui.controls;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Selection;
import android.text.Spannable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import ua.in.quireg.chan.common.library.MyLog;
import ua.in.quireg.chan.common.utils.CompatibilityUtils;
import ua.in.quireg.chan.common.utils.StringUtils;

/**
 * The TextView that handles correctly clickable spans.
 */
public class ClickableLinksTextView extends TextView {
    public static final String TAG = "ClickableLinksTextView";

    private boolean mBaseEditorCopied = false;
    private Object mBaseEditor = null;
    private Field mDiscardNextActionUpField = null;
    private Field mIgnoreActionUpEventField = null;

    public ClickableLinksTextView(Context context) {
        super(context);
    }

    public ClickableLinksTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ClickableLinksTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();

        // listview scrolling behaves incorrectly after you select and copy some text, so I've added this code
        if (this.isFocused()) {
            MyLog.d(TAG, "clear focus");
            this.clearFocus();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // the base TextView class checks if getAutoLinkMask != 0, so I added a similar code for == 0
        if (CompatibilityUtils.isTextSelectable(this) && this.getText() instanceof Spannable && this.getAutoLinkMask() == 0 && this.getLinksClickable() && this.isEnabled() && this.getLayout() != null) {
            return this.checkLinksOnTouch(event);
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Reset the MyLeadingMarginSpan2 state
        CharSequence text = this.getText();
        if (text != null && text instanceof Spannable) {
            CompatibilityUtils.resetMyLeadingMarginSpanState((Spannable) text);
        }

        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        CharSequence text = this.getText();
        if (text != null && text instanceof Spannable) {
            CompatibilityUtils.callMyLeadingMarginSpanMeasure((Spannable) text);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public int getOffsetForPosition(float x, float y) {
        // Set the current line for MyLeadingMarginSpan2
        CharSequence text = this.getText();
        if (text != null && text instanceof Spannable) {
            int line = this.getLineAtCoordinate(y);

            CompatibilityUtils.setMyLeadingMarginSpanCurrentLine((Spannable) text, line);
        }

        return super.getOffsetForPosition(x, y);
    }

    public void startSelection() {
        if (StringUtils.isEmpty(this.getText())) {
            return;
        }

        this.copyBaseEditorIfNecessary();

        Selection.setSelection((Spannable) this.getText(), 0, this.getText().length());

        try {
            Method performLongClick = this.mBaseEditor.getClass().getMethod("performLongClick", Boolean.TYPE);
            performLongClick.invoke(this.mBaseEditor, false);
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }

    private int getLineAtCoordinate(float y) {
        y -= getTotalPaddingTop();
        // Clamp the position to inside of the view.
        y = Math.max(0.0f, y);
        y = Math.min(getHeight() - getTotalPaddingBottom() - 1, y);
        y += getScrollY();
        return getLayout().getLineForVertical((int) y);
    }

    private boolean checkLinksOnTouch(MotionEvent event) {
        this.copyBaseEditorIfNecessary();

        int action = event.getAction() & 0xff; // getActionMasked()
        boolean discardNextActionUp = this.getDiscardNextActionUp();

        // call the base method anyway
        final boolean superResult = super.onTouchEvent(event);

        // the same check as in the super.onTouchEvent(event)
        if (discardNextActionUp && action == MotionEvent.ACTION_UP) {
            return superResult;
        }

        boolean isLinkClick = MyLinkMovementMethod.getInstance().isLinkClickEvent(this, (Spannable) this.getText(), event);
        boolean isTouchStarted = action == MotionEvent.ACTION_DOWN;
        boolean isTouchFinished = (action == MotionEvent.ACTION_UP) && !this.getIgnoreActionUpEvent();
        if (isLinkClick && (isTouchStarted || isTouchFinished) && this.isFocused()) {
            return true;
        }

        return superResult;
    }

    private void copyBaseEditorIfNecessary() {
        if (this.mBaseEditorCopied) {
            return;
        }

        try {
            Field field = TextView.class.getDeclaredField("mEditor");
            field.setAccessible(true);
            this.mBaseEditor = field.get(this);

            if (this.mBaseEditor != null) {
                Class editorClass = this.mBaseEditor.getClass();
                this.mDiscardNextActionUpField = editorClass.getDeclaredField("mDiscardNextActionUp");
                this.mDiscardNextActionUpField.setAccessible(true);

                this.mIgnoreActionUpEventField = editorClass.getDeclaredField("mIgnoreActionUpEvent");
                this.mIgnoreActionUpEventField.setAccessible(true);
            }

        } catch (Exception e) {
            MyLog.e(TAG, e);
        } finally {
            this.mBaseEditorCopied = true;
        }
    }

    private boolean getDiscardNextActionUp() {
        if (this.mBaseEditor == null) {
            return false;
        }

        try {
            return this.mDiscardNextActionUpField.getBoolean(this.mBaseEditor);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean getIgnoreActionUpEvent() {
        if (this.mBaseEditor == null) {
            return false;
        }

        try {
            return this.mIgnoreActionUpEventField.getBoolean(this.mBaseEditor);
        } catch (Exception e) {
            return false;
        }
    }
}
