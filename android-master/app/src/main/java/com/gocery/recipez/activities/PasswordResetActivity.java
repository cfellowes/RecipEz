package com.gocery.recipez.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
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
import com.gocery.recipez.forms.PasswordResetForm;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class PasswordResetActivity extends AppCompatActivity implements OnCompleteListener<Void> {

    private PasswordResetForm resetForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        resetForm = new PasswordResetForm((EditText) findViewById(R.id.edit_text_email));

        addActionListenerToEmailField();
    }

    /**
     * Adds a listener to the email field to close the keyboard and press the send button upon submission
     */
    private void addActionListenerToEmailField() {
        final EditText email = findViewById(R.id.edit_text_email);
        final Button sendButton = findViewById(R.id.button_send_reset_link);

        email.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    sendButton.performClick();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
    }

    public void onClickSendResetLink(View view) {
        if (resetForm.isValid()) {
            try {
                Auth.getInstance().sendPasswordResetLink(resetForm, this);
            }
            catch (Exception e) {
                onFailure();
            }
        }
        else {
            onInvalidInput();
        }
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()) {
            onSuccess();
        }
        else {
            onFailure();
        }
    }

    private void onSuccess() {
        setSuccessMessage(R.string.msg_success_reset_password);
    }

    private void onFailure() {
        setErrorMessage(R.string.msg_err_reset_failure);
    }

    private void onInvalidInput() {
        setErrorMessage(R.string.msg_err_reset_invalid_input);
    }

    private void setSuccessMessage(int stringId) {
        TextView message = findViewById(R.id.text_message);
        message.setText(stringId);
        message.setTextColor(getResources().getColor(R.color.colorSuccess));
    }

    private void setErrorMessage(int stringId) {
        TextView message = findViewById(R.id.text_message);
        message.setText(stringId);
        message.setTextColor(getResources().getColor(R.color.colorError));
    }
}
