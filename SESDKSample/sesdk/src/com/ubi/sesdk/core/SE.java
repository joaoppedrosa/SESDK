package com.ubi.sesdk.core;

import android.content.Context;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.EstimoteSDK;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.resources.IAResourceManager;
import com.ubi.sesdk.core.listeners.IBeacons;
import com.ubi.sesdk.core.listeners.IStripe;
import com.ubi.sesdk.core.managers.ARManager;
import com.ubi.sesdk.core.managers.BeaconsManager;
import com.ubi.sesdk.core.managers.GeofencingManager;
import com.ubi.sesdk.core.managers.IndoorLocationManager;
import com.ubi.sesdk.core.managers.PaymentManager;
import com.ubi.sesdk.core.managers.VuforiaVideoManager;
import com.ubi.sesdk.core.providers.VuforiaVideoProvider;

/**
 * @author João Pedro Pedrosa, SESDK on 02/05/2016.
 */
public class SE {

    private static BeaconsManager beaconsManager;
    private static IndoorLocationManager indoorLocationManager;
    private static PaymentManager paymentManager;
    private static VuforiaVideoManager vuforiaVideoManager;
    private static GeofencingManager geofencingManager;
    private static ARManager arManager;
    private static Context mContext;

    public static void init(Context context){
        mContext = context;
    }

    public static void initBeaconsManager(String estimoteAppID, String estimoteAppToken){
        EstimoteSDK.initialize(mContext, estimoteAppID, estimoteAppToken);
        EstimoteSDK.enableDebugLogging(true);
        beaconsManager = new BeaconsManager(mContext, new BeaconManager(mContext), new BeaconManager(mContext));
    }

    public static BeaconsManager getBeaconsManager() {
        return beaconsManager;
    }

    public static void initIndoorAtlas(String floorPalnID){
        indoorLocationManager = new IndoorLocationManager(IALocationManager.create(mContext), IAResourceManager.create(mContext), floorPalnID);
    }

    public static IndoorLocationManager getIndoorLocationManager() {
        return indoorLocationManager;
    }

    public static void initPaymentManager(String stipeKey){
        paymentManager = new PaymentManager(stipeKey);
    }

    public static PaymentManager getPaymentManager() {
        return paymentManager;
    }

    public static void initVuforiaVideoManager(String vuforiaKey){
        vuforiaVideoManager = new VuforiaVideoManager(new VuforiaVideoProvider(), vuforiaKey);
    }

    public static VuforiaVideoManager getVuforiaVideoManager() {
        return vuforiaVideoManager;
    }

    public static void initGeofencingManager(){
        geofencingManager = new GeofencingManager(mContext);
    }

    public static GeofencingManager getGeofencingManager() {
        return geofencingManager;
    }

    public static void initARManager(){
        arManager = new ARManager(mContext);
    }

    public static ARManager getArManager() {
        return arManager;
    }
}
