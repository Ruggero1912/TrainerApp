package it.dii.unipi.trainerapp.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.WindowManager;

import it.dii.unipi.trainerapp.GATTserver.GATTServerService;
import it.dii.unipi.trainerapp.R;
import it.dii.unipi.trainerapp.athlete.Athlete;
import it.dii.unipi.trainerapp.ui.fragments.AthleteDetailsFragment;
import it.dii.unipi.trainerapp.utilities.ServiceStatus;

public class AthleteDetailsActivity extends AppCompatActivity {
    private static final String TAG = AthleteDetailsActivity.class.getSimpleName();
    public static final String ATHLETE_OBJ_KEY = "athlete";

    private Athlete athleteObj;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_athlete_details);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Devices with a display should not go to sleep
        Bundle receivedBundle = getIntent().getExtras();
        if(receivedBundle != null){
            this.athleteObj = (Athlete) receivedBundle.getSerializable(ATHLETE_OBJ_KEY);
            if(this.athleteObj == null){
                Log.w(TAG, "Received empty athlete obj from intent");
            }
        }else{
            Log.w(TAG, "starting the activity without bundle received from intent");
        }

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container_view_tag, AthleteDetailsFragment.newInstance(this.athleteObj))
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "AthleteDetailsActivity going to register receiver for GATTServerStatus");
        LocalBroadcastManager.getInstance(this).registerReceiver(GATTServerStatusBroadcastReceiver, new IntentFilter(GATTServerService.GATT_SERVER_STATUS_ACTION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "AthleteDetailsActivity going to unregister receiver for GATTServerStatus");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(GATTServerStatusBroadcastReceiver);
    }

    private ServiceStatus GATTServerServiceStatus = ServiceStatus.UNKNOWN;
    private boolean pendingStartServiceCommandGATTServer = false;

    private void startGATTServerService(){
        if(GATTServerServiceStatus != ServiceStatus.RUNNING) {
            if( ! pendingStartServiceCommandGATTServer) {
                Log.v(TAG, "startGATTServerService: going to send a startService Intent to GATTServer");
                Intent intent = new Intent(this, GATTServerService.class);
                startService(intent);
                pendingStartServiceCommandGATTServer = true;
            }else {
                Log.d(TAG, "since there is already a pending start service command, will not broadcast startService for GATTServerService again");
            }
        }else{
            Log.d(TAG, "startGATTServerService not going to send a startService intent since the server is already running");
        }
    }

    private BroadcastReceiver GATTServerStatusBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ServiceStatus newStatus = (ServiceStatus) intent.getSerializableExtra(GATTServerService.SERVICE_STATUS_KEY);
            GATTServerServiceStatus = newStatus;
            Log.d(TAG, "received a new state for the GATTServerService; now it is: " + GATTServerServiceStatus.name());
            if(newStatus == ServiceStatus.TERMINATED){
                Log.e(TAG, "The GATTServer has just TERMINATED. This should not happen when AthleteDetailsActivity is foreground, going to restart it");
                startGATTServerService();
            }
        }
    };

}