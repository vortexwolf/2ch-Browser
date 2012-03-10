package com.vortexwolf.dvach.interfaces;

public interface IProgressChangeListener {
	public void progressChanged(long oldValue, long newValue);
	public void setContentLength(long value);
}
