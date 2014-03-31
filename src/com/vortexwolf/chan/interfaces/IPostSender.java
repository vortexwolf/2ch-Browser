package com.vortexwolf.chan.interfaces;

import com.vortexwolf.chan.exceptions.SendPostException;
import com.vortexwolf.chan.models.domain.SendPostModel;

public interface IPostSender {
    String sendPost(String boardName, SendPostModel entity) throws SendPostException;
}
