package it.dii.unipi.trainerapp.athlete;

import android.text.format.Time;
import android.util.Log;

import java.time.LocalDateTime;
import java.util.NavigableMap;
import java.util.TreeMap;

import it.dii.unipi.trainerapp.utilities.DeviceID;

/**
 * this is a singleton class thread safe.
 * Please use the public method getInstance in order to obtain the singleton instance of the class
 */

public class AthletesManager {
    private static final String TAG = AthletesManager.class.getSimpleName();

    private static AthletesManager singletonInstance = new AthletesManager();

    private AthletesManager(){
        return;
    }

    public static AthletesManager getInstance(){
        return singletonInstance;
    }

    private NavigableMap<String, Athlete> athletes = new TreeMap<>();

    private NavigableMap<String, LocalDateTime> athletesAway = new TreeMap<>();

    /**
     * adds a new athlete identified by an ID to the athletes list
     * @param athleteID the ID of the athlete that has to be added
     * @return true if the athlete was correctly added, false if it was already present in the athletes list
     */
    public boolean addAthlete(DeviceID athleteID){
        if(athletes.containsKey(athleteID.toString())){
            Log.v(TAG, "the athleteID '" + athleteID + "' is already present in the athletes list! Not going to add twice");
            markAthleteAsPresent(athleteID.toString());
            return false;
        }
        Athlete athlete = new Athlete(athleteID);
        athlete.updateFirstConnection();
        athlete.updateLastSeen();
        athlete.updateConnectionStatus(Athlete.CONNECTION_STATUS.CONNECTED);
        athletes.put(athleteID.toString(), athlete);
        return true;
    }

    /**
     * removes the athlete of given id if it was in the list
     * @param athleteID
     * @return the removed athlete Object or null
     */
    public Athlete removeAthlete(DeviceID athleteID){
        // andrebbe chiamato anche athletesAway.remove()
        return athletes.remove(athleteID.toString());
    }

    public Athlete markAthleteAsAway(String athleteID){
        Athlete athlete = getAthlete(athleteID);
        if(athlete == null){
            Log.w(TAG, "athlete not found");
            return null;
        }
        LocalDateTime currentTime = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currentTime = LocalDateTime.now();
        }else{
            Log.e(TAG, "cannot log the localdatetime of the athlete vacancy!");
            //currentTime = (new Time(Time.getCurrentTimezone()));
        }
        athlete.updateLastSeen();
        athlete.updateConnectionStatus(Athlete.CONNECTION_STATUS.AWAY);
        athletesAway.put(athleteID, currentTime);
        return athlete;
    }

    public void markAthleteAsPresent(String athleteID){
        Athlete athlete = getAthlete(athleteID);
        if(athlete == null){
            Log.w(TAG, "athlete not found");
            return;
        }
        Log.i(TAG, "the athlete " + athlete.getAthleteID() + " (friendly name: " + athlete.getName() + ") has come back, removing from athletesAway list");
        athlete.updateLastSeen();
        athlete.updateConnectionStatus(Athlete.CONNECTION_STATUS.CONNECTED);
        athletesAway.remove(athleteID);
    }

    /**
     * returns the Athlete instance (if any) associated to the given athleteID
     * @param athleteID
     * @return null if not found, else the athlete instance
     */
    public Athlete getAthlete(String athleteID){
        return athletes.getOrDefault(athleteID, null);
    }

    public boolean setName(String athleteID, String newName){
        Athlete a = getAthlete(athleteID);
        if(a == null){
            Log.e(TAG, "athlete not found for the ID '" + athleteID + "'! cannot set the name '" + newName + "'..");
            return false;
        }
        a.setName(newName);
        a.updateLastSeen();
        return true;
    }

    public String getName(String athleteID){
        Athlete a = getAthlete(athleteID);
        if(a == null){
            Log.e(TAG, "athlete not found for the ID '" + athleteID + "'! cannot get the name...");
            return null;
        }
        Log.d(TAG, "returning the athlete name '" + a.getName() + "' for athleteID '" + athleteID + "'");
        return a.getName();
    }

    public boolean storeHeartRateMeasurementForAthlete(String athleteID, Integer heartRateMeasure, LocalDateTime time){
        Athlete a = getAthlete(athleteID);
        if(a == null){
            Log.e(TAG, "athlete not found for the ID '" + athleteID + "'! cannot store the measurement '" + heartRateMeasure + "'..");
            return false;
        }
        a.storeHeartRateMeasurement(heartRateMeasure, time);
        a.updateLastSeen();
        return true;
    }

    public boolean storeHeartRateMeasurementForAthlete(String athleteID, Integer heartRateMeasure){
        LocalDateTime currentTime = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currentTime = LocalDateTime.now();
        }else{
            Log.e(TAG, "cannot log the localdatetime of the heartratemeasure!");
            //currentTime = (new Time(Time.getCurrentTimezone()));
        }
        return storeHeartRateMeasurementForAthlete(athleteID, heartRateMeasure, currentTime);
    }

    public Integer getLastHeartRateMeasureForAthlete(String athleteID){
        Athlete a = getAthlete(athleteID);
        if(a == null){
            Log.e(TAG, "athlete not found for the ID '" + athleteID + "'! cannot get the heart rate measurement");
            return null;
        }
        return a.getLastHeartRateMeasurement();
    }
}
