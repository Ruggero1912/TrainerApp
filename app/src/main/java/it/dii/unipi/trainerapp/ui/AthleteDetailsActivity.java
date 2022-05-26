package it.dii.unipi.trainerapp.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import it.dii.unipi.trainerapp.R;
import it.dii.unipi.trainerapp.athlete.Athlete;
import it.dii.unipi.trainerapp.ui.fragments.AthleteDetailsFragment;

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

}