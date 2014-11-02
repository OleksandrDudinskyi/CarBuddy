package com.dudinskyi.carbuddy;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

/**
 * Oleksandr Dudinskyi (dudinskyj@gmail.com)
 *
 */
public class LocationService extends Service {
    private NotificationManager mNotificationManager;
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private int NOTIFICATION = R.string.local_service_started;
    private LocationManager mgr;
    private long last_time;
    private Locationer gps_locationer, network_locationer;
    private Notification.Builder builder;

    private static final String DEBUG_TAG = "LocationService";

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
            gps_locationer = new Locationer(getBaseContext());
            network_locationer = new Locationer(getBaseContext());

            Criteria criteria = new Criteria();
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(false);
            criteria.setPowerRequirement(Criteria.POWER_LOW);

            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            String providerFine = mgr.getBestProvider(criteria, true);

            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            String providerCoarse = mgr.getBestProvider(criteria, true);

            if (providerCoarse != null) {
                mgr.requestLocationUpdates(providerCoarse, 30000, 5, network_locationer);
            }
            if (providerFine != null) {
                mgr.requestLocationUpdates(providerFine, 2000, 0, gps_locationer);
            }
        }
    }

    @Override
    public void onCreate() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification();
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "local service is started ", Toast.LENGTH_SHORT).show();
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        mNotificationManager.cancel(NOTIFICATION);
        mgr.removeUpdates(gps_locationer);
        mgr.removeUpdates(network_locationer);
        Toast.makeText(this, "local service is stopped", Toast.LENGTH_SHORT).show();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        CharSequence text = getText(R.string.local_service_started);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, StartActivity.class), 0);

        builder = new Notification.Builder(getBaseContext())
                .setContentTitle("You are being tracked...")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(contentIntent)
                .setOngoing(true);

        Notification notification = builder.getNotification();

        // Send the notification.
        mNotificationManager.notify(NOTIFICATION, notification);
    }

    private class gpsStatusListener implements Listener {

        public gpsStatusListener() {
        }

        @Override
        public void onGpsStatusChanged(int event) {
            boolean isGPSFix = false;

            switch (event) {
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    if (last_time != 0) {
                        isGPSFix = (SystemClock.elapsedRealtime() - last_time) < 10000;
                    }
                    if (isGPSFix) {
                        Log.d(DEBUG_TAG, "GPS has a fix");
                    } else {
                        Log.d(DEBUG_TAG, "GPS does not have a fix");
                    }
                    break;
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    isGPSFix = true;
                    Log.d(DEBUG_TAG, "GPS first fix");
                    break;
                case GpsStatus.GPS_EVENT_STARTED:
                    Log.i("GPS", "Started!");
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    Log.i("GPS", "Stopped");
                    break;
            }

        }

    }
}