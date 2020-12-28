package com.stoleg.vrface;

import org.opencv.core.Mat;

import com.stoleg.vrface.commonlib.R;

/**
 * Created by sov on 06.01.2017.
 */

public class Static {

    public static boolean makePhoto;
    public static boolean makePhoto2;

    public static final boolean LOG_MODE = true;//BuildConfig.DEBUG;

    public static boolean drawOrigTexture;
    public static boolean libsLoaded;


    public static int currentIndexEye = -1;
    public static int newIndexEye = 0;

    public static Mat glViewMatrix2;

    public static final int[] resourceDetector = {R.raw.haarcascade_frontalface_alt2_snap, R.raw.lbpcascade_frontalface, R.raw.haarcascade_frontalface_alt2, R.raw.my_detector};
    public static boolean saveVideo;
}
