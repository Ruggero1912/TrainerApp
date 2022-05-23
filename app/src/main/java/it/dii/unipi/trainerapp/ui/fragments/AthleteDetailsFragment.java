package it.dii.unipi.trainerapp.ui.fragments;

import static it.dii.unipi.trainerapp.athlete.IntentMessagesManager.ATHLETE_INTENT_ACTION_TYPE_KEY;
import static it.dii.unipi.trainerapp.athlete.IntentMessagesManager.ATHLETE_INTENT_NAME;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import it.dii.unipi.trainerapp.R;
import it.dii.unipi.trainerapp.athlete.Athlete;
import it.dii.unipi.trainerapp.athlete.IntentMessagesManager;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AthleteDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AthleteDetailsFragment extends Fragment {

    private static final String TAG = AthleteDetailsFragment.class.getSimpleName();
    private static final String ARG_BOUNDED_ATHLETE = "bounded-athlete";

    private Athlete boundedAthlete;
    private Context ctx;
    private View fragmentView;


    public AthleteDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param athleteToBind the athlete whose details should be shown by the fragment
     * @return A new instance of fragment AthleteDetailsFragment.
     */
    public static AthleteDetailsFragment newInstance(Athlete athleteToBind) {
        AthleteDetailsFragment fragment = new AthleteDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BOUNDED_ATHLETE, athleteToBind);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            boundedAthlete = (Athlete) getArguments().getSerializable(ARG_BOUNDED_ATHLETE);
        }
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        ctx = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_athlete_details, container, false);

        TextView tvAthleteName = view.findViewById(R.id.detailsFragmentAthleteName);

        updateUI();

        LocalBroadcastManager.getInstance(ctx).registerReceiver(mMessageReceiver, new IntentFilter(ATHLETE_INTENT_NAME));
        //ctx.registerReceiver(mMessageReceiver, new IntentFilter(ATHLETE_INTENT_NAME));
        this.fragmentView = view;
        return view;
    }

    /**
     * updates the UI according to the attributes of the obj this.boundedAthlete
     */
    private void updateUI(){
        Log.v(TAG, "updateUI method has been called");
        if(this.boundedAthlete == null){
            Log.w(TAG, "empty athlete object!");
            return;
        }
        TextView tvName = fragmentView.findViewById(R.id.detailsFragmentAthleteName);
        tvName.setText(boundedAthlete.getName());
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "received some info from intent broadcast!");
            // Get extra data included in the Intent
            String action_to_perform = intent.getStringExtra(ATHLETE_INTENT_ACTION_TYPE_KEY);

            Athlete receivedAthlete = (Athlete) intent.getSerializableExtra(IntentMessagesManager.ATHLETE_INTENT_ATHLETE_OBJ_KEY);
            if(receivedAthlete.getAthleteID().toString().equals(boundedAthlete.getAthleteID().toString())){
                Log.i(TAG, "New information about the athlete received from the service -> performing " + action_to_perform);
            }
            else{
                // the update is about another athlete, not the one to which the fragment is bound
                Log.i(TAG, "those infos are not about my athlete! My athlete ID: " + boundedAthlete.getAthleteID().toString() + "| received Athlete ID: " + receivedAthlete.getAthleteID().toString());
                return;
            }


            switch (action_to_perform) {
                case IntentMessagesManager
                        .ATHLETE_INTENT_ACTION_ADD_OR_UPDATE_ATHLETE:
                    boundedAthlete = receivedAthlete;
                    updateUI();
                    break;
                case IntentMessagesManager.ATHLETE_INTENT_ACTION_REMOVE_ATHLETE:
                    Log.w(TAG, "the athlete of this fragment has been removed!");
                    //updateUI(); //for the moment the UI keeps the old values about the athlete
                    Toast.makeText(context, "The current athlete has been removed from the athlete list!", Toast.LENGTH_LONG).show();
                    // consider if closing the fragment and the current activity and moving back to MainActivity
                    break;
            }
        }
    };
}