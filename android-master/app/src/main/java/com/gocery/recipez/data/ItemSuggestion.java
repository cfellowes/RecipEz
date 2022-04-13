package com.gocery.recipez.data;

import java.io.Serializable;

import androidx.annotation.NonNull;

public class ItemSuggestion implements Comparable, Serializable {

    public String itemId;
    public double score;

    @Override
    public int compareTo(@NonNull Object o) {
        if (o instanceof ItemSuggestion) {
            ItemSuggestion other = (ItemSuggestion) o;
            return Double.compare(other.score, score);
        }
        return 0;
    }
}
