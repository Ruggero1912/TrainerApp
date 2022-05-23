package it.dii.unipi.trainerapp.ui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.dii.unipi.trainerapp.R;
import it.dii.unipi.trainerapp.athlete.Athlete;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AthleteDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AthleteDetailsFragment extends Fragment {

    private static final String TAG = AthleteDetailsFragment.class.getSimpleName();
    private static final String ARG_BOUNDED_ATHLETE = "bounded-athlete";

    private Athlete boundedAthlete;


    public AthleteDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param athleteToBind the athlete whose details should be shown by the fragment
     * @return A new instance of fragment AthleteDetailsFragment.
     */
    public static AthleteDetailsFragment newInstance(Athlete athleteToBind) {
        AthleteDetailsFragment fragment = new AthleteDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BOUNDED_ATHLETE, athleteToBind);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            boundedAthlete = (Athlete) getArguments().getSerializable(ARG_BOUNDED_ATHLETE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_athlete_details, container, false);

        TextView tvAthleteName = view.findViewById(R.id.detailsFragmentAthleteName);
        if(this.boundedAthlete == null){
            Log.w(TAG, "Received empty athlete object!");
        }else{
            tvAthleteName.setText(this.boundedAthlete.getName());
        }
        return view;
    }
}