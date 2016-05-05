package com.ubi.sesdk.core.managers;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import com.beyondar.android.fragment.BeyondarFragmentSupport;
import com.beyondar.android.opengl.util.LowPassFilter;
import com.beyondar.android.plugin.radar.RadarView;
import com.beyondar.android.plugin.radar.RadarWorldPlugin;
import com.beyondar.android.util.ImageUtils;
import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.BeyondarObjectList;
import com.beyondar.android.world.World;
import com.ubi.sesdk.R;
import com.ubi.sesdk.core.interfaces.IARManager;
import java.io.File;
import java.io.IOException;


/**
 * @author João Pedro Pedrosa, SE on 22/02/2016.
 */
public class ARManager implements IARManager {

    private static final String TAG = "ARManager";
    public static final int LIST_TYPE = 1;
    private static final String TMP_IMAGE_PREFIX = "viewImage_";
    private World mWorld;
    private Context mContext;
    private RadarWorldPlugin mRadarPlugin;
    private BeyondarFragmentSupport mBeyondarFragment;

    public ARManager(Context mContext){
        this.mContext = mContext;
        this.mWorld = new World(this.mContext);
        this.mWorld.setDefaultImage(R.mipmap.ic_launcher);
        this.mWorld.setGeoPosition(41.90533734214473d, 2.565848038959814d);
        this.mRadarPlugin = new RadarWorldPlugin(this.mContext);
    }


    @Override
    public void setDefaultImage(int id) {
        this.mWorld.setDefaultImage(id);
    }

    @Override
    public void setWorld(World world) {
        this.mBeyondarFragment.setWorld(this.mWorld);
    }

    @Override
    public void setCurrentGeoLocation(double lat, double log) {
        if(this.mWorld!=null){
            this.mWorld.setGeoPosition(lat,log);
        }
    }

    @Override
    public void replaceImagesByStaticViews(View view) {
        if(this.mWorld!=null){
            cleanTempFolder(mContext);
            String path = getTmpPath(mContext);
            for (BeyondarObjectList beyondarList : this.mWorld.getBeyondarObjectLists()) {
                for (BeyondarObject beyondarObject : beyondarList) {
                    //TODO Way to do this dynamically
                    /*TextView textView = (TextView) view.findViewById(R.id.geoObjectName);
                    textView.setText(beyondarObject.getName());
                    ImageView imageView = (ImageView) view.findViewById(R.id.image);
                    Glide.with(context).load(beyondarObject.getImageUri()).fitCenter().into(imageView);*/
                    try {
                        String imageName = TMP_IMAGE_PREFIX + beyondarObject.getName() + ".png";
                        ImageUtils.storeView(view, path, imageName);
                        beyondarObject.setImageUri(path + imageName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void setBeyondarFragment(BeyondarFragmentSupport mBeyondarFragment) {
        this.mBeyondarFragment = mBeyondarFragment;
    }

    @Override
    public BeyondarFragmentSupport getBeyondarFragment() {
        return this.mBeyondarFragment;
    }

    @Override
    public void setListTypeColor(int listType, int color) {
        this.mRadarPlugin.setListColor(listType, color);
    }

    @Override
    public void setRadarMaxDistance(int distance) {
        this.mRadarPlugin.setMaxDistance(distance);
    }

    @Override
    public void setRadarView(RadarView view) {
        this.mRadarPlugin.setRadarView(view);
    }

    @Override
    public void setBeyondarMaxDistanceToRender(int distanceToRender) {
        this.mBeyondarFragment.setMaxDistanceToRender(distanceToRender);
    }

    @Override
    public void setBeyondarDistanceFactor(int distanceFactor) {
        this.mBeyondarFragment.setDistanceFactor(distanceFactor);
    }

    @Override
    public void setBeyondarPushAwayDistance(int pushAwayDistance) {
        this.mBeyondarFragment.setPushAwayDistance(pushAwayDistance);
    }

    @Override
    public void setBeyondarPullCloserDistance(int pullCloserDistance) {
        this.mBeyondarFragment.setPullCloserDistance(pullCloserDistance);
    }

    @Override
    public void setBeyondarMaxDistance(int maxDistance) {
        this.mBeyondarFragment.setMaxDistanceToRender(maxDistance);
    }

    @Override
    public void setBeyondarLowPassFilter(float lowPassFilter) {
        LowPassFilter.ALPHA = lowPassFilter;
    }

    @Override
    public void setRadar(RadarWorldPlugin radar) {
        this.mRadarPlugin = radar;
        this.mRadarPlugin.setListColor(1, Color.WHITE);
        this.mRadarPlugin.setListColor(0, Color.WHITE);
        this.mRadarPlugin.setListColor(LIST_TYPE, Color.RED);
        this.mRadarPlugin.setListDotRadius(LIST_TYPE, 3);
        this.mWorld.addPlugin(radar);
    }

    @Override
    public World getCurrentWorld() {
        return this.mWorld;
    }

    @Override
    public BeyondarFragmentSupport getCurrentBeyondarFragment() {
        return this.mBeyondarFragment;
    }

    @Override
    public RadarWorldPlugin getCurrentRadarPlugin() {
        return this.mRadarPlugin;
    }

    public static String getTmpPath(Context mContext) {
        if(mContext!=null){
            return mContext.getExternalFilesDir(null).getAbsoluteFile() + "/tmp/";
        }else{
            return null;
        }
    }

    public static void cleanTempFolder(Context context) {
        File tmpFolder = new File(getTmpPath(context));
        if (tmpFolder.isDirectory()) {
            String[] children = tmpFolder.list();
            for (int i = 0; i < children.length; i++) {
                if (children[i].startsWith(TMP_IMAGE_PREFIX)) {
                    new File(tmpFolder, children[i]).delete();
                }
            }
        }
    }
}
