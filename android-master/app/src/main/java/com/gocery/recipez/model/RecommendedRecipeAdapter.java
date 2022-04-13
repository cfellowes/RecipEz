package com.gocery.recipez.model;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gocery.recipez.R;
import com.gocery.recipez.activities.ViewRecipeActivity;
import com.gocery.recipez.activities.ViewRecommendedRecipeActivity;
import com.gocery.recipez.data.Recipe;
import com.gocery.recipez.data.RecipeData;

import java.util.List;

public class RecommendedRecipeAdapter extends RecyclerView.Adapter<RecommendedRecipeAdapter.RecipeRow> {

    private LayoutInflater inflater;
    private Context context;
    private List<RecipeData> recipes;

    public RecommendedRecipeAdapter(Context context, List<RecipeData> recipes) {
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
        holder.recipe = recipes.get(position);
        holder.name.setText(recipes.get(position).name);
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    class RecipeRow extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        RecipeData recipe;

        RecipeRow(View view) {
            super(view);

            name = view.findViewById(R.id.text_recipe_name);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(context, ViewRecommendedRecipeActivity.class);
            intent.putExtra("recipe", recipe);
            context.startActivity(intent);
        }
    }
}
