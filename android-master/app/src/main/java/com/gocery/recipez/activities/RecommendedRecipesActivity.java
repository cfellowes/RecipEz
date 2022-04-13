package com.gocery.recipez.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.gocery.recipez.R;
import com.gocery.recipez.data.RecipeData;
import com.gocery.recipez.data.User;
import com.gocery.recipez.http.RecipeApi;
import com.gocery.recipez.http.RequestCallback;
import com.gocery.recipez.model.RecommendedRecipeAdapter;

import java.util.List;

public class RecommendedRecipesActivity extends AppCompatActivity {

    private static final int NUM_RECIPES = 20;

    private RecyclerView recipesRecycler;
    private List<RecipeData> recipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommended_recipes);

        loadRecommendedRecipes();

        setUpRecycler();
        setUpToolbar();
    }

    private void loadRecommendedRecipes() {
        RecipeApi.getInstance().recommendRecipes(User.getInstance().getActivePantryId(), NUM_RECIPES, new RequestCallback<List<RecipeData>>() {
            @Override
            public void onCompleteRequest(List<RecipeData> data) {
                recipes = data;

                findViewById(R.id.layout_progress_bar).setVisibility(View.GONE);

                if (recipes.size() > 0) {
                    findViewById(R.id.recycler_recipes).setVisibility(View.VISIBLE);
                }
                else {
                    findViewById(R.id.text_default_recommended_recipes).setVisibility(View.VISIBLE);
                }

                refreshRecycler();
            }
        });
    }

    private void setUpRecycler() {
        recipesRecycler = findViewById(R.id.recycler_recipes);
        recipesRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    private void refreshRecycler() {
        recipesRecycler.setAdapter(new RecommendedRecipeAdapter(this, recipes));
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
}
