package com.dudinskyi.carbuddy;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
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
    private Button mWalletLocationButton;
    private Button mCarLocationButton;
    private Button mMonitorButton;
    private LatLng mWalletLatLng;
    private LatLng mCarLatLng;
    private static final int GET_WALLET_LOCATION = 1;
    private static final int GET_CAR_LOCATION = 2;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);
        mWalletLocationButton = (Button) findViewById(R.id.wallet_button);
        mCarLocationButton = (Button) findViewById(R.id.car_button);
        mMonitorButton = (Button) findViewById(R.id.monitor_button);
        servicesConnected();
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
                Intent i = new Intent(StartActivity.this, LocationService.class);
                startService(i);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (GET_WALLET_LOCATION  == requestCode && RESULT_OK == resultCode) {
            mWalletLatLng = data.getExtras().getParcelable(MapsActivity.POSITION);
        } else if (GET_CAR_LOCATION  == requestCode && RESULT_OK == resultCode) {
            mCarLatLng = data.getExtras().getParcelable(MapsActivity.POSITION);
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
                errorFragment.show(getFragmentManager(),"Location Updates");
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
}
