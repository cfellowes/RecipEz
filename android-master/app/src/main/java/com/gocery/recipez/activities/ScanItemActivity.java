package com.gocery.recipez.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.gocery.recipez.R;
import com.gocery.recipez.data.Item;
import com.gocery.recipez.data.LoadDataListener;
import com.gocery.recipez.data.ScanResult;
import com.gocery.recipez.model.ItemSuggestionAdapter;
import com.gocery.recipez.model.SearchPlaceholderAdapter;
import com.gocery.recipez.model.SearchSuggestionsAdapter;

import java.util.ArrayList;
import java.util.List;

public class ScanItemActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, SearchSuggestionsAdapter.OnClickSuggestionListener, TextWatcher, ItemSuggestionAdapter.OnItemClickListener {

    private SearchView searchView;
    private RecyclerView suggestionsRecycler;
    private ScanResult scanResult;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_item);

        searchView = findViewById(R.id.search_view_item);
        suggestionsRecycler = findViewById(R.id.recycler_search_suggestions);

        addBackButtonToToolbar();

        getIndex();
        getScanResult();
        setUpItemSearch();
        setItemViews();
        setItemSuggestions();
    }

    private void addBackButtonToToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        saveData();
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        saveData();
        super.onBackPressed();
    }

    private void saveData() {
        Intent intent = new Intent();
        intent.putExtra("index", index);
        intent.putExtra("scanResult", scanResult);
        setResult(ScanResultsActivity.RESULT_UPDATE, intent);
    }

    private void getIndex() {
        index = getIntent().getIntExtra("index", 0);
    }

    private void getScanResult() {
        scanResult = (ScanResult) getIntent().getSerializableExtra("scanResult");
    }

    private void setUpItemSearch() {
        searchView.setOnQueryTextListener(this);

        suggestionsRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setItemViews() {
        setItemName();
        setItemQuantity();
        setItemUnit();
    }

    private void setItemName() {
        TextView itemName = findViewById(R.id.text_item_name);
        itemName.setText(scanResult.name);
    }

    private void setItemQuantity() {
        EditText quantity = findViewById(R.id.edit_text_quantity);
        String quantityText = "" + scanResult.itemInfo.quantity;
        quantity.setText(quantityText);
        quantity.addTextChangedListener(this);
    }

    private void setItemUnit() {
        if (scanResult.itemInfo.unit != null) {
            TextView unit = findViewById(R.id.text_unit);
            unit.setText(scanResult.itemInfo.unit);
        }
    }

    private void setItemSuggestions() {
        RecyclerView suggestionRecycler = findViewById(R.id.recycler_suggestions);
        suggestionRecycler.setLayoutManager(new LinearLayoutManager(this));
        suggestionRecycler.setAdapter(new ItemSuggestionAdapter(this, scanResult.suggestions, this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scan_item_toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.button_delete) {
            deleteItem();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteItem() {
        Intent intent = new Intent();
        intent.putExtra("index", index);
        setResult(ScanResultsActivity.RESULT_DELETE, intent);
        finish();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
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

    @Override
    public void onClickSuggestion(Item item) {
        searchView.setQuery("", false);

        setScanResultItem(item);
    }

    private void setScanResultItem(Item item) {
        scanResult.itemId = item.getId();
        scanResult.name = item.getName();

        setItemViews();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

    @Override
    public void afterTextChanged(Editable editable) { }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        try {
            scanResult.itemInfo.quantity = Double.parseDouble(charSequence.toString());
        }
        catch (Exception ignore) { }
    }

    @Override
    public void onItemClick(String itemId) {
        Item.loadItemById(itemId, new LoadDataListener<Item>() {
            @Override
            public void onLoad(Item payload) {
                setScanResultItem(payload);
            }
        });
    }
}
