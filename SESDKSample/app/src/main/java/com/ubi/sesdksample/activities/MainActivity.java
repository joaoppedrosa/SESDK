package com.ubi.sesdksample.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.ubi.sesdk.core.SE;
import com.ubi.sesdk.core.managers.VuforiaVideoManager;
import com.ubi.sesdk.core.model.AssetsToVideo;
import com.ubi.sesdk.core.vuforia.VideoPlayback;
import com.ubi.sesdksample.R;

public class MainActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVuforiaFiles();
    }

    private void initVuforiaFiles(){
        VuforiaVideoManager vuforiaVideoManager = SE.getVuforiaVideoManager();
        String FILE_DATA ="vuforia/Shopping_Experience.xml";
        String VIDEO_DOLCE_GUSTO = "vuforia/dolce_gusto.mp4";
        String VIDEO_PROALIMENTAR = "vuforia/proalimentar.mp4";
        String IMAGE_DOLCE_GUSTO = "vuforia/dolce_gusto.jpg";
        String IMAGE_PROALIMENTAR = "vuforia/proalimentar.png";
        vuforiaVideoManager.setDataFile(FILE_DATA);
        vuforiaVideoManager.addToList(new AssetsToVideo(0, VIDEO_DOLCE_GUSTO, IMAGE_DOLCE_GUSTO, "dolce_gusto"));
        vuforiaVideoManager.addToList(new AssetsToVideo(1, VIDEO_PROALIMENTAR, IMAGE_PROALIMENTAR, "proalimentar"));
    }

    public void onBeaconsClick(View view) {
        if(SE.getBeaconsManager()!=null){
            Intent intent = new Intent(this, BeaconsActivity.class);
            startActivity(intent);
        }else{
            Toast.makeText(MainActivity.this, "Init BeaconsManager in Application", Toast.LENGTH_SHORT).show();
        }

    }

    public void onGeofencingClick(View view) {
        if(SE.getGeofencingManager()!=null){
            Intent intent = new Intent(this, GeofencingActivity.class);
            startActivity(intent);
        }else{
            Toast.makeText(MainActivity.this, "Init GeofencingManager in Application", Toast.LENGTH_SHORT).show();
        }
    }

    public void onPaymentClick(View view) {
        if(SE.getPaymentManager()!=null){
            Intent intent = new Intent(this, PaymentActivity.class);
            startActivity(intent);
        }else{
            Toast.makeText(MainActivity.this, "Init PaymentManager in Application", Toast.LENGTH_SHORT).show();
        }
    }

    public void onVuforiaClick(View view) {
        if(SE.getVuforiaVideoManager()!=null){
            Intent intent = new Intent(this, VideoPlayback.class);
            startActivity(intent);
        }else{
            Toast.makeText(MainActivity.this, "Init VuforiaVideoManager in Application", Toast.LENGTH_SHORT).show();
        }
    }

    public void onARClick(View view) {
        if(SE.getArManager()!=null){
            Intent intent = new Intent(this, ARActivity.class);
            startActivity(intent);
        }else{
            Toast.makeText(MainActivity.this, "Init ARManager in Application", Toast.LENGTH_SHORT).show();
        }
    }

    public void onMapClick(View view) {
        if(SE.getIndoorLocationManager()!=null){
            Intent intent = new Intent(this, MapActivity.class);
            startActivity(intent);
        }else{
            Toast.makeText(MainActivity.this, "Init IndoorLocationManager in Application", Toast.LENGTH_SHORT).show();
        }
    }
}


