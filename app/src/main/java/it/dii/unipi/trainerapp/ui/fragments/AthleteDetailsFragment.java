package it.dii.unipi.trainerapp.ui.fragments;

import static it.dii.unipi.trainerapp.athlete.IntentMessagesManager.ATHLETE_INTENT_ACTION_TYPE_KEY;
import static it.dii.unipi.trainerapp.athlete.IntentMessagesManager.ATHLETE_INTENT_NAME;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NavigableMap;

import it.dii.unipi.trainerapp.R;
import it.dii.unipi.trainerapp.athlete.Athlete;
import it.dii.unipi.trainerapp.athlete.IntentMessagesManager;
import it.dii.unipi.trainerapp.utilities.Activity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AthleteDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AthleteDetailsFragment extends Fragment {

    private static final String TAG = AthleteDetailsFragment.class.getSimpleName();
    private static final String ARG_BOUNDED_ATHLETE = "bounded-athlete";
    private static final String CHART_TYPE_SPEED = "Speed";
    private static final String CHART_TYPE_HEART_RATE = "HeartRate";
    private static final String PLOT_TITLE_SPEED = "Speed";
    private static final String PLOT_TITLE_HEART_RATE = "Heart Rate";

    private Athlete boundedAthlete;
    private Context ctx;
    private View fragmentView;
    private XYPlot plot;


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

        Activity currentActivity = boundedAthlete.getCurrentActivity();
        Integer activityIcon = -1;
        if(currentActivity == null){
            Log.v(TAG, "currentActivity is not set for the athlete " + boundedAthlete.getAthleteID() + " using default");
            currentActivity = Activity.DEFAULT_ACTIVITY;
        }
        switch (currentActivity){
            case RUNNING:
                activityIcon = R.drawable.ic_run_24;
                break;
            case WALKING:
                activityIcon = R.drawable.ic_walk_24;
                break;
            case STANDING:
                activityIcon = R.drawable.ic_standing_24;
                break;
            default:
                Log.w(TAG, "activity not recognised, assigning default icon");
                activityIcon = R.drawable.ic_standing_24;
        }
        if(boundedAthlete.getConnectionStatus() == Athlete.CONNECTION_STATUS.AWAY){
            Log.v(TAG, "the athlete is away, will show the away icon instead of the activity icon");
            activityIcon = R.drawable.ic_signal_off_24;
        }
        ImageView athleteIcon = (ImageView) fragmentView.findViewById(R.id.detailsFragmentActivityIcon);
        athleteIcon.setImageResource(activityIcon);

        TextView tvID = fragmentView.findViewById(R.id.detailsFragmentID);
        tvID.setText(boundedAthlete.getAthleteID().toString());

        TextView tvConnectionStatus = fragmentView.findViewById(R.id.detailsFragmentAthleteConnectionStatus);
        tvConnectionStatus.setText(boundedAthlete.getConnectionStatus().toString());

        TextView tvLastSeen = fragmentView.findViewById(R.id.detailsFragmentAthleteLastSeen);
        String lastSeenString = "last seen: ";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            lastSeenString += boundedAthlete.getLastSeen().toLocalTime().truncatedTo(ChronoUnit.SECONDS).toString();
        }
        else{
            lastSeenString += "NA";
        }
        tvLastSeen.setText(lastSeenString);

        Double lastSpeedMeasure = boundedAthlete.getLastSpeedMeasurement();
        String lastSpeedMeasureString = "";
        if(lastSpeedMeasure == null){
            lastSpeedMeasureString = "NA km/h";
        }else{
            lastSpeedMeasureString = lastSpeedMeasure.toString() + " km/h";
        }
        TextView tvSpeed = fragmentView.findViewById(R.id.detailsFragmentSpeed);
        tvSpeed.setText(lastSpeedMeasureString);

        TextView heartRate = fragmentView.findViewById(R.id.detailsFragmentHeartRate);
        Integer lastHRMeasure = boundedAthlete.getLastHeartRateMeasurement();
        String lastHRMString = null;
        if(lastHRMeasure == null){
            lastHRMString = "NA";
        }else{
            lastHRMString = Integer.toString(lastHRMeasure);
        }
        heartRate.setText(lastHRMString);

        plotChart(boundedAthlete.getHeartRateHistory(), CHART_TYPE_HEART_RATE);
        plotChart(boundedAthlete.getSpeedHistory(), CHART_TYPE_SPEED);
    }

    //TODO: check whether this function actually plot the graph correctly
    private void plotChart(NavigableMap<?, ?> series, String chartType){
        XYSeries xySeries = null;
        LineAndPointFormatter seriesFormat = null;

        //converting the series of LocalDateTime values into a series of Long values
        List<LocalDateTime> dateTimestamps = new ArrayList<>((Collection<? extends LocalDateTime>) series.keySet());
        List<Long> timestamps = new ArrayList<>();
        for(LocalDateTime x : dateTimestamps){
            timestamps.add(x.toEpochSecond(ZoneOffset.UTC));
        }
        
        if(chartType.equals(CHART_TYPE_HEART_RATE)){
            plot = (XYPlot) fragmentView.findViewById(R.id.plotHeartRate);
            List<Integer> hr = new ArrayList<>((Collection<? extends Integer>) series.values());
            xySeries = new SimpleXYSeries(timestamps, hr, PLOT_TITLE_HEART_RATE);
            seriesFormat = new LineAndPointFormatter(Color.BLUE, null, null, null);
        }
        else if(chartType.equals(CHART_TYPE_SPEED)){
            plot = (XYPlot) fragmentView.findViewById(R.id.plotSpeed);
            List<Double> hr = new ArrayList<>((Collection<? extends Double>) series.values());
            xySeries = new SimpleXYSeries(timestamps, hr, PLOT_TITLE_SPEED);
            seriesFormat = new LineAndPointFormatter(Color.RED, null, null, null);
        }

        plot.clear();
        plot.addSeries(xySeries, seriesFormat);
        plot.redraw();
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