package com.ubi.sesdksample;

import android.app.Application;

import com.ubi.sesdk.core.SE;

/**
 * @author João Pedro Pedrosa, SESDKSample on 02/05/2016.
 */
public class SampleApplication extends Application{
    
    public static final String ESTIMOTE_APP_ID = "";
    public static final String ESTIMOTE_APP_TOKEN = "";
    public static final String STRIPE_KEY = "";
    public static final String VUFORIA_KEY = "";
    public static final String FLOOR_PLAN_ID = "";

    @Override
    public void onCreate() {
        super.onCreate();
        SE.init(this);
        SE.initBeaconsManager(ESTIMOTE_APP_ID, ESTIMOTE_APP_TOKEN);
        SE.initGeofencingManager();
        SE.initPaymentManager(STRIPE_KEY);
        SE.initVuforiaVideoManager(VUFORIA_KEY);
        SE.initARManager();
        SE.initIndoorAtlas(FLOOR_PLAN_ID);
    }
}
