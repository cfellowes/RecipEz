package com.gocery.recipez.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.gocery.recipez.R;
import com.gocery.recipez.data.Item;
import com.gocery.recipez.data.ItemInfo;
import com.gocery.recipez.data.LoadDataListener;
import com.gocery.recipez.model.AddItemAdapter;
import com.gocery.recipez.model.SearchPlaceholderAdapter;
import com.gocery.recipez.model.SearchSuggestionsAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddItemsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, SearchSuggestionsAdapter.OnClickSuggestionListener {

    public static final int REQUEST_CODE = 1;

    private SearchView searchView;
    private RecyclerView suggestionsRecycler;
    private RecyclerView itemsRecycler;
    private HashMap<Item, ItemInfo> items = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_items);

        searchView = findViewById(R.id.search_view_item);
        suggestionsRecycler = findViewById(R.id.recycler_search_suggestions);
        itemsRecycler = findViewById(R.id.recycler_items);

        itemsRecycler.setLayoutManager(new LinearLayoutManager(this));

        addBackButtonToToolbar();
        setUpItemSearch();
    }

    private void addBackButtonToToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setUpItemSearch() {
        searchView.setOnQueryTextListener(this);

        suggestionsRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(Activity.RESULT_CANCELED);
        finish();
        return true;
    }

    @Override
    public void onClickSuggestion(Item item) {
        if (items.containsKey(item)) {
            return;
        }
        searchView.setQuery("", false);

        ItemInfo itemInfo = new ItemInfo();
        itemInfo.unit = item.getUnit();
        items.put(item, itemInfo);

        displayItems();
        enableAddButton();
    }

    private void displayItems() {
        hideDefaultItemText();
        itemsRecycler.setVisibility(View.VISIBLE);
        itemsRecycler.setAdapter(new AddItemAdapter(this, items));
    }

    private void hideDefaultItemText() {
        TextView defaultItemText = findViewById(R.id.text_default_items);
        defaultItemText.setVisibility(View.GONE);
    }

    private void enableAddButton() {
        Button addButton = findViewById(R.id.button_add_items);
        addButton.setEnabled(true);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            setSuggestions(new ArrayList<Item>());
        }
        else {
            setSearching();
            Item.searchByName(newText, new Item.SearchListener() {
                @Override
                public void onSearchResult(String query, List<Item> items) {
                    if (query.equals(searchView.getQuery().toString())) {
                        setSuggestions(items);
                    }
                }
            });
        }
        return true;
    }

    private void setSuggestions(List<Item> items) {
        suggestionsRecycler.setAdapter(new SearchSuggestionsAdapter(this, items, this));
    }

    private void setSearching() {
        suggestionsRecycler.setAdapter(new SearchPlaceholderAdapter(this));
    }

    public void onClickAddItems(View view) {
        Intent intent = new Intent();
        intent.putExtra("items", getSerializableItems());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private HashMap<String, ItemInfo> getSerializableItems() {
        HashMap<String, ItemInfo> serializableItems = new HashMap<>();
        for (Item item : items.keySet()) {
            serializableItems.put(item.getId(), items.get(item));
        }
        return serializableItems;
    }
}
