package com.gocery.recipez.model;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gocery.recipez.R;
import com.gocery.recipez.activities.ScanItemActivity;
import com.gocery.recipez.activities.ScanResultsActivity;
import com.gocery.recipez.data.Item;
import com.gocery.recipez.data.LoadDataListener;
import com.gocery.recipez.data.ScanResult;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ScanResultAdapter extends RecyclerView.Adapter<ScanResultAdapter.ScanResultRow> {

    private LayoutInflater inflater;
    private Activity activity;
    private List<ScanResult> scanResults;

    public ScanResultAdapter(Activity activity, List<ScanResult> scanResults) {
        inflater = LayoutInflater.from(activity);
        this.activity = activity;
        this.scanResults = scanResults;
    }

    @NonNull
    @Override
    public ScanResultRow onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ScanResultRow(inflater.inflate(R.layout.row_scan_result, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ScanResultRow holder, int position) {
        holder.scanResult = scanResults.get(position);
        holder.index = position;

        if (holder.scanResult.itemId != null) {
            holder.imageView.setImageDrawable(activity.getDrawable(R.drawable.ic_check_green_24dp));

            Item.loadItemById(holder.scanResult.itemId, new LoadDataListener<Item>() {
                @Override
                public void onLoad(Item payload) {
                    holder.scanResult.itemId = payload.getId();
                    holder.scanResult.name = payload.getName();
                    holder.name.setText(payload.getName());
                }
            });
        }
        else {
            holder.name.setText(holder.scanResult.name);
            holder.imageView.setImageDrawable(activity.getDrawable(R.drawable.ic_warning_red_24dp));
        }
    }

    @Override
    public int getItemCount() {
        return scanResults.size();
    }

    class ScanResultRow extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView name;
        private ImageView imageView;
        private ScanResult scanResult;
        private int index;

        ScanResultRow(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.text_item_name);
            imageView = itemView.findViewById(R.id.imageView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(activity, ScanItemActivity.class);
            intent.putExtra("scanResult", scanResult);
            intent.putExtra("index", index);

            activity.startActivityForResult(intent, ScanResultsActivity.REQUEST_CODE);
        }
    }
}
