package it.dii.unipi.trainerapp.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;

import it.dii.unipi.trainerapp.R;
import it.dii.unipi.trainerapp.preferences.Preferences;

public class WelcomeActivity extends AppCompatActivity {

    private String fileName = "settingsDump.txt";
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Button settingsConfirm = (Button) findViewById(R.id.settingsConfirm);

        settingsConfirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //get the trainer name from the text input
                EditText trainerNameInput = (EditText) findViewById(R.id.trainerNameInput);
                String inputData = trainerNameInput.getText().toString();
                Preferences.setTrainerName(inputData);
                //send back to MainActivity an intent with trainer's name attached
                Intent returnIntent = new Intent();
                returnIntent.putExtra("trainerName",inputData);
                setResult(Activity.RESULT_OK, returnIntent);
                //close this activity and return to the caller
                finish();
            }
        });
    }
}