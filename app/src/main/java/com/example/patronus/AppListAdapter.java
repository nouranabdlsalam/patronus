package com.example.patronus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<ListViewHolder> {
    private List<App> appItemData;

    private Context context;

    public AppListAdapter(Context context, List<App> appItemData){
        this.context = context;
        this.appItemData = appItemData;
    }

    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_card, parent, false);
        return new ListViewHolder(view);
    }

    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        App appItem = appItemData.get(position);
        holder.icon.setImageDrawable(appItem.getAppIcon(context));
        holder.text.setText(appItem.getName());
        if (appItem.getIcon() == 0){
            holder.trash.setImageResource(R.drawable.add);
        }
        else if(appItem.getIcon() == 2){
            holder.trash.setImageResource(R.drawable.next);
        }
        holder.trash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appItem.getIcon() == 0) {
                    SelectAppActivity.setSelectedApps(appItem.getPackageName(), appItem.getName(), 0);
                    holder.trash.setImageResource(R.drawable.trash);
                    appItem.setIcon(1);
                }
                else if (appItem.getIcon() == 1){
                    SelectAppActivity.setSelectedApps(appItem.getPackageName(), appItem.getName(), 1);
                    holder.trash.setImageResource(R.drawable.add);
                    appItem.setIcon(0);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return appItemData.size();
    }

}
