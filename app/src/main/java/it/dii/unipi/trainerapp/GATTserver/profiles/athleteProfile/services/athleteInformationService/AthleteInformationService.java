package it.dii.unipi.trainerapp.GATTserver.profiles.athleteProfile.services.athleteInformationService;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;

public class AthleteInformationService {
    private static final String TAG = AthleteInformationService.class.getSimpleName();

    public static UUID ATHLETE_INFORMATION_SERVICE = UUID.fromString("a173b614-8dff-455d-83d1-37de25b9432c");
    public static UUID ATHLETE_NAME_CHARACTERISTIC = UUID.fromString("4fe10359-2ce1-4e3e-848d-aec36a32930c");

    public static BluetoothGattService createAthleteInformationService(){
        BluetoothGattService service = new BluetoothGattService(ATHLETE_INFORMATION_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        // athlete_name characteristic
        BluetoothGattCharacteristic athleteNameCharacteristic = new BluetoothGattCharacteristic(ATHLETE_NAME_CHARACTERISTIC,
                BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);

        service.addCharacteristic(athleteNameCharacteristic);

        return service;
    }
}
