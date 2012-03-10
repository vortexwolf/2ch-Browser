package com.vortexwolf.dvach.activities.boards;

public class BoardEntity implements IBoardListEntity {
	private String code;
	private String title;
		
	public BoardEntity(String code, String title) {
		this.code = code;
		this.title = title;
	}
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public boolean isSection() {
		return false;
	}
}
