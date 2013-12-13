package com.vortexwolf.dvach.interfaces;

import com.vortexwolf.dvach.models.domain.PostsList;

public interface IPostsListView extends IListView<PostsList> {

    /**
     * Вызывается в случае успешной загрузки данных при частичном обновлении
     * 
     * @param from
     *            Пост, с которого начинается список ответов
     * @param list
     *            Список ответов
     */
    void updateData(String from, PostsList list);

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
