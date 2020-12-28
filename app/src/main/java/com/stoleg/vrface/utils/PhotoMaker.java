package com.stoleg.vrface.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Environment;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;

import com.stoleg.vrface.commonlib.Settings;


/**
 * Created by sov on 08.01.2017.
 */

public class PhotoMaker {

    public static void makePhotoFromGrey(Mat mGrey, Context context) {
        Mat rgba = new Mat(mGrey.width(), mGrey.height(), CvType.CV_8UC4);
        Imgproc.cvtColor(mGrey, rgba, Imgproc.COLOR_GRAY2RGBA);
        makePhoto(rgba, context);
        rgba.release();

    }

    public static String getNewVideoFileName(Context context) {
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File newFile = new File(file, Settings.DIRECTORY_SELFIE);
        if (!newFile.exists()) {
            newFile.mkdirs();
        }
        final SharedPreferences prefs = context.getSharedPreferences(Settings.PREFS, Context.MODE_PRIVATE);
        int counter = prefs.getInt(Settings.COUNTER_VIDEO, 0);
        counter++;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(Settings.COUNTER_VIDEO, counter);
        editor.commit();
        File f = new File(newFile, Settings.DIRECTORY_SELFIE + "_" + counter + ".mp4");
        return f.getPath();
    }
    public static String makePhoto(Mat mRgba, Context context) {
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File newFile = new File(file, Settings.DIRECTORY_SELFIE);
        if (!newFile.exists()) {
            newFile.mkdirs();
        }
        final SharedPreferences prefs = context.getSharedPreferences(Settings.PREFS, Context.MODE_PRIVATE);
        int counter = prefs.getInt(Settings.COUNTER_PHOTO, 0);
        counter++;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(Settings.COUNTER_PHOTO, counter);
        editor.commit();
        final File fileJpg = new File(newFile, Settings.DIRECTORY_SELFIE + "_" + counter + ".jpg");

        saveMatToFile(mRgba, fileJpg);
        // TODO посмотреть альтернативные способы
        MediaScannerConnection.scanFile(context, new String[]{fileJpg.getPath()}, new String[]{"image/jpeg"}, null);
        return fileJpg.getPath();
    }

    public static void saveMatToFile(Mat mRgba, File fileJpg) {
        Bitmap bitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mRgba, bitmap);
        FileUtils.saveBitmap(fileJpg.getPath(), bitmap);
        bitmap.recycle();
    }
}
