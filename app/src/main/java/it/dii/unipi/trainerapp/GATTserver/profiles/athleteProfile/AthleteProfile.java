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
import it.dii.unipi.trainerapp.GATTserver.profiles.athleteProfile.services.heartRateService.HeartRateService;
import it.dii.unipi.trainerapp.GATTserver.profiles.athleteProfile.services.movementServices.MovementService;

public class AthleteProfile {

    private static final String TAG = AthleteProfile.class.getSimpleName();
    public static UUID ATHLETE_NAME_CHARACTERISTIC = AthleteInformationService.ATHLETE_NAME_CHARACTERISTIC;
    public static UUID HEART_RATE_CHARACTERISTIC = HeartRateService.HEART_RATE_CHARACTERISTIC;
    public static UUID SPEED_CHARACTERISTIC = MovementService.SPEED_CHARACTERISTIC;
    public static UUID RECOGNIZED_ACTIVITY_CHARACTERISTIC = MovementService.RECOGNIZED_ACTIVITY_CHARACTERISTIC;
    public static UUID PACE_CHARACTERISTIC = MovementService.PACE_CHARACTERISTIC;
    public static UUID STEP_COUNTER_CHARACTERISTIC = MovementService.STEP_COUNTER_CHARACTERISTIC;
    public static UUID DISTANCE_CHARACTERISTIC = MovementService.DISTANCE_CHARACTERISTIC;


    public static BluetoothGattService getAthleteInformationService() {
        return AthleteInformationService.createAthleteInformationService();
    }

    public static BluetoothGattService getHeartRateService() {
        return HeartRateService.createHeartRateService();
    }

    public static BluetoothGattService getMovementService() {
        return MovementService.createMovementService();
    }

}
