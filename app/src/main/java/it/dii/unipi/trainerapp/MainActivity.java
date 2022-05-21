package it.dii.unipi.trainerapp;

import android.app.Activity;
import android.content.Intent;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;

import it.dii.unipi.trainerapp.GATTserver.GATTServerActivity;
import it.dii.unipi.trainerapp.athlete.Athlete;
import it.dii.unipi.trainerapp.ui.AthleteAdapter;
import it.dii.unipi.trainerapp.ui.SettingsActivity;
import it.dii.unipi.trainerapp.utilities.Utility;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean settingsInizialized = false;
    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    private String trainerName;
    private String fileName = "settingsDump.txt";

    public static AthleteAdapter adapter; //should be private
    public static  ArrayList<Athlete> arrayOfAthletes; //should be private

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String action_to_perform = intent.getStringExtra("action-to-perform");
            Log.i(TAG, "New athlete received from the service -> performing " + action_to_perform);

            switch (action_to_perform) {
                case "add-athlete":
                    Athlete newAthlete = (Athlete) intent.getSerializableExtra("athlete");
                    Log.i(TAG, "Adding athlete: " + newAthlete.getName()
                            + ", position: " + adapter.getPosition(newAthlete));
                    if(adapter.getPosition(newAthlete)<0) {
                        adapter.add(newAthlete);
                        adapter.notifyDataSetChanged();
                    }
                    for(int i=0 ; i<adapter.getCount() ; i++){
                        Athlete obj = (Athlete) adapter.getItem(i);
                        Log.d(TAG, "Adapter item " + i + ": " + obj.getName());
                    }
                    break;
                case "update-athlete":
                    Athlete updatedAthlete = (Athlete) intent.getSerializableExtra("athlete");
                    Athlete target_athlete = (Athlete) intent.getSerializableExtra("athlete-to-remove");
                    Log.i(TAG, "Updating athlete: " + updatedAthlete.getName()
                            + ", position: " + adapter.getPosition(updatedAthlete));
                    adapter.remove(target_athlete);
                    adapter.add(updatedAthlete);
                    adapter.notifyDataSetChanged();
                    break;
                case "remove-athlete":
                    Athlete athleteMarkedAsAway = (Athlete) intent.getSerializableExtra("athlete");
                    Log.i(TAG, "removing athlete: " + athleteMarkedAsAway.getName()
                            + ", position: " + adapter.getPosition(athleteMarkedAsAway));
                    adapter.remove(athleteMarkedAsAway);
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Devices with a display should not go to sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //we need to start the GATTServer Service sending an Intent to it
        Intent intent = new Intent(this, GATTServerActivity.class);
        startService(intent);

        initializeAthletesList();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("update-athlete-list"));


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