package it.dii.unipi.trainerapp.utilities;

import android.bluetooth.BluetoothDevice;

public class DeviceID {
    public String ID;

    public DeviceID(BluetoothDevice device){
        this.ID = device.getAddress();
    }

    public String toString(){
        return this.ID;
    }
}
