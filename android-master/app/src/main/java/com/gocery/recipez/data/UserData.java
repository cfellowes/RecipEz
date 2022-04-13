package com.gocery.recipez.data;

import java.util.HashMap;

@SuppressWarnings("WeakerAccess")
class UserData {
    public String name;
    public HashMap<String, Boolean> pantries = new HashMap<>();
    public String active_pantry;
    public HashMap<String, Boolean> recipes = new HashMap<>();
}
