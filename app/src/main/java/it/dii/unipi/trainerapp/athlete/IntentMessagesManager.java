package it.dii.unipi.trainerapp.athlete;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class IntentMessagesManager {

    public static final String ATHLETE_INTENT_NAME = "update-athlete-list";
    public static final String ATHLETE_INTENT_ACTION_TYPE_KEY = "action-to-perform";
    public static final String ATHLETE_INTENT_ACTION_ADD_ATHLETE = "add-athlete";
    public static final String ATHLETE_INTENT_ACTION_UPDATE_ATHLETE = "update-athlete";
    public static final String ATHLETE_INTENT_ACTION_REMOVE_ATHLETE = "remove-athlete";
    public static final String ATHLETE_INTENT_ATHLETE_OBJ_KEY = "athlete";

    private static final String TAG = IntentMessagesManager.class.getSimpleName();

    private Context context;
    private LocalBroadcastManager broadcastManager;

    public IntentMessagesManager(Context c){
        this.context = c;
        this.broadcastManager = LocalBroadcastManager.getInstance(this.context);
    }

    /** Send an Intent with an action named ATHLETE_INTENT_NAME, the Intent sent is
     * received by the MainActivity.
     * @param newAthlete
     * @param action_to_perform
     */
    public void sendMessage(Athlete newAthlete, String action_to_perform) {
        Log.d(TAG, "going to broadcast a message");
        Intent intent = new Intent(ATHLETE_INTENT_NAME);
        intent.putExtra(ATHLETE_INTENT_ATHLETE_OBJ_KEY, newAthlete);
        intent.putExtra(ATHLETE_INTENT_ACTION_TYPE_KEY, action_to_perform);
        this.broadcastManager.sendBroadcast(intent);
    }
/*
    public void sendMessage(Athlete newAthlete, String action_to_perform, Athlete athlete_to_remove) {
        Log.d(TAG, "going to broadcast a message");
        Intent intent = new Intent(ATHLETE_INTENT_NAME);
        intent.putExtra(ATHLETE_INTENT_ATHLETE_OBJ_KEY, newAthlete);
        intent.putExtra(ATHLETE_INTENT_ACTION_TYPE_KEY, action_to_perform);
        intent.putExtra("athlete-to-remove", athlete_to_remove);
        this.broadcastManager.sendBroadcast(intent);
    }
 */
}
