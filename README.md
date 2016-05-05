
SESDK
-------------------

SDK that make easy the integration of multiple mobile technologies such as augmented reality, indoor location, beacons , geofencing and payements

How do I use SESDK?
-------------------

```gradle
buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath 'com.android.tools.build:gradle:2.1.0'
    }
}

allprojects {
    repositories {
        jcenter()
        maven {
            url "http://indooratlas-ltd.bintray.com/mvn-public"
        }
    }
}
```


```java
public class SampleApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        SE.init(this);
    }
}
```

BeaconsManager
---------
Easy integration with the Estimote SDK, replace the key for your own (https://cloud.estimote.com/)

```java
public static final String ESTIMOTE_APP_ID = "";
public static final String ESTIMOTE_APP_TOKEN = "";

public class SampleApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        SE.init(this);
        SE.initBeaconsManager(ESTIMOTE_APP_ID, ESTIMOTE_APP_TOKEN);
    }
}
```

After this just enable the ranging or monitoring in your Activity or Fragment
```java
@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacons);
        initBeaconsManager();
    }

    private void initBeaconsManager(){
        BeaconsManager beaconsManager = SE.getBeaconsManager();
        beaconsManager.setBeaconsListener(this);
        BeaconID beaconID = new BeaconID("B9407F30-F5F8-466E-AFF9-25556B57FE6D",15621,39312);
        beaconsManager.addRegionToMonitor(beaconID);
        beaconsManager.startMonitoring();
        beaconsManager.addRegionToRanging(beaconID);
        beaconsManager.startRanging();
    }

    @Override
    public void onDiscoveryBeacon(List<Beacon> beacons) {
        Log.d(TAG, "onDiscoveryBeacon: " + beacons.toString());
    }

    @Override
    public void onBeaconTemperature(float temperature) {
        Log.d(TAG, "onBeaconTemperature: " + temperature);
    }

    @Override
    public void onBeaconMotionListener(MotionState motionState) {
        Log.d(TAG, "onBeaconMotionListener: " + motionState.toString());
    }

    @Override
    public void onEnterRegion(Region region) {
        Log.d(TAG, "onEnterRegion: " + region.toString());
    }

    @Override
    public void onExitedRegion(Region region) {
        Log.d(TAG, "onExitedRegion: " + region.toString());
    }
```

You need to

GeofencingManager
---------

Init the GeofencingManager in Application

```java
public class SampleApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        SE.init(this);
        SE.initGeofencingManager();
    }
}
```

Create a service that extends GeofencingIntentService and call onFindGeofences()

```java
public class GeoService extends GeofencingIntentService {

    @Override
    public void onFindGeofences(List<Geofence> geofences) {
    }
}
```

Add the service and the Google Play Services metadata into AndroidManifest.xml

```xml
	<meta-data
		android:name="com.google.android.gms.version"
        android:value="@integer/google_play_services_version" />

	<service android:name=".service.GeoService" />
```

And then just regist geofences

```java

   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GeofencingManager geofencingManager = SE.getGeofencingManager();
        geofencingManager.setGeofencingIntentService(new GeoService());
        geofencingManager.addGeofencing(geofencingManager.generateGeofencing("Welcome to Ubiwhere", 40.638197, -8.635206, 120000, 1));

    }

```

PaymentManager
---------
Easy integration with the Stripe SDK, replace the key for your own (https://dashboard.stripe.com)

```java
public class SampleApplication extends Application{
    public static final String STRIPE_KEY = "";

    @Override
    public void onCreate() {
        super.onCreate();
        SE.init(this);
        SE.initPaymentManager(STRIPE_KEY);
    }
}
```

And then just create a credit card and a token

```java
    private void initPaymentManager(){
        this.paymentManager = SE.getPaymentManager();
        this.paymentManager.setiStripe(this);
        this.paymentManager.createCreditCard("NOME","4242424242424242",12,20,"123");
    }

    @Override
    public void validCreditCard(Card card) {
        paymentManager.createTokenToCharge(card,"description",12.00,"EUR");
    }

    @Override
    public void invalideCreditCard() {
        Log.e(TAG, "invalideCreditCard");
    }

    @Override
    public void stripeToken(StripeToken stripeToken) {
        Log.d(TAG, "stripeToken: " + stripeToken);
    }

    @Override
    public void stripeTokenError(String message) {
        Log.e(TAG, "stripeTokenError: " + message);
    }
```

VuforiaVideoManager
---------
Easy integration with the Vuforia SDK, replace the key for your own (https://developer.vuforia.com/)

```java
public class SampleApplication extends Application{

    public static final String VUFORIA_KEY = "";

    @Override
    public void onCreate() {
        super.onCreate();
        SE.init(this);
        SE.initVuforiaVideoManager(VUFORIA_KEY);
    }
}
```
And then just copy the data obtain in vuforia target manager and the images and videos to assests folder and add them to VuforiaVideoManager

```java
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
```

MapManager
---------
Easy integration with the IndoorAtlas SDK, replace the floor plan id for your own (https://www.indooratlas.com/)

```java
public class SampleApplication extends Application{
    public static final String FLOOR_PLAN_ID = "";

    @Override
    public void onCreate() {
        super.onCreate();
        SE.init(this);
        SE.initIndoorAtlas(FLOOR_PLAN_ID);
    }
}
```

Add API KEYS to AndroidManifest.xml

```xml
	<meta-data
            android:name="com.indooratlas.android.sdk.API_KEY"
            android:value="@string/indooratlas_key" />
        <meta-data
            android:name="com.indooratlas.android.sdk.API_SECRET"
            android:value="@string/indooratlas_secret" />
```

And then just get your floor plan in a Activity or fragment

```java
@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        this.blueDotView = (BlueDotView) findViewById(R.id.blueDotView);
        this.indoorLocationManager = SE.getIndoorLocationManager();
        this.indoorLocationManager.loadFloorPlan(this);
    }
```
