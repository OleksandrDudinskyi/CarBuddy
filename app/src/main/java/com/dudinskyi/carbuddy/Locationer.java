package com.dudinskyi.carbuddy;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

class Locationer implements LocationListener {

	private Context ctx;
	private long mLastLocationMillis;

	private static final String DEBUG_TAG = "Locationer";
	private static final String[] Status = {"out of service", "temporarily unavailable", "available"};
	private static final double ACCURACY_THRESHOLD = 100.0;
	
	public Locationer(Context context) {
		ctx = context;
	}
	
	@Override
	public void onLocationChanged(Location location) {
        if ((location == null)||(location.getAccuracy() > ACCURACY_THRESHOLD)) {
        	return;
        }
        mLastLocationMillis = SystemClock.elapsedRealtime();
        
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.d(DEBUG_TAG, provider + " disabled.");
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d(DEBUG_TAG, provider + " enabled.");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(DEBUG_TAG, provider + " status changed" + status );
	}

}