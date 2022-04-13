package com.gocery.recipez.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.gocery.recipez.R;
import com.gocery.recipez.data.Item;
import com.gocery.recipez.data.ItemInfo;
import com.gocery.recipez.data.LoadDataListener;
import com.gocery.recipez.data.Pantry;

public class PantryItemActivity extends AppCompatActivity {

    private String pantryId;
    private String itemId;
    private ItemInfo itemInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_item);

        setUpToolbar();

        getExtraData();
        setItemViews();
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pantry_item_toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.button_delete) {
            displayDeleteDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.alert_delete_item);
        builder.setPositiveButton(R.string.button_yes, deleteItemDialogListener);
        builder.setNegativeButton(R.string.button_no, deleteItemDialogListener);
        builder.show();
    }

    private DialogInterface.OnClickListener deleteItemDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            if (i == DialogInterface.BUTTON_POSITIVE) {
                deleteItem();
            }
        }
    };

    private void deleteItem() {
        Pantry.loadPantryById(pantryId, new LoadDataListener<Pantry>() {
            @Override
            public void onLoad(Pantry payload) {
                payload.removeItem(itemId, new LoadDataListener<Boolean>() {
                    @Override
                    public void onLoad(Boolean payload) {
                        if (payload) {
                            finish();
                        }
                    }
                });
            }
        });
    }

    private void getExtraData() {
        pantryId = getIntent().getStringExtra("pantryId");
        itemId = getIntent().getStringExtra("itemId");
        itemInfo = (ItemInfo) getIntent().getSerializableExtra("itemInfo");
    }

    private void setItemViews() {
        setItemName();
        setItemUnit();

        if (itemInfo.quantity == 0) {
            uncheckTrackQuantity();
        }
        else {
            setItemQuantity();
        }
    }

    private void setItemName() {
        final TextView titleText = findViewById(R.id.text_title);
        Item.loadItemById(itemId, new LoadDataListener<Item>() {
            @Override
            public void onLoad(Item payload) {
                titleText.setText(payload.getName());
            }
        });
    }

    private void setItemUnit() {
        TextView unitText = findViewById(R.id.text_unit);
        unitText.setText(itemInfo.unit);
    }

    private void uncheckTrackQuantity() {
        CheckBox trackQuantityCheckbox = findViewById(R.id.checkbox_track_quantity);
        trackQuantityCheckbox.performClick();
    }

    public void onClickTrackQuantity(View view) {
        ViewGroup quantityLayout = findViewById(R.id.layout_quantity);
        if (quantityLayout.getVisibility() == View.VISIBLE) {
            quantityLayout.setVisibility(View.GONE);
        }
        else {
            quantityLayout.setVisibility(View.VISIBLE);
        }
    }

    private void setItemQuantity() {
        EditText quantityText = findViewById(R.id.edit_text_quantity);
        String quantity = "" + itemInfo.quantity;
        quantityText.setText(quantity);
    }

    public void onClickSave(View view) {
        disableSaveButton();

        double quantity = 0;
        if (isTrackingQuantity()) {
            quantity = getQuantity();
        }
        saveItemQuantity(quantity, new LoadDataListener<Boolean>() {
            @Override
            public void onLoad(Boolean payload) {
                finish();
            }
        });
    }

    private boolean isTrackingQuantity() {
        CheckBox trackQuantityCheckbox = findViewById(R.id.checkbox_track_quantity);
        return trackQuantityCheckbox.isChecked();
    }

    private double getQuantity() {
        EditText quantityText = findViewById(R.id.edit_text_quantity);
        return Double.parseDouble(quantityText.getText().toString());
    }

    private void disableSaveButton() {
        Button saveButton = findViewById(R.id.button_save);
        saveButton.setEnabled(false);
        saveButton.setText(R.string.button_save_in_progress);
    }

    private void saveItemQuantity(double quantity, final LoadDataListener<Boolean> listener) {
        itemInfo.quantity = quantity;
        Pantry.loadPantryById(pantryId, new LoadDataListener<Pantry>() {
            @Override
            public void onLoad(Pantry payload) {
                payload.setItemInfo(itemId, itemInfo, listener);
            }
        });
    }
}
