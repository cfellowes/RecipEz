package com.gocery.recipez.forms;

import android.widget.EditText;

public class CreatePantryForm implements Form {

    private EditText pantryName;

    public CreatePantryForm(EditText pantryName) {
        this.pantryName = pantryName;
    }

    public String getPantryName() {
        return pantryName.getText().toString();
    }

    @Override
    public boolean isValid() {
        // Non-empty pantry name
        return pantryName.length() > 0;
    }
}
