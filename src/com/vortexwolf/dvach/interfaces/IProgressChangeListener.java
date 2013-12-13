package com.vortexwolf.dvach.interfaces;

public interface IProgressChangeListener {
    public void progressChanged(long newValue);

    public void indeterminateProgress();
    
    public long getContentLength();
    
    public void setContentLength(long value);
}
