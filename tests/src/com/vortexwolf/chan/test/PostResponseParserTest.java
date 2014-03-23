package com.vortexwolf.chan.test;

import android.test.InstrumentationTestCase;

import com.vortexwolf.chan.exceptions.SendPostException;
import com.vortexwolf.chan.services.domain.PostResponseParser;

public class PostResponseParserTest extends InstrumentationTestCase {

    private final PostResponseParser mParser = new PostResponseParser();

    public void testAddPostSuccess() throws SendPostException {
        String response = "Reload the page to get source for: http://2ch.so/test/wakaba.pl";
        boolean result = this.mParser.isPostSuccessful(response);

        assertTrue(result);
    }

    public void testAddWithoutComment() {
        String response = "<hr style=\"clear: left;\">" + "<center>" + "<strong><font size=\"5\">Ошибка: Вы ничего не написали в сообщении.</strong></font><br />" + "<h2 style=\"text-align: center\">" + "<a href=\"http://2ch.so/test/res/52984.html\">Назад</a><br />" + "</h2>" + "</center>";

        String message = null;
        try {
            this.mParser.isPostSuccessful(response);
        } catch (SendPostException e) {
            message = e.getMessage();
        }

        assertEquals(message, "Ошибка: Вы ничего не написали в сообщении.");
    }

    public void testAddThreadWithoutFile() {
        String response = "<hr style=\"clear: left;\"><center><strong><font size=\"5\">Ошибка: В этом разделе для начала треда нужно загрузить файл.</strong></font><br /><h2 style=\"text-align: center\"><a href=\"http://2ch.so/test/\">Назад</a><br /></h2></center><br /><br />";

        String message = null;
        try {
            this.mParser.isPostSuccessful(response);
        } catch (SendPostException e) {
            message = e.getMessage();
        }

        assertEquals(message, "Ошибка: В этом разделе для начала треда нужно загрузить файл.");
    }

    public void testAddWithoutCaptcha() {
        String response = "<center><strong><font size=\"5\">Вероятно, вы забыли ввести капчу для отправки сообщений. Обновите страницу и кликните в поле - Подтверждение</strong></font><br /><h2 style=\"text-align: center\"><a href=\"http://2ch.so/test/\">Назад</a><br /></h2></center>";

        String message = null;
        try {
            this.mParser.isPostSuccessful(response);
        } catch (SendPostException e) {
            message = e.getMessage();
        }

        assertEquals(message, "Вероятно, вы забыли ввести капчу для отправки сообщений. Обновите страницу и кликните в поле - Подтверждение");
    }

    public void testAddIncorrectCaptcha() {
        String response = "<center><strong><font size=\"5\">Ошибка: Неверный код подтверждения.</strong></font><br /><h2 style=\"text-align: center\"><a href=\"http://2ch.so/test/\">Назад</a><br /></h2></center>";

        String message = null;
        try {
            this.mParser.isPostSuccessful(response);
        } catch (SendPostException e) {
            message = e.getMessage();
        }

        assertEquals(message, "Ошибка: Неверный код подтверждения.");
    }

    public void testAddExistingFile() {
        String response = "<center><strong><font size=\"5\">Ошибка: Этот файл уже был загружен <a href=\"/test/res/50701.html\">здесь</a>.</strong></font><br /><h2 style=\"text-align: center\"><a href=\"http://2ch.so/test/\">Назад</a><br /></h2></center>";

        String message = null;
        try {
            this.mParser.isPostSuccessful(response);
        } catch (SendPostException e) {
            message = e.getMessage();
        }

        assertEquals(message, "Ошибка: Этот файл уже был загружен здесь.");
    }

    public void testAddLargeFile() {
        String response = "<center><strong><font size=\"5\">Объем или разрешение вашего изображения превышает допустимое для данного раздела.<br />Для размещения подобных изображений вы можете воспользоваться разделом <a href=\"/hr/\">/hr/</a>.</strong></font><br /><h2 style=\"text-align: center\"><a href=\"http://2ch.so/test/\">Назад</a><br /></h2></center>";

        String message = null;
        try {
            this.mParser.isPostSuccessful(response);
        } catch (SendPostException e) {
            message = e.getMessage();
        }

        assertEquals(message, "Объем или разрешение вашего изображения превышает допустимое для данного раздела.Для размещения подобных изображений вы можете воспользоваться разделом /hr/.");
    }

    public void testAddSecondThread() {
        String response = "<center><strong><font size=\"5\">Ошибка: Вы уже создали один тред, подождите 30 минут перед созданием нового.</strong></font><br /><h2 style=\"text-align: center\"><a href=\"http://2ch.so/test/\">Назад</a><br /></h2></center>";

        String message = null;
        try {
            this.mParser.isPostSuccessful(response);
        } catch (SendPostException e) {
            message = e.getMessage();
        }

        assertEquals(message, "Ошибка: Вы уже создали один тред, подождите 30 минут перед созданием нового.");
    }

    public void testMultipleCenterBlocks() {
        String response = "<center>Irrelevant text<a href=\"/me/\" title=\"/me/\"><img style='border: 0px none;' src=\"/../ololo/me_1.png\"></a></center>" + "<center>Real error.<a href=\"http://2ch.so/test/\">Назад</a><br /></center>" + "<center>Irrelevant text<a></a></center>";

        String message = null;
        try {
            this.mParser.isPostSuccessful(response);
        } catch (SendPostException e) {
            message = e.getMessage();
        }

        assertEquals(message, "Real error.");
    }
}
