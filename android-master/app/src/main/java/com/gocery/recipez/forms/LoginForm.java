package com.gocery.recipez.forms;

import android.util.Patterns;
import android.widget.EditText;

public class LoginForm implements Form {
    private EditText email;
    private EditText password;

    public LoginForm(EditText email, EditText password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email.getText().toString();
    }

    public String getPassword() {
        return password.getText().toString();
    }

    @Override
    public boolean isValid() {
        // Non-empty email matches pattern
        if (email.length() == 0 || !Patterns.EMAIL_ADDRESS.matcher(email.getText()).matches()) {
            return false;
        }
        // Non-empty password
        return password.length() > 0;
    }
}
