package com.gocery.recipez.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gocery.recipez.R;
import com.gocery.recipez.data.Item;
import com.gocery.recipez.data.ItemSuggestion;
import com.gocery.recipez.data.LoadDataListener;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ItemSuggestionAdapter extends RecyclerView.Adapter<ItemSuggestionAdapter.ItemRow> {

    public interface OnItemClickListener {
        void onItemClick(String itemId);
    }

    private LayoutInflater inflater;
    private List<String> itemIds = new ArrayList<>();
    private OnItemClickListener listener;

    public ItemSuggestionAdapter(Context context, TreeSet<ItemSuggestion> suggestions, OnItemClickListener listener) {
        inflater = LayoutInflater.from(context);
        this.listener = listener;

        for (ItemSuggestion suggestion : suggestions) {
            itemIds.add(suggestion.itemId);
        }
    }

    @NonNull
    @Override
    public ItemRow onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemRow(inflater.inflate(R.layout.row_item_suggestion, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemRow holder, int position) {
        holder.itemId = itemIds.get(position);

        Item.loadItemById(holder.itemId, new LoadDataListener<Item>() {
            @Override
            public void onLoad(Item payload) {
                holder.name.setText(payload.getName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemIds.size();
    }

    class ItemRow extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView name;
        private String itemId;

        ItemRow(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.text_item_name);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onItemClick(itemId);
        }
    }
}
