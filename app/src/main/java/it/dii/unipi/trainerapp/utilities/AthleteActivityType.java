package it.dii.unipi.trainerapp.utilities;

public enum AthleteActivityType {
    IN_VEHICLE,
    ON_BICYCLE,
    ON_FOOT,
    STILL,
    UNKNOWN,
    TILTING,
    UNRECOGNISED,
    WALKING,
    RUNNING;

    public static AthleteActivityType defaultAthleteActivityType = WALKING;

    /**
     * utility method to cast integer to AthleteActivityType
     * @param index
     * @return
     */
    public static AthleteActivityType fromInt(Integer index){
        return AthleteActivityType.values()[index];
    }

    /**
     * utility method to parse from AthleteActivityType to Int: useful to transmit the activityType over GATT
     * @return
     */
    public Integer toInt(){
        return this.ordinal();
    }
};
