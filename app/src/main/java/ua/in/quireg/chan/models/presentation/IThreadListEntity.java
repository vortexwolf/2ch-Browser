package ua.in.quireg.chan.models.presentation;

/**
 * Created by Arcturus Mengsk on 3/17/2018, 6:08 AM.
 * 2ch-Browser
 */

public interface IThreadListEntity {

    enum Type{
        DIVIDER,
        THREAD,
        HIDDEN_THREAD
    }

    Type getType();
}
