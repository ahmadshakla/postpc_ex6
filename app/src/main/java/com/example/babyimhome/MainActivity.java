package com.example.babyimhome;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 99;
    private static final String STATE = "tracking";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String SHOW_HOME_LOCATION = "show home location";
    private static final int MIN_ACCURAY = 50;
    private static final String STOP = "Stop Tracking";
    private static final String START = "Start Tracking";
    public final static String ACTION = "POST_PC.ACTION_SEND_SMS";
    private static final String LOCATION_PERMISSION_ERROR = "without location permission, the app wont work";
    private static final String SMS_PERMISSION_ERROR = "without SMS permission, you can't send messages";
    private TextView latitude;
    private TextView longitude;
    private TextView accuracy;
    private LocationHandler locationHandler;
    public static final String SHARED_PREF = "shared preferences";

    SharedPreferences sharedPreferences;
    private Button setHomeLocation;
    private Button clearHomeLocation;
    private Button refresh;
    private Button setSMS;
    private Button sendSMS;
    private TextView homeLocation;
    private Location currLocation;
    private BroadcastReceiver receiver;
    private boolean tracking = false;
    SMSPopup smsPopup;



    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(STATE, tracking).apply();
        smsPopup = new SMSPopup(sharedPreferences);
        currLocation = null;
        float savedLatitude = sharedPreferences.getFloat(LATITUDE, 0F);
        float savedLongitude = sharedPreferences.getFloat(LONGITUDE, 0F);
        if (savedLatitude != 0 || savedLongitude != 0) {
            homeLocation.setVisibility(View.VISIBLE);
            homeLocation.setText("your home location is defined as <" + savedLatitude + " , " + savedLongitude + ">");
        }
        locationHandler = new LocationHandler(this);
        handleStartButton();
        handleSetSMSButton();
        handleSendSMSButton();
        receiver = getReceiver();
    }

    private void initUI() {
        latitude = findViewById(R.id.textView3);
        longitude = findViewById(R.id.textView7);
        accuracy = findViewById(R.id.textView9);
        refresh = findViewById(R.id.button);
        setHomeLocation = findViewById(R.id.button2);
        setHomeLocation.setVisibility(View.INVISIBLE);
        clearHomeLocation = findViewById(R.id.button3);
        clearHomeLocation.setVisibility(View.INVISIBLE);
        homeLocation = findViewById(R.id.textView2);
        homeLocation.setVisibility(View.INVISIBLE);
        setSMS = findViewById(R.id.button4);
        sendSMS = findViewById(R.id.button5);
    }

    private void handleStartButton() {
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tracking = !tracking;
                if (tracking) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationHandler.startTracking();
                        refresh.setText(STOP);
                        registerReceiver(receiver, new IntentFilter(LocationHandler.INTENT_SEND));
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
                        tracking = !tracking;
                    }
                } else {
                    refresh.setText(START);
                    setUI(0d, 0d, 0d);
                    setHomeLocation.setVisibility(View.INVISIBLE);
                    locationHandler.stopTracking();
                    unregisterReceiver(receiver);
                }
            }
        });
    }

    private BroadcastReceiver getReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                double newLatitude = intent.getDoubleExtra(LocationHandler.LATITUDE, 0);
                double newLongitude = intent.getDoubleExtra(LocationHandler.LONGITUDE, 0);
                double newAccuracy = intent.getFloatExtra(LocationHandler.ACCURACY, 0);
                if (newAccuracy <= MIN_ACCURAY) {
                    setHomeLocation.setVisibility(View.VISIBLE);
                    handleSetHomeButton(newLatitude, newLongitude);
                } else {
                    setHomeLocation.setVisibility(View.INVISIBLE);
                }
                setUI(newLatitude, newLongitude, newAccuracy);
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationHandler.startTracking();
                refresh.setText(STOP);
                tracking = true;
                registerReceiver(receiver, new IntentFilter(LocationHandler.INTENT_SEND));
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this, LOCATION_PERMISSION_ERROR, Toast.LENGTH_SHORT).show();

                }
            }
        } else if (requestCode == LocalSendSmsBroadcastReceiver.MSG_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                smsPopup.show(getSupportFragmentManager(), "example dialog");

            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                    Toast.makeText(this, SMS_PERMISSION_ERROR, Toast.LENGTH_SHORT).show();

                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tracking) {

            unregisterReceiver(receiver);

        }

    }

    private void handleSetHomeButton(final double latitude, final double longitude) {
        setHomeLocation.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                homeLocation.setVisibility(View.VISIBLE);
                homeLocation.setText("your home location is defined as <" + latitude + " , " + longitude + ">");
                sharedPreferences.edit().putFloat(LATITUDE, (float) latitude).putFloat(LONGITUDE, (float) longitude).apply();
                clearHomeLocation.setVisibility(View.VISIBLE);
                clearHomeLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        homeLocation.setText("");
                        homeLocation.setVisibility(View.INVISIBLE);
                        sharedPreferences.edit().putFloat(LATITUDE, 0).putFloat(LONGITUDE, 0).apply();
                        clearHomeLocation.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });
    }

    private void handleSetSMSButton() {
        setSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    smsPopup.show(getSupportFragmentManager(), "example dialog");
                }
                else {
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.SEND_SMS},LocalSendSmsBroadcastReceiver.MSG_REQUEST_CODE);

                }
            }
        });
    }

    private void handleSendSMSButton() {
        sendSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNum = sharedPreferences.getString(LocalSendSmsBroadcastReceiver.PHONE, "");
                if (!phoneNum.equals("")) {
                    Intent intent = new Intent(ACTION);
                    intent.putExtra(LocalSendSmsBroadcastReceiver.PHONE, phoneNum);
                    intent.putExtra(LocalSendSmsBroadcastReceiver.CONTENT, "Honey, I'm home");
                    sendBroadcast(intent);
                }
            }
        });
    }

    private void setUI(Double savedLatitude, Double savedLongitude, Double savedAccuracy) {
        latitude.setText(String.valueOf(savedLatitude));
        longitude.setText(String.valueOf(savedLongitude));
        accuracy.setText(String.valueOf(savedAccuracy));
    }
}
