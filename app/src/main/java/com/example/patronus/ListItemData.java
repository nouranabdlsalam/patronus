package com.example.patronus;

public class ListItemData{
    int icon;
    String text;
    int trash;
    boolean add;

    public ListItemData(int icon, String text, int trash, boolean add){
        this.icon = icon;
        this.text = text;
        this.trash = trash;
        this.add = add;
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
