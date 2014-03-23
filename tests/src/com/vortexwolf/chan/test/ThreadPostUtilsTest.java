package com.vortexwolf.chan.test;

import java.util.Date;
import java.util.TimeZone;

import android.test.InstrumentationTestCase;

import com.vortexwolf.chan.common.utils.ThreadPostUtils;

public class ThreadPostUtilsTest extends InstrumentationTestCase {

    public void testGetMoscowDateFromTimestamp() {
        long testMiliSeconds = 1365759192000L;

        String testDate = ThreadPostUtils.getMoscowDateFromTimestamp(this.getInstrumentation().getContext(), testMiliSeconds);
        assertEquals("4/12/2013, 1:33 PM", testDate);
    }
    
    public void testGetMoscowDateDayLightSavingTime(){
        long testMiliSeconds = 1354641768000L;
        
        String testDate = ThreadPostUtils.getMoscowDateFromTimestamp(this.getInstrumentation().getContext(), testMiliSeconds);
        assertEquals("12/4/2012, 9:22 PM", testDate);
    }

    public void testGetLocalDateFromTimestamp() {
        long testMiliSeconds = 1365759192000L;
        double offsetInHours = TimeZone.getDefault().getOffset(testMiliSeconds) / 1000.0 / 3600.0;

        String testDate = ThreadPostUtils.getLocalDateFromTimestamp(this.getInstrumentation().getContext(), testMiliSeconds);
        assertEquals("4/12/2013, 9:33 AM", testDate);
    }
    
    public void testGetUtcDateFromTimestamp() {
        long testMiliSeconds = 1365759192000L;

        String testDate = ThreadPostUtils.getDateFromTimestamp(this.getInstrumentation().getContext(), testMiliSeconds, TimeZone.getTimeZone("UTC"));
        assertEquals("4/12/2013, 9:33 AM", testDate);
    }
    
    public void testParseMoscowTextDate() {
        long result = ThreadPostUtils.parseMoscowTextDate("Птн 12 Апр 2013 13:33:12");
        
        assertEquals(1365759192000L, result);
    }
}
