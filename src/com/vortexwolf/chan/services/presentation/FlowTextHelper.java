package com.vortexwolf.chan.services.presentation;

import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.FloatMath;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.controls.MyLeadingMarginSpan2;
import com.vortexwolf.chan.models.presentation.FloatImageModel;

public class FlowTextHelper {

    public static boolean sNewClassAvailable = Constants.SDK_VERSION >= 8;

    static class MyLeadingMarginSpan2Factory {
        static MyLeadingMarginSpan2 create(int lines, int margin) {
            return new MyLeadingMarginSpan2(lines, margin);
        }
    }

    public static SpannableStringBuilder tryFlowText(SpannableStringBuilder ss, FloatImageModel floatModel) {
        // There is nothing I can do for older versions, so just return
        if (!sNewClassAvailable || ss == null || ss.length() == 0) {
            return ss;
        }

        // Get height and width of the image and height of the text line
        if (floatModel.getHeight() == 0 || floatModel.getWidth() == 0) {
            return ss;
        }

        // Set the span according to the number of lines and width of the image
        int lines = (int) FloatMath.ceil(floatModel.getHeight() / floatModel.getTextLineHeight());
        int offset = floatModel.getWidth() + floatModel.getRightMargin();

        ss.setSpan(MyLeadingMarginSpan2Factory.create(lines, offset), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return ss;
    }

    public static void setFloatLayoutPosition(View thumbnailView, TextView messageView) {
        if (!sNewClassAvailable) {
            return;
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) messageView.getLayoutParams();
        int[] rules = params.getRules();
        rules[RelativeLayout.RIGHT_OF] = 0;

        thumbnailView.bringToFront();
    }

    public static void setDefaultLayoutPosition(View thumbnailView, TextView messageView) {
        if (!sNewClassAvailable) {
            return;
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) messageView.getLayoutParams();
        int[] rules = params.getRules();
        rules[RelativeLayout.RIGHT_OF] = thumbnailView.getId();
    }
}
