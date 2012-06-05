package com.vortexwolf.dvach.services.presentation;

import java.util.HashMap;

import com.vortexwolf.dvach.exceptions.JsonApiReaderException;
import com.vortexwolf.dvach.interfaces.IBoardSettingsStorage;
import com.vortexwolf.dvach.interfaces.IJsonApiReader;
import com.vortexwolf.dvach.models.domain.BoardSettings;

public class BoardSettingsStorage implements IBoardSettingsStorage {

	private final IJsonApiReader mReader;
	private final HashMap<String, BoardSettings> mBoardSettings;
	
	public BoardSettingsStorage(IJsonApiReader reader){
		this.mReader = reader;
		this.mBoardSettings = new HashMap<String, BoardSettings>();
	}

	@Override
	public BoardSettings getSettings(String boardName) throws JsonApiReaderException{
		BoardSettings bs = this.mBoardSettings.get(boardName);
		if(bs == null){
			bs = this.mReader.readBoardSettings(boardName);
			this.mBoardSettings.put(boardName, bs);
		}
		
		return bs;
	}
}
