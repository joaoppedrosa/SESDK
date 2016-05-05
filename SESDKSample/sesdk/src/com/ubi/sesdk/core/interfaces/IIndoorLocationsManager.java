package com.ubi.sesdk.core.interfaces;

import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IARegion;
import com.indooratlas.android.sdk.resources.IAFloorPlan;
import com.ubi.sesdk.core.listeners.IndoorAtlasListener;

/**
 * @author João Pedro Pedrosa, SE on 18/02/2016.
 */
public interface IIndoorLocationsManager {

    void loadFloorPlan(IndoorAtlasListener indoorAtlasListener);

    IARegion.Listener getRegionListener();

    IALocationManager getLocationManager();

    String getMapFilePath();

    IAFloorPlan getFloorPlant();
}
