package com.dudinskyi.carbuddy;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.LatLng;

/**
 * @author Oleksandr Dudinskyi(dudinskyj@gmail.com)
 */
public class StartActivity extends Activity {
    public static final String MARKER_TITLE = "MARKER_TITLE";
    public static final String MARKER_POSITION = "MARKER_POSITION";
    public static final String WALLET_POSITION = "WALLET_POSITION";
    public static final String CAR_POSITION = "CAR_POSITION";
    public static final String ACTION_GET_LOCATION = "ACTION_GET_LOCATION";
    public static final String LOCATION_SERVICE_RECEIVER = "com.dudinskyi.carbuddy.startactivity";
    private Button mWalletLocationButton;
    private Button mCarLocationButton;
    private Button mMonitorButton;
    private LatLng mWalletLatLng;
    private LatLng mCarLatLng;
    private LocationManager mlocManager;
    private static final int GET_WALLET_LOCATION = 1;
    private static final int GET_CAR_LOCATION = 2;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);
        servicesConnected();
        registerReceiver(mMessageReceiver, new IntentFilter(LOCATION_SERVICE_RECEIVER));
        bindServiceIfRunning();
        mWalletLocationButton = (Button) findViewById(R.id.wallet_button);
        mCarLocationButton = (Button) findViewById(R.id.car_button);
        mMonitorButton = (Button) findViewById(R.id.monitor_button);
        mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mWalletLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getWalletLocation = new Intent(StartActivity.this, MapsActivity.class);
                getWalletLocation.putExtra(MARKER_TITLE, "Wallet location");
                if (mWalletLatLng != null) {
                    getWalletLocation.putExtra(MARKER_POSITION, mWalletLatLng);
                }
                startActivityForResult(getWalletLocation, GET_WALLET_LOCATION);
            }
        });
        mCarLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getWalletLocation = new Intent(StartActivity.this, MapsActivity.class);
                getWalletLocation.putExtra(MARKER_TITLE, "Car location");
                if (mCarLatLng != null) {
                    getWalletLocation.putExtra(MARKER_POSITION, mCarLatLng);
                }
                startActivityForResult(getWalletLocation, GET_CAR_LOCATION);
            }
        });
        mMonitorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWalletLatLng == null) {
                    new AlertDialog.Builder(StartActivity.this)
                            .setTitle("No wallet location defined")
                            .setMessage("Please select wallet location")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .show();
                } else if (mCarLatLng == null) {
                    new AlertDialog.Builder(StartActivity.this)
                            .setTitle("No car location defined")
                            .setMessage("Please select car location")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .show();
                } else {
                    if (mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        startService();
                    } else {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(StartActivity.this);
                        alertDialogBuilder
                                .setMessage("GPS is disabled in your device. Enable it?")
                                .setCancelable(false)
                                .setPositiveButton("Enable GPS",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,
                                                                int id) {
                                                Intent callGPSSettingIntent = new Intent(
                                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                                startActivity(callGPSSettingIntent);
                                            }
                                        });
                        alertDialogBuilder.setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = alertDialogBuilder.create();
                        alert.show();

                    }
                }
            }
        });
    }

    private void startService() {
        Intent startServiceIntent = new Intent(StartActivity.this, LocationService.class);
        startServiceIntent.putExtra(WALLET_POSITION, mWalletLatLng);
        startServiceIntent.putExtra(CAR_POSITION, mCarLatLng);
        startService(startServiceIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (GET_WALLET_LOCATION == requestCode && RESULT_OK == resultCode) {
            mWalletLatLng = data.getExtras().getParcelable(MapsActivity.POSITION);
            if (mCarLatLng != null) {
                if (isServiceRunning()) {
                    Intent startServiceIntent = new Intent(StartActivity.this, LocationService.class);
                    bindService(startServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
                }
            }
        } else if (GET_CAR_LOCATION == requestCode && RESULT_OK == resultCode) {
            mCarLatLng = data.getExtras().getParcelable(MapsActivity.POSITION);
            if (mWalletLatLng != null) {
                Intent startServiceIntent = new Intent(StartActivity.this, LocationService.class);
                if (isServiceRunning()) {
                    bindService(startServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
                }
            }
        }
    }

    private void bindServiceIfRunning() {
        if (isServiceRunning()) {
            Intent startServiceIntent = new Intent(StartActivity.this, LocationService.class);
            startServiceIntent.setAction(ACTION_GET_LOCATION);
            bindService(startServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason.
            // resultCode holds the error code.
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getFragmentManager(), "Location Updates");
            }
        }
        return false;
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LocationService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // handler for received Intents for the "my-event" event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                Bundle bundle = intent.getExtras();
                if (bundle.containsKey(StartActivity.WALLET_POSITION)) {
                    mWalletLatLng = bundle.getParcelable(StartActivity.WALLET_POSITION);
                }
                if (bundle.containsKey(StartActivity.CAR_POSITION)) {
                    mCarLatLng = intent.getExtras().getParcelable(StartActivity.CAR_POSITION);
                }
            }
        }
    };

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
        }

        public void onServiceDisconnected(ComponentName className) {
        }
    };
}
