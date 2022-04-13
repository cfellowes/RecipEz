package com.gocery.recipez.forms;

import android.util.Patterns;
import android.widget.EditText;

public class RegistrationForm implements Form {
    private EditText name;
    private EditText email;
    private EditText password;
    private EditText confirmPassword;

    public RegistrationForm(EditText name, EditText email, EditText password, EditText confirmPassword) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public String getName() {
        return name.getText().toString();
    }

    public String getEmail() {
        return email.getText().toString();
    }

    public String getPassword() {
        return password.getText().toString();
    }

    @Override
    public boolean isValid() {
        // Non-empty name
        if (name.length() == 0) {
            return false;
        }
        // Non-empty email matches pattern
        if (email.length() == 0 || !Patterns.EMAIL_ADDRESS.matcher(email.getText()).matches()) {
            return false;
        }
        // Non-empty passwords match
        return password.length() > 0 && password.getText().toString().equals(confirmPassword.getText().toString());
    }
}
