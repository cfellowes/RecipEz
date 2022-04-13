package com.gocery.recipez.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.gocery.recipez.R;
import com.gocery.recipez.data.ItemSuggestion;
import com.gocery.recipez.data.LoadDataListener;
import com.gocery.recipez.data.Pantry;
import com.gocery.recipez.data.ScanResult;
import com.gocery.recipez.data.User;
import com.gocery.recipez.model.ScanResultAdapter;

import java.util.ArrayList;
import java.util.Arrays;

public class ScanResultsActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 1;
    public static final int RESULT_DELETE = 111;
    public static final int RESULT_UPDATE = 222;

    private RecyclerView recyclerView;
    private ArrayList<ScanResult> scanResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_results);

        ScanResult[] scanResults = (ScanResult[]) getIntent().getSerializableExtra("scanResults");
        if (scanResults != null) {
            this.scanResults = new ArrayList<>(Arrays.asList(scanResults));

            if (this.scanResults.size() == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.alert_failed_scan);
                builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                builder.show();
            }
        }

        setUpRecyclerView();
        refresh();
    }

    private void setUpRecyclerView() {
        recyclerView = findViewById(R.id.recycler_pantry_items);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setRecyclerAdapter() {
        setScanSuggestions();

        recyclerView.setAdapter(new ScanResultAdapter(this, scanResults));
    }

    private void setScanSuggestions() {
        for (ScanResult scanResult : scanResults) {
            if (scanResult.itemId == null && scanResult.suggestions.size() > 0) {
                scanResult.itemId = scanResult.suggestions.first().itemId;
            }
        }
    }

    private void refresh() {
        setRecyclerAdapter();
        if (isReadyToAdd()) {
            enableAddButton();
        }
    }

    private void enableAddButton() {
        Button addButton = findViewById(R.id.button_add_items);
        addButton.setEnabled(true);
        addButton.setText(R.string.button_add_items_to_pantry);
    }

    private void disableAddButton() {
        Button addButton = findViewById(R.id.button_add_items);
        addButton.setEnabled(false);
        addButton.setText(R.string.button_add_in_progress);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case (RESULT_DELETE):
                scanResults.remove(data.getIntExtra("index", 0));
                refresh();
                break;
            case (RESULT_UPDATE):
                int index = data.getIntExtra("index", 0);
                ScanResult scanResult = (ScanResult) data.getSerializableExtra("scanResult");
                scanResults.remove(index);
                scanResults.add(index, scanResult);
                refresh();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onClickAddItems(View view) {
        disableAddButton();

        addItemsToPantry(new LoadDataListener<Boolean>() {
            @Override
            public void onLoad(Boolean payload) {
                finish();
            }
        });
    }

    private boolean isReadyToAdd() {
        for (ScanResult scanResult : scanResults) {
            if (scanResult.itemId == null) {
                return false;
            }
        }
        return true;
    }

    private void addItemsToPantry(final LoadDataListener<Boolean> listener) {
        final Number itemsAdded = new Number();

        User.getInstance().loadActivePantry(new LoadDataListener<Pantry>() {
            @Override
            public void onLoad(Pantry pantry) {
                for (ScanResult scanResult : scanResults) {
                    pantry.addItem(scanResult.itemId, scanResult.itemInfo, new LoadDataListener<Boolean>() {
                        @Override
                        public void onLoad(Boolean payload) {
                            itemsAdded.num++;
                            if (itemsAdded.num == scanResults.size()) {
                                listener.onLoad(true);
                            }
                        }
                    });
                }
            }
        });
    }

    class Number {
        int num;
    }
}
