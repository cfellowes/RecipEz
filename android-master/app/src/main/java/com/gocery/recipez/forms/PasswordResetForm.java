package com.gocery.recipez.forms;

import android.util.Patterns;
import android.widget.EditText;

public class PasswordResetForm implements Form {
    private EditText email;

    public PasswordResetForm(EditText email) {
        this.email = email;
    }

    public String getEmail() {
        return email.getText().toString();
    }

    @Override
    public boolean isValid() {
        // Non-empty email matches pattern
        return email.length() > 0 && Patterns.EMAIL_ADDRESS.matcher(email.getText()).matches();
    }
}
