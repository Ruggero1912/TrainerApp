package it.dii.unipi.trainerapp.utilities;

public enum AthleteActivityType {
    RUNNING,
    WALKING,
    STANDING;

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
