package com.ubi.sesdk.core.managers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.ubi.sesdk.core.geofencing.GeofenceErrorMessages;
import com.ubi.sesdk.core.geofencing.GeofencingIntentService;
import com.ubi.sesdk.core.interfaces.IGeofencingManager;

import java.util.ArrayList;
import java.util.List;


/**
 * @author João Pedro Pedrosa, SE on 18/02/2016.
 */
public class GeofencingManager implements IGeofencingManager, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    private static final String TAG = "GeofencingManager";
    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    public static final float GEOFENCE_RADIUS_IN_METERS = 1609; // 1 mile, 1.6 km

    private Context mContext;
    private GeofencingIntentService geofencingIntentService;
    private GoogleApiClient mGoogleApiClient;
    private ArrayList<Geofence> mGeofenceList;

    public GeofencingManager(Context mContext){
        this.mContext = mContext;
        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this.mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public void setGeofencingIntentService(GeofencingIntentService geofencingIntentService) {
        this.geofencingIntentService = geofencingIntentService;
    }

    @Override
    public Geofence generateGeofencing(String id, double latitude, double longitude, float radius, int type){
        return new Geofence.Builder()
                .setRequestId(id)
                .setCircularRegion(latitude, longitude,radius)
                .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(type)
                .build();
    }

    @Override
    public void addGeofencingList(ArrayList<Geofence> mGeofenceList) {
        this.mGeofenceList = mGeofenceList;
        addGeofencingToGoogleLocationServices();
    }

    @Override
    public ArrayList<Geofence> getGeofencingList() {
        return this.mGeofenceList;
    }

    @Override
    public void addGeofencing(Geofence geofence) {
        if(this.mGeofenceList==null){
            this.mGeofenceList = new ArrayList<>();
        }
        this.mGeofenceList.add(geofence);
        addGeofencingToGoogleLocationServices();
    }

    @Override
    public void removeAllGeofencing() {
        if (!mGoogleApiClient.isConnected()) {
            Log.e(TAG,"Google API Client not connected!");
            return;
        }
        try {
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException securityException) {
            logSecurityException(securityException);
        }
    }

    @Override
    public void removeGeofencing(Geofence geofence) {
        this.mGeofenceList.remove(geofence);
        addGeofencingToGoogleLocationServices();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }


    public void addGeofencingToGoogleLocationServices() {
        if (!mGoogleApiClient.isConnected()) {
            Log.e(TAG,"Google API Client not connected!");
            return;
        }
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException securityException) {
            logSecurityException(securityException);
        }
    }

    private void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " + "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(mContext, geofencingIntentService.getClass());
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            Log.i(TAG,"Geofence added with success!");
        } else {
            String errorMessage = GeofenceErrorMessages.getErrorString(mContext, status.getStatusCode());
            Log.e(TAG, errorMessage);
        }
    }
}
