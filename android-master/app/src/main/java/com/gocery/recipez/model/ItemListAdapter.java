package com.gocery.recipez.model;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gocery.recipez.R;
import com.gocery.recipez.activities.PantryItemActivity;
import com.gocery.recipez.data.Item;
import com.gocery.recipez.data.ItemInfo;
import com.gocery.recipez.data.LoadDataListener;
import com.gocery.recipez.data.Pantry;

import java.util.ArrayList;
import java.util.List;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ItemRow> {

    private Context context;
    private Pantry pantry;
    private LayoutInflater inflater;
    private List<String> itemIds = new ArrayList<>();
    private List<ItemInfo> items = new ArrayList<>();

    public ItemListAdapter(Context context, Pantry pantry) {
        inflater = LayoutInflater.from(context);

        this.context = context;
        this.pantry = pantry;

        for (String key : pantry.getItems().keySet()) {
            itemIds.add(key);
            this.items.add(pantry.getItems().get(key));
        }
    }

    @NonNull
    @Override
    public ItemRow onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemRow(inflater.inflate(R.layout.row_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemRow item, int pos) {
        item.itemId = itemIds.get(pos);
        item.itemInfo = items.get(pos);

        Item.loadItemById(itemIds.get(pos), new LoadDataListener<Item>() {
            @Override
            public void onLoad(Item payload) {
                item.name.setText(payload.getName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ItemRow extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        String itemId;
        ItemInfo itemInfo;

        ItemRow(View view) {
            super(view);

            name = view.findViewById(R.id.text_item_name);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(context, PantryItemActivity.class);
            intent.putExtra("itemId", itemId);
            intent.putExtra("itemInfo", itemInfo);
            intent.putExtra("pantryId", pantry.getId());
            context.startActivity(intent);
        }
    }
}
