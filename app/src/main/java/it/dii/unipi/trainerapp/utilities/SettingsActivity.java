package it.dii.unipi.trainerapp.utilities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import it.dii.unipi.trainerapp.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new it.dii.unipi.trainerapp.utilities.SettingsFragment())
                .commit();
    }
}