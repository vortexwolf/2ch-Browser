package com.vortexwolf.chan.interfaces;

import com.vortexwolf.chan.common.controls.ClickableURLSpan;

import android.view.View;

public interface IURLSpanClickListener {
    void onClick(View v, ClickableURLSpan span, String url);
}
