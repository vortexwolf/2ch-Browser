package ua.in.quireg.chan.models.domain;

import java.io.Serializable;

/**
 * Created by Arcturus Mengsk on 12/20/2017, 2:58 AM.
 * 2ch-Browser
 */

public class BoardIconModel implements Serializable {

    private String mIconName;
    private String mIconUrl;
    private int mIconNumber;

    public String getIconName() {
        return mIconName;
    }

    public void setIconName(String mIconName) {
        this.mIconName = mIconName;
    }

    public String getIconUrl() {
        return mIconUrl;
    }

    public void setIconUrl(String mIconUrl) {
        this.mIconUrl = mIconUrl;
    }

    public int getIconNumber() {
        return mIconNumber;
    }

    public void setIconNumber(int mIconNumber) {
        this.mIconNumber = mIconNumber;
    }
}
