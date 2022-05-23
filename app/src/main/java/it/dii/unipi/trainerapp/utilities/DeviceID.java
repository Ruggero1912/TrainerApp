package it.dii.unipi.trainerapp.utilities;

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;

public class DeviceID implements Serializable {
    public String ID;

    public DeviceID(BluetoothDevice device){
        this.ID = device.getAddress();
    }

    public String toString(){
        return this.ID;
    }
}
