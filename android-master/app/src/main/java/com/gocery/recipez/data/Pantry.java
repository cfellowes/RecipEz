package com.gocery.recipez.data;

import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Pantry extends DataReference<PantryData> {

    public static final int NONE = 0;
    public static final int NOT_ENOUGH = 1;
    public static final int ENOUGH = 2;

    private DatabaseReference reference;

    private Pantry(DatabaseReference reference) {
        super(PantryData.class);
        this.reference = reference;
    }

    public static void createPantry(final String name, final LoadDataListener<Pantry> listener) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("pantries").push();
        final Pantry pantry = new Pantry(reference);
        pantry.initialize(new LoadDataListener<Boolean>() {
            @Override
            public void onLoad(Boolean payload) {
                pantry.setName(name, new LoadDataListener<Boolean>() {
                    @Override
                    public void onLoad(Boolean payload) {
                        if (payload) {
                            listener.onLoad(pantry);
                        }
                    }
                });
            }
        });
    }

    public static void deletePantry(String pantryId, final LoadDataListener<Boolean> listener) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("pantries").child(pantryId);
        reference.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                listener.onLoad(databaseError != null);
            }
        });
    }

    public static void loadPantryById(String pantryId, final LoadDataListener<Pantry> listener) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("pantries").child(pantryId);
        final Pantry pantry = new Pantry(reference);
        pantry.initialize(new LoadDataListener<Boolean>() {
            @Override
            public void onLoad(Boolean payload) {
                if (payload) {
                    listener.onLoad(pantry);
                }
            }
        });
    }

    @Override
    public boolean equals(Object other) {
        if (other.getClass() != Pantry.class)
            return false;
        Pantry otherPantry = (Pantry) other;
        return otherPantry.getId().equals(getId());
    }

    @Override
    protected DatabaseReference getReference() {
        return reference;
    }

    private void setName(String name, final LoadDataListener<Boolean> listener) {
        getReference().child("name").setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                listener.onLoad(task.isSuccessful());
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

    public void addItem(String itemId, final ItemInfo itemInfo, final LoadDataListener<Boolean> listener) {
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

    public void setItemInfo(String itemId, ItemInfo itemInfo, final LoadDataListener<Boolean> listener) {
        reference.child("items").child(itemId).setValue(itemInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                listener.onLoad(task.isSuccessful());
            }
        });
    }

    public void removeItem(String itemId, final LoadDataListener<Boolean> listener) {
        reference.child("items").child(itemId).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                listener.onLoad(databaseError == null);
            }
        });
    }

    public HashMap<String, ItemInfo> getItems() {
        return data.items;
    }

    public int getItemAmount(String itemId, double quantity) {
        ItemInfo info = data.items.get(itemId);
        if (info != null) {
            if (info.quantity == 0 || info.quantity >= quantity) {
                return ENOUGH;
            }
            else {
                return NOT_ENOUGH;
            }
        }
        return NONE;
    }

    public String getName() {
        return data.name;
    }

    public String getId() {
        return reference.getKey();
    }

    public void makeRecipe(Recipe recipe, LoadDataListener<Boolean> listener) {
        for (String itemId : recipe.getItems().keySet()) {
            ItemInfo recipeItem = recipe.getItems().get(itemId);
            ItemInfo pantryItem = data.items.get(itemId);
            if (recipeItem != null && pantryItem != null && recipeItem.quantity > 0 && pantryItem.quantity > 0) {
                pantryItem.quantity -= recipeItem.quantity;
                if (pantryItem.quantity <= 0) {
                    removeItem(itemId, new LoadDataListener<Boolean>() {
                        @Override
                        public void onLoad(Boolean payload) {

                        }
                    });
                }
                else {
                    setItemInfo(itemId, pantryItem, new LoadDataListener<Boolean>() {
                        @Override
                        public void onLoad(Boolean payload) {

                        }
                    });
                }
            }
        }
        listener.onLoad(true);
    }

    public void makeRecipe(RecipeData recipe, LoadDataListener<Boolean> listener) {
        for (String itemId : recipe.items.keySet()) {
            ItemInfo recipeItem = recipe.items.get(itemId);
            ItemInfo pantryItem = data.items.get(itemId);
            if (recipeItem != null && pantryItem != null && recipeItem.quantity > 0 && pantryItem.quantity > 0) {
                pantryItem.quantity -= recipeItem.quantity;
                if (pantryItem.quantity <= 0) {
                    removeItem(itemId, new LoadDataListener<Boolean>() {
                        @Override
                        public void onLoad(Boolean payload) {

                        }
                    });
                }
                else {
                    setItemInfo(itemId, pantryItem, new LoadDataListener<Boolean>() {
                        @Override
                        public void onLoad(Boolean payload) {

                        }
                    });
                }
            }
        }
        listener.onLoad(true);
    }
}
