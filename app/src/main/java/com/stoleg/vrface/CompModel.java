package com.stoleg.vrface;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.IOException;

import com.stoleg.vrface.utils.FileUtils;

/**
 * Created by sov on 04.01.2017.
 */

public class CompModel {

    private static final String TAG = "CompModel_class";

    public CascadeClassifier mJavaDetector;
    public volatile DetectionBasedTracker mNativeDetector;
    public File mCascadeFile;

    public File lbpFrontalPath;
    public File lbpLeftPath;
    public File lbpRightPat;

    // in
    public Context context;

    public void load3lbpModels(int resource1, int resource2, int resource3) {
        if (Static.LOG_MODE) Log.i(TAG, "load3lbpModels");
        File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
        lbpFrontalPath = new File(cascadeDir, "lbpFrontalPath.xml");
        lbpLeftPath = new File(cascadeDir, "lbpLeftPath.xml");
        lbpRightPat = new File(cascadeDir, "lbpRightPat.xml");
        try {
            FileUtils.resourceToFile(context.getResources().openRawResource(resource1), lbpFrontalPath);
            FileUtils.resourceToFile(context.getResources().openRawResource(resource2), lbpLeftPath);
            FileUtils.resourceToFile(context.getResources().openRawResource(resource3), lbpRightPat);
        } catch (Resources.NotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            if (Static.LOG_MODE) Log.i(TAG, "load3lbpModels error "+ e.getMessage());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            if (Static.LOG_MODE) Log.i(TAG, "load3lbpModels error "+ e.getMessage());
        }
        if (Static.LOG_MODE) Log.i(TAG, "load3lbpModels end");
    }
    public void loadHaarModel(int resource) {
        if (Static.LOG_MODE) Log.i(TAG, "loadHaarModel " + context.getResources().getResourceName(resource));
        File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);

        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
        try {
            FileUtils.resourceToFile(context.getResources().openRawResource(resource), mCascadeFile);
        } catch (Resources.NotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        if (mJavaDetector.empty()) {
            Log.e(TAG, "Failed to load cascade classifier");
            mJavaDetector = null;
        } else {
            if (Static.LOG_MODE) Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
        }

    }

    public MatOfRect findFaces(Mat mGray, int mAbsoluteFaceSize) {
        MatOfRect faces = new MatOfRect();
        if (mJavaDetector != null) {
            mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO:
                    // objdetect.CV_HAAR_SCALE_IMAGE
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }
        return faces;
    }
}
