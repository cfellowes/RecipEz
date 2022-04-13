package com.gocery.recipez.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gocery.recipez.R;
import com.gocery.recipez.data.Item;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SearchSuggestionsAdapter extends RecyclerView.Adapter<SearchSuggestionsAdapter.SuggestionRow> {

    public interface OnClickSuggestionListener {
        void onClickSuggestion(Item item);
    }

    private LayoutInflater inflater;
    private List<Item> items;
    private OnClickSuggestionListener listener;

    public SearchSuggestionsAdapter(Context context, List<Item> items, OnClickSuggestionListener listener) {
        inflater = LayoutInflater.from(context);
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SuggestionRow onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SuggestionRow(inflater.inflate(R.layout.row_item_suggestion, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionRow holder, int position) {
        holder.item = items.get(position);
        holder.name.setText(holder.item.getName());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class SuggestionRow extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView name;
        private Item item;

        SuggestionRow(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.text_item_name);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onClickSuggestion(item);
        }
    }
}
