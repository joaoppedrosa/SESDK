package com.ubi.sesdk.core.managers;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.WindowManager;

import com.ubi.sesdk.R;
import com.ubi.sesdk.core.exceptions.SampleApplicationException;
import com.ubi.sesdk.core.listeners.SampleApplicationControl;
import com.ubi.sesdk.core.model.AssetsToVideo;
import com.ubi.sesdk.core.providers.VuforiaVideoProvider;
import com.vuforia.CameraCalibration;
import com.vuforia.CameraDevice;
import com.vuforia.Matrix44F;
import com.vuforia.Renderer;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Vec2I;
import com.vuforia.VideoBackgroundConfig;
import com.vuforia.VideoMode;
import com.vuforia.Vuforia;

/**
 * @author João Pedro Pedrosa, SE on 27/04/2016.
 */
public class VuforiaVideoManager implements Vuforia.UpdateCallbackInterface {

    private static final String TAG = "VuforiaVideoManager";
    private String API_KEY;
    private final Object mShutdownLock = new Object();
    private Activity mActivity;
    private VuforiaVideoProvider mProvider;
    private SampleApplicationControl mSessionControl;
    private InitVuforiaTask mInitVuforiaTask;
    private LoadTrackerTask mLoadTrackerTask;
    private Matrix44F mProjectionMatrix;
    private boolean mStarted = false;
    private boolean mCameraRunning = false;
    private boolean mIsPortrait = false;
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;
    private int mVuforiaFlags = 0;
    private int mCamera = CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT;
    private int[] mViewport;

    public VuforiaVideoManager(VuforiaVideoProvider mProvider, String API_KEY){
        this.API_KEY = API_KEY;
        this.mProvider = mProvider;
    }

    public void addToList(AssetsToVideo assetsToVideo){
        this.mProvider.addToList(assetsToVideo);
    }

    public void addToList(int id, String videoPath, String imagePath, String key){
        this.mProvider.addToList(id, videoPath, imagePath, key);
    }

    public String getVideoFile(int key){
        return this.mProvider.getVideoFile(key);
    }

    public String getImageFile(int key){
        return this.mProvider.getImageFile(key);
    }

    public int getSize(){
        return this.mProvider.getSize();
    }

    public int getKeyVideo(String key){
        return this.mProvider.getKeyVideo(key);
    }

    public void setmSessionControl(SampleApplicationControl mSessionControl) {
        this.mSessionControl = mSessionControl;
    }

    public void setDataFile(String dataFile){
        this.mProvider.setDataFile(dataFile);
    }

    public String getDataFile(){
        return  this.mProvider.getDataFile();
    }

    public void initAR(Activity activity, int screenOrientation) {
        SampleApplicationException vuforiaException = null;
        this.mActivity = activity;
        if ((screenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR) && (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO)) screenOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
        OrientationEventListener orientationEventListener = new OrientationEventListener(mActivity) {
            @Override
            public void onOrientationChanged(int i) {
                int activityRotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
                if(mLastRotation != activityRotation){
                    setProjectionMatrix();
                    mLastRotation = activityRotation;
                }
            }
            int mLastRotation = -1;
        };

        if(orientationEventListener.canDetectOrientation()){
            orientationEventListener.enable();
        }

        this.mActivity.setRequestedOrientation(screenOrientation);
        updateActivityOrientation();
        storeScreenDimensions();
        this.mActivity.getWindow().setFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.mVuforiaFlags = Vuforia.GL_20;
        if (this.mInitVuforiaTask != null){
            String logMessage = "Cannot initialize SDK twice";
            vuforiaException = new SampleApplicationException(SampleApplicationException.VUFORIA_ALREADY_INITIALIZATED,logMessage);
            Log.e(TAG, logMessage);
        }

        if (vuforiaException == null){
            try{
                mInitVuforiaTask = new InitVuforiaTask();
                mInitVuforiaTask.execute();
            } catch (Exception e){
                String logMessage = "Initializing Vuforia SDK failed";
                vuforiaException = new SampleApplicationException(
                        SampleApplicationException.INITIALIZATION_FAILURE,
                        logMessage);
                Log.e(TAG, logMessage);
            }
        }
        if (vuforiaException != null){
            mSessionControl.onInitARDone(vuforiaException);
        }
    }

    // Starts Vuforia, initialize and starts the camera and start the trackers
    public void startAR(int camera) throws SampleApplicationException{
        String error;
        if(this.mCameraRunning){
            error = "Camera already running, unable to open again";
            Log.e(TAG, error);
            throw new SampleApplicationException(SampleApplicationException.CAMERA_INITIALIZATION_FAILURE, error);
        }

        this.mCamera = camera;
        if (!CameraDevice.getInstance().init(camera)){
            error = "Unable to open camera device: " + camera;
            Log.e(TAG, error);
            throw new SampleApplicationException(SampleApplicationException.CAMERA_INITIALIZATION_FAILURE, error);
        }

        if (!CameraDevice.getInstance().selectVideoMode(CameraDevice.MODE.MODE_DEFAULT)){
            error = "Unable to set video mode";
            Log.e(TAG, error);
            throw new SampleApplicationException(SampleApplicationException.CAMERA_INITIALIZATION_FAILURE, error);
        }

        // Configure the rendering of the video background
        configureVideoBackground();

        if (!CameraDevice.getInstance().start()){
            error = "Unable to start camera device: " + camera;
            Log.e(TAG, error);
            throw new SampleApplicationException(SampleApplicationException.CAMERA_INITIALIZATION_FAILURE, error);
        }

        setProjectionMatrix();
        this.mSessionControl.doStartTrackers();
        this.mCameraRunning = true;
        if(!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO)){
            if(!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO))
                CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);
        }
    }


    // Stops any ongoing initialization, stops Vuforia
    public void stopAR() throws SampleApplicationException{
        if (mInitVuforiaTask != null && mInitVuforiaTask.getStatus() != InitVuforiaTask.Status.FINISHED){
            mInitVuforiaTask.cancel(true);
            mInitVuforiaTask = null;
        }
        if (mLoadTrackerTask != null && mLoadTrackerTask.getStatus() != LoadTrackerTask.Status.FINISHED){
            mLoadTrackerTask.cancel(true);
            mLoadTrackerTask = null;
        }
        mInitVuforiaTask = null;
        mLoadTrackerTask = null;
        mStarted = false;
        stopCamera();

        // Ensure that all asynchronous operations to initialize Vuforia
        // and loading the tracker datasets do not overlap:
        synchronized (mShutdownLock) {
            boolean unloadTrackersResult;
            boolean deinitTrackersResult;
            unloadTrackersResult = mSessionControl.doUnloadTrackersData();
            deinitTrackersResult = mSessionControl.doDeinitTrackers();
            Vuforia.deinit();
            if (!unloadTrackersResult){
                throw new SampleApplicationException(
                        SampleApplicationException.UNLOADING_TRACKERS_FAILURE,
                        "Failed to unload trackers\' data");
            }
            if (!deinitTrackersResult){
                throw new SampleApplicationException(
                        SampleApplicationException.TRACKERS_DEINITIALIZATION_FAILURE,
                        "Failed to deinitialize trackers");
            }
        }
    }

    // Resumes Vuforia, restarts the trackers and the camera
    public void resumeAR() throws SampleApplicationException {
        Vuforia.onResume();
        if (mStarted){
            startAR(mCamera);
        }
    }


    // Pauses Vuforia and stops the camera
    public void pauseAR() throws SampleApplicationException{
        if (mStarted){
            stopCamera();
        }
        Vuforia.onPause();
    }


    // Gets the projection matrix to be used for rendering
    public Matrix44F getProjectionMatrix()
    {
        return mProjectionMatrix;
    }

    // Gets the viewport to be used for rendering
    public int[] getViewport()
    {
        return mViewport;
    }

    // Callback called every cycle
    @Override
    public void Vuforia_onUpdate(State s)
    {
        mSessionControl.onVuforiaUpdate(s);
    }


    // Manages the configuration changes
    public void onConfigurationChanged() {
        updateActivityOrientation();
        storeScreenDimensions();
        if (isARRunning()){
            configureVideoBackground();
            setProjectionMatrix();
        }
    }

    // Methods to be called to handle lifecycle
    public void onResume()
    {
        Vuforia.onResume();
    }


    public void onPause()
    {
        Vuforia.onPause();
    }


    public void onSurfaceChanged(int width, int height)
    {
        Vuforia.onSurfaceChanged(width, height);
    }


    public void onSurfaceCreated()
    {
        Vuforia.onSurfaceCreated();
    }

    // An async task to initialize Vuforia asynchronously.
    private class InitVuforiaTask extends AsyncTask<Void, Integer, Boolean> {
        private int mProgressValue = -1;

        protected Boolean doInBackground(Void... params){
            synchronized (mShutdownLock){
                Vuforia.setInitParameters(mActivity, mVuforiaFlags, API_KEY);
                do{
                    this.mProgressValue = Vuforia.init();
                    publishProgress(mProgressValue);
                } while (!isCancelled() && mProgressValue >= 0 && mProgressValue < 100);
                return (mProgressValue > 0);
            }
        }

        protected void onProgressUpdate(Integer... values)  {}

        protected void onPostExecute(Boolean result){
            SampleApplicationException vuforiaException;
            if (result){
                Log.d(TAG, "InitVuforiaTask.onPostExecute: Vuforia " + "initialization successful");
                boolean initTrackersResult;
                initTrackersResult = mSessionControl.doInitTrackers();
                if (initTrackersResult) {
                    try {
                        mLoadTrackerTask = new LoadTrackerTask();
                        mLoadTrackerTask.execute();
                    } catch (Exception e) {
                        String logMessage = "Loading tracking data set failed";
                        vuforiaException = new SampleApplicationException(
                                SampleApplicationException.LOADING_TRACKERS_FAILURE,
                                logMessage);
                        Log.e(TAG, logMessage);
                        mSessionControl.onInitARDone(vuforiaException);
                    }
                } else {
                    vuforiaException = new SampleApplicationException(
                            SampleApplicationException.TRACKERS_INITIALIZATION_FAILURE,
                            "Failed to initialize trackers");
                    mSessionControl.onInitARDone(vuforiaException);
                }
            } else {
                String logMessage;
                logMessage = getInitializationErrorString(mProgressValue);
                Log.e(TAG, "InitVuforiaTask.onPostExecute: " + logMessage + " Exiting.");
                vuforiaException = new SampleApplicationException(SampleApplicationException.INITIALIZATION_FAILURE,logMessage);
                mSessionControl.onInitARDone(vuforiaException);
            }
        }
    }

    private class LoadTrackerTask extends AsyncTask<Void, Integer, Boolean>{
        protected Boolean doInBackground(Void... params){
            synchronized (mShutdownLock){
                return mSessionControl.doLoadTrackersData();
            }
        }

        protected void onPostExecute(Boolean result) {
            SampleApplicationException vuforiaException = null;
            Log.d(TAG, "LoadTrackerTask.onPostExecute: execution " + (result ? "successful" : "failed"));
            if (!result){
                String logMessage = "Failed to load tracker data.";
                Log.e(TAG, logMessage);
                vuforiaException = new SampleApplicationException(
                        SampleApplicationException.LOADING_TRACKERS_FAILURE,
                        logMessage);
            } else{
                System.gc();
                Vuforia.registerCallback(VuforiaVideoManager.this);
                mStarted = true;
            }
            mSessionControl.onInitARDone(vuforiaException);
        }
    }


    // Returns the error message for each error code
    private String getInitializationErrorString(int code) {
        if (code == Vuforia.INIT_DEVICE_NOT_SUPPORTED)
            return mActivity.getString(R.string.INIT_ERROR_DEVICE_NOT_SUPPORTED);
        if (code == Vuforia.INIT_NO_CAMERA_ACCESS)
            return mActivity.getString(R.string.INIT_ERROR_NO_CAMERA_ACCESS);
        if (code == Vuforia.INIT_LICENSE_ERROR_MISSING_KEY)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_MISSING_KEY);
        if (code == Vuforia.INIT_LICENSE_ERROR_INVALID_KEY)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_INVALID_KEY);
        if (code == Vuforia.INIT_LICENSE_ERROR_NO_NETWORK_TRANSIENT)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_NO_NETWORK_TRANSIENT);
        if (code == Vuforia.INIT_LICENSE_ERROR_NO_NETWORK_PERMANENT)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_NO_NETWORK_PERMANENT);
        if (code == Vuforia.INIT_LICENSE_ERROR_CANCELED_KEY)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_CANCELED_KEY);
        if (code == Vuforia.INIT_LICENSE_ERROR_PRODUCT_TYPE_MISMATCH)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_PRODUCT_TYPE_MISMATCH);
        else{
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_UNKNOWN_ERROR);
        }
    }


    // Stores screen dimensions
    private void storeScreenDimensions() {
        DisplayMetrics metrics = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
    }


    // Stores the orientation depending on the current resources configuration
    private void updateActivityOrientation() {
        Configuration config = mActivity.getResources().getConfiguration();
        switch (config.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                mIsPortrait = true;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                mIsPortrait = false;
                break;
            case Configuration.ORIENTATION_UNDEFINED:
            default:
                break;
        }

        Log.i(TAG, "Activity is in "+ (mIsPortrait ? "PORTRAIT" : "LANDSCAPE"));
    }

    public void setProjectionMatrix() {
        CameraCalibration camCal = CameraDevice.getInstance().getCameraCalibration();
        mProjectionMatrix = Tool.getProjectionGL(camCal, 10.0f, 5000.0f);
    }

    public void stopCamera() {
        if(mCameraRunning){
            mSessionControl.doStopTrackers();
            CameraDevice.getInstance().stop();
            CameraDevice.getInstance().deinit();
            mCameraRunning = false;
        }
    }

    // Configures the video mode and sets offsets for the camera's image
    private void configureVideoBackground(){
        CameraDevice cameraDevice = CameraDevice.getInstance();
        VideoMode vm = cameraDevice.getVideoMode(CameraDevice.MODE.MODE_DEFAULT);
        VideoBackgroundConfig config = new VideoBackgroundConfig();
        config.setEnabled(true);
        config.setPosition(new Vec2I(0, 0));

        int xSize = 0, ySize = 0;
        if (mIsPortrait){
            xSize = (int) (vm.getHeight() * (mScreenHeight / (float) vm
                    .getWidth()));
            ySize = mScreenHeight;

            if (xSize < mScreenWidth){
                xSize = mScreenWidth;
                ySize = (int) (mScreenWidth * (vm.getWidth() / (float) vm
                        .getHeight()));
            }
        } else {
            xSize = mScreenWidth;
            ySize = (int) (vm.getHeight() * (mScreenWidth / (float) vm
                    .getWidth()));

            if (ySize < mScreenHeight){
                xSize = (int) (mScreenHeight * (vm.getWidth() / (float) vm
                        .getHeight()));
                ySize = mScreenHeight;
            }
        }

        config.setSize(new Vec2I(xSize, ySize));
        // Calculate viewport centred in the screen
        mViewport = new int[4];
        mViewport[0] = ((mScreenWidth - xSize) / 2) + config.getPosition().getData()[0];
        mViewport[1] = ((mScreenHeight - ySize) / 2) + config.getPosition().getData()[1];
        mViewport[2] = xSize;
        mViewport[3] = ySize;
        Log.i(TAG, "Video (" + vm.getWidth() + " , " + vm.getHeight() + "), Screen (" + mScreenWidth + " , " + mScreenHeight + "), mSize (" + xSize + " , " + ySize + ")");
        Renderer.getInstance().setVideoBackgroundConfig(config);
    }

    private boolean isARRunning()
    {
        return mStarted;
    }
}
