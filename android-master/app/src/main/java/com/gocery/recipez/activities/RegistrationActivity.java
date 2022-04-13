package com.gocery.recipez.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gocery.recipez.Auth;
import com.gocery.recipez.R;
import com.gocery.recipez.data.LoadDataListener;
import com.gocery.recipez.data.User;
import com.gocery.recipez.forms.RegistrationForm;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class RegistrationActivity extends AppCompatActivity implements OnCompleteListener<AuthResult> {

    private RegistrationForm registrationForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        registrationForm = new RegistrationForm((EditText) findViewById(R.id.edit_text_name), (EditText) findViewById(R.id.edit_text_email),
                (EditText) findViewById(R.id.edit_text_password), (EditText) findViewById(R.id.edit_text_confirm_password));

        addActionListenerToConfirmPasswordField();
    }

    /**
     * Adds a listener to the confirm password field to close the keyboard and press the register button upon submission
     */
    private void addActionListenerToConfirmPasswordField() {
        final EditText confirmPassword = findViewById(R.id.edit_text_confirm_password);
        final Button registerButton = findViewById(R.id.button_register);

        confirmPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    registerButton.performClick();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    if (imm != null && getCurrentFocus() != null) {
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public void onClickLogin(View view) {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public void onClickRegister(View view) {
        if (registrationForm.isValid()) {
            try {
                disableRegisterButton();
                Auth.getInstance().createUserAndLogin(registrationForm, this);
            } catch (Exception e) {
                onFailure();
            }
        } else {
            onInvalidInput();
        }
    }

    private void disableRegisterButton() {
        Button registerButton = findViewById(R.id.button_register);
        registerButton.setText(R.string.button_register_in_progress);
        registerButton.setEnabled(false);
    }

    @Override
    public void onComplete(@NonNull Task<AuthResult> task) {
        if (task.isSuccessful()) {
            User.getInstance().setName(registrationForm.getName(), new LoadDataListener<Boolean>() {
                @Override
                public void onLoad(Boolean successful) {
                    if (successful) {
                        onSuccess();
                    } else {
                        onFailure();
                    }
                }
            });
        } else {
            onFailure();
        }
    }

    private void onSuccess() {
        startActivity(new Intent(this, LaunchActivity.class));
        finish();
    }

    private void onFailure() {
        setErrorMessage(R.string.msg_err_registration_failure);
        resetRegisterButton();
    }

    private void resetRegisterButton() {
        Button registerButton = findViewById(R.id.button_register);
        registerButton.setText(R.string.button_register);
        registerButton.setEnabled(true);
    }

    private void onInvalidInput() {
        setErrorMessage(R.string.msg_err_registration_invalid_input);
    }

    private void setErrorMessage(int stringId) {
        TextView message = findViewById(R.id.text_message);
        message.setText(stringId);
        message.setTextColor(getResources().getColor(R.color.colorError));
    }
}
