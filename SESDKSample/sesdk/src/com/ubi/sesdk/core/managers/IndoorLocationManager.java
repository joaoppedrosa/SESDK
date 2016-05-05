package com.ubi.sesdk.core.managers;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;

import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IARegion;
import com.indooratlas.android.sdk.resources.IAFloorPlan;
import com.indooratlas.android.sdk.resources.IAResourceManager;
import com.indooratlas.android.sdk.resources.IAResult;
import com.indooratlas.android.sdk.resources.IAResultCallback;
import com.indooratlas.android.sdk.resources.IATask;
import com.ubi.sesdk.core.interfaces.IIndoorLocationsManager;
import com.ubi.sesdk.core.listeners.IndoorAtlasListener;

import java.io.File;


/**
 * @author João Pedro Pedrosa, SE on 18/02/2016.
 */
public class IndoorLocationManager implements IIndoorLocationsManager {

    private static final String TAG = "IndoorLocationManager";
    private String FLOOR_PLAN_ID;
    private IALocationManager mIALocationManager;
    private IAResourceManager mFloorPlanManager;
    private IARegion.Listener mRegionListener;
    private IndoorAtlasListener indoorAtlasListener;
    private IATask<IAFloorPlan> mIATask;
    private String path;
    private IAFloorPlan mFloorPlan;
    private IAResultCallback<IAFloorPlan> floorPlanResultCallback = new IAResultCallback<IAFloorPlan>() {
        @Override
        public void onResult(IAResult<IAFloorPlan> iaResult) {
            if (iaResult.isSuccess()) {
                IAFloorPlan iaFloorPlan = iaResult.getResult();
                saveImageLocal(iaFloorPlan);
            }
        }
    };

    public IndoorLocationManager(IALocationManager mIALocationManager, IAResourceManager mFloorPlanManager, String FLOOR_PLAN_ID){
        this.mIALocationManager = mIALocationManager;
        this.mFloorPlanManager = mFloorPlanManager;
        this.FLOOR_PLAN_ID = FLOOR_PLAN_ID;
    }

    public String getTAG() {
        return TAG;
    }

    @Override
    public void loadFloorPlan(IndoorAtlasListener indoorAtlasListener) {
        IALocation location = IALocation.from(IARegion.floorPlan(FLOOR_PLAN_ID));
        mIALocationManager.setLocation(location);
        this.indoorAtlasListener = indoorAtlasListener;
        loadAndGetMapInformation();
    }

    @Override
    public IARegion.Listener getRegionListener() {
        return this.mRegionListener;
    }

    @Override
    public IALocationManager getLocationManager() {
        return this.mIALocationManager;
    }

    @Override
    public String getMapFilePath() {
        return this.path;
    }

    @Override
    public IAFloorPlan getFloorPlant() {
        return this.mFloorPlan;
    }


    private void loadAndGetMapInformation(){
        mRegionListener = new IARegion.Listener() {
            @Override
            public void onEnterRegion(IARegion region) {
                if (mIATask != null && !mIATask.isCancelled()) {
                    mIATask.cancel();
                }
                mIATask = mFloorPlanManager.fetchFloorPlanWithId(region.getId());
                mIATask.setCallback(floorPlanResultCallback, Looper.getMainLooper());
            }
            @Override
            public void onExitRegion(IARegion region) {

            }
        };
    }

    private void saveImageLocal(IAFloorPlan mFloorPlan){
        String fileName = mFloorPlan.getId() + ".img";
        String filePath = Environment.getExternalStorageDirectory() + "/"+ Environment.DIRECTORY_DOWNLOADS + "/" + fileName;
        File file = new File(filePath);
        if (!file.exists()) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mFloorPlan.getUrl()));
            request.setDescription("IndoorAtlas floor plan");
            request.setTitle("Floor plan");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            }
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        }
        this.path = filePath;
        this.mFloorPlan = mFloorPlan;
        indoorAtlasListener.onDownloadMapComplete(filePath,mFloorPlan);
    }
}
