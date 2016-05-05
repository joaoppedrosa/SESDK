package com.ubi.sesdksample.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ubi.sesdk.core.SE;
import com.ubi.sesdk.core.managers.GeofencingManager;
import com.ubi.sesdksample.R;
import com.ubi.sesdksample.service.GeoService;

public class GeofencingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofencing);
        initGeofencing();
    }

    /**GEOFENCING NEED TO HAVE LOCATION TURN ON TO WORK CORRECTLY**/
    private void initGeofencing(){
        GeofencingManager geofencingManager = SE.getGeofencingManager();
        geofencingManager.setGeofencingIntentService(new GeoService());
        geofencingManager.addGeofencing(geofencingManager.generateGeofencing("Welcome to Ubiwhere", 40.638197, -8.635206, 120000, 1));
    }
}
