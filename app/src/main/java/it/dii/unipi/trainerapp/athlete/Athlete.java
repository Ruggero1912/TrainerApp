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
    private NavigableMap<LocalDateTime, Double> paceHistory = new TreeMap<>();
    private NavigableMap<LocalDateTime, Double> totalDistanceHistory = new TreeMap<>();


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
    public void storePaceMeasurement(Double currentPace, LocalDateTime time){
        paceHistory.put(time, currentPace);
    }
    public void storeTotalDistanceMeasurement(Double totalDistance, LocalDateTime time){
        totalDistanceHistory.put(time, totalDistance);
    }
    public Double getLastTotalDistanceMeasurement(){
        if(totalDistanceHistory.isEmpty()){
            LogAccessingEmptyResource(TAG, "trying to access to total distance measure for empty set for the athlete ID: '" + this.athleteID + "'");
            return null;
        }
        return totalDistanceHistory.lastEntry().getValue();
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

    /**
     *
     * @return the EMA for heart rate or last hr measure if EMA is not available
     *  if also the lastHRMeasure is null, returns null
     */
    public Double getHeartRateEMAorLastHR(){
        if(getHeartRateEMA() == null){
            return ( getLastHeartRateMeasurement() != null ? Double.valueOf( getLastHeartRateMeasurement() ) : null );
        }
        return getHeartRateEMA();
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

    public enum TRAINING_STATUS{
        OVER_TRAINING,
        GOOD,
        LOW,
        REST;

        public static TRAINING_STATUS DEFAULT = REST;

        public static Double OVER_TRAINING_HR_THRESHOLD = 150.0;
        public static Double GOOD_TRAINING_HR_THRESHOLD = 100.0;
        public static Double LOW_TRAINING_HR_THRESHOLD = 80.0;
    }

    /**
     *
     * @return the current training status according to the available statistics
     */
    public TRAINING_STATUS getCurrentTrainingStatus(){
        if( ! isInitialized() ){
            return TRAINING_STATUS.DEFAULT;
        }
        Double hr = getHeartRateEMAorLastHR();
        if(hr == null){
            return TRAINING_STATUS.DEFAULT;
        }
        if(hr <= TRAINING_STATUS.LOW_TRAINING_HR_THRESHOLD){
            return TRAINING_STATUS.REST;
        }else if(hr <= TRAINING_STATUS.GOOD_TRAINING_HR_THRESHOLD){
            return TRAINING_STATUS.LOW;
        }else if(hr <= TRAINING_STATUS.OVER_TRAINING_HR_THRESHOLD){
            return TRAINING_STATUS.GOOD;
        }else{
            return TRAINING_STATUS.OVER_TRAINING;
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
        final int THIS_BEFORE = -1;
        final int THE_OTHER_BEFORE = 1;
        Athlete b = (Athlete) o;    // ! good
        //if the athlete is not initialized, it should go at the bottom of the list
        if(this.isInitialized() == false){
            return THE_OTHER_BEFORE;
        }
        if(b.isInitialized() == false){
            return THIS_BEFORE;
        }
        //the connected athlete are shown before the away athlete
        if(this.getConnectionStatus() != b.getConnectionStatus()) {
            if(this.getConnectionStatus() == CONNECTION_STATUS.CONNECTED){
                return THIS_BEFORE;
            }else{
                // case in which this is AWAY and b is connected
                return THE_OTHER_BEFORE;
            }
        }

        Double thisEMA = this.getHeartRateEMAorLastHR();
        Double otherEMA = b.getHeartRateEMAorLastHR();

        if(thisEMA != null){
            if(otherEMA == null){
                return THIS_BEFORE;
            }
            if(thisEMA > otherEMA){
                return THIS_BEFORE;
            }else if(thisEMA < otherEMA){
                return THE_OTHER_BEFORE;
            }
        }else if(otherEMA != null){
            return THE_OTHER_BEFORE;
        }

        // case in which both athletes are initialized, both connected (or both away)
        // and both do not have hr EMA

        // will sort by firstConnection ts
        if(this.getFirstConnection() == null){
            if(this.getName().compareTo(b.getName()) > 0){
                return THE_OTHER_BEFORE;
            }
            return THIS_BEFORE;
        }
        if( this.getFirstConnection().isAfter( b.getFirstConnection() ) ){
            //first the new devices
            return THIS_BEFORE;
        }else{
            return THE_OTHER_BEFORE;
        }
    }
}
