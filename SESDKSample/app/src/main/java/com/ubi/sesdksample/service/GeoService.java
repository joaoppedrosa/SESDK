package com.ubi.sesdksample.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.ubi.sesdk.core.geofencing.GeofencingIntentService;
import com.ubi.sesdksample.R;
import com.ubi.sesdksample.activities.MainActivity;

import java.util.List;

/**
 * @author João Pedro Pedrosa, SESDKSample on 02/05/2016.
 */

public class GeoService extends GeofencingIntentService {

    private static final String TAG = "GeoService";

    @Override
    public void onFindGeofences(List<Geofence> geofences) {
        if(!geofences.isEmpty()){
            String geofenceTransitionDetails = getGeofenceTransitionDetails(geofences);
            showNotification(geofenceTransitionDetails);
            Log.d(TAG, "onFindGeofence: " + geofenceTransitionDetails);
        }else{
            Log.e(TAG, "onFindGeofence: geofence is empty");
        }
    }

    public void showNotification(String text) {
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Geofencing")
                .setContentText(text)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }
}
