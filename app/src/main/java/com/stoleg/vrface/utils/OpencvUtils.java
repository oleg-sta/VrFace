package com.stoleg.vrface.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import com.stoleg.vrface.commonlib.Settings;

/**
 * Created by sov on 04.01.2017.
 */

public class OpencvUtils {

    public static Point orient(Point point, int width, int heigth) {
        return orient(point, 0, width, heigth);
    }

    public static Point orient(Point point, int orient, int width, int heigth) {
        if (true) {
            return point;
        }
        if (orient == 3) {
            return point;
        } else if (orient == 0) {
            return new Point(point.y, heigth - point.x);
        } else if (orient == 1) {
            return new Point(width - point.x, heigth - point.y);
        } else {
            return new Point(width - point.y, point.x);
        }
    }

    public static double angleOfYx(Point p1, Point p2) {
        // NOTE: Remember that most math has the Y axis as positive above the X.
        // However, for screens we have Y as positive below. For this reason,
        // the Y values are inverted to get the expected results.
        final double deltaX = (p1.y - p2.y);
        final double deltaY = (p2.x - p1.x);
        final double result = Math.toDegrees(Math.atan2(deltaY, deltaX));
        return (result < 0) ? (360d + result) : result;
    }

    public static Point convertToGl(Point old, int width, int height) {
        return new Point(old.x / width, 1 - old.y / height);
    }

    public static Mat loadFromResource(Context context, int resourceId) {
        // используем загрузку через андроид, т.к. opencv ломает цвета
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bmp = BitmapFactory.decodeStream(context.getResources().openRawResource(resourceId), null, opts);
        Mat newEyeTmp2 = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(bmp, newEyeTmp2, true);
        return newEyeTmp2;
    }

    public static void makeLogo(Mat rgba, Context context) {
        if (Settings.resourceLogo == null) return;
        Mat logotip = OpencvUtils.loadFromResource(context, Settings.resourceLogo);
        int widthLogo = (int)(rgba.width() * 0.2f);
        int heightLogo = logotip.height() * widthLogo / logotip.width();
        Mat submat = rgba.submat(rgba.height() - heightLogo, rgba.height(), rgba.width() - widthLogo, rgba.width());
        Imgproc.resize(logotip, logotip, new Size(widthLogo, heightLogo));
        List<Mat> layers = new ArrayList<Mat>();
        Core.split(logotip, layers);
        logotip.copyTo(submat, layers.get(3));
        logotip.release();
        submat.release();
    }
}
