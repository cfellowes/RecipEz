package com.gocery.recipez.model;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.gocery.recipez.R;
import com.gocery.recipez.data.Item;
import com.gocery.recipez.data.ItemInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AddItemAdapter extends RecyclerView.Adapter<AddItemAdapter.ItemRow> {

    private LayoutInflater inflater;
    private HashMap<Item, ItemInfo> itemMap;
    private List<Item> items = new ArrayList<>();

    public AddItemAdapter(Context context, HashMap<Item, ItemInfo> itemMap) {
        inflater = LayoutInflater.from(context);

        this.itemMap = itemMap;

        items.addAll(itemMap.keySet());
    }

    @NonNull
    @Override
    public ItemRow onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemRow(inflater.inflate(R.layout.row_add_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemRow holder, int position) {
        holder.item = items.get(position);
        holder.name.setText(holder.item.getName());
        ItemInfo itemInfo = itemMap.get(holder.item);
        if (itemInfo != null) {
            String quantity = "" + itemInfo.quantity;
            holder.quantity.setText(quantity);
            holder.unit.setText(itemInfo.unit);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ItemRow extends RecyclerView.ViewHolder implements TextWatcher {

        private TextView name;
        private EditText quantity;
        private TextView unit;
        private Item item;

        ItemRow(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.text_item_name);
            quantity = itemView.findViewById(R.id.edit_text_quantity);
            unit = itemView.findViewById(R.id.text_unit);

            quantity.addTextChangedListener(this);
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            ItemInfo itemInfo = itemMap.get(item);
            if (itemInfo != null) {
                if (TextUtils.isEmpty(charSequence)) {
                    itemInfo.quantity = 0;
                }
                else {
                    itemInfo.quantity = Double.parseDouble(charSequence.toString());
                }
                itemMap.put(item, itemInfo);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) { }
    }
}
