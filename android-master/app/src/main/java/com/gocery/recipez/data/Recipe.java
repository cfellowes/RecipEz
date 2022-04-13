package com.gocery.recipez.data;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Recipe extends DataReference<RecipeData> {

    private DatabaseReference reference;

    private Recipe(DatabaseReference reference) {
        super(RecipeData.class);
        this.reference = reference;
    }

    // My thought process was that when the "create recipe" button was pressed it would call this method,
    // then when "save recipe" was pressed it would call addItems() and addInstructions()
    public static void createRecipe(final String name, final HashMap<String, ItemInfo> items, final String instructions, final LoadDataListener<Recipe> listener) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("recipes").push();
        final Recipe recipe = new Recipe(reference);
        recipe.initialize(new LoadDataListener<Boolean>() {
            @Override
            public void onLoad(Boolean payload) {
                recipe.setName(name, new LoadDataListener<Boolean>() {
                    @Override
                    public void onLoad(Boolean payload) {
                        recipe.addItems(items, new LoadDataListener<Boolean>() {
                            @Override
                            public void onLoad(Boolean payload) {
                                recipe.addInstructions(instructions, new LoadDataListener<Boolean>() {
                                    @Override
                                    public void onLoad(Boolean payload) {
                                        listener.onLoad(recipe);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    public static void deleteRecipe(String recipeId, final LoadDataListener<Boolean> listener) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("recipes").child(recipeId);
        reference.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                listener.onLoad(databaseError != null);
            }
        });
    }

    public static void loadRecipeById(String recipeId, final LoadDataListener<Recipe> listener) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("recipes").child(recipeId);
        final Recipe recipe = new Recipe(reference);
        recipe.initialize(new LoadDataListener<Boolean>() {
            @Override
            public void onLoad(Boolean payload) {
                if (payload) {
                    listener.onLoad(recipe);
                }
            }
        });
    }

    public void addItems(final HashMap<String, ItemInfo> items, final LoadDataListener<Boolean> listener) {
        final HashMap<String, Boolean> itemsAdded = new HashMap<>();
        for (final String itemId : items.keySet()) {
            addItem(itemId, items.get(itemId), new LoadDataListener<Boolean>() {
                @Override
                public void onLoad(Boolean payload) {
                    itemsAdded.put(itemId, true);
                    if (itemsAdded.size() == items.size()) {
                        listener.onLoad(true);
                    }
                }
            });
        }
    }

    private void addItem(String itemId, final ItemInfo itemInfo, final LoadDataListener<Boolean> listener) {
        reference.child("items").child(itemId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ItemInfo oldInfo = dataSnapshot.getValue(ItemInfo.class);
                    if (oldInfo != null) {
                        itemInfo.quantity += oldInfo.quantity;
                    }
                }
                dataSnapshot.getRef().setValue(itemInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        listener.onLoad(task.isSuccessful());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void addInstructions(final String instructions, final LoadDataListener<Boolean> listener){
        reference.child("instructions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().setValue(instructions).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        listener.onLoad(task.isSuccessful());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    @Override
    protected DatabaseReference getReference() {
        return reference;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Item) {
            return ((Item) other).getId().equals(getId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public String getId() {
        return reference.getKey();
    }

    public void setName(String name, final LoadDataListener<Boolean> listener) {
        getReference().child("name").setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                listener.onLoad(task.isSuccessful());
            }
        });
    }

    public String getName() {
        return data.name;
    }

    public void setInstructions(String instructions, final LoadDataListener<Boolean> listener) {
        getReference().child("instructions").setValue(instructions).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                listener.onLoad(task.isSuccessful());
            }
        });
    }

    public String getInstructions() {
        return data.instructions;
    }

    public void setItems(HashMap<String, ItemInfo> items, final LoadDataListener<Boolean> listener) {
        getReference().child("items").setValue(items).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                listener.onLoad(task.isSuccessful());
            }
        });
    }

    public HashMap<String, ItemInfo> getItems() {
        return data.items;
    }

}
