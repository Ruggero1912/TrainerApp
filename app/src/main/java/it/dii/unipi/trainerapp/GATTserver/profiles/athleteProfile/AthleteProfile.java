package it.dii.unipi.trainerapp.GATTserver.profiles.athleteProfile;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import it.dii.unipi.trainerapp.GATTserver.profiles.athleteProfile.services.athleteInformationService.AthleteInformationService;

public class AthleteProfile {

    private static final String TAG = AthleteProfile.class.getSimpleName();
    public static UUID ATHLETE_NAME_CHARACTERISTIC = AthleteInformationService.ATHLETE_NAME_CHARACTERISTIC;

    private static Map<String, String> athleteNames = new HashMap<>();

    public static BluetoothGattService getAthleteInformationService() {
        return AthleteInformationService.createAthleteInformationService();
    }

    @SuppressWarnings("MissingPermission")
    public static byte[] getAthleteName(BluetoothDevice clientDevice) {
        Log.i(TAG, "working on getAthleteName for device " + clientDevice.getName() + "...");

        //return clientDevice.getName().getBytes(StandardCharsets.UTF_8);
        return athleteNames.getOrDefault(clientDevice.getAddress(), "NAME NOT SET").getBytes(StandardCharsets.UTF_8);
    }
    public static void setAthleteName(String deviceKey, byte[] athleteName){
        String athleteNameS = new String(athleteName, StandardCharsets.UTF_8);
        String athleteNameASCII = new String(athleteName, StandardCharsets.US_ASCII);
        Log.d(TAG, "The received string is in UTF8: " + athleteNameS + " | in ASCII: " + athleteNameASCII);
        Log.i(TAG, "Am going to store the athlete name '" + athleteNameS + "' for the device '" + deviceKey + "'");
        athleteNames.put(deviceKey, athleteNameS);
    }
}
