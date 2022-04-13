package com.gocery.recipez.activities;

import androidx.appcompat.app.ActionBar;
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

import com.gocery.recipez.R;
import com.gocery.recipez.data.LoadDataListener;
import com.gocery.recipez.data.Pantry;
import com.gocery.recipez.data.User;
import com.gocery.recipez.forms.CreatePantryForm;

public class CreatePantryActivity extends AppCompatActivity {

    private CreatePantryForm form;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pantry);

        form = new CreatePantryForm((EditText) findViewById(R.id.edit_text_pantry_name));

        addActionListenerToPantryNameField();

        addBackButtonToToolbar();
    }

    /**
     * Adds a listener to the pantry name field to close the keyboard and press the create pantry button upon submission
     */
    private void addActionListenerToPantryNameField() {
        final EditText pantryNameEditText = findViewById(R.id.edit_text_pantry_name);
        final Button createPantryButton = findViewById(R.id.button_create_pantry);

        pantryNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    createPantryButton.performClick();
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

    private void addBackButtonToToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public void onClickCreatePantry(View view) {
        if (form.isValid()) {
            disableCreatePantryButton();
            Pantry.createPantry(form.getPantryName(), new LoadDataListener<Pantry>() {
                @Override
                public void onLoad(final Pantry pantry) {
                    User.getInstance().addPantry(pantry, new LoadDataListener<Pantry>() {
                        @Override
                        public void onLoad(Pantry payload) {
                            User.getInstance().setActivePantry(pantry, new LoadDataListener<Boolean>() {
                                @Override
                                public void onLoad(Boolean payload) {
                                    if (payload) {
                                        finish();
                                    }
                                }
                            });
                        }
                    });
                }
            });
        }
        else {
            displayErrorMessage();
        }
    }

    private void disableCreatePantryButton() {
        Button button = findViewById(R.id.button_create_pantry);
        button.setEnabled(false);
        button.setText(R.string.button_create_in_progress);
    }

    private void displayErrorMessage() {
        TextView errorText = findViewById(R.id.text_message);
        errorText.setText(R.string.msg_err_create_pantry_invalid_input);
    }
}
