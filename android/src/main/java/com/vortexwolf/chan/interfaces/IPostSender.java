package com.vortexwolf.chan.interfaces;

import com.vortexwolf.chan.models.domain.SendPostModel;
import com.vortexwolf.chan.models.domain.SendPostResult;

public interface IPostSender {
    SendPostResult sendPost(String boardName, SendPostModel entity);
}
