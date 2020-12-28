package com.stoleg.vrface;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import com.stoleg.vrface.commonlib.R;

/**
 * Created by sov on 05.08.2017.
 */

public class Initializer {

    public static CompModel compModel;

    ModelLoaderTask.Callback cllbacker;
    private static final String TAG = "Initializer";
    private BaseLoaderCallback mLoaderCallback;

    public Initializer(ModelLoaderTask.Callback cllbacker) {
        this.cllbacker = cllbacker;
        if (!Static.libsLoaded) {
            compModel = new CompModel();
            compModel.context = (Context) cllbacker;
        }
        mLoaderCallback = new BaseLoaderCallback((Context) cllbacker) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS: {
                        Log.i(TAG, "OpenCV loaded successfully");
                        // Load native library after(!) OpenCV initialization
                        System.loadLibrary("detection_based_tracker");

                        Static.libsLoaded = true;
                        // load cascade file from application resources
                        Log.e(TAG, "findLandMarks onManagerConnected");
                        compModel.loadHaarModel(Static.resourceDetector[0]);
                        compModel.load3lbpModels(R.raw.lbp_frontal_face, R.raw.lbp_left_face, R.raw.lbp_right_face);
                    }
                    break;
                    default: {
                        super.onManagerConnected(status);
                    }
                    break;
                }
            }
        };
    }

    public void init() {
        if (Static.LOG_MODE) Log.i(TAG, "init ...");
        OpenCVLoader.initDebug();
        if (Static.LOG_MODE) Log.i(TAG, "init2 ...");
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        if (Static.LOG_MODE) Log.i(TAG, "init3 ...");
        ModelLoaderTask modelLoaderTask = new ModelLoaderTask(cllbacker);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            modelLoaderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, compModel);
        } else {
            modelLoaderTask.execute(compModel);
        }
    }
}
