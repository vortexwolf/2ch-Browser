package com.vortexwolf.dvach.interfaces;

import android.content.DialogInterface;

public interface IProgressView {

	public void setProgress(int value);

	public void setMax(int value);

	public void show();

	public void hide();

	public void setOnCancelListener(final DialogInterface.OnCancelListener listener);

}