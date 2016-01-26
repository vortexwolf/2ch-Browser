package com.vortexwolf.chan.interfaces;

public interface ICheckCaptchaView {
    void beforeCheck();

    void showSuccess();
    
    void showError(String message);
}
