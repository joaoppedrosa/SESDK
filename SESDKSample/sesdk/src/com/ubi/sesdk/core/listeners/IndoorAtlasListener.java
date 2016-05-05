package com.ubi.sesdk.core.listeners;

import com.indooratlas.android.sdk.resources.IAFloorPlan;

/**
 * @author João Pedro Pedrosa, SE on 24/02/2016.
 */
public interface IndoorAtlasListener {
    void onDownloadMapComplete(String path, IAFloorPlan mFloorPlan);
}
