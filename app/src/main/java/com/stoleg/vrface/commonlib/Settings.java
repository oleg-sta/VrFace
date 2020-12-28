package com.stoleg.vrface.commonlib;

import com.stoleg.vrface.camera.FrameCamera;

public class Settings {

    public static String PHOTO = "photo";

    public static boolean debugMode = false;
    public static String DIRECTORY_SELFIE = "Masks"; // dir and prefix name
    
    public static final String PREFS = "eselfie";
    public static final String DEBUG_MODE = "debugMode";
    public static final String MULTI_MODE = "multiMode";
    public static final String PUPILS_MODE = "pupilsMode";
    public static final String COUNTER_PHOTO = "photoCounter";
    public static final String COUNTER_VIDEO = "photoCounter";
    public static final String MODEL_PATH = "modelPath";
    public static final String MODEL_PATH_DEFAULT = "/storage/extSdCard/sp_s2.dat";
    public static boolean useLinear;
    public static boolean useKalman;

    public static Class clazz;

    public static Integer resourceLogo;
    public static boolean useBroader;
    public static boolean addDebug;
    public static float min = 0.01f;
    public static boolean flagOrtho;
    public static boolean superDebugMode;
    public static float seek3;
    public static float seek2;

    public static boolean useFakeCamera;
    public static FrameCamera fakeCamera;
    public static ErrorInterface errorClass;
}
