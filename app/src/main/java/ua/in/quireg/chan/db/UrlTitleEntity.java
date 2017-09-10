package ua.in.quireg.chan.db;

import ua.in.quireg.chan.common.Websites;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.common.utils.UriUtils;
import ua.in.quireg.chan.interfaces.IUrlBuilder;

public class UrlTitleEntity {
    private long id;
    private String website;
    private String board;
    private String thread;
    private String title;

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

    public String getWebsite() {
        return this.website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board = board;
    }

    public String getThread() {
        return thread;
    }

    public void setThread(String thread) {
        this.thread = thread;
    }

    public String buildUrl() {
        IUrlBuilder builder = Websites.fromName(this.website).getUrlBuilder();
        return UriUtils.getBoardOrThreadUrl(builder, this.board, 0, this.thread);
    }

    public String getTitleOrDefault() {
        if (!StringUtils.isEmpty(this.title)) {
            return this.title;
        } else if (StringUtils.isEmpty(this.thread)) {
            return this.board;
        } else {
            return this.board + "/" + this.thread;
        }
    }
}
