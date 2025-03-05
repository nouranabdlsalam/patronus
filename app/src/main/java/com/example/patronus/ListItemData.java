package com.example.patronus;

import android.graphics.drawable.Drawable;

public class ListItemData {
    int icon;
    String text;
    int trash;

    public ListItemData(int icon, String text, int trash){
        this.icon = icon;
        this.text = text;
        this.trash = trash;
    }

    public int getIcon() {
        return icon;
    }

    public String getText() {
        return text;
    }

    public int getTrash() {
        return trash;
    }
}
