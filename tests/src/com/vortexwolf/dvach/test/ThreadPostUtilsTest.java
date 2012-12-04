package com.vortexwolf.dvach.test;

import java.util.Date;
import java.util.TimeZone;

import com.vortexwolf.dvach.common.utils.ThreadPostUtils;

import android.test.InstrumentationTestCase;

public class ThreadPostUtilsTest extends InstrumentationTestCase {

	public void testGetMoscowDateFromTimestamp(){
		long testMiliSeconds = 1354641768000L;
		
		Date testDate = ThreadPostUtils.getMoscowDateFromTimestamp(testMiliSeconds);
		assertEquals(48, testDate.getSeconds());
		assertEquals(22, testDate.getMinutes());
		assertEquals(17 + 4, testDate.getHours());
	}
	
	public void testGetLocalDateFromTimestamp(){
		long testMiliSeconds = 1354641768000L;
		double offsetInHours = TimeZone.getDefault().getOffset(testMiliSeconds) / 1000.0 / 3600.0 ;
		
		Date testDate = ThreadPostUtils.getLocalDateFromTimestamp(testMiliSeconds);
		assertEquals(48, testDate.getSeconds());
		assertEquals(22, testDate.getMinutes());
		assertEquals(17 + (int)offsetInHours, testDate.getHours());
	}
}
