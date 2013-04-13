package com.vortexwolf.dvach.common.controls;

import java.lang.reflect.Field;

import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.CompatibilityUtils;

import android.content.Context;
import android.text.Layout;
import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/** The TextView that handles correctly clickable spans.
 */
public class ClickableLinksTextView extends TextView {
    public static final String TAG = "ClickableLinksTextView";
    
    private boolean mEditorCreated = false;
    private Object mEditor = null;
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
    
    private final void copyBaseEditorIfNecessary(){
        if(this.mEditorCreated) {
            return;
        }
        
        try {
            Field field = TextView.class.getDeclaredField("mEditor");
            field.setAccessible(true);
            this.mEditor = field.get(this);
            
            if (this.mEditor != null) {
                this.mDiscardNextActionUpField = this.mEditor.getClass().getDeclaredField("mDiscardNextActionUp");
                this.mDiscardNextActionUpField.setAccessible(true);
            
                this.mIgnoreActionUpEventField = this.mEditor.getClass().getDeclaredField("mIgnoreActionUpEvent");
                this.mIgnoreActionUpEventField.setAccessible(true);
            }
            
        } catch (Exception e) {
            MyLog.e(TAG, e);
        } finally {
            this.mEditorCreated = true;
        }
    }
    
    private boolean getDiscardNextActionUp() {
        if(this.mEditor == null) {
            return false;
        }
        
        try {
            return this.mDiscardNextActionUpField.getBoolean(this.mEditor);
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean getIgnoreActionUpEvent() {
        if(this.mEditor == null) {
            return false;
        }
        
        try {
            return this.mIgnoreActionUpEventField.getBoolean(this.mEditor);
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TextView checks if getAutoLinkMask != 0, so I add a similar code for == 0
        if(CompatibilityUtils.isTextSelectable(this)
            && this.getText() instanceof Spannable
            && this.getAutoLinkMask() == 0
            && this.getLinksClickable()
            && this.isEnabled()
            && this.getLayout() != null) {
            return this.checkLinksOnTouch(event);
        }
        
        return super.onTouchEvent(event);
    }
    
    private boolean checkLinksOnTouch(MotionEvent event) {
        this.copyBaseEditorIfNecessary();
        
        int action = event.getAction() & 0xff; // getActionMasked()
        boolean discardNextActionUp = this.getDiscardNextActionUp();
        
        // call the base method anyway
        final boolean superResult = super.onTouchEvent(event);
        MyLog.v(TAG, "superResult = " + superResult);
        
        // the same check as in the super.onTouchEvent(event)
        if(discardNextActionUp && action == MotionEvent.ACTION_UP) {
            MyLog.v(TAG, "discardNextActionUp");
            return superResult;
        }

        final boolean touchIsFinished = (action == MotionEvent.ACTION_UP) && !this.getIgnoreActionUpEvent() && isFocused();
  
        // Copied from the LinkMovementMethod class
        if (touchIsFinished) {
            Spannable spannable = (Spannable)this.getText();
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= this.getTotalPaddingLeft();
            y -= this.getTotalPaddingTop();

            x += this.getScrollX();
            y += this.getScrollY();

            Layout layout = this.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] link = spannable.getSpans(off, off, ClickableSpan.class);

            if (link.length != 0) {
                link[0].onClick(this);
                return true;
            }
        }
        
        return superResult;
    }
}
