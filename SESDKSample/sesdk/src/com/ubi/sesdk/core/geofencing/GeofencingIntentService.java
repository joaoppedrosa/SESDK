package com.ubi.sesdk.core.geofencing;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.ubi.sesdk.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author João Pedro Pedrosa, SE on 22/02/2016.
 */
public class GeofencingIntentService extends IntentService {

    private static final String TAG = "GeofencingIntentService";

    public GeofencingIntentService(){
        super(TAG);
    }

    @Override
    final protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            String geofenceTransitionDetails = getGeofenceTransitionDetails(triggeringGeofences);
            onFindGeofences(triggeringGeofences);
            Log.i(TAG, geofenceTransitionDetails);
        } else {
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }
    }

    public String getGeofenceTransitionDetails(List<Geofence> triggeringGeofences) {
        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        return TextUtils.join(", ",  triggeringGeofencesIdsList);
    }


    public void onFindGeofences(List<Geofence> geofences){

    }
}
