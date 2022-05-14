package it.dii.unipi.trainerapp.athlete;

import android.util.Log;

import java.time.LocalDateTime;
import java.util.NavigableMap;
import java.util.TreeMap;

import it.dii.unipi.trainerapp.utilities.DeviceID;

public class Athlete {
    private static final String TAG = Athlete.class.getSimpleName();

    private DeviceID athleteID;
    private String name;
    private NavigableMap<LocalDateTime, Integer> heartRateHistory = new TreeMap<>();
    private NavigableMap<LocalDateTime, Double> speedHistory = new TreeMap<>();

    private Integer currentHeartRate;
    private Double currentSpeed;

    private static final String NAME_NOT_SET = "NAME NOT SET";

    public Athlete(DeviceID athleteID){
        this.athleteID = athleteID;
    }

    public DeviceID getAthleteID(){
        return this.athleteID;
    }

    public String getName(){
        if(this.name == null){
            return NAME_NOT_SET;
        }
        return this.name;
    }
    public void setName(String name){
        if(this.name != null){
            Log.v(TAG, "Going to update the name for the athlete from '" + this.name + "' to '" + name + "'");
        }
        this.name = name;
    }
    public void storeHeartRateMeasurement(Integer heartRateMeasurement, LocalDateTime time){
        heartRateHistory.put(time, heartRateMeasurement);
    }
    public Integer getLastHeartRateMeasurement(){
        if(heartRateHistory.isEmpty()){
            Log.i(TAG, "trying to access to heart rate measure for empty set for the athlete ID: '" + this.athleteID + "'");
            return null;
        }
        return heartRateHistory.lastEntry().getValue();
    }
    public void storeSpeedMeasurement(Double currentSpeed, LocalDateTime time){
        speedHistory.put(time, currentSpeed);
    }
    public Double getLastSpeedMeasurement(){
        if(speedHistory.isEmpty()){
            Log.i(TAG, "trying to access to speed measure for empty set for the athlete ID: '" + this.athleteID + "'");
            return null;
        }
        return speedHistory.lastEntry().getValue();
    }
}
