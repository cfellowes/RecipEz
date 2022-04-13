package com.gocery.recipez.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gocery.recipez.Auth;
import com.gocery.recipez.R;
import com.gocery.recipez.activities.AddItemsActivity;
import com.gocery.recipez.activities.CreatePantryActivity;
import com.gocery.recipez.activities.LoginActivity;
import com.gocery.recipez.data.Item;
import com.gocery.recipez.data.ItemInfo;
import com.gocery.recipez.data.LoadDataListener;
import com.gocery.recipez.data.Pantry;
import com.gocery.recipez.data.User;
import com.gocery.recipez.model.ItemListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PantryFragment extends Fragment implements LoadDataListener<List<Pantry>> {

    private AppCompatActivity activity;
    private View rootView;
    private RecyclerView itemsRecycler;
    private Spinner pantrySpinner;
    private List<Pantry> pantries;
    private DialogInterface.OnClickListener deletePantryDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            if (i == DialogInterface.BUTTON_POSITIVE) {
                deletePantry();
            }
        }
    };
    private PopupMenu.OnMenuItemClickListener popupMenuListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.button_delete_pantry) {
                displayDeleteDialog();
            } else if (item.getItemId() == R.id.button_logout) {
                logout();
            }
            return true;
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_pantry, container, false);
        activity = (AppCompatActivity) getActivity();

        setUpToolbar();
        setUpRecycler();
        loadPantries();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        loadPantries();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AddItemsActivity.REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                @SuppressWarnings("unchecked") final HashMap<String, ItemInfo> items = (HashMap) data.getSerializableExtra("items");

                pantries.get(0).addItems(items, new LoadDataListener<Boolean>() {
                    @Override
                    public void onLoad(Boolean payload) {
                        setRecyclerAdapter(pantries.get(0));
                    }
                });
            }
        }
    }

    private void setUpToolbar() {
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);

        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        setHasOptionsMenu(true);
    }

    private void setUpRecycler() {
        itemsRecycler = rootView.findViewById(R.id.recycler_pantry_items);
        itemsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void setRecyclerAdapter(Pantry pantry) {
        if (pantry.getItems().size() == 0) {
            itemsRecycler.setVisibility(View.GONE);
            rootView.findViewById(R.id.text_default_pantry).setVisibility(View.VISIBLE);
        } else {
            itemsRecycler.setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.text_default_pantry).setVisibility(View.GONE);
            itemsRecycler.setAdapter(new ItemListAdapter(getContext(), pantry));
        }
    }

    private void loadPantries() {
        User.getInstance().loadPantries(this);
    }

    @Override
    public void onLoad(List<Pantry> payload) {
        pantries = payload;
        User.getInstance().loadActivePantry(new LoadDataListener<Pantry>() {
            @Override
            public void onLoad(Pantry activePantry) {
                orderPantries(activePantry);
                setUpSpinner();
                setRecyclerAdapter(activePantry);
            }
        });
    }

    private void orderPantries(Pantry activePantry) {
        pantries.remove(activePantry);
        pantries.add(0, activePantry);
    }

    private void setUpSpinner() {
        pantrySpinner = rootView.findViewById(R.id.spinner_pantries);
        setSpinnerAdapter();

        pantrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == pantries.size()) {
                    startActivity(new Intent(getContext(), CreatePantryActivity.class));
                } else {
                    onSelectPantry(pantries.get(i));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void setSpinnerAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(rootView.getContext(), R.layout.spinner_item_pantry, getSpinnerList());
        pantrySpinner.setAdapter(adapter);
    }

    private String[] getSpinnerList() {
        String[] spinnerItems = new String[pantries.size() + 1];
        int i = 0;
        while (i < pantries.size()) {
            spinnerItems[i] = pantries.get(i).getName();
            i++;
        }
        spinnerItems[i] = getResources().getString(R.string.spinner_new_pantry);
        return spinnerItems;
    }

    private void onSelectPantry(final Pantry pantry) {
        if (User.getInstance().isActivePantry(pantry)) {
            return;
        }

        User.getInstance().setActivePantry(pantry, new LoadDataListener<Boolean>() {
            @Override
            public void onLoad(Boolean payload) {
                orderPantries(pantry);
                setSpinnerAdapter();
            }
        });

        setRecyclerAdapter(pantry);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.pantry_toolbar_menu, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.button_add) {
            startActivityForResult(new Intent(getContext(), AddItemsActivity.class), AddItemsActivity.REQUEST_CODE);
        } else if (item.getItemId() == R.id.button_more) {
            displayPopupMenu();
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayPopupMenu() {
        if (getContext() != null) {
            PopupMenu popupMenu = new PopupMenu(getContext(), rootView.findViewById(R.id.button_more));
            popupMenu.getMenuInflater().inflate(R.menu.pantry_more_menu, popupMenu.getMenu());
            popupMenu.show();
            popupMenu.setOnMenuItemClickListener(popupMenuListener);
        }
    }

    private void displayDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.alert_delete_pantry);
        builder.setPositiveButton(R.string.button_yes, deletePantryDialogListener);
        builder.setNegativeButton(R.string.button_no, deletePantryDialogListener);
        builder.show();
    }

    private void deletePantry() {
        Pantry pantry = pantries.get(0);
        User.getInstance().removePantry(pantry, new LoadDataListener<Boolean>() {
            @Override
            public void onLoad(Boolean payload) {
                loadPantries();
            }
        });
        Pantry.deletePantry(pantry.getId(), new LoadDataListener<Boolean>() {
            @Override
            public void onLoad(Boolean payload) {
            }
        });
    }

    private void logout() {
        if (activity != null) {
            Auth.getInstance().logout();
            startActivity(new Intent(activity, LoginActivity.class));
            activity.finish();
        }
    }
}