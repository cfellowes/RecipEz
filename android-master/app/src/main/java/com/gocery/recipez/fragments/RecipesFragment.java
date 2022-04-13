package com.gocery.recipez.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gocery.recipez.R;
import com.gocery.recipez.activities.RecommendedRecipesActivity;
import com.gocery.recipez.activities.CreateRecipeActivity;
import com.gocery.recipez.data.LoadDataListener;
import com.gocery.recipez.data.Recipe;
import com.gocery.recipez.data.User;
import com.gocery.recipez.model.RecipeListAdapter;

import java.util.List;

public class RecipesFragment extends Fragment implements View.OnClickListener {

    private AppCompatActivity activity;
    private RecyclerView recipeRecycler;
    private View rootView;
    private List<Recipe> recipes;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_recipes, container, false);

        activity = (AppCompatActivity) getActivity();

        setUpRecycler();
        setUpToolbar();
        setOnClickListeners();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        loadRecipes();
    }

    private void loadRecipes() {
        User.getInstance().loadRecipes(new LoadDataListener<List<Recipe>>() {
            @Override
            public void onLoad(List<Recipe> payload) {
                recipes = payload;

                if (recipes.size() > 0) {
                    rootView.findViewById(R.id.text_default_recipes).setVisibility(View.GONE);
                    rootView.findViewById(R.id.recycler_recipes).setVisibility(View.VISIBLE);

                    refreshRecycler();
                }
                else {
                    rootView.findViewById(R.id.text_default_recipes).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.recycler_recipes).setVisibility(View.GONE);
                }
            }
        });
    }

    private void setUpRecycler() {
        recipeRecycler = rootView.findViewById(R.id.recycler_recipes);
        recipeRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void refreshRecycler() {
        if (recipeRecycler != null) {
            recipeRecycler.setAdapter(new RecipeListAdapter(getContext(), recipes));
        }
    }

    private void setUpToolbar() {
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);

        setHasOptionsMenu(true);
    }

    private void setOnClickListeners() {
        rootView.findViewById(R.id.button_request_recipes).setOnClickListener(this);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.recipes_toolbar_menu, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.button_add) {
            onClickAddButton();
        }
        return super.onOptionsItemSelected(item);
    }

    private void onClickAddButton() {
        startActivity(new Intent(getContext(), CreateRecipeActivity.class));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_request_recipes) {
            startActivity(new Intent(getContext(), RecommendedRecipesActivity.class));
        }
    }
}