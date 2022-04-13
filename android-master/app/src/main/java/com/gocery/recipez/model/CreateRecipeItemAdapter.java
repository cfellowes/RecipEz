package com.gocery.recipez.model;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.gocery.recipez.R;
import com.gocery.recipez.data.Item;
import com.gocery.recipez.data.ItemInfo;
import com.gocery.recipez.data.LoadDataListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CreateRecipeItemAdapter extends RecyclerView.Adapter<CreateRecipeItemAdapter.ItemRow> {

    public interface QuantityListener {
        void onQuantityChanged(String itemId, double quantity);
    }

    public interface DeleteListener {
        void onItemDeleted(String itemId);
    }

    private LayoutInflater inflater;
    private QuantityListener quantityListener;
    private DeleteListener deleteListener;
    private List<String> itemIds = new ArrayList<>();
    private List<ItemInfo> itemInfo = new ArrayList<>();

    public CreateRecipeItemAdapter(Context context, HashMap<String, ItemInfo> items, QuantityListener quantityListener, DeleteListener deleteListener) {
        inflater = LayoutInflater.from(context);

        this.quantityListener = quantityListener;
        this.deleteListener = deleteListener;

        for (String itemId : items.keySet()) {
            itemIds.add(itemId);
            itemInfo.add(items.get(itemId));
        }
    }

    @NonNull
    @Override
    public ItemRow onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemRow(inflater.inflate(R.layout.row_create_recipe_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemRow holder, int position) {
        holder.unit.setText(itemInfo.get(position).unit);
        String quantityText = "" + itemInfo.get(position).quantity;
        holder.quantity.setText(quantityText);
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

    class ItemRow extends RecyclerView.ViewHolder implements View.OnClickListener, TextWatcher {
        private TextView name;
        private TextView unit;
        private EditText quantity;
        private String itemId;

        ItemRow(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.text_item_name);
            unit = itemView.findViewById(R.id.text_unit);
            quantity = itemView.findViewById(R.id.edit_text_quantity);

            quantity.addTextChangedListener(this);

            View deleteButton = itemView.findViewById(R.id.button_delete);
            deleteButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.button_delete) {
                deleteListener.onItemDeleted(itemId);
            }
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            double quantity = 0;
            try {
                quantity = Double.parseDouble(charSequence.toString());
            }
            catch (NumberFormatException ignore) { }
            quantityListener.onQuantityChanged(itemId, quantity);
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }
}
