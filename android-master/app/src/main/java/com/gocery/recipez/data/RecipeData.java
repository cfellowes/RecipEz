package com.gocery.recipez.data;

import java.io.Serializable;
import java.util.HashMap;

public class RecipeData implements Serializable {

    public String name;
    public HashMap<String, ItemInfo> items = new HashMap<>();
    public HashMap<String, ItemInfo> unrecognizedItems = new HashMap<>();
    public String instructions;
}
