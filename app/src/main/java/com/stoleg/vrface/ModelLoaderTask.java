package com.stoleg.vrface;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.stoleg.vrface.commonlib.R;
import com.stoleg.vrface.commonlib.Settings;
import com.stoleg.vrface.model.ImgLabModel;
import com.stoleg.vrface.model.SimpleModel;
import com.stoleg.vrface.utils.FileUtils;

/**
 * Loads ert model in background
 */
public class ModelLoaderTask extends AsyncTask<CompModel, Void, Void> {

    CompModel compModel;
    private static final String TAG = "LoadModel_class";
    Callback callback;

    public ModelLoaderTask(Callback callback) {
        this.callback = callback;
        if (Static.LOG_MODE) Log.i(TAG, "ModelLoaderTask constr");
    }
    @Override
    protected Void doInBackground(CompModel... params) {
        if (Static.LOG_MODE) Log.i(TAG, "ModelLoaderTask doInBackground0");
        compModel = params[0];
        if (Static.LOG_MODE) Log.i(TAG, "ModelLoaderTask doInBackground");
        File cascadeDir = compModel.context.getDir("cascade", Context.MODE_PRIVATE);
        File fModel = new File(cascadeDir, "testing_with_face_landmarks.xml");
        try {
            int res = FileUtils.resourceToFile(compModel.context.getResources().openRawResource(R.raw.monkey_68), fModel);
            if (Static.LOG_MODE) Log.i(TAG, "ModelLoaderTask doInBackground111" + res + " " + fModel.length());
        } catch (Resources.NotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //Log.i(TAG, "ModelLoaderTask doInBackground1");
        SimpleModel modelFrom = new ImgLabModel(fModel.getPath());
        if (Static.LOG_MODE) Log.i(TAG, "ModelLoaderTask doInBackground2");
        //pointsWas = modelFrom.getPointsWas();
        if (Static.LOG_MODE) Log.i(TAG, "ModelLoaderTask doInBackground3");
        //lines = modelFrom.getLines();
        if (Static.LOG_MODE) Log.i(TAG, "ModelLoaderTask doInBackground4");
        // load ready triangulation model from file
        AssetManager assetManager = compModel.context.getAssets();
        //trianlges = StupidTriangleModel.getTriagles(pointsWas, lines);
        if (Static.LOG_MODE) Log.i(TAG, "ModelLoaderTask doInBackground6");

        final SharedPreferences prefs = compModel.context.getSharedPreferences(Settings.PREFS, Context.MODE_PRIVATE);
        String detectorName = prefs.getString(Settings.MODEL_PATH, Settings.MODEL_PATH_DEFAULT);
        if (!new File(detectorName).exists()) {
            detectorName = "/storage/emulated/0/best_model.dat";
        }

        if (!new File(detectorName).exists()) {
            if (Static.LOG_MODE) Log.i(TAG, "ModelLoaderTask doInBackground66");
            try {
                File ertModel = new File(cascadeDir, "ert_model.dat");
                InputStream ims = assetManager.open("sp68.dat");
                int bytes = FileUtils.resourceToFile(ims, ertModel);
                ims.close();
                detectorName = ertModel.getAbsolutePath();
                if (Static.LOG_MODE) Log.i(TAG, "ModelLoaderTask doInBackground66 " + detectorName + " " + ertModel.exists() + " " + ertModel.length() + " " + bytes);
            } catch (Resources.NotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                if (Static.LOG_MODE) Log.i(TAG, "ModelLoaderTask doInBackground667", e);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                if (Static.LOG_MODE) Log.i(TAG, "ModelLoaderTask doInBackground667", e);
            }
        }
        compModel.mNativeDetector = new DetectionBasedTracker(compModel.mCascadeFile.getAbsolutePath(), 0, detectorName, compModel.lbpFrontalPath.getAbsolutePath(), compModel.lbpLeftPath.getAbsolutePath(), compModel.lbpRightPat.getAbsolutePath());
        if (Static.LOG_MODE) Log.i(TAG, "ModelLoaderTask doInBackground7");
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        callback.onModelLoaded();
    }


    public interface Callback {
        void onModelLoaded();
    }
}
