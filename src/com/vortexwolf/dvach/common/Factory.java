package com.vortexwolf.dvach.common;

public class Factory {

	private static Container sContainer = new Container();
	
	public static Container getContainer(){
		return sContainer;
	}
}
