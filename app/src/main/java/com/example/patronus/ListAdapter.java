package com.example.patronus;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListViewHolder> {
    private List<ListItemData> listItemData;

    public ListAdapter(List<ListItemData> listItemData){
        this.listItemData = listItemData;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_card, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        ListItemData listItem = listItemData.get(position);
        holder.icon.setImageResource(listItem.getIcon());
        holder.text.setText(listItem.getText());
        holder.trash.setImageResource(listItem.getTrash());
        holder.trash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return listItemData.size();
    }
}
