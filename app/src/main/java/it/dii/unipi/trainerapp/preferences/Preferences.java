package it.dii.unipi.trainerapp.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import java.util.Map;
import java.util.Set;

public class Preferences implements SharedPreferences{

    private static final String TRAINER_NAME_KEY = "trainerName";
    private static final String TRAINER_NAME_NOT_FOUND = "Name not found";
    private static final String DARK_THEME_KEY = "themeSwitch";

    private static Preferences myPreferences;
    private static SharedPreferences sharedPreferences;
    private static Editor editor;

    private Preferences(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        editor = sharedPreferences.edit();
        editor.apply();
    }

    public static Preferences getPreferences(Context context) {
        if (myPreferences == null) myPreferences = new Preferences(context);
        return myPreferences;
    }

    @Override
    public Map<String, ?> getAll() {
        return sharedPreferences.getAll();
    }

    @Nullable
    @Override
    public String getString(String s, @Nullable String s1) {
        return sharedPreferences.getString(s,s1);
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String s, @Nullable Set<String> set) {
        return sharedPreferences.getStringSet(s,set);
    }

    @Override
    public int getInt(String s, int i) {
        return sharedPreferences.getInt(s,i);
    }

    @Override
    public long getLong(String s, long l) {
        return sharedPreferences.getLong(s,l);
    }

    @Override
    public float getFloat(String s, float v) {
        return sharedPreferences.getFloat(s,v);
    }

    @Override
    public boolean getBoolean(String s, boolean defaultValue) {
        return sharedPreferences.getBoolean(s,defaultValue);
    }

    @Override
    public boolean contains(String s) {
        return sharedPreferences.contains(s);
    }

    @Override
    public Editor edit() {
        return sharedPreferences.edit();
    }

    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    public static void setTrainerName(String name){
        editor.putString(TRAINER_NAME_KEY, name);
        editor.apply();
    }

    public static String getTrainerName(){
        //if no data is available for Config.USER_NAME then this getString() method returns
        //a default value that is mentioned in second parameter
        return sharedPreferences.getString(TRAINER_NAME_KEY, TRAINER_NAME_NOT_FOUND);
    }

    public static boolean getDarkThemeValue(){
        return sharedPreferences.getBoolean(DARK_THEME_KEY, false);
    }

    //TODO: can add below getter and setter methods for any key/value pair we need
}