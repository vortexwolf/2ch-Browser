package com.vortexwolf.chan.interfaces;

public interface IPostSendView {

    void showSuccess(String redirectedPage);

    void showError(String error);

    void showPostLoading();

    void hidePostLoading();
}
