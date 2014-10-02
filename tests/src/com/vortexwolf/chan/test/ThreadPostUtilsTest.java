package com.vortexwolf.chan.test;

import java.util.Date;
import java.util.TimeZone;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.text.format.DateFormat;

import com.vortexwolf.chan.common.utils.ThreadPostUtils;

public class ThreadPostUtilsTest extends InstrumentationTestCase {

    public void testGetMoscowDateFromTimestamp() {
        long testMiliSeconds = 1365759192000L;

        String testDate = ThreadPostUtils.getMoscowDateFromTimestamp(this.getInstrumentation().getContext(), testMiliSeconds);
        assertEquals("4/12/2013, 13:33", testDate);
    }
    
    public void testGetMoscowDateDayLightSavingTime(){
        long testMiliSeconds = 1354641768000L;
        
        String testDate = ThreadPostUtils.getMoscowDateFromTimestamp(this.getInstrumentation().getContext(), testMiliSeconds);
        assertEquals("12/4/2012, 21:22", testDate);
    }

    public void testGetLocalDateFromTimestamp() {
        long testMiliSeconds = 1365759192000L;
        int offset = TimeZone.getDefault().getOffset(testMiliSeconds);
        long utcMilliseconds = testMiliSeconds + offset;

        Context context = this.getInstrumentation().getContext();
        
        String expectedDate = ThreadPostUtils.getDateFromTimestamp(context, utcMilliseconds, TimeZone.getTimeZone("UTC"));
        String actualDate = ThreadPostUtils.getLocalDateFromTimestamp(context, testMiliSeconds);
        assertEquals(expectedDate, actualDate);
    }
    
    public void testGetUtcDateFromTimestamp() {
        long testMiliSeconds = 1365759192000L;

        String testDate = ThreadPostUtils.getDateFromTimestamp(this.getInstrumentation().getContext(), testMiliSeconds, TimeZone.getTimeZone("UTC"));
        assertEquals("4/12/2013, 09:33", testDate);
    }
    
    public void testParseMoscowTextDate() {
        long result = ThreadPostUtils.parseMoscowTextDate("Птн 12 Апр 2013 13:33:12");
        
        assertEquals(1365759192000L, result);
    }
}
