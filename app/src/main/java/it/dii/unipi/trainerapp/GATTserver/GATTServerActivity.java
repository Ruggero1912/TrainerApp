package it.dii.unipi.trainerapp.GATTserver;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.Process;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.widget.Toast;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.dii.unipi.trainerapp.GATTserver.profiles.athleteProfile.AthleteProfile;
import it.dii.unipi.trainerapp.GATTserver.profiles.athleteProfile.services.athleteInformationService.AthleteInformationService;
import it.dii.unipi.trainerapp.athlete.Athlete;
import it.dii.unipi.trainerapp.athlete.AthletesManager;
import it.dii.unipi.trainerapp.ui.AthleteAdapter;
import it.dii.unipi.trainerapp.utilities.AthleteActivityType;
import it.dii.unipi.trainerapp.utilities.DeviceID;
import it.dii.unipi.trainerapp.utilities.ServiceStatus;

public class GATTServerActivity extends Service {
    private static final String TAG = GATTServerActivity.class.getSimpleName();

    /* Bluetooth API */
    private BluetoothManager mBluetoothManager;
    private BluetoothGattServer mBluetoothGattServer;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    /* Collection of notification subscribers */
    private Set<BluetoothDevice> mRegisteredDevices = new HashSet<>();

    /* needed to manage the thread created by the service */
    private HandlerThread thread;
    private Looper serviceLooper;
    private ServiceHandler serviceHandler;

    private AthletesManager athletesManager = AthletesManager.getInstance();

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            // we insert here the functionalities executed by the thread
            startAdvertising();
            startServer();

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            //stopSelf(msg.arg1);
        }
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onCreate() {
        //super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_server);
        //mLocalTimeView = (TextView) findViewById(R.id.text_time);

        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
        // We can't continue without proper Bluetooth support
        if (!checkBluetoothSupport(bluetoothAdapter)) {
            Log.i(TAG, "Bluetooth not supported! finishing...");
            stopSelf();
            return;
        }

        // Register for system Bluetooth events
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothReceiver, filter);

        //here we have to initialize the context for the athletesManager, so that it will be able to broadcast the intent messages
        if( ! this.athletesManager.isContextInitialized()){
            this.athletesManager.initializeContext(getApplicationContext());
        }else{
            Log.d(TAG, "skipping the athletesManager context initialize since it is already initialized...");
        }

        //questo receiver si occupa di gestire il caso in cui BluetoothAdapter.STATE cambia (ovvero quando si accende o spegne il bluetooth)
        // quando ciò succede, viene chiamata la mBluetoothReceiver callback corrispondente che si occupa di avviare advertising e server / stopparli
        if (!bluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Bluetooth is currently disabled...enabling");

            if( canBluetoothConnect()){
                bluetoothAdapter.enable();
                Log.d(TAG, "I have just enabled the Bluetooth Adapter that was disabled...");
            }else{
                askBluetoothPermissions();
                return;
            }
        } else {
            Log.d(TAG, "Bluetooth enabled...starting services");

            // Start up the thread running the service. Note that we create a
            // separate thread because the service normally runs in the process's
            // main thread, which we don't want to block. We also make it
            // background priority so CPU-intensive work doesn't disrupt our UI.
            //FENOM: is THREAD_PRIORITY_BACKGROUND the best priority choice?
            // Maybe the server should have an higher priority to handle all the GATT requests fastly
            thread = new HandlerThread("ServiceStartArguments",
                    Process.THREAD_PRIORITY_MORE_FAVORABLE);    //THREAD_PRIORITY_BACKGROUND
            thread.start();

            // Get the HandlerThread's Looper and use it for our Handler
            serviceLooper = thread.getLooper();
            serviceHandler = new ServiceHandler(serviceLooper);
        }

        Log.v(TAG, "GATTServerService.onStartCommand: notifying broadcast message for new Service status: RUNNING");
        broadcastStatusUpdate(ServiceStatus.RUNNING);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //super.onStart();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);

        return START_STICKY; // decide which policy is the best fit for the Service
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {

        BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
        if (bluetoothAdapter.isEnabled()) {
            stopServer();
            stopAdvertising();
        }

        unregisterReceiver(mBluetoothReceiver);

        Log.v(TAG, "executing onDestroy method: sending broadcast notification for new status TERMINATED");

        broadcastStatusUpdate(ServiceStatus.TERMINATED);

        Log.d(TAG, "Going to call thread.quit()");

        boolean closed = thread.quit();
        if(!closed){
            Log.d(TAG, "Unable to quit the Thread");
        } else {
            Log.d(TAG, "GATTServer Thread correctly killed");
        };
        super.onDestroy();
    }

    /**
     * Verify the level of Bluetooth support provided by the hardware.
     * @param bluetoothAdapter System {@link BluetoothAdapter}.
     * @return true if Bluetooth is properly supported, false otherwise.
     */
    private boolean checkBluetoothSupport(BluetoothAdapter bluetoothAdapter) {

        if (bluetoothAdapter == null) {
            Log.w(TAG, "Bluetooth is not supported");
            return false;
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.w(TAG, "Bluetooth LE is not supported");
            return false;
        }

        return true;
    }

    private boolean canBluetoothConnect(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                //FENOM: qui sarebbe meglio gestire la richiesta dei permessi e poi non fare return ma vedere come si fa in questo caso
                Toast.makeText(getApplicationContext(), "BLUETOOTH_CONNECT permission missing! return...", Toast.LENGTH_LONG).show();
                return false;
            }
        }else{
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "Api level under VERSION_CODES.S, Bluetooth Admin permission not granted!");
                return false;
            }
        }
        return true;
    }

    private boolean canBluetoothAdvertise(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.i(TAG, "The ADVERTISE permission was not granted! CANNOT ADVERTISE...");
                return false;
            }
        }else if(ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, " | canBluetoothAdvertise | Api level under VERSION_CODES.S, Bluetooth Admin permission not granted!");
            return false;
        }
        return true;
    }

    private void askBluetoothPermissions(){
        //FENOM: TODO: ma va implementato o no? Sembra che i permessi del bluetooth non vadano chiesti a runtime mai, dovrebbe bastare specificarli nel manifest...
        return;
    }

    /**
     * Listens for Bluetooth adapter events to enable/disable
     * advertising and server functionality.
     */
    private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);

            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    startAdvertising();
                    startServer();
                    break;
                case BluetoothAdapter.STATE_OFF:
                    stopServer();
                    stopAdvertising();
                    break;
                default:
                    // Do nothing
            }

        }
    };

    /**
     * Begin advertising over Bluetooth that this device is connectable
     * and supports the Current Time Service.
     */
    @SuppressWarnings("MissingPermission")
    private void startAdvertising() {
        BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        if (mBluetoothLeAdvertiser == null) {
            Log.w(TAG, "Failed to create advertiser");
            return;
        }

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build();


        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(false)
                //TODO: broadcast the trainer name and a right UUID that should be knwown by the client app
                //.addServiceData(new ParcelUuid(AthleteInformationService.ATHLETE_INFORMATION_SERVICE), "trainerApp".getBytes(StandardCharsets.UTF_8))
                .addServiceUuid(new ParcelUuid(AthleteInformationService.ATHLETE_INFORMATION_SERVICE))
                //FENOM: TODO: add service UUID here (ma vanno aggiunti gli UUID di tutti i service o solo di uno basta?)
                .build();

        if(canBluetoothAdvertise()) {
            mBluetoothLeAdvertiser
                    .startAdvertising(settings, data, mAdvertiseCallback);
        }
    }

    /**
     * Stop Bluetooth advertisements.
     */
    @SuppressWarnings("MissingPermission")
    private void stopAdvertising() {
        if (mBluetoothLeAdvertiser == null) return;

        if(canBluetoothAdvertise()) {
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
        }
    }

    /**
     * Initialize the GATT server instance with the services/characteristics
     * from the Time Profile.
     */
    @SuppressWarnings("MissingPermission")
    private void startServer() {
        if( ! canBluetoothConnect()){
            return;
        }
        mBluetoothGattServer = mBluetoothManager.openGattServer(this, mGattServerCallback);
        if (mBluetoothGattServer == null) {
            Log.w(TAG, "Unable to create GATT server");
            return;
        } else {
            Log.i(TAG, "GATT server created");
        }

        //mBluetoothGattServer.addService(TimeProfile.createTimeService());
        //FENOM: TODO: add here the other services
        this.addServiceToQueueOfPendingServices(AthleteProfile.getAthleteInformationService());
        this.addServiceToQueueOfPendingServices(AthleteProfile.getHeartRateService());

    }

    private List<BluetoothGattService> pendingGATTServices = new ArrayList<>();
    private boolean isAddingAService = false;

    /**
     * call this method to add another service to the list of available services of this GATT Server
     * if you directly call gattServer.addService, it will be a mess
     * @param newPendingService
     */
    @SuppressLint("MissingPermission")
    private void addServiceToQueueOfPendingServices(BluetoothGattService newPendingService){
        if(mBluetoothGattServer == null){
            Log.e(TAG, "cannot add pending service since the mBluetoothGATTServer was not initialized!!");
            return;
        }
        if( ! isAddingAService){
            mBluetoothGattServer.addService(newPendingService);
            isAddingAService = true;
        }
        else{
            pendingGATTServices.add(newPendingService);
        }
    }

    /**
     * Shut down the GATT server.
     */
    @SuppressWarnings("MissingPermission")
    private void stopServer() {
        if (mBluetoothGattServer == null) return;

        if(! canBluetoothConnect()){
            return;
        }
        mBluetoothGattServer.close();
    }

    /**
     * Callback to receive information about the advertisement process.
     */
    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.i(TAG, "LE Advertise Started.");
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.w(TAG, "LE Advertise Failed: " + errorCode);
        }
    };

    /*
    /**
     * Send a time service notification to any devices that are subscribed
     * to the characteristic.
     * /
    private void notifyRegisteredDevices(long timestamp, byte adjustReason) {
        if (mRegisteredDevices.isEmpty()) {
            Log.i(TAG, "No subscribers registered");
            return;
        }
        byte[] exactTime = TimeProfile.getExactTime(timestamp, adjustReason);

        Log.i(TAG, "Sending update to " + mRegisteredDevices.size() + " subscribers");
        for (BluetoothDevice device : mRegisteredDevices) {
            BluetoothGattCharacteristic timeCharacteristic = mBluetoothGattServer
                    .getService(TimeProfile.TIME_SERVICE)
                    .getCharacteristic(TimeProfile.CURRENT_TIME);
            timeCharacteristic.setValue(exactTime);
            mBluetoothGattServer.notifyCharacteristicChanged(device, timeCharacteristic, false);
        }
    }

     */

    /**
     * Callback to handle incoming requests to the GATT server.
     * All read/write requests for characteristics and descriptors are handled here.
     */
    private BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {

        @SuppressLint("MissingPermission")
        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);

            Log.v(TAG, "The service " + service.getUuid().toString() + " was correctly added");

            if( ! pendingGATTServices.isEmpty()){
                BluetoothGattService toBeAdded = pendingGATTServices.remove(0);
                Log.v(TAG, "Going to add the pending service " + toBeAdded.getUuid().toString());
                mBluetoothGattServer.addService(toBeAdded);
            }
            else{
                Log.v(TAG, "all the services were added correctly");
                isAddingAService = false;
            }
        }

        @Override
        @SuppressWarnings("MissingPermission")
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "BluetoothDevice CONNECTED: " + device + " - device name: " + device.getName() );
                // here we have to add the device to the list of athletes devices
                boolean added = athletesManager.addAthlete(new DeviceID(device));
                Athlete newAthlete = athletesManager.getAthlete(new DeviceID(device).toString());

                if( !added ){
                    Log.d(TAG, "the device was not added since it is already present");
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "BluetoothDevice DISCONNECTED: " + device);
                //Remove device from any active subscriptions
                mRegisteredDevices.remove(device);
                Athlete athleteMarkedAsAway = athletesManager.markAthleteAsAway(new DeviceID(device).toString());
                if(athleteMarkedAsAway == null){
                    Log.e(TAG, "the just disconnected device did not correspond to an athlete | device: " + device);
                }else{
                    Log.i(TAG, "the athlete '" + athleteMarkedAsAway.getName() + "' was marked as away");
                }
            }
        }

        @Override
        @SuppressWarnings("MissingPermission")
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                BluetoothGattCharacteristic characteristic) {
            if( ! canBluetoothConnect()){
                Log.e(TAG, "cannot handle characteristicReadRequest because missing permissions");
                return;
            }
            if(AthleteProfile.ATHLETE_NAME_CHARACTERISTIC.equals(characteristic.getUuid())){
                Log.i(TAG, "received a read request on ATHLETE_NAME_CHARACTERISTIC from '"+ device + "'");
                mBluetoothGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        athletesManager.getName(new DeviceID(device).toString()).getBytes(StandardCharsets.UTF_8)
                );
            }
            /*
            long now = System.currentTimeMillis();
            if (TimeProfile.CURRENT_TIME.equals(characteristic.getUuid())) {
                Log.i(TAG, "Read CurrentTime");
                mBluetoothGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        TimeProfile.getExactTime(now, TimeProfile.ADJUST_NONE));
            } else if (TimeProfile.LOCAL_TIME_INFO.equals(characteristic.getUuid())) {
                Log.i(TAG, "Read LocalTimeInfo");
                mBluetoothGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        TimeProfile.getLocalTimeInfo(now));
            }
             */
            else {
                // Invalid characteristic
                Log.w(TAG, "Invalid Characteristic Read: " + characteristic.getUuid());

                mBluetoothGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        null);
            }
        }

        @Override
        @SuppressWarnings("MissingPermission")
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {

            //super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            if( ! canBluetoothConnect() ){
                Log.e(TAG, "cannot handle characteristicWriteRequest because missing permissions");
                return;
            }
            Log.v(TAG, "received charWrite on the characteristic : " + characteristic.getUuid().toString());

            if(AthleteProfile.ATHLETE_NAME_CHARACTERISTIC.equals(characteristic.getUuid())) {

                String athleteNameString = new String(value, StandardCharsets.UTF_8);
                athletesManager.setName(new DeviceID(device).toString(), athleteNameString);

                if(responseNeeded) {
                    mBluetoothGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_SUCCESS,
                            0,
                            "saved".getBytes(StandardCharsets.UTF_8));
                }else {
                    Log.v(TAG, "skipping response");
                }
            }else if(AthleteProfile.HEART_RATE_CHARACTERISTIC.equals(characteristic.getUuid())){
                //TODO: the received value could directly be an encoded integer and not a string, parse it in the right way and test it
                int receivedHR = new BigInteger(value).intValue();

                if(receivedHR > 200 || receivedHR < 0){
                    Log.w(TAG, "the received HR is out of bounds! received value converted as int: " + receivedHR + " | raw data: " + value);
                }
                /*
                String athleteHRString = new String(value, StandardCharsets.UTF_8);
                Integer hrParsed = null;
                try{

                    Integer.parseInt(athleteHRString);
                } catch (Exception e){
                    Log.e(TAG, "the given hr measurement was not recognised as valid Integer!");
                }
                 */
                athletesManager.storeHeartRateMeasurementForAthlete(new DeviceID(device).toString(), receivedHR);

                if(responseNeeded) {
                    mBluetoothGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_SUCCESS,
                            0,
                            "saved".getBytes(StandardCharsets.UTF_8));
                }else {
                    Log.v(TAG, "skipping response");
                }
            }else if(AthleteProfile.SPEED_CHARACTERISTIC.equals(characteristic.getUuid())){
                double receivedSpeed = ByteBuffer.wrap(value).getDouble();

                if(receivedSpeed > 37 || receivedSpeed < 0){
                    Log.w(TAG, "the received speed is out of bounds! received value converted as double: " + receivedSpeed + " | raw data: " + value);
                }

                athletesManager.storeSpeedMeasurementForAthlete(new DeviceID(device).toString(), receivedSpeed);

                if(responseNeeded) {
                    mBluetoothGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_SUCCESS,
                            0,
                            "saved".getBytes(StandardCharsets.UTF_8));
                }else {
                    Log.v(TAG, "skipping response");
                }
            }else if(AthleteProfile.RECOGNIZED_ACTIVITY_CHARACTERISTIC.equals(characteristic.getUuid())){
                int activityIndex = new BigInteger(value).intValue();
                AthleteActivityType activity = AthleteActivityType.fromInt(activityIndex);

                if(activity.toString().equals("")){
                    Log.w(TAG, "the received activity is empty! received value converted as string: " + activity + " | raw data: " + value);
                }

                athletesManager.storeNewActivityForAthlete(new DeviceID(device).toString(), activity);

                if(responseNeeded) {
                    mBluetoothGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_SUCCESS,
                            0,
                            "saved".getBytes(StandardCharsets.UTF_8));
                }else {
                    Log.v(TAG, "skipping response");
                }
            }else if(AthleteProfile.STEP_COUNTER_CHARACTERISTIC.equals(characteristic.getUuid())){
                int receivedStepCount = new BigInteger(value).intValue();

                if(receivedStepCount < 0){
                    Log.w(TAG, "the received step count is negative! received value converted as int: " + receivedStepCount + " | raw data: " + value);
                }
                athletesManager.storeStepCountForAthlete(new DeviceID(device).toString(), receivedStepCount);

                if(responseNeeded) {
                    mBluetoothGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_SUCCESS,
                            0,
                            "saved".getBytes(StandardCharsets.UTF_8));
                }else {
                    Log.v(TAG, "skipping response");
                }
            }else if(AthleteProfile.PEACE_CHARACTERISTIC.equals(characteristic.getUuid())){
                double receivedPeace = ByteBuffer.wrap(value).getDouble();

                if(receivedPeace > 2.5 || receivedPeace < 0){
                    Log.w(TAG, "the received peace is out of bounds! received value converted as double: " + receivedPeace + " | raw data: " + value);
                }

                athletesManager.storePeaceMeasurementForAthlete(new DeviceID(device).toString(), receivedPeace);

                if(responseNeeded) {
                    mBluetoothGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_SUCCESS,
                            0,
                            "saved".getBytes(StandardCharsets.UTF_8));
                }else {
                    Log.v(TAG, "skipping response");
                }
            }
            else {
                // Invalid characteristic
                Log.w(TAG, "Invalid Characteristic Write: " + characteristic.getUuid());

                if(responseNeeded) {
                    mBluetoothGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_FAILURE,
                            0,
                            null);
                }else{
                    Log.v(TAG, "skipping response");
                }
            }

        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset,
                                            BluetoothGattDescriptor descriptor) {
            /*
            if (TimeProfile.CLIENT_CONFIG.equals(descriptor.getUuid())) {
                Log.d(TAG, "Config descriptor read");
                byte[] returnValue;
                if (mRegisteredDevices.contains(device)) {
                    returnValue = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                } else {
                    returnValue = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                }
                mBluetoothGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        returnValue);
            } else {
                Log.w(TAG, "Unknown descriptor read request");
                mBluetoothGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        null);
            }
             */
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
                                             BluetoothGattDescriptor descriptor,
                                             boolean preparedWrite, boolean responseNeeded,
                                             int offset, byte[] value) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            /*
            if (TimeProfile.CLIENT_CONFIG.equals(descriptor.getUuid())) {
                if (Arrays.equals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE, value)) {
                    Log.d(TAG, "Subscribe device to notifications: " + device);
                    mRegisteredDevices.add(device);
                } else if (Arrays.equals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE, value)) {
                    Log.d(TAG, "Unsubscribe device from notifications: " + device);
                    mRegisteredDevices.remove(device);
                }

                if (responseNeeded) {

                    mBluetoothGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_SUCCESS,
                            0,
                            null);
                }
            } else {
                Log.w(TAG, "Unknown descriptor write request");
                if (responseNeeded) {
                    mBluetoothGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_FAILURE,
                            0,
                            null);
                }
            }
            */
        }
    };


    public final static String GATT_SERVER_STATUS_ACTION = "gatt-server-status";
    public final static String SERVICE_STATUS_KEY = "status";

    /**
     * call this method to notify the rest of the app for GATT Server status changes
     * @param status
     */
    private void broadcastStatusUpdate(final ServiceStatus status) {
        final Intent intent = new Intent(GATT_SERVER_STATUS_ACTION);
        intent.putExtra(SERVICE_STATUS_KEY, status);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}