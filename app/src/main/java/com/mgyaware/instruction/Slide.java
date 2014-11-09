package com.mgyaware.instruction;

public class Slide extends BaseOrder {
    public String content;
    public String picture;
    public String video;

    public Slide(String title, int order, String content, String picture, String video) {
        this.title = title;
        this.order = order;
        this.content = content;
        this.picture = picture;
        this.video = video;
    }
}
