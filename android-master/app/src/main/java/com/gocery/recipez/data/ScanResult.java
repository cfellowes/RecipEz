package com.gocery.recipez.data;

import java.io.Serializable;
import java.util.TreeSet;

public class ScanResult implements Serializable {

    public String itemId;
    public String name;
    public ItemInfo itemInfo;
    public TreeSet<ItemSuggestion> suggestions = new TreeSet<>();
}
