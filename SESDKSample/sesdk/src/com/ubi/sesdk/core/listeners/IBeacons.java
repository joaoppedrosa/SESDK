package com.ubi.sesdk.core.listeners;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.Region;
import com.estimote.sdk.connection.MotionState;
import java.util.List;

/**
 * @author João Pedro Pedrosa, SE on 23-02-2016.
 */
public interface IBeacons {

    void onDiscoveryBeacon(List<Beacon> beacons);

    void onBeaconTemperature(float temperature);

    void onBeaconMotionListener(MotionState motionState);

    void onEnterRegion(Region region);

    void onExitedRegion(Region region);
}
