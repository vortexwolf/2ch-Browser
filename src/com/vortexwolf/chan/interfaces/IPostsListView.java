package com.vortexwolf.chan.interfaces;

import com.vortexwolf.chan.boards.dvach.models.DvachPostsList;
import com.vortexwolf.chan.models.domain.PostModel;

public interface IPostsListView extends IListView<PostModel[]> {

    /**
     * Вызывается в случае успешной загрузки данных при частичном обновлении
     * 
     * @param from
     *            Пост, с которого начинается список ответов
     * @param list
     *            Список ответов
     */
    void updateData(String from, PostModel[] posts);

    /**
     * Вызывается в случае ошибки при частичном обновлении
     * 
     * @param error
     *            Текст ошибки
     */
    void showUpdateError(String error);

    /**
     * Показывает индикатор загрузки при частичном обновлении
     */
    void showUpdateLoading();

    /**
     * Прячет индикатор загрузки при частичном обновлении
     */
    void hideUpdateLoading();
}
