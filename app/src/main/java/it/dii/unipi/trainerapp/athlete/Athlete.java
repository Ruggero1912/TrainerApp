package it.dii.unipi.trainerapp.athlete;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.NavigableMap;
import java.util.TreeMap;

import it.dii.unipi.trainerapp.utilities.AthleteActivityType;
import it.dii.unipi.trainerapp.utilities.DeviceID;

public class Athlete implements Serializable, Comparable {
    private static final String TAG = Athlete.class.getSimpleName();

    private static final boolean SHOULD_LOG_EMPTY_ACCESSES = false;


    private DeviceID athleteID;
    private String name;
    private LocalDateTime firstConnection;
    private LocalDateTime lastSeen;
    private CONNECTION_STATUS currentConnectionStatus;
    private NavigableMap<LocalDateTime, Integer> heartRateHistory = new TreeMap<>();
    private NavigableMap<LocalDateTime, Double> speedHistory = new TreeMap<>();
    private NavigableMap<LocalDateTime, AthleteActivityType> activityHistory = new TreeMap<>();
    private NavigableMap<LocalDateTime, Integer> stepCounterHistory = new TreeMap<>();
    private NavigableMap<LocalDateTime, Double> peaceHistory = new TreeMap<>();


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
        updateHeartRateEMA();
    }
    public void storeSpeedMeasurement(Double currentSpeed, LocalDateTime time){
        speedHistory.put(time, currentSpeed);
    }
    public void storeRecognizedActivity(AthleteActivityType activity, LocalDateTime time){
        activityHistory.put(time, activity);
    }
    public void storeStepCounterMeasurement(Integer stepCount, LocalDateTime time){
        stepCounterHistory.put(time, stepCount);
    }
    public void storePeaceMeasurement(Double currentPeace, LocalDateTime time){
        peaceHistory.put(time, currentPeace);
    }
    public Integer getLastHeartRateMeasurement(){
        if(heartRateHistory.isEmpty()){
            LogAccessingEmptyResource(TAG, "trying to access to heart rate measure for empty set for the athlete ID: '" + this.athleteID + "'");
            return null;
        }
        return heartRateHistory.lastEntry().getValue();
    }

    private static final int HR_WINDOW_SIZE = 10;
    private static final int HR_SMOOTHING_FACTOR = 2;

    private Double lastHeartRateEMA = null;

    /**
     *
     * @return the exponential moving average for the heart rate measurement for the athlete
     * if the number of HR measures is lower than the window size, return null
     */
    public Double getHeartRateEMA(){
        return lastHeartRateEMA;
    }

    private Double updateHeartRateEMA(){
        if(heartRateHistory.size() < HR_WINDOW_SIZE){
            Log.v(TAG, "calculateHeartRateEMA: since the hrHistory size is lower than WINDOW_SIZE will not calculate EMA, returning null");
            return null;
            //return Double.valueOf(getLastHeartRateMeasurement());
        }
        if(lastHeartRateEMA == null){
            lastHeartRateEMA = calculateHeartRateSMA();
            return lastHeartRateEMA;
        }
        Double currentHR = Double.valueOf( getLastHeartRateMeasurement() );
        final double k = HR_SMOOTHING_FACTOR / Double.valueOf( 1 + HR_WINDOW_SIZE);
        lastHeartRateEMA = currentHR * (k) + lastHeartRateEMA * (1 - k);
        return lastHeartRateEMA;
    }

    /**
     * calculate the Simple Moving Average for the heart rate using a window of WINDOW_SIZE
     * @return
     */
    private Double calculateHeartRateSMA(){
        int counter = 0;
        int sum = 0;
        for (int hrMeasure:
             heartRateHistory.descendingMap().values()) {
            if(counter >= HR_WINDOW_SIZE){
                break;
            }
            sum += hrMeasure;
            counter++;
        }
        return sum / Double.valueOf(counter) ;
    }

    public Double getLastSpeedMeasurement(){
        if(speedHistory.isEmpty()){
            LogAccessingEmptyResource(TAG, "trying to access to speed measure for empty set for the athlete ID: '" + this.athleteID + "'");
            return null;
        }
        return speedHistory.lastEntry().getValue();
    }
    public void setCurrentActivity(AthleteActivityType athleteActivityType, LocalDateTime time){
        this.activityHistory.put(time, athleteActivityType);
    }

    public AthleteActivityType getCurrentActivity(){
        if(activityHistory.isEmpty()){
            LogAccessingEmptyResource(TAG, "trying to access to activity info for empty set for the athlete ID: '" + this.athleteID + "'");
            return null;
        }
        return activityHistory.lastEntry().getValue();
    }

    public Integer getLastStepsMeasurement(){
        if(stepCounterHistory.isEmpty()){
            LogAccessingEmptyResource(TAG, "trying to access to steps for empty set for the athlete ID: '" + this.athleteID + "'");
            return null;
        }
        return stepCounterHistory.lastEntry().getValue();
    }

    public NavigableMap<LocalDateTime, Integer> getHeartRateHistory(){
        if(heartRateHistory.isEmpty()){
            LogAccessingEmptyResource(TAG, "trying to access to heart rate info for empty set for the athlete ID: '" + this.athleteID + "'");
        }
        return heartRateHistory;
    }

    public NavigableMap<LocalDateTime, Double> getSpeedHistory(){
        if(speedHistory.isEmpty()){
            LogAccessingEmptyResource(TAG, "trying to access to speed history for empty set for the athlete ID: '" + this.athleteID + "'");
        }
        return speedHistory;
    }

    private void LogAccessingEmptyResource(String TAG, String logRow){
        if(SHOULD_LOG_EMPTY_ACCESSES){
            Log.v(TAG, logRow);
        }
    }

    /**
     * method used to sort an ArrayList of Athletes.
     * It orders them by their statistics
     * @param o
     * @return a positive number if this should go after Athlete b, else a negative number
     */
    @SuppressLint("NewApi")
    @Override
    public int compareTo(Object o) {
        Athlete b = (Athlete) o;    // ! good
        //if the athlete is not initialized, it should go at the bottom of the list
        if(this.isInitialized() == false){
            return 1;
        }
        if(b.isInitialized() == false){
            return -1;
        }
        //the connected athlete are shown before the away athlete
        if(this.getConnectionStatus() != b.getConnectionStatus()) {
            if(this.getConnectionStatus() == CONNECTION_STATUS.CONNECTED){
                return -1;
            }else{
                // case in which this is AWAY and b is connected
                return 1;
            }
        }

        Double thisEMA = this.getHeartRateEMA();
        Double otherEMA = b.getHeartRateEMA();

        if(thisEMA != null){
            if(otherEMA == null){
                return -1;
            }
            if(thisEMA > otherEMA){
                return -1;
            }else if(thisEMA < otherEMA){
                return 1;
            }
        }else if(otherEMA != null){
            return 1;
        }

        // case in which both athletes are initialized, both connected (or both away)
        // and both do not have hr EMA

        // will sort by firstConnection ts
        if(this.getFirstConnection() == null){
            if(this.getName().compareTo(b.getName()) > 0){
                return 1;
            }
            return -1;
        }
        if( this.getFirstConnection().isAfter( b.getFirstConnection() ) ){
            //first the new devices
            return -1;
        }else{
            return 1;
        }
    }
}
