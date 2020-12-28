package com.stoleg.vrface;

import java.io.File;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import android.util.Log;

public class DetectionBasedTracker
{
    public DetectionBasedTracker(String cascadeName, int minFaceSize, String modelSp, String lbpFrontalPath, String lbpLeftPath, String lbpRightPath) {
        mNativeObj = nativeCreateObject(cascadeName, minFaceSize);
        if (new File(modelSp).exists()) {
            Log.e("DetectionBasedTracker", "findLandMarks DetectionBasedTracker !" + modelSp);
            // use trackface
//            long nat = nativeCreateModel(modelSp);
//            if (nat != 0) {
//                mNativeModel = nat;
//            }
        } else {
            Log.e("DetectionBasedTracker", "findLandMarks file doesn't exists !" + modelSp);
        }
        mNativeFaceFollower = trackFaceInit(modelSp, cascadeName, lbpFrontalPath, lbpLeftPath, lbpRightPath);

        Log.e("DetectionBasedTracker", "findLandMarks mNativeModel " + mNativeModel);
    }

    public void start() {
        nativeStart(mNativeObj);
    }

    public void stop() {
        nativeStop(mNativeObj);
    }

    public void setMinFaceSize(int size) {
        nativeSetFaceSize(mNativeObj, size);
    }

    public void detect(Mat imageGray, MatOfRect faces) {
        nativeDetect(mNativeObj, imageGray.getNativeObjAddr(), faces.getNativeObjAddr());
    }

    public void release() {
        nativeDestroyObject(mNativeObj);
        mNativeObj = 0;
    }

    @Deprecated
    public Point[] findLandMarks(Mat imageGray, Rect face) {
        if (mNativeModel != null) {
//            return findLandMarks(mNativeObj, imageGray.getNativeObjAddr(), face.x, face.y, face.width, face.height, mNativeModel);
            return findLandMarks(mNativeObj, imageGray.getNativeObjAddr(), face.x, face.y, face.width, face.height, mNativeModel);
        } else {
            return new Point[0];
        }
    }
    
    public void mergeAlpha(Mat fromImage, Mat toImage) {
        mergeAlpha(fromImage.getNativeObjAddr(), toImage.getNativeObjAddr());
    }
    
    public void morhpFace(Mat input2dShape, Mat output3dShape, Mat jmatrixinitial, String modelpath, boolean flag, boolean useLinear, boolean useBroader, Mat projectedModel, boolean useBlends) {
        if (mNative3d == 0) {
            mNative3d = morhpFaceInit(modelpath);
        }
        morhpFace(input2dShape.getNativeObjAddr(), output3dShape.getNativeObjAddr(), jmatrixinitial.getNativeObjAddr(), mNative3d, flag? 1 : 0, useLinear? 1 : 0, useBroader? 1 : 0, projectedModel.getNativeObjAddr(), useBlends? 1 : 0);
    }

    @Deprecated
    public void trackFaceInit2(String modelFeatures, String modelHaar, String lpbFront, String lbpLeft, String lbpRight) {
        mNativeFaceFollower = trackFaceInit(modelFeatures, modelHaar, lpbFront, lbpLeft, lbpRight);
    }

    public boolean trackFace(Mat imgGrey, Mat prevFace, Mat lands, boolean prevFaceFound) {
        return trackFace(imgGrey.getNativeObjAddr(), prevFace.getNativeObjAddr(), lands.getNativeObjAddr(), prevFaceFound ? 1 : 0, mNativeFaceFollower) == 1? true : false;
    }

    private long mNativeFaceFollower = 0;
    private long mNativeObj = 0;
    private Long mNativeModel = null;
    private long mNative3d = 0;

    private static native long nativeCreateObject(String cascadeName, int minFaceSize);
    private static native long nativeCreateModel(String cascadeName);
    private static native void nativeDestroyObject(long thiz);
    private static native void nativeStart(long thiz);
    private static native void nativeStop(long thiz);
    private static native void nativeSetFaceSize(long thiz, int size);
    private static native void nativeDetect(long thiz, long inputImage, long faces);
    private static native Point[] findLandMarks(long thiz, long inputImage, int x, int y, int height, int width, long modelSp);
    private static native void mergeAlpha(long fromImage, long toImage);

    private static native void morhpFace(long jmatrix2dLands, long jmatrix3dFace, long jmatrixinitial, long modelpath, int flag, int useLinear, int useBrodader, long projected3d, int useBlends);
    private static native long morhpFaceInit(String modelpath);

    private static native long trackFaceInit(String str, String str2, String lbpFrontalPath, String lbpLeftPath, String lbpRightPath);
    private static native int trackFace(long jGreyImg, long jmatrixFacePrev, long jmatrix2dLands, int flag, long model);
    
}
