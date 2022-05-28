package it.dii.unipi.trainerapp.ui;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import it.dii.unipi.trainerapp.R;
import it.dii.unipi.trainerapp.athlete.Athlete;
import it.dii.unipi.trainerapp.utilities.AthleteActivityType;

public class AthleteAdapter extends ArrayAdapter<Athlete> {
    private static String TAG = AthleteAdapter.class.getSimpleName();

    public AthleteAdapter(Context context, ArrayList<Athlete> athletes) {
        super(context, 0, athletes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        Athlete a = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        boolean isNew = false;
        if (convertView == null) {
            isNew = true;
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.athlete_item, parent, false);
        }


        if( ! a.isInitialized() ){
            Log.d(TAG, "skipping the device '" + a.getAthleteID() + "' since it is not initialized");
            convertView.setVisibility(View.INVISIBLE);
            return convertView;
        }else{
            Log.v(TAG, "going to create or update a view for the given athlete " + a.getAthleteID());
            convertView.setVisibility(View.VISIBLE);
        }

        Athlete.TRAINING_STATUS currentTrainingStatus = a.getCurrentTrainingStatus();

        switch (currentTrainingStatus){
            case REST:
                convertView.setBackgroundResource(R.drawable.grey_gradient_color);
                break;
            case LOW:
                convertView.setBackgroundResource(R.drawable.green_gradient_color);
                break;
            case GOOD:
                convertView.setBackgroundResource(R.drawable.blue_gradient_color);
                break;
            case OVER_TRAINING:
                convertView.setBackgroundResource(R.drawable.red_gradient_color);
                break;
            default:
                Log.v(TAG, "athlete adapter unrecognised TRAINING_STATUS");
        }

        AthleteActivityType currentAthleteActivityType = a.getCurrentActivity();
        Integer activityIcon = -1;
        if(currentAthleteActivityType == null){
            Log.v(TAG, "currentActivity is not set for the athlete " + a.getAthleteID() + " using default");
            currentAthleteActivityType = AthleteActivityType.defaultAthleteActivityType;
        }
        //note that the icon is set to the current activity or, if the athlete is away, to the away icon
        switch (currentAthleteActivityType){
            case RUNNING:
                activityIcon = R.drawable.ic_run_24;
                break;
            case WALKING:
                activityIcon = R.drawable.ic_walk_24;
                break;
            case STILL:
                activityIcon = R.drawable.ic_standing_24;
                break;
            default:
                Log.w(TAG, "activity not recognised, assigning default icon");
                activityIcon = R.drawable.ic_standing_24;
        }
        if(a.getConnectionStatus() == Athlete.CONNECTION_STATUS.AWAY){
            Log.v(TAG, "the athlete is away, will show the away icon instead of the activity icon");
            activityIcon = R.drawable.ic_signal_off_24;
        }
        ImageView athleteIcon = (ImageView) convertView.findViewById(R.id.imgIcon);
        athleteIcon.setImageResource(activityIcon);


        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        // Populate the data into the template view using the data object
        tvName.setText(a.getName());

        TextView tvID = (TextView) convertView.findViewById(R.id.tvID);
        tvID.setText(a.getAthleteID().toString());

        TextView tvLastSeen = (TextView) convertView.findViewById(R.id.tvLastSeen);
        String lastSeenString = "last seen: ";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            lastSeenString += a.getLastSeen().toLocalTime().truncatedTo(ChronoUnit.SECONDS).toString();
        }
        else{
            lastSeenString += "NA";
        }
        tvLastSeen.setText(lastSeenString);

        Double lastSpeedMeasure = a.getLastSpeedMeasurement();
        String lastSpeedMeasureString = "";
        if(lastSpeedMeasure == null){
            lastSpeedMeasureString = "NA km/h";
        }else{
            lastSpeedMeasureString = lastSpeedMeasure.toString() + " km/h";
        }

        TextView tvSpeed = (TextView) convertView.findViewById(R.id.tvSpeed);
        tvSpeed.setText(lastSpeedMeasureString);

        TextView heartRate = (TextView) convertView.findViewById(R.id.tvHeartRate);
        String lastHRMString = null;
        Double HRMeasure = a.getHeartRateEMA();
        if(HRMeasure == null){
            //case in which the EMA is not available yet
            Integer lastHRM = a.getLastHeartRateMeasurement();
            if(lastHRM == null){
                lastHRMString = "NA";
            }
            else{
                lastHRMString = lastHRM.toString();
            }
        }else{
            lastHRMString = String.format("%.1f", HRMeasure);//Double.toString(HRMeasure);
        }

        heartRate.setText(lastHRMString);
        heartRate.setBackgroundResource(R.drawable.heart_rate_animation_drawable);
        AnimationDrawable frameAnimation = (AnimationDrawable) heartRate.getBackground();
        if(isNew || (! frameAnimation.isRunning() && a.getConnectionStatus() == Athlete.CONNECTION_STATUS.CONNECTED && ! lastHRMString.equals("NA")))
            frameAnimation.start();
        //https://developer.android.com/reference/android/graphics/drawable/AnimationDrawable

        // Return the completed view to render on screen
        return convertView;
    }
}
