package com.vortexwolf.dvach.common.view;

public class MyLeadingMarginSpan2Wrapper {
	   private MyLeadingMarginSpan2 mInstance;

	   /* class initialization fails when this throws an exception */
	   static {
	       try {
	    	   Class.forName("android.text.style.LeadingMarginSpan$LeadingMarginSpan2");
	       } catch (Exception ex) {
	           throw new RuntimeException(ex);
	       }
	   }

	   /* calling here forces class initialization */
	   public static void checkAvailable() {}

	   public MyLeadingMarginSpan2Wrapper(int lines, int margin) {
	       mInstance = new MyLeadingMarginSpan2(lines, margin);
	   }
	   
	   public MyLeadingMarginSpan2 getInstance(){
		   return mInstance;
	   }
}
