package com.gocery.recipez.model;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.gocery.recipez.R;
import com.gocery.recipez.activities.ScanItemActivity;
import com.gocery.recipez.activities.ViewRecipeActivity;
import com.gocery.recipez.data.Recipe;

import java.util.ArrayList;
import java.util.List;

public class RecipeListAdapter extends RecyclerView.Adapter<RecipeListAdapter.RecipeRow> {

    private LayoutInflater inflater;
    private Context context;
    private List<Recipe> recipes;

    public RecipeListAdapter(Context context, List<Recipe> recipes) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.recipes = recipes;
    }

    @NonNull
    @Override
    public RecipeRow onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecipeRow(inflater.inflate(R.layout.row_recipe, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeRow holder, int position) {
        holder.name.setText(recipes.get(position).getName());
        holder.recipeId = recipes.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    class RecipeRow extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        String recipeId;

        RecipeRow(View view) {
            super(view);

            name = view.findViewById(R.id.text_recipe_name);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(context, ViewRecipeActivity.class);
            intent.putExtra("recipeId", recipeId);
            context.startActivity(intent);
        }
    }
}
