package com.vortexwolf.chan.settings;

import com.vortexwolf.chan.models.domain.BoardModel;

import java.util.ArrayList;

public class SettingsEntity {
    public int theme;
    public boolean isDisplayDate;
    public boolean isLocalDate;
    public boolean isLoadThumbnails;
    public boolean isDisplayAllBoards;
    public boolean isSwipeToRefresh;

    @Override
    public boolean equals(Object obj) {
        boolean result;
        if((obj == null) || (getClass() != obj.getClass())){
            result = false;
        }
        else{
            SettingsEntity se = (SettingsEntity)obj;
            result = (this.theme == se.theme) &&
                    (this.isDisplayDate == se.isDisplayDate) &&
                    (this.isLocalDate == se.isLocalDate) &&
                    (this.isLoadThumbnails == se.isLoadThumbnails) &&
                    (this.isDisplayAllBoards == se.isDisplayAllBoards) &&
                    (this.isSwipeToRefresh == se.isSwipeToRefresh);
        }
        return result;
    }
}
