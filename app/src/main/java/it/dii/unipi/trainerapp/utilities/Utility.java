package it.dii.unipi.trainerapp.utilities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public final class Utility {

    static final int sdkVersion = Build.VERSION.SDK_INT;

    static final String[] PERMISSIONS_OVER_SDK31 = {
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT
    };

    public static String readFromFile(Context context, String fileName) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(fileName);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
            else return null;
        }
        catch (FileNotFoundException e) {
            Log.w("Utility Class", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.w("Utility Class", "Can not read file: " + e.toString());
        }

        return ret;
    }

    public static boolean writeToFile(String data, Context context, String fileName) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            return true;
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            return false;
        }
    }

    public static boolean hasPermissions(String[] permissions, Context ctx) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(ctx , permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("PERMISSIONS", "Permission is not granted: " + permission);
                    return false;
                }
                Log.d("PERMISSIONS", "Permission already granted: " + permission);
            }
            return true;
        }
        return false;
    }

    public static void askPermissions(ActivityResultLauncher<String[]> multiplePermissionLauncher, Context ctx) {
        if(sdkVersion >= 31){
            if (!hasPermissions(PERMISSIONS_OVER_SDK31,ctx)) {
                Log.d("PERMISSIONS", "Launching multiple contract permission launcher for ALL required permissions");
                multiplePermissionLauncher.launch(PERMISSIONS_OVER_SDK31);
            } else {
                Log.d("PERMISSIONS", "All permissions are already granted");
            }
        } else {
            Log.d("PERMISSIONS", "All permissions are already granted");
        }
    }

    public static String[] getPERMISSIONS_OVER_SDK31() {
        return PERMISSIONS_OVER_SDK31;
    }

    public static int getSdkVersion() {
        return sdkVersion;
    }


}
