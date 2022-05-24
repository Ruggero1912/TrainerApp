package it.dii.unipi.trainerapp;

import android.app.Activity;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;

import it.dii.unipi.trainerapp.GATTserver.GATTServerActivity;
import it.dii.unipi.trainerapp.athlete.Athlete;
import it.dii.unipi.trainerapp.athlete.IntentMessagesManager;
import it.dii.unipi.trainerapp.ui.AthleteAdapter;
import it.dii.unipi.trainerapp.ui.AthleteDetailsActivity;
import it.dii.unipi.trainerapp.ui.WelcomeActivity;
import it.dii.unipi.trainerapp.preferences.Preferences;
import it.dii.unipi.trainerapp.preferences.SettingsActivity;

public class MainActivity extends AppCompatActivity {

    private final String TRAINER_NAME_KEY = "trainerName";
    private final String ACTION_TO_PERFORM_KEY = "action-to-perform";
    private final String INTENT_ACTION = "update-athlete-list";

    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean settingsInizialized = false;
    private ActivityResultLauncher<Intent> welcomeActivityResultLauncher;
    public static AthleteAdapter adapter; //should be private
    public static  ArrayList<Athlete> arrayOfAthletes; //should be private
    private String trainerNameFirstLaunch;
    private String fileName = "settingsDump.txt";
    private Preferences myPreferences;
    private String insertedTrainerName;
    private boolean darkThemeEnabled;
    private TextView trainerName;

    private SharedPreferences.OnSharedPreferenceChangeListener sharedPrefListener =
        new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            //TRAINER NAME
            insertedTrainerName = Preferences.getTrainerName();
            updateTrainerName(insertedTrainerName);

            //DARK THEME
            darkThemeEnabled = Preferences.getDarkThemeValue();
            updateDarkTheme(darkThemeEnabled);

        }
    };

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String action_to_perform = intent.getStringExtra(ACTION_TO_PERFORM_KEY);
            Log.i(TAG, "New athlete received from the service -> performing " + action_to_perform);

            switch (action_to_perform) {
                case IntentMessagesManager
                        .ATHLETE_INTENT_ACTION_ADD_OR_UPDATE_ATHLETE:
                    Athlete receivedAthlete = (Athlete) intent.getSerializableExtra(IntentMessagesManager.ATHLETE_INTENT_ATHLETE_OBJ_KEY);
                    if(adapter.getPosition(receivedAthlete)<0) {
                        adapter.add(receivedAthlete);
                        adapter.notifyDataSetChanged();
                        Log.i(TAG, "Adding athlete: " + receivedAthlete.getName()
                                + ", position: " + adapter.getPosition(receivedAthlete));
                    }
                    else{
                        int athleteIndex = adapter.getPosition(receivedAthlete);
                        Log.i(TAG, "Updating athlete: " + receivedAthlete.getName()
                                + ", position: " + athleteIndex);
                        arrayOfAthletes.set(athleteIndex, receivedAthlete);
                        adapter.notifyDataSetChanged();
                    }
                    break;
                case IntentMessagesManager.ATHLETE_INTENT_ACTION_REMOVE_ATHLETE:
                    Athlete athleteMarkedAsAway = (Athlete) intent.getSerializableExtra(IntentMessagesManager.ATHLETE_INTENT_ATHLETE_OBJ_KEY);
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
        myPreferences = Preferences.getPreferences(this);
        trainerName = findViewById(R.id.welcomeLabel);
        myPreferences.registerOnSharedPreferenceChangeListener(sharedPrefListener); // register for changes on preferences
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Devices with a display should not go to sleep

        //we need to start the GATTServer Service sending an Intent to it
        Intent intent = new Intent(this, GATTServerActivity.class);
        startService(intent);

        initializeAthletesList();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(INTENT_ACTION));


        //firstly checks whether the trainer name has already been set
        trainerNameFirstLaunch = Preferences.getTrainerName();

        if(!trainerNameFirstLaunch.equals("Name not found")){
            settingsInizialized=true;
        }

        if(!settingsInizialized) {
            welcomeActivityResultLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            trainerNameFirstLaunch = data.getStringExtra(TRAINER_NAME_KEY);
                            settingsInizialized = true;
                            Toast.makeText(getApplicationContext(),"Name saved successfully!", Toast.LENGTH_SHORT).show();
                        }
                    });
            //send an intent to Welcome Activity
            openWelcomeActivityForResult();
        }
        else {
            TextView trainerNameFirstLaunchLabel = (TextView) findViewById(R.id.welcomeLabel);
            trainerNameFirstLaunchLabel.append(trainerNameFirstLaunch);
        }

        darkThemeEnabled = Preferences.getDarkThemeValue();
        updateDarkTheme(darkThemeEnabled);
    }

    public void updateTrainerName(String name) {
        trainerName.setText("Welcome "+name);
    }

    public void updateDarkTheme(boolean darkThemeEnabled) {
        if (darkThemeEnabled){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    // function that automatically inflates the navbar on the top
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // send an intent to start SettingsActivity that shows the preference list
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openWelcomeActivityForResult() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        welcomeActivityResultLauncher.launch(intent);
    }

    public void initializeAthletesList() {
        Log.i(TAG, "initializeAthletesList()");
        // Construct the data source
        arrayOfAthletes = new ArrayList<>();
        // Create the adapter to convert the array to views
        adapter = new AthleteAdapter(this, arrayOfAthletes);
        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.lvAthletes);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), AthleteDetailsActivity.class);
                Bundle b = new Bundle();
                b.putSerializable(AthleteDetailsActivity.ATHLETE_OBJ_KEY, arrayOfAthletes.get(i));
                intent.putExtras(b);
                Log.d(TAG, "starting the activity athleteDetailsActivity using an intent and passed the athlete '" + arrayOfAthletes.get(i).getName() + "' to it whose index is " + i);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "MainActivity.onDestroy has been called");

        Intent intent = new Intent(this, GATTServerActivity.class);
        stopService(intent);

    }
}