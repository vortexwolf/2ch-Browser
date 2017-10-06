package ua.in.quireg.chan.ui.controls;

import android.text.Layout;
import android.text.NoCopySpan;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class MyLinkMovementMethod extends ScrollingMovementMethod {
    private static final int CLICK = 1;
    private static final int UP = 2;
    private static final int DOWN = 3;

    private boolean mSkipNextClick = false;

    @Override
    public boolean onKeyDown(TextView widget, Spannable buffer, int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (event.getRepeatCount() == 0) {
                    if (this.action(CLICK, widget, buffer)) {
                        return true;
                    }
                }
        }

        return super.onKeyDown(widget, buffer, keyCode, event);
    }

    @Override
    protected boolean up(TextView widget, Spannable buffer) {
        if (this.action(UP, widget, buffer)) {
            return true;
        }

        return super.up(widget, buffer);
    }

    @Override
    protected boolean down(TextView widget, Spannable buffer) {
        if (this.action(DOWN, widget, buffer)) {
            return true;
        }

        return super.down(widget, buffer);
    }

    @Override
    protected boolean left(TextView widget, Spannable buffer) {
        if (this.action(UP, widget, buffer)) {
            return true;
        }

        return super.left(widget, buffer);
    }

    @Override
    protected boolean right(TextView widget, Spannable buffer) {
        if (this.action(DOWN, widget, buffer)) {
            return true;
        }

        return super.right(widget, buffer);
    }

    private boolean action(int what, TextView widget, Spannable buffer) {
        Layout layout = widget.getLayout();

        int padding = widget.getTotalPaddingTop() + widget.getTotalPaddingBottom();
        int areatop = widget.getScrollY();
        int areabot = areatop + widget.getHeight() - padding;

        int linetop = layout.getLineForVertical(areatop);
        int linebot = layout.getLineForVertical(areabot);

        int first = layout.getLineStart(linetop);
        int last = layout.getLineEnd(linebot);

        ClickableSpan[] candidates = buffer.getSpans(first, last, ClickableSpan.class);

        int a = Selection.getSelectionStart(buffer);
        int b = Selection.getSelectionEnd(buffer);

        int selStart = Math.min(a, b);
        int selEnd = Math.max(a, b);

        if (selStart < 0) {
            if (buffer.getSpanStart(FROM_BELOW) >= 0) {
                selStart = selEnd = buffer.length();
            }
        }

        if (selStart > last) {
            selStart = selEnd = Integer.MAX_VALUE;
        }
        if (selEnd < first) {
            selStart = selEnd = -1;
        }

        switch (what) {
            case CLICK:
                if (selStart == selEnd) {
                    return false;
                }

                ClickableSpan[] link = buffer.getSpans(selStart, selEnd, ClickableSpan.class);

                if (link.length != 1) {
                    return false;
                }

                link[0].onClick(widget);
                break;

            case UP:
                int beststart,
                bestend;

                beststart = -1;
                bestend = -1;

                for (int i = 0; i < candidates.length; i++) {
                    int end = buffer.getSpanEnd(candidates[i]);

                    if (end < selEnd || selStart == selEnd) {
                        if (end > bestend) {
                            beststart = buffer.getSpanStart(candidates[i]);
                            bestend = end;
                        }
                    }
                }

                if (beststart >= 0) {
                    Selection.setSelection(buffer, bestend, beststart);
                    return true;
                }

                break;

            case DOWN:
                beststart = Integer.MAX_VALUE;
                bestend = Integer.MAX_VALUE;

                for (int i = 0; i < candidates.length; i++) {
                    int start = buffer.getSpanStart(candidates[i]);

                    if (start > selStart || selStart == selEnd) {
                        if (start < beststart) {
                            beststart = start;
                            bestend = buffer.getSpanEnd(candidates[i]);
                        }
                    }
                }

                if (bestend < Integer.MAX_VALUE) {
                    Selection.setSelection(buffer, beststart, bestend);
                    return true;
                }

                break;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            if (this.mSkipNextClick) {
                this.mSkipNextClick = false;
            } else {
                if (isLinkClickEvent(widget, buffer, event)) {
                    return true;
                } else {
                    this.removeSelection(buffer);
                }
            }
        }

        return super.onTouchEvent(widget, buffer, event);
    }

    public boolean isLinkClickEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

            if (link.length != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    link[0].onClick(widget);
                } else if (action == MotionEvent.ACTION_DOWN) {
                    Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public void initialize(TextView widget, final Spannable text) {
        widget.setOnLongClickListener(new TextView.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                MyLinkMovementMethod.this.mSkipNextClick = true;
                MyLinkMovementMethod.this.removeSelection(text);
                return false;
            }
        });
        this.removeSelection(text);
        text.removeSpan(FROM_BELOW);
    }

    @Override
    public void onTakeFocus(TextView view, Spannable text, int dir) {
        this.removeSelection(text);

        if ((dir & View.FOCUS_BACKWARD) != 0) {
            text.setSpan(FROM_BELOW, 0, 0, Spanned.SPAN_POINT_POINT);
        } else {
            text.removeSpan(FROM_BELOW);
        }
    }

    public static MyLinkMovementMethod getInstance() {
        if (sInstance == null) {
            sInstance = new MyLinkMovementMethod();
        }

        return sInstance;
    }

    private void removeSelection(Spannable text) {
        Selection.removeSelection(text);
    }

    private static MyLinkMovementMethod sInstance;
    private static Object FROM_BELOW = new NoCopySpan.Concrete();
}
