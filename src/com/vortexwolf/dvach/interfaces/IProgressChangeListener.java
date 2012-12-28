package com.vortexwolf.dvach.interfaces;

public interface IProgressChangeListener {
    public void progressChanged(long newValue);

    public void indeterminateProgress();

    public void setContentLength(long value);
}
