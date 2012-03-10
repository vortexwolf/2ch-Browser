package com.vortexwolf.dvach.interfaces;

import com.vortexwolf.dvach.api.JsonApiReaderException;
import com.vortexwolf.dvach.api.entities.BoardSettings;

public interface IBoardSettingsStorage {

	/** Загружает настройки борды из интернета или берет из кэша, если уже загружены */
	public BoardSettings getSettings(String boardName) throws JsonApiReaderException;
}