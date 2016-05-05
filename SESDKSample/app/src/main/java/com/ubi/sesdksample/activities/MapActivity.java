package com.ubi.sesdksample.activities;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.resources.IAFloorPlan;
import com.indooratlas.android.sdk.resources.IALatLng;
import com.indooratlas.android.sdk.resources.IALocationListenerSupport;
import com.ubi.sesdk.core.SE;
import com.ubi.sesdk.core.listeners.IndoorAtlasListener;
import com.ubi.sesdk.core.managers.IndoorLocationManager;
import com.ubi.sesdk.core.utils.BlueDotView;
import com.ubi.sesdksample.R;

public class MapActivity extends AppCompatActivity implements IndoorAtlasListener {

    private static final float dotRadius = 0.50f;
    private static final String TAG = "MapActivity";
    private IAFloorPlan mFloorPlan;
    private BlueDotView blueDotView;
    private IndoorLocationManager indoorLocationManager;

    /**INDOORLOCATION NEED TO HAVE LOCATION TURN ON TO WORK CORRECTLY**/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        this.blueDotView = (BlueDotView) findViewById(R.id.blueDotView);
        this.indoorLocationManager = SE.getIndoorLocationManager();
        this.indoorLocationManager.loadFloorPlan(this);
    }

    @Override
    public void onDownloadMapComplete(String path, IAFloorPlan mFloorPlan) {
        Log.e(TAG,"onDownloadMapComplete");
        this.mFloorPlan = mFloorPlan;
        loadImageMap(this.mFloorPlan);
    }

    private void loadImageMap(final IAFloorPlan mFloorPlan){
        Glide.with(this)
                .load(mFloorPlan.getUrl())
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                        blueDotView.setImage(ImageSource.bitmap(bitmap));
                        blueDotView.setRadius(mFloorPlan.getMetersToPixels() * dotRadius);
                        blueDotView.setDrawingCacheEnabled(true);
                        blueDotView.buildDrawingCache();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        this.indoorLocationManager.getLocationManager().requestLocationUpdates(IALocationRequest.create(), mLocationListener);
        this.indoorLocationManager.getLocationManager().registerRegionListener(this.indoorLocationManager.getRegionListener());
    }

    @Override
    public void onPause() {
        super.onPause();
        this.indoorLocationManager.getLocationManager().removeLocationUpdates(mLocationListener);
        this.indoorLocationManager.getLocationManager().unregisterRegionListener(this.indoorLocationManager.getRegionListener());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.indoorLocationManager.getLocationManager().destroy();
    }

    private IALocationListener mLocationListener = new IALocationListenerSupport() {
        @Override
        public void onLocationChanged(IALocation location) {
            Log.d(TAG, "location is: " + location.getLatitude() + "," + location.getLongitude());
            if (blueDotView != null && blueDotView.isReady()) {
                IALatLng latLng = new IALatLng(location.getLatitude(), location.getLongitude());
                PointF point = mFloorPlan.coordinateToPoint(latLng);
                blueDotView.setDotCenter(point);
                blueDotView.postInvalidate();
            }
        }
    };
}
