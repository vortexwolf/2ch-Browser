package com.vortexwolf.dvach.interfaces;

import com.vortexwolf.dvach.exceptions.SendPostException;
import com.vortexwolf.dvach.models.domain.PostEntity;
import com.vortexwolf.dvach.models.domain.PostFields;

public interface IPostSender {

	/**
	 * Отправляет post запрос на 2ch с каким-либо сообщением
	 * @param boardName Имя доски
	 * @param threadNumber Номер треда
	 * @param fields Названия полей html-формы
	 * @param entity Отправляемое сообщение
	 * @throws SendPostException Выбрасывается в случае неудачной отправки
	 */
	String sendPost(String boardName, String threadNumber, PostFields fields, PostEntity entity) throws SendPostException;
}
