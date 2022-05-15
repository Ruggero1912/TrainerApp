package it.dii.unipi.trainerapp.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import it.dii.unipi.trainerapp.R;
import it.dii.unipi.trainerapp.athlete.Athlete;

public class AthleteAdapter extends ArrayAdapter<Athlete> {

    public AthleteAdapter(Context context, ArrayList<Athlete> athletes) {
        super(context, 0, athletes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.athlete_item, parent, false);
        }

        // Get the data item for this position
        Athlete a = getItem(position);

        // Lookup view for data population
        //TextView tvID = (TextView) convertView.findViewById(R.id.tvID);
        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        // Populate the data into the template view using the data object
        //tvID.setText(a.getAthleteID().toString());
        tvName.setText(a.getName());
        // Return the completed view to render on screen
        return convertView;
    }
}
