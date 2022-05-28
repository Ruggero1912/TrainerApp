package it.dii.unipi.trainerapp.GATTserver.profiles.athleteProfile.services.movementServices;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;


public class MovementService {
    private static final String TAG = MovementService.class.getSimpleName();

    public static UUID MOVEMENT_SERVICE = UUID.fromString("6b94f55a-dc3f-11ec-9d64-0242ac120002");
    public static UUID RECOGNIZED_ACTIVITY_CHARACTERISTIC = UUID.fromString("6b94f7e4-dc3f-11ec-9d64-0242ac120002");
    public static UUID SPEED_CHARACTERISTIC = UUID.fromString("6b94f92e-dc3f-11ec-9d64-0242ac120002");
    public static UUID PACE_CHARACTERISTIC = UUID.fromString("6b94fc58-dc3f-11ec-9d64-0242ac120002");
    public static UUID STEP_COUNTER_CHARACTERISTIC = UUID.fromString("6b94fd70-dc3f-11ec-9d64-0242ac120002");

    public static BluetoothGattService createMovementService() {
        BluetoothGattService service = new BluetoothGattService(MOVEMENT_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        // recognized activity characteristic
        BluetoothGattCharacteristic athleteActivityCharacteristic = new BluetoothGattCharacteristic(RECOGNIZED_ACTIVITY_CHARACTERISTIC,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);

        // speed characteristic
        BluetoothGattCharacteristic athleteSpeedCharacteristic = new BluetoothGattCharacteristic(SPEED_CHARACTERISTIC,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);

        // pace characteristic
        BluetoothGattCharacteristic athletePaceCharacteristic = new BluetoothGattCharacteristic(PACE_CHARACTERISTIC,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);

        // step counter characteristic
        BluetoothGattCharacteristic athleteStepCounterCharacteristic = new BluetoothGattCharacteristic(STEP_COUNTER_CHARACTERISTIC,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);

        service.addCharacteristic(athleteActivityCharacteristic);
        service.addCharacteristic(athleteSpeedCharacteristic);
        service.addCharacteristic(athletePaceCharacteristic);
        service.addCharacteristic(athleteStepCounterCharacteristic);

        return service;
    }
}
