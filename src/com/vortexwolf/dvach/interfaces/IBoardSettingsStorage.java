package com.vortexwolf.dvach.interfaces;

import com.vortexwolf.dvach.exceptions.JsonApiReaderException;
import com.vortexwolf.dvach.models.domain.BoardSettings;

public interface IBoardSettingsStorage {

	/** Загружает настройки борды из интернета или берет из кэша, если уже загружены */
	public BoardSettings getSettings(String boardName) throws JsonApiReaderException;
}