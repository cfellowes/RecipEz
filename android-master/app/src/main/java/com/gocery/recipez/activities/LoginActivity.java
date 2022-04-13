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
import com.gocery.recipez.forms.LoginForm;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class LoginActivity extends AppCompatActivity implements OnCompleteListener<AuthResult> {

    private LoginForm loginForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginForm = new LoginForm((EditText)findViewById(R.id.edit_text_email), (EditText)findViewById(R.id.edit_text_password));

        addActionListenerToPasswordField();
    }

    /**
     * Adds a listener to the password field to close the keyboard and press the login button upon submission
     */
    private void addActionListenerToPasswordField() {
        final EditText passwordEditText = findViewById(R.id.edit_text_password);
        final Button loginButton = findViewById(R.id.button_login);

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    loginButton.performClick();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    if (imm != null && getCurrentFocus() != null) {
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public void onClickForgotPassword(View view) {
        startActivity(new Intent(this, PasswordResetActivity.class));
    }

    public void onClickRegister(View view) {
        startActivity(new Intent(this, RegistrationActivity.class));
        finish();
    }

    public void onClickLogin(View view) {
        if (loginForm.isValid()) {
            try {
                disableLoginButton();
                Auth.getInstance().login(loginForm, this);
            }
            catch (Exception e) {
                onFailure();
            }
        }
        else {
            onInvalidInput();
        }
    }

    private void disableLoginButton() {
        Button loginButton = findViewById(R.id.button_login);
        loginButton.setText(R.string.button_login_in_progress);
        loginButton.setEnabled(false);
    }

    @Override
    public void onComplete(@NonNull Task<AuthResult> task) {
        if (task.isSuccessful()) {
            onSuccess();
        }
        else {
            onFailure();
        }
    }

    private void onSuccess() {
        startActivity(new Intent(this, LaunchActivity.class));
        finish();
    }

    private void onFailure() {
        setErrorMessage(R.string.msg_err_login_failure);
        resetLoginButton();
    }

    private void resetLoginButton() {
        Button loginButton = findViewById(R.id.button_login);
        loginButton.setText(R.string.button_login);
        loginButton.setEnabled(true);
    }

    private void onInvalidInput() {
        setErrorMessage(R.string.msg_err_login_invalid_input);
    }

    private void setErrorMessage(int stringId) {
        final TextView errorText = findViewById(R.id.text_message);
        errorText.setText(stringId);
    }
}
