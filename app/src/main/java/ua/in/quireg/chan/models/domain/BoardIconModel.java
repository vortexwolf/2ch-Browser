package ua.in.quireg.chan.models.domain;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by Arcturus Mengsk on 12/20/2017, 2:58 AM.
 * 2ch-Browser
 */
@Entity(tableName = "boardIconModel")
public class BoardIconModel implements Serializable {

    @PrimaryKey
    @ColumnInfo(name = "iconurl")
    @NonNull
    private String mIconUrl;
    @ColumnInfo(name = "iconname")
    private String mIconName;
    @ColumnInfo(name = "iconnumber")
    private int mIconNumber;
    @ColumnInfo(name = "board")
    private String mBoard;

    public String getBoard() {
        return mBoard;
    }

    public void setBoard(String board) {
        this.mBoard = board;
    }

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
