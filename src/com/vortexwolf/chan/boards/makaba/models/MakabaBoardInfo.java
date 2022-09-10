package com.vortexwolf.chan.boards.makaba.models;

import org.codehaus.jackson.annotate.JsonProperty;


public class MakabaBoardInfo {

    @JsonProperty("bump_limit")
    public int bump_limit;

    @JsonProperty("category")
    public String category;

    @JsonProperty("default_name")
    public String default_name;

    @JsonProperty("enable_likes")
    public boolean enable_likes;

    @JsonProperty("enable_posting")
    public boolean enable_posting;

    @JsonProperty("enable_thread_tags")
    public boolean enable_thread_tags;

    @JsonProperty("id")
    public String id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("max_pages")
    public int pages;

    @JsonProperty("enable_sage")
    public boolean sage;

    @JsonProperty("enable_trips")
    public boolean tripcodes;

    @JsonProperty("icons")
    public MakabaIconInfo[] icons;
}
