package com.example.patronus;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ListViewHolder extends RecyclerView.ViewHolder {
    ImageView icon;
    TextView text;
    ImageButton trash;

    public ListViewHolder(View itemView){
        super(itemView);

        icon = itemView.findViewById(R.id.list_icon);
        text = itemView.findViewById(R.id.list_text);
        trash = itemView.findViewById(R.id.trash_btn);
    }

}
