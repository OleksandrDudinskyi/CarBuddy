package com.dudinskyi.carbuddy;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

/**
 * @author Oleksandr Dudinskyi (dudinskyj@gmail.com)
 */
class LocationDetector implements LocationListener {

	private Context ctx;
    private LatLng mCarLocation;

	private static final String DEBUG_TAG = "Locationer";
	private static final String[] Status = {"out of service", "temporarily unavailable", "available"};
	private static final double ACCURACY_THRESHOLD = 100.0;
	
	public LocationDetector(Context context, LatLng carLocation) {
		ctx = context;
        mCarLocation = carLocation;
	}
	
	@Override
	public void onLocationChanged(Location location) {
        if ((location == null)||(location.getAccuracy() > ACCURACY_THRESHOLD)) {
        	return;
        }
        float[] result = new float[3];
        Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                mCarLocation.latitude, mCarLocation.longitude, result);
        float distanceToCar = result[0];
        if (distanceToCar < 2.0) {
            ctx.sendBroadcast(new Intent("com.dudinskyi.carbuddy"));
        }

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