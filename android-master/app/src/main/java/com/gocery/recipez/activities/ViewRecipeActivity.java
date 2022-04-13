package com.gocery.recipez.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gocery.recipez.R;
import com.gocery.recipez.data.ItemInfo;
import com.gocery.recipez.data.LoadDataListener;
import com.gocery.recipez.data.Pantry;
import com.gocery.recipez.data.Recipe;
import com.gocery.recipez.data.User;
import com.gocery.recipez.model.CreateRecipeItemAdapter;
import com.gocery.recipez.model.RecipeItemAdapter;

import java.util.HashMap;

public class ViewRecipeActivity extends AppCompatActivity implements CreateRecipeItemAdapter.DeleteListener, CreateRecipeItemAdapter.QuantityListener {

    private RecyclerView itemRecycler;
    private Recipe recipe;
    private boolean isInEditMode;
    private Pantry activePantry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recipe);

        User.getInstance().loadActivePantry(new LoadDataListener<Pantry>() {
            @Override
            public void onLoad(Pantry payload) {
                activePantry = payload;
            }
        });

        setUpToolbar();
        setUpRecycler();
        loadRecipeData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_recipe_toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.button_edit) {
            if (isInEditMode) {
                exitEditMode();
            }
            else {
                enterEditMode();
            }
        }
        else if (item.getItemId() == R.id.button_delete) {
            displayDeleteDialog();
        }
        else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void setUpToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        exitEditMode();
        finish();
        return true;
    }

    private void setUpRecycler() {
        itemRecycler = findViewById(R.id.recycler_items);
        itemRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadRecipeData() {
        String recipeId = getIntent().getStringExtra("recipeId");
        Recipe.loadRecipeById(recipeId, new LoadDataListener<Recipe>() {
            @Override
            public void onLoad(Recipe payload) {
                recipe = payload;
                setUpRecipeViews();
                setUpEditViews();
            }
        });
    }

    private void setUpRecipeViews() {
        ((TextView) findViewById(R.id.text_recipe_name)).setText(recipe.getName());
        ((TextView) findViewById(R.id.text_instructions)).setText(recipe.getInstructions());

        refreshItems();
    }

    private void refreshItems() {
        itemRecycler.setAdapter(new RecipeItemAdapter(this, recipe.getItems()));
    }

    private void displayDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.alert_delete_recipe);
        builder.setPositiveButton(R.string.button_yes, deleteItemDialogListener);
        builder.setNegativeButton(R.string.button_no, deleteItemDialogListener);
        builder.show();
    }

    private DialogInterface.OnClickListener deleteItemDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            if (i == DialogInterface.BUTTON_POSITIVE) {
                deleteRecipe();
            }
        }
    };

    private void deleteRecipe() {
        User.getInstance().removeRecipe(recipe, new LoadDataListener<Boolean>() {
            @Override
            public void onLoad(Boolean payload) {
                if (payload) {
                    Recipe.deleteRecipe(recipe.getId(), new LoadDataListener<Boolean>() {
                        @Override
                        public void onLoad(Boolean payload) {
                            finish();
                        }
                    });
                }
            }
        });
    }

    private void enterEditMode() {
        isInEditMode = true;
        setEditVisibility(View.VISIBLE);
        setNormalVisibility(View.GONE);
        setEditRecycler();
        setUpEditViews();
    }

    private void exitEditMode() {
        isInEditMode = false;

        String name = ((EditText) findViewById(R.id.edit_text_recipe_name)).getText().toString();
        final String instructions = ((EditText) findViewById(R.id.edit_text_instructions)).getText().toString();
        recipe.setName(name, new LoadDataListener<Boolean>() {
            @Override
            public void onLoad(Boolean payload) {
                recipe.setInstructions(instructions, new LoadDataListener<Boolean>() {
                    @Override
                    public void onLoad(Boolean payload) {
                        setEditVisibility(View.GONE);
                        setNormalVisibility(View.VISIBLE);
                        itemRecycler.setAdapter(new RecipeItemAdapter(ViewRecipeActivity.this, recipe.getItems()));
                        setUpRecipeViews();
                    }
                });
            }
        });
    }

    private void setEditVisibility(int visibility) {
        findViewById(R.id.edit_text_recipe_name).setVisibility(visibility);
        findViewById(R.id.button_add_items).setVisibility(visibility);
        findViewById(R.id.edit_text_instructions).setVisibility(visibility);
    }

    private void setNormalVisibility(int visibility) {
        findViewById(R.id.text_recipe_name).setVisibility(visibility);
        findViewById(R.id.text_instructions).setVisibility(visibility);
        findViewById(R.id.button_make_recipe).setVisibility(visibility);
    }

    private void setUpEditViews() {
        ((EditText) findViewById(R.id.edit_text_recipe_name)).setText(recipe.getName());
        ((EditText) findViewById(R.id.edit_text_instructions)).setText(recipe.getInstructions());
    }

    @Override
    public void onItemDeleted(String itemId) {
        HashMap<String, ItemInfo> items = recipe.getItems();
        items.remove(itemId);
        recipe.setItems(items, new LoadDataListener<Boolean>() {
            @Override
            public void onLoad(Boolean payload) {
                setEditRecycler();
            }
        });
    }

    @Override
    public void onQuantityChanged(String itemId, double quantity) {
        HashMap<String, ItemInfo> items = recipe.getItems();
        ItemInfo itemInfo = items.get(itemId);
        if (itemInfo != null) {
            itemInfo.quantity = quantity;
            recipe.setItems(items, new LoadDataListener<Boolean>() {
                @Override
                public void onLoad(Boolean payload) { }
            });
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
            HashMap<String, ItemInfo> items = (HashMap<String, ItemInfo>) data.getSerializableExtra("items");
            if (items != null) {
                recipe.addItems(items, new LoadDataListener<Boolean>() {
                    @Override
                    public void onLoad(Boolean payload) {
                        setEditRecycler();
                    }
                });
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setEditRecycler() {
        itemRecycler.setAdapter(new CreateRecipeItemAdapter(this, recipe.getItems(), this, this));
    }

    public void onClickMakeRecipe(View view) {
        if (hasAllItems()) {
            makeRecipe();
        }
        else {
            displayConfirmationDialog();
        }
    }

    private void makeRecipe() {
        activePantry.makeRecipe(recipe, new LoadDataListener<Boolean>() {
            @Override
            public void onLoad(Boolean payload) {
                displayConfirmation();
                enableMakeButton();
                refreshItems();
            }
        });
    }

    private void displayConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.alert_recipe_confirmation);
        builder.setPositiveButton(R.string.button_yes, confirmationListener);
        builder.setNegativeButton(R.string.button_no, confirmationListener);
        builder.show();
    }

    private AlertDialog.OnClickListener confirmationListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            if (i == AlertDialog.BUTTON_POSITIVE) {
                makeRecipe();
            }
        }
    };

    private boolean hasAllItems() {
        for (String itemId : recipe.getItems().keySet()) {
            ItemInfo itemInfo = recipe.getItems().get(itemId);
            if (itemInfo != null) {
                if (activePantry.getItemAmount(itemId, itemInfo.quantity) != Pantry.ENOUGH) {
                    return false;
                }
            }
        }
        return true;
    }

    private void enableMakeButton() {
        Button button = findViewById(R.id.button_make_recipe);
        button.setEnabled(true);
        button.setText(R.string.button_make_recipe);
    }

    private void displayConfirmation() {
        Toast toast = Toast.makeText(this, R.string.msg_success_make_recipe, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
