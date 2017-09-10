package ua.in.quireg.chan.common.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import ua.in.quireg.chan.models.domain.BoardModel;

import java.util.List;

/**
 * Created by Arcturus on 12/15/2016.
 */

public class GeneralUtils {

    public static boolean equalLists(List<BoardModel> a, List<BoardModel> b){
        for (BoardModel modelA: a) {
            boolean matchFound = false;
            for (BoardModel modelB: b) {
                if(modelA.getId().equals(modelB.getId())){
                    matchFound = true;
                    break;
                }
            }
            if(!matchFound){

                return false;
            }
        }
        return true;
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }
}
