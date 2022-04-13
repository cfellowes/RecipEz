package com.gocery.recipez.http;

import com.gocery.recipez.Auth;
import com.gocery.recipez.data.ItemInfo;
import com.gocery.recipez.data.RecipeData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * The RecipeApi class follows the Singleton pattern and exposes a public API for communicating with
 * the recipe recommendation backend.
 */
public class RecipeApi extends Api {

    private static final String route = "/api/recipes";

    private static RecipeApi instance;

    public static RecipeApi getInstance() {
        if (instance == null) {
            instance = new RecipeApi();
        }
        return instance;
    }

    /**
     * Requests a list of recommended recipes from the backend for a specified pantry. When the list
     * is returned, a callback is made to the provided RequestCallback interface.
     *
     * @param pantryId   The pantry to recommend recipes for.
     * @param numRecipes The number of recipes to recommend.
     * @param callback   The callback listener to return the requested recipes to.
     */
    public void recommendRecipes(final String pantryId, final int numRecipes, final RequestCallback<List<RecipeData>> callback) {

        Auth.getInstance().getIdToken(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    String token = task.getResult().getToken();

                    new HttpTask(baseUrl + route).setRequestCallback(new RequestCallback<String>() {
                        @Override
                        public void onCompleteRequest(String data) {
                            callback.onCompleteRequest(parseJson(data));
                        }
                    }).execute("form", formatParameters(token, pantryId, numRecipes));
                }
            }
        });
    }

    private String formatParameters(String token, String pantryId, int numRecipes) {
        return "token=" + token + "&pantry=" + pantryId + "&num=" + numRecipes;
    }

    private List<RecipeData> parseJson(String json) {
        List<RecipeData> recipes = new ArrayList<>();

        try {
            JSONArray recipeArray = new JSONArray(json);
            for (int i = 0; i < recipeArray.length(); i++) {
                JSONObject recipe = recipeArray.getJSONObject(i);

                RecipeData data = new RecipeData();
                data.name = recipe.getString("recipe");
                data.instructions = recipe.getString("instructions");

                JSONArray items = recipe.getJSONArray("items");
                for (int k = 0; k < items.length(); k++) {
                    JSONObject item = items.getJSONObject(k);

                    ItemInfo itemInfo = new ItemInfo();
                    itemInfo.quantity = item.getDouble("quantity");
                    itemInfo.unit = item.getString("units");

                    if (item.has("id")) {
                        data.items.put(item.getString("id"), itemInfo);
                    }
                    else {
                        data.unrecognizedItems.put(item.getString("item"), itemInfo);
                    }
                }

                recipes.add(data);
            }

        }
        catch (NullPointerException | JSONException e) {
            e.printStackTrace();
        }

        return recipes;
    }
}
