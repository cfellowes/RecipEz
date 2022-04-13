package com.gocery.recipez.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gocery.recipez.R;
import com.gocery.recipez.data.Item;
import com.gocery.recipez.data.ItemInfo;
import com.gocery.recipez.data.LoadDataListener;
import com.gocery.recipez.data.Pantry;
import com.gocery.recipez.data.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecipeItemAdapter extends RecyclerView.Adapter<RecipeItemAdapter.ItemRow> {

    private LayoutInflater inflater;
    private Context context;
    private List<String> itemIds = new ArrayList<>();
    private List<ItemInfo> itemInfo = new ArrayList<>();

    public RecipeItemAdapter(Context context, HashMap<String, ItemInfo> items) {
        inflater = LayoutInflater.from(context);
        this.context = context;

        for (String itemId : items.keySet()) {
            itemIds.add(itemId);
            itemInfo.add(items.get(itemId));
        }
    }

    @NonNull
    @Override
    public ItemRow onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemRow(inflater.inflate(R.layout.row_recipe_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemRow holder, final int position) {
        final String itemId = itemIds.get(position);
        final ItemInfo itemInfo = this.itemInfo.get(position);

        if (itemInfo.quantity > 0) {
            String quantity = "" + itemInfo.quantity;
            holder.quantity.setText(quantity);
            holder.unit.setText(itemInfo.unit);
        }

        Item.loadItemById(itemId, new LoadDataListener<Item>() {
            @Override
            public void onLoad(Item payload) {
                holder.name.setText(payload.getName());
            }
        });

        holder.name.setTextColor(context.getColor(R.color.colorNone));
        User.getInstance().loadActivePantry(new LoadDataListener<Pantry>() {
            @Override
            public void onLoad(Pantry payload) {
                switch (payload.getItemAmount(itemId, itemInfo.quantity)) {
                    case (Pantry.ENOUGH):
                        holder.name.setTextColor(context.getColor(R.color.colorEnough));
                        break;
                    case (Pantry.NOT_ENOUGH):
                        holder.name.setTextColor(context.getColor(R.color.colorNotEnough));
                        break;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemIds.size();
    }

    class ItemRow extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView quantity;
        private TextView unit;

        ItemRow(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.text_item_name);
            quantity = itemView.findViewById(R.id.text_quantity);
            unit = itemView.findViewById(R.id.text_unit);
        }
    }
}
