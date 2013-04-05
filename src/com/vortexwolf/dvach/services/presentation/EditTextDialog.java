package com.vortexwolf.dvach.services.presentation;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.activities.AddPostActivity;

/** A dialog with a text input. */
public class EditTextDialog {

	private final Context mContext;
	private final AlertDialog.Builder mBuilder;
	
	private String mHint;
	private String mTitle;
	private View.OnClickListener mPositiveButtonListener;
	private AlertDialog mDialog;
	private EditText mEditTextView;
	
	public EditTextDialog(Context context){
		this.mContext = context;
		this.mBuilder = new AlertDialog.Builder(context);
	}
	
	public void setTitle(String title){
		this.mTitle = title;
	}
	
	public void setHint(String hint){
		this.mHint = hint;
	}
	
	public void setPositiveButtonListener(View.OnClickListener listener){
		this.mPositiveButtonListener = listener;
	}
	
	public String getText(){
		return this.mEditTextView.getText().toString();
	}
	
	public void show(){
        View view = this.createView();

        this.mDialog = this.mBuilder
        		.setTitle(this.mTitle)
				.setNegativeButton(this.mContext.getString(R.string.cancel), null)
				.setPositiveButton(this.mContext.getString(R.string.ok), null)
				.setView(view)
				.create();

        this.mDialog.show();
        
        Button b = this.mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        b.setOnClickListener(EditTextDialog.this.mPositiveButtonListener);
	}
	
	public void dismiss(){
		this.mDialog.dismiss();
	}
	
	private View createView(){
        this.mEditTextView = new EditText(this.mContext);
        this.mEditTextView.setHint(this.mHint);
        
        FrameLayout rootLayout = new FrameLayout(this.mContext);
        rootLayout.setPadding(10, 0, 10, 0);
        rootLayout.addView(this.mEditTextView);
        
        return rootLayout;
	}
}
