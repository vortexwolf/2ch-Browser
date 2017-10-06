package ua.in.quireg.chan.boards.makaba.models;

import org.codehaus.jackson.annotate.JsonProperty;


public class MakabaBoardInfo {

    @JsonProperty("bump_limit")
    public String bump_limit;

    @JsonProperty("category")
    public String category;

    @JsonProperty("default_name")
    public String default_name;

    @JsonProperty("enable_likes")
    public int enable_likes;

    @JsonProperty("enable_posting")
    public int enable_posting;

    @JsonProperty("enable_thread_tags")
    public int enable_thread_tags;

    @JsonProperty("id")
    public String id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("pages")
    public int pages;

    @JsonProperty("sage")
    public int sage;

    @JsonProperty("tripcodes")
    public int tripcodes;

    @JsonProperty("icons")
    public MakabaIconInfo[] icons;
}
