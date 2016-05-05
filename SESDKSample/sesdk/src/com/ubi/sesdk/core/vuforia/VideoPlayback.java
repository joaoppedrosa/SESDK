/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.


Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.ubi.sesdk.core.vuforia;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

import com.ubi.sesdk.R;
import com.ubi.sesdk.core.SE;
import com.ubi.sesdk.core.exceptions.SampleApplicationException;
import com.ubi.sesdk.core.listeners.SampleApplicationControl;
import com.ubi.sesdk.core.managers.VuforiaVideoManager;
import com.ubi.sesdk.core.utils.LoadingDialogHandler;
import com.ubi.sesdk.core.utils.SampleApplicationGLView;
import com.ubi.sesdk.core.utils.Texture;
import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.HINT;
import com.vuforia.ObjectTracker;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.State;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;

import java.util.Vector;

// The AR activity for the VideoPlayback sample.
public class VideoPlayback extends Activity implements SampleApplicationControl {

    private static final String TAG = "VideoPlayback";

    private VuforiaVideoManager vuforiaVideoManager;

    // Helpers to detect events such as double tapping:
    private GestureDetector mGestureDetector = null;
    private SimpleOnGestureListener mSimpleListener = null;

    // Movie for the Targets:
    public static int NUM_TARGETS;
    private VideoPlayerHelper mVideoPlayerHelper[] = null;
    private int mSeekPosition[] = null;
    private boolean mWasPlaying[] = null;
    private String mMovieName[] = null;

    // A boolean to indicate whether we come from full screen:
    private boolean mReturningFromFullScreen = false;

    // Our OpenGL view:
    private SampleApplicationGLView mGlView;

    // Our renderer:
    private VideoPlaybackRenderer mRenderer;

    // The textures we will use for rendering:
    private Vector<Texture> mTextures;

    private DataSet dataSet = null;

    private RelativeLayout mUILayout;

    private boolean mPlayFullscreenVideo = false;

    private LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);

    // Alert Dialog used to display SDK errors
    private AlertDialog mErrorDialog;

    private boolean mIsDroidDevice = false;
    private boolean mIsInitialized = false;


    protected void onCreate(Bundle savedInstanceState){
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        this.vuforiaVideoManager = SE.getVuforiaVideoManager();
        NUM_TARGETS = this.vuforiaVideoManager.getSize();
        initObjAndTextures();
    }

    private void initObjAndTextures(){
        this.vuforiaVideoManager.setmSessionControl(this);
        startLoadingAnimation();
        this.vuforiaVideoManager.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Load any sample specific textures:
        this.mTextures = new Vector<>();
        loadTextures();

        // Create the gesture detector that will handle the single and double taps
        this.mSimpleListener = new SimpleOnGestureListener();
        this.mGestureDetector = new GestureDetector(getApplicationContext(), mSimpleListener);
        this.mVideoPlayerHelper = new VideoPlayerHelper[NUM_TARGETS];
        this.mSeekPosition = new int[NUM_TARGETS];
        this.mWasPlaying = new boolean[NUM_TARGETS];
        this.mMovieName = new String[NUM_TARGETS];

        // Create the video player helper that handles the playback of the movie for the targets:
        for (int i = 0; i < NUM_TARGETS; i++){
            this.mVideoPlayerHelper[i] = new VideoPlayerHelper();
            this.mVideoPlayerHelper[i].init();
            this.mVideoPlayerHelper[i].setActivity(this);
        }

        for(int i = 0; i < NUM_TARGETS;i++){
            this.mMovieName[i] = vuforiaVideoManager.getVideoFile(i);
        }

        // Set the double tap listener:
        mGestureDetector.setOnDoubleTapListener(new OnDoubleTapListener(){
            public boolean onDoubleTap(MotionEvent e){
                return false;
            }

            public boolean onDoubleTapEvent(MotionEvent e){
                return false;
            }

            public boolean onSingleTapConfirmed(MotionEvent e){
                return onSingleTapVideo(e);
            }
        });
    }

    private void startLoadingAnimation(){
        this.mUILayout = (RelativeLayout) View.inflate(this, R.layout.camera_overlay,null);
        this.mUILayout.setVisibility(View.VISIBLE);
        this.mUILayout.setBackgroundColor(Color.BLACK);
        // Gets a reference to the loading dialog
        this.loadingDialogHandler.mLoadingDialogContainer = mUILayout.findViewById(R.id.loading_indicator);
        // Shows the loading indicator at start
        this.loadingDialogHandler.sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);
        // Adds the inflated layout to the view
        addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
    }


    private boolean onSingleTapVideo(MotionEvent e){
        boolean isSingleTapHandled = false;
        // Do not react if the StartupScreen is being displayed
        for (int i = 0; i < NUM_TARGETS; i++){
            // Verify that the tap happened inside the target
            if (mRenderer!= null && mRenderer.isTapOnScreenInsideTarget(i, e.getX(), e.getY())){
                // Check if it is playable on texture
                if (mVideoPlayerHelper[i].isPlayableOnTexture()){
                    // We can play only if the movie was paused, ready or stopped
                    if ((mVideoPlayerHelper[i].getStatus() == VideoPlayerHelper.MEDIA_STATE.PAUSED)
                            || (mVideoPlayerHelper[i].getStatus() == VideoPlayerHelper.MEDIA_STATE.READY)
                            || (mVideoPlayerHelper[i].getStatus() == VideoPlayerHelper.MEDIA_STATE.STOPPED)
                            || (mVideoPlayerHelper[i].getStatus() == VideoPlayerHelper.MEDIA_STATE.REACHED_END)){
                        // Pause all other media
                        pauseAll(i);
                        // If it has reached the end then rewind
                        if ((mVideoPlayerHelper[i].getStatus() == VideoPlayerHelper.MEDIA_STATE.REACHED_END))
                            mSeekPosition[i] = 0;

                        mVideoPlayerHelper[i].play(mPlayFullscreenVideo,
                                mSeekPosition[i]);
                        mSeekPosition[i] = VideoPlayerHelper.CURRENT_POSITION;
                    } else if (mVideoPlayerHelper[i].getStatus() == VideoPlayerHelper.MEDIA_STATE.PLAYING){
                        // If it is playing then we pause it
                        mVideoPlayerHelper[i].pause();
                    }
                } else if (mVideoPlayerHelper[i].isPlayableFullscreen()){
                    // If it isn't playable on texture
                    // Either because it wasn't requested or because it
                    // isn't supported then request playback fullscreen.
                    mVideoPlayerHelper[i].play(true,VideoPlayerHelper.CURRENT_POSITION);
                }

                isSingleTapHandled = true;

                // Even though multiple videos can be loaded only one
                // can be playing at any point in time. This break
                // prevents that, say, overlapping videos trigger
                // simultaneously playback.
                break;
            }
        }

        return isSingleTapHandled;
    }

    // We want to load specific textures from the APK, which we will later
    // use for rendering.
    private void loadTextures() {
        for(int i=0;i<NUM_TARGETS;i++){
            mTextures.add(Texture.loadTextureFromApk(vuforiaVideoManager.getImageFile(i), getAssets()));
        }
        mTextures.add(Texture.loadTextureFromApk("vuforia/VideoPlayback/play.png", getAssets()));
        mTextures.add(Texture.loadTextureFromApk("vuforia/VideoPlayback/busy.png",getAssets()));
        mTextures.add(Texture.loadTextureFromApk("vuforia/VideoPlayback/error.png",getAssets()));
    }


    // Called when the activity will start interacting with the user.
    protected void onResume(){
        Log.d(TAG, "onResume");
        super.onResume();

        // This is needed for some Droid devices to force portrait
        if (mIsDroidDevice){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        try{
            vuforiaVideoManager.resumeAR();
        } catch (SampleApplicationException e){
            Log.e(TAG, e.getString());
        }

        // Resume the GL view:
        if (mGlView != null){
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }

        // Reload all the movies
        if (mRenderer != null){
            for (int i = 0; i < NUM_TARGETS; i++){
                if (!mReturningFromFullScreen){
                    mRenderer.requestLoad(i, mMovieName[i], mSeekPosition[i], false);
                } else{
                    mRenderer.requestLoad(i, mMovieName[i], mSeekPosition[i], mWasPlaying[i]);
                }
            }
        }
        mReturningFromFullScreen = false;
    }


    // Called when returning from the full screen player
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == 1){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            if (resultCode == RESULT_OK){
                String movieBeingPlayed = data.getStringExtra("movieName");
                mReturningFromFullScreen = true;
                for (int i = 0; i < NUM_TARGETS; i++){
                    if (movieBeingPlayed.compareTo(mMovieName[i]) == 0){
                        mSeekPosition[i] = data.getIntExtra("currentSeekPosition", 0);
                        mWasPlaying[i] = false;
                    }
                }
            }
        }
    }


    public void onConfigurationChanged(Configuration config){
        Log.d(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);
        vuforiaVideoManager.onConfigurationChanged();
    }


    // Called when the system is about to start resuming a previous activity.
    protected void onPause(){
        Log.d(TAG, "onPause");
        super.onPause();

        if (mGlView != null) {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }

        // Store the playback state of the movies and unload them:
        for (int i = 0; i < NUM_TARGETS; i++){
            // If the activity is paused we need to store the position in which
            // this was currently playing:
            if (mVideoPlayerHelper[i].isPlayableOnTexture()){
                mSeekPosition[i] = mVideoPlayerHelper[i].getCurrentPosition();
                mWasPlaying[i] = mVideoPlayerHelper[i].getStatus() == VideoPlayerHelper.MEDIA_STATE.PLAYING ? true : false;
            }

            // We also need to release the resources used by the helper, though
            // we don't need to destroy it:
            if (mVideoPlayerHelper[i] != null)
                mVideoPlayerHelper[i].unload();
        }

        mReturningFromFullScreen = false;

        try{
            vuforiaVideoManager.pauseAR();
        } catch (SampleApplicationException e){
            Log.e(TAG, e.getString());
        }
    }


    // The final call you receive before your activity is destroyed.
    protected void onDestroy(){
        Log.d(TAG, "onDestroy");
        super.onDestroy();

        for (int i = 0; i < NUM_TARGETS; i++) {
            // If the activity is destroyed we need to release all resources:
            if (mVideoPlayerHelper[i] != null)
                mVideoPlayerHelper[i].deinit();
            mVideoPlayerHelper[i] = null;
        }

        try{
            vuforiaVideoManager.stopAR();
        } catch (SampleApplicationException e)
        {
            Log.e(TAG, e.getString());
        }

        // Unload texture:
        mTextures.clear();
        mTextures = null;

        System.gc();
    }


    // Pause all movies except one
    // if the value of 'except' is -1 then
    // do a blanket pause
    private void pauseAll(int except)
    {
        // And pause all the playing videos:
        for (int i = 0; i < NUM_TARGETS; i++){
            // We can make one exception to the pause all calls:
            if (i != except){
                // Check if the video is playable on texture
                if (mVideoPlayerHelper[i].isPlayableOnTexture()){
                    // If it is playing then we pause it
                    mVideoPlayerHelper[i].pause();
                }
            }
        }
    }


    // Do not exit immediately and instead show the startup screen
    public void onBackPressed(){
        pauseAll(-1);
        super.onBackPressed();
    }


    // Initializes AR application components.
    private void initApplicationAR(){
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        mGlView = new SampleApplicationGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);

        mRenderer = new VideoPlaybackRenderer(this, vuforiaVideoManager);
        mRenderer.setTextures(mTextures);

        // The renderer comes has the OpenGL context, thus, loading to texture
        // must happen when the surface has been created. This means that we
        // can't load the movie from this thread (GUI) but instead we must
        // tell the GL thread to load it once the surface has been created.
        for (int i = 0; i < NUM_TARGETS; i++) {
            mRenderer.setVideoPlayerHelper(i, mVideoPlayerHelper[i]);
            mRenderer.requestLoad(i, mMovieName[i], 0, false);
        }

        mGlView.setRenderer(mRenderer);

        for (int i = 0; i < NUM_TARGETS; i++){
            float[] temp = { 0f, 0f, 0f };
            mRenderer.targetPositiveDimensions[i].setData(temp);
            mRenderer.videoPlaybackTextureID[i] = -1;
        }
    }


    // We do not handle the touch event here, we just forward it to the
    // gesture detector
    public boolean onTouchEvent(MotionEvent event){
        boolean result = false;
        if (!result)
            mGestureDetector.onTouchEvent(event);

        return result;
    }


    @Override
    public boolean doInitTrackers(){
        // Indicate if the trackers were initialized correctly
        boolean result = true;
        // Initialize the image tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        Tracker tracker = trackerManager.initTracker(ObjectTracker
                .getClassType());
        if (tracker == null)
        {
            Log.d(TAG, "Failed to initialize ObjectTracker.");
            result = false;
        }

        return result;
    }


    @Override
    public boolean doLoadTrackersData(){
        // Get the image tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager.getTracker(ObjectTracker.getClassType());
        if (objectTracker == null){
            Log.d(TAG,"Failed to load tracking data set because the ObjectTracker has not been initialized.");
            return false;
        }

        // Create the data sets:
        dataSet = objectTracker.createDataSet();
        if (dataSet == null){
            Log.d(TAG, "Failed to create a new tracking data.");
            return false;
        }

        // Load the data sets:
        if (!dataSet.load(vuforiaVideoManager.getDataFile(),STORAGE_TYPE.STORAGE_APPRESOURCE)){
            Log.d(TAG, "Failed to load data set.");
            return false;
        }

        // Activate the data set:
        if (!objectTracker.activateDataSet(dataSet)){
            Log.d(TAG, "Failed to activate data set.");
            return false;
        }

        Log.d(TAG, "Successfully loaded and activated data set.");
        return true;
    }


    @Override
    public boolean doStartTrackers() {
        // Indicate if the trackers were started correctly
        boolean result = true;

        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null){
            objectTracker.start();
            Vuforia.setHint(HINT.HINT_MAX_SIMULTANEOUS_IMAGE_TARGETS, 2);
        } else
            result = false;

        return result;
    }


    @Override
    public boolean doStopTrackers(){
        // Indicate if the trackers were stopped correctly
        boolean result = true;

        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.stop();
        else
            result = false;

        return result;
    }


    @Override
    public boolean doUnloadTrackersData(){
        // Indicate if the trackers were unloaded correctly
        boolean result = true;

        // Get the image tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null){
            Log.d(TAG,"Failed to destroy the tracking data set because the ObjectTracker has not been initialized.");
            return false;
        }

        if (dataSet != null){
            if (objectTracker.getActiveDataSet() == dataSet && !objectTracker.deactivateDataSet(dataSet)){
                Log.d(TAG,"Failed to destroy the tracking data set StonesAndChips because the data set could not be deactivated.");
                result = false;
            } else if (!objectTracker.destroyDataSet(dataSet)){
                Log.d(TAG,"Failed to destroy the tracking data set StonesAndChips.");
                result = false;
            }

            dataSet = null;
        }

        return result;
    }


    @Override
    public boolean doDeinitTrackers(){
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;

        // Deinit the image tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        trackerManager.deinitTracker(ObjectTracker.getClassType());

        return result;
    }


    @Override
    public void onInitARDone(SampleApplicationException exception){
        if (exception == null){
            initApplicationAR();
            mRenderer.mIsActive = true;

            // Now add the GL surface view. It is important
            // that the OpenGL ES surface view gets added
            // BEFORE the camera is started and video
            // background is configured.
            addContentView(mGlView, new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));

            // Sets the UILayout to be drawn in front of the camera
            mUILayout.bringToFront();

            // Hides the Loading Dialog
            loadingDialogHandler.sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);

            // Sets the layout background to transparent
            mUILayout.setBackgroundColor(Color.TRANSPARENT);

            try {
                vuforiaVideoManager.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);
            } catch (SampleApplicationException e){
                Log.e(TAG, e.getString());
            }

            boolean result = CameraDevice.getInstance().setFocusMode(
                    CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

            if (!result)
                Log.e(TAG, "Unable to enable continuous autofocus");


        } else{
            Log.e(TAG, exception.getString());
            showInitializationErrorMessage(exception.getString());
        }

    }


    // Shows initialization error messages as System dialogs
    public void showInitializationErrorMessage(String message){
        final String errorMessage = message;
        runOnUiThread(new Runnable() {
            public void run() {
                if (mErrorDialog != null) {
                    mErrorDialog.dismiss();
                }

                // Generates an Alert Dialog to show the error message
                AlertDialog.Builder builder = new AlertDialog.Builder( VideoPlayback.this);
                builder.setMessage(errorMessage)
                        .setTitle(getString(R.string.INIT_ERROR))
                        .setCancelable(false)
                        .setIcon(0)
                        .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                mErrorDialog = builder.create();
                mErrorDialog.show();
            }
        });
    }


    @Override
    public void onVuforiaUpdate(State state)
    {
    }
}
