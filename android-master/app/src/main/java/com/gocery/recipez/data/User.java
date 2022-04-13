package com.gocery.recipez.data;

import com.gocery.recipez.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class User extends DataReference<UserData> {

    private static User instance;

    private User() {
        this(new LoadDataListener<Boolean>() {
            @Override
            public void onLoad(Boolean payload) {

            }
        });
    }

    public User(final LoadDataListener<Boolean> listener) {
        super(UserData.class);

        initialize(new LoadDataListener<Boolean>() {
            @Override
            public void onLoad(Boolean payload) {
                // Create default pantry if none exist
                if (data.pantries.size() == 0) {
                    addDefaultPantry(new LoadDataListener<Pantry>() {
                        @Override
                        public void onLoad(Pantry payload) {
                            listener.onLoad(true);
                        }
                    });
                }
                else {
                    listener.onLoad(true);
                }
            }
        });
    }

    public static User getInstance() {
        if (instance == null) {
            instance = new User();
        }
        return instance;
    }

    public static void setInstance(User instance) {
        User.instance = instance;
    }

    private void addDefaultPantry(final LoadDataListener<Pantry> listener) {
        Pantry.createPantry("My Pantry", new LoadDataListener<Pantry>() {
            @Override
            public void onLoad(Pantry payload) {
                addPantry(payload, new LoadDataListener<Pantry>() {
                    @Override
                    public void onLoad(final Pantry pantry) {
                        setActivePantry(pantry, new LoadDataListener<Boolean>() {
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
        });
    }

    @Override
    protected DatabaseReference getReference() {
        return FirebaseDatabase.getInstance().getReference().child("users").child(Auth.getInstance().getUserId());
    }

    public void addPantry(final Pantry pantry, final LoadDataListener<Pantry> listener) {
        getReference().child("pantries").child(pantry.getId()).setValue(true)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Pantry.loadPantryById(pantry.getId(), listener);
                    }
                });
    }

    public void loadPantries(final LoadDataListener<List<Pantry>> listener) {
        final List<Pantry> pantries = new ArrayList<>();

        if (data.pantries.size() == 0) {
            listener.onLoad(pantries);
        }

        for (String pantryId : data.pantries.keySet()) {
            Pantry.loadPantryById(pantryId, new LoadDataListener<Pantry>() {
                @Override
                public void onLoad(Pantry payload) {
                    pantries.add(payload);

                    if (pantries.size() == data.pantries.size()) {
                        listener.onLoad(pantries);
                    }
                }
            });
        }
    }

    public void removePantry(final Pantry pantry, final LoadDataListener<Boolean> listener) {
        getReference().child("pantries").child(pantry.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (data.pantries.size() == 0) {
                    addDefaultPantry(new LoadDataListener<Pantry>() {
                        @Override
                        public void onLoad(Pantry payload) {
                            listener.onLoad(true);
                        }
                    });
                }
                else {
                    Pantry.loadPantryById(data.pantries.keySet().iterator().next(), new LoadDataListener<Pantry>() {
                        @Override
                        public void onLoad(Pantry payload) {
                            setActivePantry(payload, listener);
                        }
                    });
                }
            }
        });
    }

    public void addRecipe(final Recipe recipe, final LoadDataListener<Boolean> listener) {
        getReference().child("recipes").child(recipe.getId()).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                listener.onLoad(task.isSuccessful());
            }
        });
    }

    public void loadRecipes(final LoadDataListener<List<Recipe>> listener) {
        final List<Recipe> recipes = new ArrayList<>();

        if (data.recipes.size() == 0) {
            listener.onLoad(recipes);
        }

        for (String recipeId : data.recipes.keySet()) {
            Recipe.loadRecipeById(recipeId, new LoadDataListener<Recipe>() {
                @Override
                public void onLoad(Recipe payload) {
                    recipes.add(payload);

                    if (recipes.size() == data.recipes.size()) {
                        listener.onLoad(recipes);
                    }
                }
            });
        }
    }

    public void removeRecipe(final Recipe recipe, final LoadDataListener<Boolean> listener) {
        getReference().child("recipes").child(recipe.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                listener.onLoad(task.isSuccessful());
            }
        });
    }

    public String getName() {
        return data.name;
    }

    public void setName(String name, final LoadDataListener<Boolean> listener) {
        getReference().child("name").setValue(name)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        listener.onLoad(task.isSuccessful());
                    }
                });
    }

    public String getActivePantryId() {
        return data.active_pantry;
    }

    public void loadActivePantry(final LoadDataListener<Pantry> listener) {
        Pantry.loadPantryById(data.active_pantry, new LoadDataListener<Pantry>() {
            @Override
            public void onLoad(Pantry payload) {
                listener.onLoad(payload);
            }
        });
    }

    public void setActivePantry(Pantry pantry, final LoadDataListener<Boolean> listener) {
        getReference().child("active_pantry").setValue(pantry.getId())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        listener.onLoad(task.isSuccessful());
                    }
                });
    }

    public boolean isActivePantry(Pantry pantry) {
        return pantry.getId().equals(data.active_pantry);
    }
}
