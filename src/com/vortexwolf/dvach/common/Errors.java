package com.vortexwolf.dvach.common;

import com.vortexwolf.dvach.R;

import android.content.res.Resources;

public class Errors {
	private final Resources resources;
	
	public Errors(Resources res){
		this.resources = res;
	}

	public String getError(int id){
		return resources.getString(id);
	}
	
	public String getSendPostError(){
		return this.getError(R.string.error_send_post);
	}
	
	public String getDownloadDataError(){
		return this.getError(R.string.error_download_data);
	}
	
	public String getUnknownError(){
		return this.getError(R.string.error_unknown);
	}
	
	public String getIncorrectArgumentError(){
		return this.getError(R.string.error_incorrect_argument);
	}
	
	public String getJsonParseError(){
		return this.getError(R.string.error_json_parse);
	}

	public String getSaveFileError(){
		return this.getError(R.string.error_save_file);
	}
	
	public String getFileExistError(){
		return this.getError(R.string.error_file_exist);
	}
}
