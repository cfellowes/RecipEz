package com.gocery.recipez.data;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

public abstract class DataReference<T> {

    protected T data;

    private final Class<T> typeClass;

    DataReference(Class<T> typeClass) {
        this.typeClass = typeClass;
    }

    final void initialize(final LoadDataListener<Boolean> listener) {
        DatabaseReference reference = getReference();

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                data = dataSnapshot.getValue(typeClass);
                listener.onLoad(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onLoad(false);
            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                data = dataSnapshot.getValue(typeClass);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    protected abstract DatabaseReference getReference();
}
