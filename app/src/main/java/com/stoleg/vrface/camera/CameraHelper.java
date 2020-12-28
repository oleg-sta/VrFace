package com.stoleg.vrface.camera;

import android.hardware.Camera;
import android.util.Log;

import java.util.List;

import com.stoleg.vrface.Static;

/**
 * Created by sov on 07.02.2017.
 */

public class CameraHelper {

    private static final String LOGTAG = "CameraHelper";

    // FIXME camera aspect ratio is very wrong, e.g. 320x240 didn't really 320x240 on screen, it sized by height!!!
    // find the preview size that best suits with aspect ratio and lower max size
    // TODO should consider size and ratio simultaneously, e.g. min( abs(aspect - (float)w/h) + abs(w-maxWidth)/maxWidth + abs(h-maxHeight)/maxHeight)
    public static void calculateCameraPreviewSize(Camera.Parameters param, int maxWidth, int maxHeight) {
        if (Static.LOG_MODE) Log.i(LOGTAG, "calculateCameraPreviewSize: "+maxWidth+"x"+maxHeight);

        //param.get
        List<Camera.Size> psize = param.getSupportedPreviewSizes();
        int bestWidth = 0, bestHeight = 0;
        if (psize.size() > 0) {
            float aspect = (float)maxWidth / maxHeight;
            for (Camera.Size size : psize) {
                int w = size.width, h = size.height;
                if (Static.LOG_MODE) Log.d(LOGTAG, "checking camera preview size: "+w+"x"+h);
                if ( w <= maxWidth && h <= maxHeight &&
                        w >= bestWidth && h >= bestHeight &&
                        Math.abs(aspect - (float)w/h) < 0.2 ) {
                    bestWidth = w;
                    bestHeight = h;
                }
            }
            if(bestWidth <= 0 || bestHeight <= 0) {
                bestWidth  = psize.get(0).width;
                bestHeight = psize.get(0).height;
                if (Static.LOG_MODE) Log.e(LOGTAG, "Error: best size was not selected, using "+bestWidth+" x "+bestHeight);
            } else {
                if (Static.LOG_MODE) Log.i(LOGTAG, "Selected best size: "+bestWidth+" x "+bestHeight);
            }
            param.setPreviewSize(bestWidth, bestHeight);
        }
        if (Static.LOG_MODE) Log.i(LOGTAG, "calculateCameraPreviewSize: "+bestWidth+"x"+bestHeight);
        for (Camera.Size size : param.getSupportedPictureSizes()) {
            if (Static.LOG_MODE) Log.i(LOGTAG, "calculateCameraPreviewSize pic size: "+size.width+" x "+size.height);
        }
    }
}
