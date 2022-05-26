package it.dii.unipi.trainerapp.GATTserver.profiles.athleteProfile.services.heartRateService;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;

import it.dii.unipi.trainerapp.GATTserver.profiles.athleteProfile.services.athleteInformationService.AthleteInformationService;

public class HeartRateService {
    private static final String TAG = HeartRateService.class.getSimpleName();

    public static UUID HEART_RATE_SERVICE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
    public static UUID HEART_RATE_CHARACTERISTIC = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");

    public static BluetoothGattService createHeartRateService(){
        BluetoothGattService service = new BluetoothGattService(HEART_RATE_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        // heart rate characteristic
        BluetoothGattCharacteristic athleteHeartRateCharacteristic = new BluetoothGattCharacteristic(HEART_RATE_CHARACTERISTIC,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);

        service.addCharacteristic(athleteHeartRateCharacteristic);

        return service;
    }
}
