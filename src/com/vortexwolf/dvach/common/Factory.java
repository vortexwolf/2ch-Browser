package com.vortexwolf.dvach.common;

public class Factory {

	private static final Container sContainer = new Container();
	
	public static Container getContainer(){
		return sContainer;
	}
}
