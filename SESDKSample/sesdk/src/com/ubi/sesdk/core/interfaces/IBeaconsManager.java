package com.ubi.sesdk.core.interfaces;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.Region;
import com.estimote.sdk.connection.BeaconConnection;

import java.util.UUID;

/**
 * @author João Pedro Pedrosa, SE on 18/02/2016.
 */
public interface IBeaconsManager {
    void startMonitoring();

    void startRanging();

    void stopMonitoringBeacons(Region region);

    void stopRangingBeacons(Region region);

    Region createRegion(String identifier, UUID UUID, int major, int minor);

    Region createRegionForAllBeacons(String identifier);

    void startListenTemperatureBeacon(Beacon beacon);

    void startListenMotionBeacon(Beacon beacon);

    BeaconConnection getBeaconConnections();

    String getBeaconDistance(Beacon beacon);

    String getBeaconProximity(Beacon beacon);
}
