package com.vortexwolf.dvach.interfaces;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

public interface INavigationService {

	/** Opens the specified uri either inside the application on in the external browser */
	public abstract void navigate(Uri uri, Context context);

	public abstract void navigate(Uri uri, Context context, Bundle extras);

}