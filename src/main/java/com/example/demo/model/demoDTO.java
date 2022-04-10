package com.example.demo.model;

public class demoDTO {
    public String lastBuildDate;
    public int total;
    public int start;
    public int display;
    public Item[] items;

    class Item {
        public String title;
        public String bloggerlink;
        public String bloggername;
        public String description;
        public String link;
        public String postdate;
    }
}
