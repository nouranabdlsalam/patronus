package com.example.patronus;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ChatMessage> chatMessages;
    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }
    @Override
    public int getItemViewType(int position) {
        return chatMessages.get(position).getType();
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int
            viewType) {
        if (viewType == ChatMessage.TYPE_USER) {
            View view =
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_user, parent, false);
            return new MessageViewHolder(view);
        } else {
            View view =
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_bot, parent, false);
            return new MessageViewHolder(view);
        }
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((MessageViewHolder)
                holder).messageText.setText(chatMessages.get(position).getMessage());
    }
    @Override
    public int getItemCount() {
        return chatMessages.size();
    }
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
        }
    }
}