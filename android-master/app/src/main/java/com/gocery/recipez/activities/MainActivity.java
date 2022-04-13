package com.gocery.recipez.activities;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

import com.gocery.recipez.R;
import com.gocery.recipez.data.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpBottomNavBar();

        displayWelcomeMessage();
    }

    private void setUpBottomNavBar() {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        NavigationUI.setupWithNavController(navView, navController);
    }

    private void displayWelcomeMessage() {
        String message = getResources().getString(R.string.msg_welcome_part_1)
                + User.getInstance().getName()
                + getResources().getString(R.string.msg_welcome_part_2);

        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
