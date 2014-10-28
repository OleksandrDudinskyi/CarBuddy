package com.dudinskyi.carbuddy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * @author Oleksandr Dudinskyi(dudinskyj@gmail.com)
 */
public class StartActivity extends Activity {
    private Button mWalletLocationButton;
    private Button mCarLocationButton;
    private Button mMonitorButton;
    private static final int GET_WALLET_LOCATION = 1;
    private static final int GET_CAR_LOCATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);
        mWalletLocationButton = (Button) findViewById(R.id.wallet_button);
        mCarLocationButton = (Button) findViewById(R.id.car_button);
        mMonitorButton = (Button) findViewById(R.id.monitor_button);
        mWalletLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getWalletLocation = new Intent(StartActivity.this, MapsActivity.class);
                startActivityForResult(getWalletLocation, GET_WALLET_LOCATION);
            }
        });
        mCarLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getWalletLocation = new Intent(StartActivity.this, MapsActivity.class);
                startActivityForResult(getWalletLocation, GET_CAR_LOCATION);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (GET_CAR_LOCATION  == requestCode && RESULT_OK == resultCode) {

        } else if (GET_WALLET_LOCATION  == requestCode && RESULT_OK == resultCode) {

        }
    }
}
