package com.vortexwolf.chan.interfaces;

public interface ICheckPasscodeView {
    void onPasscodeRemoved();
    void onPasscodeChecked(boolean isSuccess, String errorMessage);
}
