package it.dii.unipi.trainerapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.ArrayList;

import it.dii.unipi.trainerapp.GATTserver.GATTServerActivity;
import it.dii.unipi.trainerapp.athlete.Athlete;
import it.dii.unipi.trainerapp.ui.AthleteAdapter;
import it.dii.unipi.trainerapp.ui.SettingsActivity;
import it.dii.unipi.trainerapp.utilities.Utility;

public class MainActivity extends GATTServerActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean settingsInizialized = false;
    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    private String trainerName;
    private String fileName = "settingsDump.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Devices with a display should not go to sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initializeAthletesList();

        //firstly checks whether the trainer name has already been set
        trainerName = Utility.readFromFile(this, fileName);

        if(!trainerName.equals("")){
            settingsInizialized=true;
        }

        if(!settingsInizialized) {
            someActivityResultLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            trainerName = data.getStringExtra("trainerName");
                            settingsInizialized = true;
                            Toast.makeText(getApplicationContext(),"Settings saved successfully!", Toast.LENGTH_SHORT).show();
                            TextView trainerNameLabel = (TextView) findViewById(R.id.welcomeLabel);
                            trainerNameLabel.append(trainerName);
                        }
                    });
            //send an intent to Settings Activity
            openSomeActivityForResult();
        }
    else {
            TextView trainerNameLabel = (TextView) findViewById(R.id.welcomeLabel);
            trainerNameLabel.append(trainerName);
        }
    }

    public void openSomeActivityForResult() {
        Intent intent = new Intent(this, SettingsActivity.class);
        someActivityResultLauncher.launch(intent);
    }

    //TODO: this method should be moved somewhere else, waiting for GATTServer class to become a Service
    public void initializeAthletesList() {
        Log.i(TAG, "initializeAthletesList()");
        // Construct the data source
        arrayOfAthletes = new ArrayList<>();
        // Create the adapter to convert the array to views
        adapter = new AthleteAdapter(this, arrayOfAthletes);
        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.lvAthletes);
        listView.setAdapter(adapter);
    }
}