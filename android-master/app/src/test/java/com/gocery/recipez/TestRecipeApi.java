package com.gocery.recipez;

import com.gocery.recipez.data.ItemInfo;
import com.gocery.recipez.data.LoadDataListener;
import com.gocery.recipez.data.Pantry;
import com.gocery.recipez.data.RecipeData;
import com.gocery.recipez.http.RecipeApi;
import com.gocery.recipez.http.RefreshApi;
import com.gocery.recipez.http.RequestCallback;
import com.gocery.recipez.http.ScanApi;

import org.hamcrest.core.IsNull;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestRecipeApi {

    private void testNullRecipes(List<RecipeData> data) {
        assertThat(data,is(IsNull.nullValue()));
    }
    private void testRecipeData(List<RecipeData> data){
        assertThat(data,is(IsNull.notNullValue()));
    }
    @Test
    public void TestRecipeGetInstance(){
        //checks if get instance returns a non null object
        assertThat(RecipeApi.getInstance(),is(IsNull.notNullValue()));
    }
    @Test
    public void TestRecipeNull(){
        //checks if get instance returns a non null object
        RecipeApi.getInstance().recommendRecipes(null, 0, new RequestCallback<List<RecipeData>>() {
            @Override
            public void onCompleteRequest(List<RecipeData> data) {
                assertThat(data.size(),is(0));
            }
        });
    }
    @Test
    public void TestRecipeOne(){
        final Pantry[] testPantry = {null};
        Pantry.createPantry("Test", new LoadDataListener<Pantry>() {
            @Override
            public void onLoad(Pantry payload) {
                testPantry[0] = payload;
            }
        });
        RecipeApi.getInstance().recommendRecipes(testPantry[0], 1, new RequestCallback<List<RecipeData>>() {
            @Override
            public void onCompleteRequest(List<RecipeData> data) {
                assertThat(data.size(),is(1));
                RecipeData testRecipe = data.get(0);
                assertThat(testRecipe,is(IsNull.notNullValue()));
                assertThat(testRecipe.name,instanceOf(String.class));
                assertThat(testRecipe.items,instanceOf(HashMap.class));
                assertThat(testRecipe.instructions,instanceOf(String.class));
            }
        });
    }
    @Test
    public void TestRecipeEggs(){
        final Pantry[] testPantry = {null};
        Pantry.createPantry("TestEggs", new LoadDataListener<Pantry>() {
            @Override
            public void onLoad(Pantry payload) {
                HashMap<String, ItemInfo> itemToAdd = new HashMap<>();
                ItemInfo itemToAddInfo = new ItemInfo();
                itemToAddInfo.quantity = 5;
                itemToAddInfo.unit = "";
                itemToAdd.put("Egg",itemToAddInfo);
                payload.addItems(itemToAdd, new LoadDataListener<Boolean>() {
                    @Override
                    public void onLoad(Boolean payload) {
                    }
                });
                testPantry[0] = payload;
            }

        });
        RecipeApi.getInstance().recommendRecipes(testPantry[0], 1, new RequestCallback<List<RecipeData>>() {
            @Override
            public void onCompleteRequest(List<RecipeData> data) {
                assertThat(data.size(),is(1));
                RecipeData testRecipe = data.get(0);
                assertThat(testRecipe,is(IsNull.notNullValue()));
                assertThat(testRecipe.name,instanceOf(String.class));
                assertThat(testRecipe.items,instanceOf(HashMap.class));
                assertThat(testRecipe.items.get("Egg"),IsNull.notNullValue());
            }
        });
    }




    @Test
    public void RefreshGetInstance(){
        //checks if get instance returns a non null object
        assertThat(RefreshApi.getInstance(),is(IsNull.notNullValue()));
    }
    @Test
    public void ScanGetInstance(){
        //checks if get instance returns a non null object
        assertThat(ScanApi.getInstance(),is(IsNull.notNullValue()));
    }
}
