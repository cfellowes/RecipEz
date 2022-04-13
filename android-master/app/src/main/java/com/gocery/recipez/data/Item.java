package com.gocery.recipez.data;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;

public class Item extends DataReference<ItemData> {

    public interface SearchListener {
        void onSearchResult(String query, List<Item> items);
    }

    private DatabaseReference reference;

    private Item(DatabaseReference reference) {
        super(ItemData.class);
        this.reference = reference;
    }

    public static void loadItemById(String itemId, final LoadDataListener<Item> listener) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("items").child(itemId);
        final Item item = new Item(reference);
        item.initialize(new LoadDataListener<Boolean>() {
            @Override
            public void onLoad(Boolean payload) {
                if (payload) {
                    listener.onLoad(item);
                }
            }
        });
    }

    public static void searchByName(String prefix, final SearchListener listener) {
        final String query = prefix.toLowerCase();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("items");
        reference.orderByChild("name").startAt(query).endAt(query + "\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                final List<Item> items = new ArrayList<>();
                if (dataSnapshot.getChildrenCount() == 0) {
                    listener.onSearchResult(query, items);
                }

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    final Item item = new Item(child.getRef());
                    item.initialize(new LoadDataListener<Boolean>() {
                        @Override
                        public void onLoad(Boolean payload) {
                            items.add(item);
                            if (items.size() == dataSnapshot.getChildrenCount()) {
                                items.sort(new Comparator<Item>() {
                                    @Override
                                    public int compare(Item item1, Item item2) {
                                        int length1 = item1.getName().length();
                                        int length2 = item2.getName().length();
                                        if (length1 < length2) {
                                            return -1;
                                        }
                                        else if (length1 > length2) {
                                            return 1;
                                        }
                                        else {
                                            return item1.getName().compareTo(item2.getName());
                                        }
                                    }
                                });
                                listener.onSearchResult(query, items);
                            }
                        }
                    });
                }
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

    public String getName() {
        return data.name;
    }

    public String getUnit() {
        return data.unit;
    }
}
