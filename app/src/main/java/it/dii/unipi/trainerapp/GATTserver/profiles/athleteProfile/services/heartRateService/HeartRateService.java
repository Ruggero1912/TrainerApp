package it.dii.unipi.trainerapp.GATTserver.profiles.athleteProfile.services.heartRateService;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;

import it.dii.unipi.trainerapp.GATTserver.profiles.athleteProfile.services.athleteInformationService.AthleteInformationService;

public class HeartRateService {
    private static final String TAG = AthleteInformationService.class.getSimpleName();

    public static UUID HEART_RATE_SERVICE = UUID.fromString("180D");
    public static UUID HEART_RATE_CHARACTERISTIC = UUID.fromString("2a37");

    public static BluetoothGattService createHeartRateService(){
        BluetoothGattService service = new BluetoothGattService(HEART_RATE_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        // athlete_name characteristic
        BluetoothGattCharacteristic athleteNameCharacteristic = new BluetoothGattCharacteristic(HEART_RATE_CHARACTERISTIC,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);

        service.addCharacteristic(athleteNameCharacteristic);

        return service;
    }
}
