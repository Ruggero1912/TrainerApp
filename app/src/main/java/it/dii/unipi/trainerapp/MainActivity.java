package it.dii.unipi.trainerapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ListView;

import java.util.ArrayList;

import it.dii.unipi.trainerapp.GATTserver.GATTServerActivity;
import it.dii.unipi.trainerapp.athlete.Athlete;
import it.dii.unipi.trainerapp.ui.AthleteAdapter;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Devices with a display should not go to sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //we need to start the GATTServer Service sending an Intent to it
        Intent intent = new Intent(this, GATTServerActivity.class);
        startService(intent);

        //initializeAthletesList();
    }

    //this method should be in GATTServerActivity, and called by the OnCreate method above,
    //but doing so it ends up giving an error about threads execution :(
    /*public void initializeAthletesList() {
        Log.i(TAG, "initializeAthletesList()");
        // Construct the data source
        arrayOfAthletes = new ArrayList<>();
        // Create the adapter to convert the array to views
        adapter = new AthleteAdapter(this, arrayOfAthletes);
        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.lvAthletes);
        listView.setAdapter(adapter);
    }*/
}