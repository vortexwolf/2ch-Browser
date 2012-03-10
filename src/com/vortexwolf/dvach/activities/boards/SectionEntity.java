package com.vortexwolf.dvach.activities.boards;

public class SectionEntity implements IBoardListEntity {
	private final String mTitle;
	
	public SectionEntity(String title) {
		this.mTitle = title;
	}
	
	public String getTitle(){
		return mTitle;
	}

	@Override
	public boolean isSection() {
		return true;
	}
}
