package com.gocery.recipez.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gocery.recipez.R;
import com.gocery.recipez.data.ItemInfo;
import com.gocery.recipez.data.LoadDataListener;
import com.gocery.recipez.data.Recipe;
import com.gocery.recipez.data.User;
import com.gocery.recipez.model.CreateRecipeItemAdapter;

import java.util.HashMap;

public class CreateRecipeActivity extends AppCompatActivity implements CreateRecipeItemAdapter.DeleteListener, CreateRecipeItemAdapter.QuantityListener, TextWatcher {

    private RecyclerView itemRecycler;
    private EditText recipeName;
    private EditText instructionsText;
    private Button createRecipeButton;
    private HashMap<String, ItemInfo> items = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);

        createRecipeButton = findViewById(R.id.button_create_recipe);
        instructionsText = findViewById(R.id.edit_text_instructions);
        recipeName = findViewById(R.id.edit_text_recipe_name);
        recipeName.addTextChangedListener(this);

        setUpToolbar();
        setUpRecycler();
    }

    private void setUpToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    private void setUpRecycler() {
        itemRecycler = findViewById(R.id.recycler_items);
        itemRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    private void refreshRecycler() {
        itemRecycler.setAdapter(new CreateRecipeItemAdapter(this, items, this, this));
        refreshCreateButton();
    }

    private void refreshCreateButton() {
        if (TextUtils.isEmpty(recipeName.getText()) || items.size() == 0) {
            createRecipeButton.setEnabled(false);
        }
        else {
            createRecipeButton.setEnabled(true);
        }
    }

    public void onClickAddItems(View view) {
        Intent intent = new Intent(this, AddItemsActivity.class);
        startActivityForResult(intent, AddItemsActivity.REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AddItemsActivity.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            @SuppressWarnings("unchecked")
            HashMap<String, ItemInfo> addedItems = (HashMap<String, ItemInfo>) data.getSerializableExtra("items");
            if (addedItems != null) {
                addItems(addedItems);
                refreshRecycler();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addItems(HashMap<String, ItemInfo> addedItems) {
        for (String itemId : addedItems.keySet()) {
            ItemInfo itemInfo = items.get(itemId);
            if (itemInfo != null) {
                ItemInfo addedItemInfo = addedItems.get(itemId);
                if (addedItemInfo != null) {
                    itemInfo.quantity += addedItemInfo.quantity;
                }
            }
            else {
                itemInfo = addedItems.get(itemId);
            }
            items.put(itemId, itemInfo);
        }
    }

    @Override
    public void onQuantityChanged(String itemId, double quantity) {
        ItemInfo itemInfo = items.get(itemId);
        if (itemInfo != null) {
            itemInfo.quantity = quantity;
            items.put(itemId, itemInfo);
        }
    }

    @Override
    public void onItemDeleted(String itemId) {
        items.remove(itemId);
        refreshRecycler();
    }

    @Override
    public void afterTextChanged(Editable editable) {
        refreshCreateButton();
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

    public void onClickCreateRecipe(View view) {
        disableCreateButton();
        String name = recipeName.getText().toString();
        String instructions = instructionsText.getText().toString();

        Recipe.createRecipe(name, items, instructions, new LoadDataListener<Recipe>() {
            @Override
            public void onLoad(Recipe payload) {
                User.getInstance().addRecipe(payload, new LoadDataListener<Boolean>() {
                    @Override
                    public void onLoad(Boolean payload) {
                        finish();
                    }
                });
            }
        });
    }

    private void disableCreateButton() {
        createRecipeButton.setEnabled(false);
        createRecipeButton.setText(R.string.button_create_in_progress);
    }
}
