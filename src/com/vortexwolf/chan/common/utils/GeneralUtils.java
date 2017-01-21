package com.vortexwolf.chan.common.utils;

import com.vortexwolf.chan.models.domain.BoardModel;

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
}
