package com.ubi.sesdk.core.interfaces;

import android.view.View;

import com.beyondar.android.fragment.BeyondarFragmentSupport;
import com.beyondar.android.plugin.radar.RadarView;
import com.beyondar.android.plugin.radar.RadarWorldPlugin;
import com.beyondar.android.world.World;

/**
 * @author João Pedro Pedrosa, SE on 22/02/2016.
 */
public interface IARManager {

    void setDefaultImage(int id);

    void setWorld(World world);

    void setCurrentGeoLocation(double lat, double log);

    void replaceImagesByStaticViews(View view);

    void setBeyondarFragment(BeyondarFragmentSupport mBeyondarFragment);

    BeyondarFragmentSupport getBeyondarFragment();

    void setListTypeColor(int listType, int color);

    void setRadarMaxDistance(int distance);

    void setRadarView(RadarView view);

    void setBeyondarMaxDistanceToRender(int distanceToRender);

    void setBeyondarDistanceFactor(int distanceFactor);

    void setBeyondarPushAwayDistance(int pushAwayDistance);

    void setBeyondarPullCloserDistance(int pullCloserDistance);

    void setBeyondarMaxDistance(int maxDistance);

    void setBeyondarLowPassFilter(float lowPassFilter);

    void setRadar(RadarWorldPlugin radar);

    World getCurrentWorld();

    BeyondarFragmentSupport getCurrentBeyondarFragment();

    RadarWorldPlugin getCurrentRadarPlugin();
}
