package com.example.clipboardkeyboard.data;

public class ClipItem {
    public int id;
    public String title;
    public String content;
    public String tag;
    public int color;
    public int displayOrder;

    public ClipItem(int id, String title, String content, String tag, int color, int displayOrder) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.tag = tag;
        this.color = color;
        this.displayOrder = displayOrder;
    }
}
