package com.vortexwolf.chan.settings;

public class SettingsEntity implements Comparable{
    public int theme;
    public boolean isDisplayDate;
    public boolean isLocalDate;
    public boolean isLoadThumbnails;
    public boolean isDisplayAllBoards;
    public boolean isSwipeToRefresh;

    @Override
    public int compareTo(Object obj) {

        if((obj == null) || (getClass() != obj.getClass())){
            return -1;
        }
        else{
            SettingsEntity se = (SettingsEntity)obj;
            if( (this.theme == se.theme) &&
                    (this.isDisplayDate == se.isDisplayDate) &&
                    (this.isLocalDate == se.isLocalDate) &&
                    (this.isLoadThumbnails == se.isLoadThumbnails) &&
                    (this.isDisplayAllBoards == se.isDisplayAllBoards) &&
                    (this.isSwipeToRefresh == se.isSwipeToRefresh)
                ){
                return 0;
            }
            return -1;
        }
    }
}
