package it.dii.unipi.trainerapp.athlete;

import android.util.Log;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.NavigableMap;
import java.util.TreeMap;

import it.dii.unipi.trainerapp.utilities.AthleteActivityType;
import it.dii.unipi.trainerapp.utilities.DeviceID;

public class Athlete implements Serializable {
    private static final String TAG = Athlete.class.getSimpleName();


    private DeviceID athleteID;
    private String name;
    private LocalDateTime firstConnection;
    private LocalDateTime lastSeen;
    private CONNECTION_STATUS currentConnectionStatus;
    private NavigableMap<LocalDateTime, Integer> heartRateHistory = new TreeMap<>();
    private NavigableMap<LocalDateTime, Double> speedHistory = new TreeMap<>();
    private NavigableMap<LocalDateTime, AthleteActivityType> activityHistory = new TreeMap<>();


    private static final String NAME_NOT_SET = "NAME NOT SET";

    public enum CONNECTION_STATUS{
        CONNECTED,
        AWAY;
    }

    public Athlete(DeviceID athleteID){
        this.athleteID = athleteID;
    }

    public boolean updateFirstConnection(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            this.firstConnection = LocalDateTime.now();
            return true;
        }else{
            Log.e(TAG, "cannot log the LocalDateTime of the first connection!");
            return false;
        }
    }

    public LocalDateTime getFirstConnection(){
        return this.firstConnection;
    }

    public boolean updateLastSeen(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            this.lastSeen = LocalDateTime.now();
            return true;
        }else{
            Log.e(TAG, "cannot log the LocalDateTime of the last seen!");
            return false;
        }
    }

    public LocalDateTime getLastSeen(){
        return this.lastSeen;
    }

    public void updateConnectionStatus(CONNECTION_STATUS connectionStatus){
        this.currentConnectionStatus = connectionStatus;
    }

    public CONNECTION_STATUS getConnectionStatus(){
        return this.currentConnectionStatus;
    }

    /**
     * checks if the athlete has stored some information or if is empty
     * in this latter case it is not considered as valid
     *      (it could be a third device connected to the current device's GATT server that has nothing toi do with the app)
     * @return true if isInitialized, else false
     */
    public boolean isInitialized(){
        if(this.name != null){
            return true;
        }
        return false;
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
            Log.v(TAG, "trying to access to heart rate measure for empty set for the athlete ID: '" + this.athleteID + "'");
            return null;
        }
        return heartRateHistory.lastEntry().getValue();
    }
    public void storeSpeedMeasurement(Double currentSpeed, LocalDateTime time){
        speedHistory.put(time, currentSpeed);
    }
    public Double getLastSpeedMeasurement(){
        if(speedHistory.isEmpty()){
            Log.v(TAG, "trying to access to speed measure for empty set for the athlete ID: '" + this.athleteID + "'");
            return null;
        }
        return speedHistory.lastEntry().getValue();
    }
    public void setCurrentActivity(AthleteActivityType athleteActivityType, LocalDateTime time){
        this.activityHistory.put(time, athleteActivityType);
    }

    public AthleteActivityType getCurrentActivity(){
        if(activityHistory.isEmpty()){
            Log.v(TAG, "trying to access to activity info for empty set for the athlete ID: '" + this.athleteID + "'");
            return null;
        }
        return activityHistory.lastEntry().getValue();
    }

    public NavigableMap<LocalDateTime, Integer> getHeartRateHistory(){
        if(heartRateHistory.isEmpty()){
            Log.v(TAG, "trying to access to heart rate info for empty set for the athlete ID: '" + this.athleteID + "'");
        }
        return heartRateHistory;
    }

    public NavigableMap<LocalDateTime, Double> getSpeedHistory(){
        if(speedHistory.isEmpty()){
            Log.v(TAG, "trying to access to speed history for empty set for the athlete ID: '" + this.athleteID + "'");
        }
        return speedHistory;
    }
}
