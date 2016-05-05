package com.ubi.sesdksample.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.Region;
import com.estimote.sdk.connection.MotionState;
import com.ubi.sesdk.core.SE;
import com.ubi.sesdk.core.listeners.IBeacons;
import com.ubi.sesdk.core.managers.BeaconsManager;
import com.ubi.sesdk.core.model.BeaconID;
import com.ubi.sesdksample.R;

import java.util.List;

public class BeaconsActivity extends AppCompatActivity implements IBeacons {
    private static final String TAG = "BeaconsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacons);
        initBeaconsManager();
    }

    private void initBeaconsManager(){
        BeaconsManager beaconsManager = SE.getBeaconsManager();
        beaconsManager.setBeaconsListener(this);
        BeaconID beaconID = new BeaconID("B9407F30-F5F8-466E-AFF9-25556B57FE6D",15621,39312);
        beaconsManager.addRegionToMonitor(beaconID);
        beaconsManager.startMonitoring();
        beaconsManager.addRegionToRanging(beaconID);
        beaconsManager.startRanging();
    }

    @Override
    public void onDiscoveryBeacon(List<Beacon> beacons) {
        Log.d(TAG, "onDiscoveryBeacon: " + beacons.toString());
    }

    @Override
    public void onBeaconTemperature(float temperature) {
        Log.d(TAG, "onBeaconTemperature: " + temperature);
    }

    @Override
    public void onBeaconMotionListener(MotionState motionState) {
        Log.d(TAG, "onBeaconMotionListener: " + motionState.toString());
    }

    @Override
    public void onEnterRegion(Region region) {
        Log.d(TAG, "onEnterRegion: " + region.toString());
    }

    @Override
    public void onExitedRegion(Region region) {
        Log.d(TAG, "onExitedRegion: " + region.toString());
    }
}
