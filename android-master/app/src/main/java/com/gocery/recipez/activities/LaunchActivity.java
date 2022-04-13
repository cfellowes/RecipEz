package com.gocery.recipez.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

import com.gocery.recipez.Auth;
import com.gocery.recipez.R;
import com.gocery.recipez.data.LoadDataListener;
import com.gocery.recipez.data.User;
import com.gocery.recipez.http.RefreshApi;

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        if (Auth.getInstance().isUserLoggedIn()) {
            User.setInstance(new User(new LoadDataListener<Boolean>() {
                @Override
                public void onLoad(Boolean successful) {
                    if (successful) {
                        RefreshApi.getInstance().refresh();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                    finish();
                }
            }));
        }
        else {
            startActivity(new Intent(this, RegistrationActivity.class));
            finish();
        }

    }
}
