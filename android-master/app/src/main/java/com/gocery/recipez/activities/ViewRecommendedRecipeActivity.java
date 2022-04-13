package com.gocery.recipez.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gocery.recipez.R;
import com.gocery.recipez.data.ItemInfo;
import com.gocery.recipez.data.LoadDataListener;
import com.gocery.recipez.data.Pantry;
import com.gocery.recipez.data.RecipeData;
import com.gocery.recipez.data.User;
import com.gocery.recipez.model.RecipeItemAdapter;

public class ViewRecommendedRecipeActivity extends AppCompatActivity {

    private RecyclerView itemRecycler;
    private RecipeData recipe;
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

        recipe = (RecipeData) getIntent().getSerializableExtra("recipe");

        setUpToolbar();
        setUpRecipeViews();
    }

    private void setUpToolbar() {
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

    private void setUpRecipeViews() {
        ((TextView) findViewById(R.id.text_recipe_name)).setText(recipe.name);
        ((TextView) findViewById(R.id.text_instructions)).setText(recipe.instructions);
        setUpRecycler();
    }

    private void setUpRecycler() {
        itemRecycler = findViewById(R.id.recycler_items);
        itemRecycler.setLayoutManager(new LinearLayoutManager(this));
        refreshRecycler();
    }

    private void refreshRecycler() {
        itemRecycler.setAdapter(new RecipeItemAdapter(this, recipe.items));
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
                refreshRecycler();
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
        for (String itemId : recipe.items.keySet()) {
            ItemInfo itemInfo = recipe.items.get(itemId);
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
