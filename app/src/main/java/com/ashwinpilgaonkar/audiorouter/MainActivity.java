package com.ashwinpilgaonkar.audiocontrol;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import static android.media.AudioManager.STREAM_MUSIC;

public class MainActivity extends AppCompatActivity {

    TextView Status;
    TextView Static;
    Button Apply;
    RadioButton Earpiece;
    RadioButton Speaker;
    RadioButton Headphones;
    Switch Enable;
    CheckBox Proximity;
    int radioChecked; //WHICH RADIO BUTTON IS CHECKED
    int applied; //WHICH OF THE 3 STATES IS CURRENTLY ACTIVE

    AudioManager manager;
    MediaPlayer mediaPlayer;

    PowerManager pmanager;
    PowerManager.WakeLock lock;

    private PreferenceChangeListener mPreferenceListener = null;
    SharedPreferences prefs;

    boolean m=false, p=false, n=false, a=false;

    //PREFERENCE ONCHANGE LISTENER
    private class PreferenceChangeListener implements
            SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //INITIALIZE UI ELEMENTS
        Status = (TextView) findViewById(R.id.Status_TextView);
        Static = (TextView) findViewById(R.id.Static_Route);
        Apply = (Button) findViewById(R.id.button);
        Earpiece = (RadioButton) findViewById(R.id.Earpiece_Radio);
        Speaker = (RadioButton) findViewById(R.id.Speaker_Radio);
        Headphones = (RadioButton) findViewById(R.id.Headphones_Radio);
        Enable = (Switch) findViewById(R.id.Enable_Switch);
        Proximity = (CheckBox) findViewById(R.id.Proximity_Checkbox);

        manager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer = new MediaPlayer();
        pmanager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        lock = pmanager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "ASD");
        Enable.setChecked(false);
        disableUI();

        //SHARED PREFERENCES AND PREFERENCE ON CHANGE LISTENER
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPreferenceListener = new PreferenceChangeListener();
        prefs.registerOnSharedPreferenceChangeListener((SharedPreferences.OnSharedPreferenceChangeListener) mPreferenceListener);


        //Set a CheckedChange Listener for Switch Button
        Enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean on){

                if(on) {
                    //IF SWITCH IS ON, ENABLE UI
                    Apply.setEnabled(true);
                    Earpiece.setEnabled(true);
                    Speaker.setEnabled(true);
                    Static.setEnabled(true);
                    Proximity.setEnabled(true);

                    //ENABLE HEADPHONE OPTION ONLY IF IT IS PLUGGED IN
                    if(manager.isWiredHeadsetOn())
                        Headphones.setEnabled(true);

                    else
                        Headphones.setEnabled(false);

                    getStatus();
                }

                //IF SWITCH IS OFF, DISABLE UI
                else
                    disableUI();
            }
        });


        //LISTENER FOR PROXIMITY SENSOR CHECKBOX
        Proximity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean on){

                if(on)
                        lock.acquire();

                else {
                    //IF LOCK HAS BEEN ACQUIRED, RELEASE IT
                    if(lock.isHeld())
                        lock.release();
                }
            }
        });


        Apply.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View V) {
                        ApplyChanges();
                    }
                }
        );
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.Earpiece_Radio:
                if (checked) {
                    radioChecked = 1;
                    Speaker.setChecked(false);
                    Headphones.setChecked(false);
                }
                break;

            case R.id.Speaker_Radio:
                if (checked) {
                    radioChecked = 2;
                    Earpiece.setChecked(false);
                    Headphones.setChecked(false);
                }
                break;

            case R.id.Headphones_Radio:
                if (checked) {
                    radioChecked = 3;
                    Speaker.setChecked(false);
                    Earpiece.setChecked(false);
                }
                break;

            default: radioChecked = 0;
                break;

        }
    }


    //OVERFLOW SETTINGS MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.action_about) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setCancelable(true);

            builder.setTitle("About");
            builder.setMessage("AudioControl v1.0.0 \nCreated by Ashwin Pilgaonkar \n\nFor any feedback/feature requests/bug reports write to me at ashwinpilgaonkar@gmail.com");
            //builder.setView(LayoutInflater.from(this).inflate(R.layout.help_main_dialog,null));

            builder.setNeutralButton("Dismiss",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int which) {
                        }
                    });
            builder.show();
            return true;
        }

        if (id == R.id.action_help) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setCancelable(true);

            builder.setTitle("Help");
            builder.setView(LayoutInflater.from(this).inflate(R.layout.help_main_dialog,null));

            builder.setNeutralButton("Dismiss",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int which) {
                        }
                    });
            builder.show();
            return true;
        }

        if (id == R.id.action_settings) {

            Intent intent = new Intent(this, SettingsActivity.class); //Explicit Intent
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private void getStatus()
    {
        switch (applied){

            case 1: Status.setText("EARPIECE");
                    break;

            case 2: Status.setText("SPEAKER");
                break;

            case 3: Status.setText("HEADPHONES");
                break;

            default: Status.setText("DISABLED");
        }
    }

    private void disableUI()
    {
        //DISABLE UI ELEMENTS
        Static.setEnabled(false);
        Apply.setEnabled(false);
        Earpiece.setEnabled(false);
        Speaker.setEnabled(false);
        Headphones.setEnabled(false);
        Proximity.setEnabled(false);
        Status.setText("DISABLED");

        //SET ROUTING TO DEFAULT SPEAKER
        manager.setMode(AudioManager.MODE_NORMAL);
        manager.setSpeakerphoneOn(false);
        mediaPlayer.setAudioStreamType(STREAM_MUSIC);

        //INITIALIZE TO DEFAULT VALUES
        if(manager.isWiredHeadsetOn()){
            Speaker.setChecked(false);
            Headphones.setChecked(true);
            Earpiece.setChecked(false);
            Proximity.setChecked(false);
            radioChecked = 3;
            applied = 3;
        }

        else {
            Speaker.setChecked(true);
            Headphones.setChecked(false);
            Earpiece.setChecked(false);
            Proximity.setChecked(false);
            radioChecked = 2;
            applied = 2;
        }
    }


    public void ApplyChanges(){

        //IF USER TRIES TO APPLY AN OPTION THAT HAS ALREADY BEEN APPLIED
        if(applied==radioChecked){
            Toast toast = Toast.makeText(getApplicationContext(), "Selected option is already active", Toast.LENGTH_SHORT);
            toast.show();
        }


        else {
            switch (radioChecked) {

                //EARPIECE
                case 1:
                    manager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    manager.setSpeakerphoneOn(false);
                    mediaPlayer.setAudioStreamType(AudioManager.USE_DEFAULT_STREAM_TYPE);
                    applied = 1;
                    getStatus();
                    break;

                //SPEAKER
                case 2:
                    if (manager.isWiredHeadsetOn()) {
                        manager.setWiredHeadsetOn(false);
                        manager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                        manager.setSpeakerphoneOn(true);
                        mediaPlayer.setAudioStreamType(AudioManager.USE_DEFAULT_STREAM_TYPE);
                        applied = 2;
                        getStatus();
                    }

                    else {
                        manager.setMode(AudioManager.MODE_NORMAL);
                        manager.setSpeakerphoneOn(true);
                        mediaPlayer.setAudioStreamType(AudioManager.USE_DEFAULT_STREAM_TYPE);
                        applied = 2;
                        getStatus();
                    }

                    break;

                //HEADPHONES
                case 3:
                    manager.setMode(AudioManager.MODE_NORMAL);
                    manager.setSpeakerphoneOn(true);
                    mediaPlayer.setAudioStreamType(AudioManager.USE_DEFAULT_STREAM_TYPE);
                    applied = 3;
                    getStatus();
                    break;

                default:
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                    builder.setTitle("Error");
                    builder.setMessage("No option was selected.");

                    builder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    Speaker.setChecked(true);
                                    radioChecked = 2;
                                    applied=2;
                                }
                            });
                    builder.show();
                    break;
            }
        }

    }
}