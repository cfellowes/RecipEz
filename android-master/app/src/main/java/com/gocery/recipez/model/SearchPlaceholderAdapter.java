package com.gocery.recipez.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gocery.recipez.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SearchPlaceholderAdapter extends RecyclerView.Adapter<SearchPlaceholderAdapter.Placeholder> {

    private LayoutInflater inflater;

    public SearchPlaceholderAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public Placeholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Placeholder(inflater.inflate(R.layout.row_item_suggestion, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Placeholder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 1;
    }

    class Placeholder extends RecyclerView.ViewHolder {

        Placeholder(@NonNull View itemView) {
            super(itemView);

            ((TextView) itemView.findViewById(R.id.text_item_name)).setText(R.string.text_searching);
        }
    }
}
