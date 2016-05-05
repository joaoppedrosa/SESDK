package com.ubi.sesdksample.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;
import android.widget.Toast;

import com.beyondar.android.fragment.BeyondarFragmentSupport;
import com.beyondar.android.plugin.radar.RadarView;
import com.beyondar.android.plugin.radar.RadarWorldPlugin;
import com.beyondar.android.view.OnClickBeyondarObjectListener;
import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.World;
import com.ubi.sesdk.core.SE;
import com.ubi.sesdk.core.managers.ARManager;
import com.ubi.sesdksample.R;
import com.ubi.sesdksample.utils.CustomWorldHelper;

import java.util.ArrayList;

public class ARActivity extends AppCompatActivity implements OnClickBeyondarObjectListener {

    private World mWorld;
    private ARManager arManager;
    private BeyondarFragmentSupport mBeyondarFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        this.arManager = SE.getArManager();
        initComponents();
    }

    private void initComponents(){
        if(this.mBeyondarFragment==null){
            this.mBeyondarFragment = (BeyondarFragmentSupport) getSupportFragmentManager().findFragmentById( R.id.beyondarFragment);
            this.arManager.setBeyondarFragment(mBeyondarFragment);
        }

        RadarView radarView = (RadarView) findViewById(R.id.radarView);
        RadarWorldPlugin radarWorldPlugin = new RadarWorldPlugin(this);
        radarWorldPlugin.setRadarView(radarView);
        radarWorldPlugin.setMaxDistance(100);
        radarWorldPlugin.setListColor(ARManager.LIST_TYPE, Color.RED);
        radarWorldPlugin.setListDotRadius(ARManager.LIST_TYPE, 3);

        this.mWorld = CustomWorldHelper.generateObjects(this);
        this.mBeyondarFragment.setWorld(mWorld);
        this.mWorld.addPlugin(radarWorldPlugin);
        this.mBeyondarFragment.showFPS(true);
        this.mBeyondarFragment.setOnClickBeyondarObjectListener(this);
    }

    @Override
    public void onClickBeyondarObject(ArrayList<BeyondarObject> beyondarObjects) {
        Toast.makeText(ARActivity.this, "Click", Toast.LENGTH_SHORT).show();
        if (beyondarObjects.size() == 0) {
            return;
        }
        BeyondarObject beyondarObject = beyondarObjects.get(0);
    }
}
