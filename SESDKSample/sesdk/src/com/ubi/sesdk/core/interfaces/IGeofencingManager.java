package com.ubi.sesdk.core.interfaces;

import com.google.android.gms.location.Geofence;

import java.util.ArrayList;

/**
 * @author João Pedro Pedrosa, SE on 18/02/2016.
 */
public interface IGeofencingManager {

    Geofence generateGeofencing(String id, double latitude, double longitude, float radius, int type);

    void addGeofencingList(ArrayList<Geofence> mGeofenceList);

    ArrayList<Geofence> getGeofencingList();

    void addGeofencing(Geofence geofence);

    void removeAllGeofencing();

    void removeGeofencing(Geofence geofence);
}
