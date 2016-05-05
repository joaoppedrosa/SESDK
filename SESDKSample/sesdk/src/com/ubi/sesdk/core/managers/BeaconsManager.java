package com.ubi.sesdk.core.managers;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.estimote.sdk.cloud.model.BeaconInfo;
import com.estimote.sdk.connection.BeaconConnection;
import com.estimote.sdk.connection.MotionState;
import com.estimote.sdk.connection.Property;
import com.estimote.sdk.exception.EstimoteDeviceException;
import com.ubi.sesdk.core.interfaces.IBeaconsManager;
import com.ubi.sesdk.core.listeners.IBeacons;
import com.ubi.sesdk.core.model.BeaconID;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author João Pedro Pedrosa, SE on 18/02/2016.
 */
public class BeaconsManager implements IBeaconsManager, BeaconManager.ErrorListener{

    private static final String TAG = "BeaconsManager";
    private Context mContext;
    private BeaconManager beaconManagerMonitoring;
    private BeaconManager beaconManagerRanging;
    private IBeacons iBeacons;
    private BeaconConnection connection;
    private List<Region> regionsToMonitor = new ArrayList<>();
    private List<Region> regionsToRanging = new ArrayList<>();

    public BeaconsManager(Context mContext, BeaconManager beaconManagerMonitoring, BeaconManager beaconManagerRanging){
        this.mContext = mContext;
        this.beaconManagerMonitoring = beaconManagerMonitoring;
        this.beaconManagerRanging = beaconManagerRanging;
        this.beaconManagerMonitoring.setErrorListener(this);
        this.beaconManagerRanging.setErrorListener(this);
        startMonitoringBeacons();
        startRangingBeacons();
    }

    public void addRegionToMonitor(BeaconID beaconID){
        Region region = beaconID.toBeaconRegion();
        regionsToMonitor.add(region);
    }

    public void addRegionToRanging(BeaconID beaconID){
        Region region = beaconID.toBeaconRegion();
        regionsToRanging.add(region);
    }

    public void setBeaconsListener(IBeacons iBeacons) {
        this.iBeacons = iBeacons;
    }

    @Override
    public void startMonitoring() {
        beaconManagerMonitoring.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                for (Region region : regionsToMonitor) {
                    beaconManagerMonitoring.startMonitoring(region);
                }
            }
        });
    }

    private void startMonitoringBeacons() {
        beaconManagerMonitoring.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<com.estimote.sdk.Beacon> list) {
                iBeacons.onEnterRegion(region);
            }

            @Override
            public void onExitedRegion(Region region) {
                iBeacons.onExitedRegion(region);
            }
        });
    }


    @Override
    public void startRanging() {
        beaconManagerRanging.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                for (Region region : regionsToRanging) {
                    beaconManagerRanging.startRanging(region);
                }
            }
        });
    }

    public void startRangingBeacons() {
        beaconManagerRanging.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<com.estimote.sdk.Beacon> list) {
                iBeacons.onDiscoveryBeacon(list);
            }
        });
    }

    @Override
    public void stopMonitoringBeacons(Region region) {
        if(beaconManagerMonitoring!=null){
            beaconManagerMonitoring.stopMonitoring(region);
        }
    }

    @Override
    public void stopRangingBeacons(Region region) {
        if(beaconManagerRanging!=null){
            beaconManagerRanging.stopRanging(region);
        }
    }

    @Override
    public Region createRegion(String identifier, UUID UUID, int major, int minor) {
        if(identifier!=null){
            return new Region(identifier,UUID,major,minor);
        }else{
            return null;
        }
    }

    @Override
    public Region createRegionForAllBeacons(String identifier) {
        if(identifier!=null){
            return new Region(identifier,null,null,null);
        }else{
            return null;
        }
    }

    @Override
    public void startListenTemperatureBeacon(com.estimote.sdk.Beacon beacon) {
        connection = new BeaconConnection(mContext, beacon, new BeaconConnection.ConnectionCallback() {
            @Override
            public void onAuthenticationError(EstimoteDeviceException e) {
                Log.e(TAG, "onAuthenticationError",e);
            }

            @Override
            public void onConnected(BeaconInfo beaconInfo) {
                refreshTemperature(iBeacons);
            }

            @Override
            public void onDisconnected() {
                Log.e(TAG, "onDisconnected");
            }

            @Override
            public void onAuthorized(BeaconInfo beaconInfo) {
                Log.e(TAG, "onAuthorized");
            }
        });
    }

    @Override
    public void startListenMotionBeacon(com.estimote.sdk.Beacon beacon) {
        connection = new BeaconConnection(mContext, beacon, new BeaconConnection.ConnectionCallback() {
            @Override
            public void onAuthenticationError(EstimoteDeviceException e) {
                Log.e(TAG, "onAuthenticationError",e);
            }

            @Override
            public void onConnected(BeaconInfo beaconInfo) {
                connection.edit().set(connection.motionDetectionEnabled(), true).commit(new BeaconConnection.WriteCallback() {
                    @Override
                    public void onSuccess() {
                        enableMotionListner(iBeacons);
                    }

                    @Override
                    public void onError(EstimoteDeviceException exception) {
                        Log.e(TAG, "onError: Failed to enable motion detection", exception);
                    }
                });
            }

            @Override
            public void onDisconnected() {
                Log.e(TAG, "onDisconnected");
            }

            @Override
            public void onAuthorized(BeaconInfo beaconInfo) {
                Log.e(TAG, "onAuthorized");
            }
        });
    }

    @Override
    public BeaconConnection getBeaconConnections() {
        return this.connection;
    }

    @Override
    public String getBeaconDistance(com.estimote.sdk.Beacon beacon) {
        return String.format("%.2fm", Utils.computeAccuracy(beacon));
    }

    @Override
    public String getBeaconProximity(com.estimote.sdk.Beacon beacon) {
        return Utils.computeProximity(beacon).toString();
    }

    @Override
    public void onError(Integer integer) {
        Log.e(TAG, "onError: " + integer);
    }

    private void refreshTemperature(final IBeacons iBeacons) {
        connection.temperature().getAsync(new Property.Callback<Float>() {
            @Override
            public void onValueReceived(final Float value) {
                iBeacons.onBeaconTemperature(value);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {refreshTemperature(iBeacons);
                    }
                }, 200000);
            }

            @Override
            public void onFailure() {
                Log.e(TAG, "onFailure: Unable to read temperature from beacon");
            }
        });
    }

    private void enableMotionListner(final IBeacons iBeacons) {
        connection.setMotionListener(new Property.Callback<MotionState>() {
            @Override
            public void onValueReceived(final MotionState value) {
                iBeacons.onBeaconMotionListener(value);
            }

            @Override
            public void onFailure() {
                Log.e(TAG, "onFailure: Unable to register motion listener");
            }
        });
    }
}
