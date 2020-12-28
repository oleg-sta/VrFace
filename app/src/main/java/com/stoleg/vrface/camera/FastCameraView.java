package com.stoleg.vrface.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

import com.stoleg.vrface.commonlib.Settings;
import com.stoleg.vrface.Static;
import com.stoleg.vrface.renderer.MaskRenderer;

/**
 * Created by sov on 06.02.2017.
 */

public class FastCameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    public FrameCamera frameCamera = new FrameCamera();
    private static final int MAGIC_TEXTURE_ID = 10;
    boolean cameraFacing;
    private byte mBuffer[];
    private static final String TAG = "FastView";
    private Camera mCamera;
    private SurfaceTexture mSurfaceTexture;

    int numberOfCameras;
    int cameraIndex;

    int previewWidth;
    int previewHeight;

    int cameraWidth;
    int cameraHeight;

    public FastCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);

        cameraIndex = 0;
        numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraFacing = true;
                cameraIndex = i;
            }
        }

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        getHolder().addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        //mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (Static.LOG_MODE) Log.d(TAG, "surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (Static.LOG_MODE) Log.d(TAG, "surfaceChanged " + format + " " + w + " " + h);
        previewHeight = h;
        previewWidth = w;
        // TODO release camera if was
        startCameraPreview(w , h);
    }

    private void startCameraPreview(int previewWidthLocal, int previewHeightLocal) {
        if (Static.LOG_MODE) Log.d(TAG, "startCameraPreview " + previewWidthLocal + " " + previewHeightLocal + " " + cameraIndex);
        releaseCamera(); // easiest way TODO fix to right way
        mCamera = Camera.open(cameraIndex);
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

//        if (mHolder.getSurface() == null){
//            // preview surface does not exist
//            return;
//        }

        // stop preview before making changes
//        try {
//            mCamera.stopPreview();
//        } catch (Exception e){
//            // ignore: tried to stop a non-existent preview
//        }

        // we transpose view
        Camera.Parameters params = mCamera.getParameters();
        if (Static.LOG_MODE) Log.d(TAG, "preview format " + params.getPreviewFormat());
        params.setPreviewFormat(ImageFormat.NV21);
        CameraHelper.calculateCameraPreviewSize(params, previewHeightLocal, previewWidthLocal);
        cameraWidth = params.getPreviewSize().width;
        cameraHeight = params.getPreviewSize().height;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && !Build.MODEL.equals("GT-I9100"))
            params.setRecordingHint(true);
        List<String> FocusModes = params.getSupportedFocusModes();
        if (FocusModes != null && FocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
        {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }

        mCamera.setParameters(params);

        int size = cameraWidth * cameraHeight;
        size  = size * ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8;
        mBuffer = new byte[size];
        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            //mCamera.setPreviewDisplay(mHolder);

            // TODO if necessary you could use more buffer for speed
            mCamera.addCallbackBuffer(mBuffer);
            mCamera.setPreviewCallbackWithBuffer(this);

            // do not preview
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mSurfaceTexture = new SurfaceTexture(MAGIC_TEXTURE_ID);
                mCamera.setPreviewTexture(mSurfaceTexture);
            } else
                mCamera.setPreviewDisplay(null);

            mCamera.startPreview();
            if (Static.LOG_MODE) Log.d(TAG, "Got a camera frame " + params.getPreviewFormat() + " " + ImageFormat.getBitsPerPixel(params.getPreviewFormat()) + " " + params.getPreviewSize().height + " " + params.getPreviewSize().width + " " + params.getPictureSize().height + " " + params.getPictureSize().width);
        } catch (Exception e){
            if (Static.LOG_MODE) Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (Static.LOG_MODE) Log.d(TAG, "surfaceDestroyed");
        releaseCamera();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (Static.LOG_MODE) Log.d(TAG, "Got a camera frame " + data.length);
        synchronized (frameCamera) {
            // TODO find face and features here or another thread for optimization
            // TODO we can not copy buffer just find face features, morph face and let it go to renderer
            if (!Settings.useFakeCamera || Settings.fakeCamera == null) {
                frameCamera.cameraWidth = cameraWidth;
                frameCamera.cameraHeight = cameraHeight;
                frameCamera.facing = cameraFacing;
                if (frameCamera.bufferFromCamera == null || frameCamera.bufferFromCamera.length != data.length) {
                    frameCamera.bufferFromCamera = new byte[data.length];
                }
                System.arraycopy(data, 0, frameCamera.bufferFromCamera, 0, data.length);
            } else {
                synchronized (Settings.fakeCamera) {
                    frameCamera.cameraWidth = Settings.fakeCamera.cameraWidth;
                    frameCamera.cameraHeight = Settings.fakeCamera.cameraHeight;
                    frameCamera.facing = Settings.fakeCamera.facing;
                    if (frameCamera.bufferFromCamera == null || frameCamera.bufferFromCamera.length != Settings.fakeCamera.bufferFromCamera.length) {
                        frameCamera.bufferFromCamera = new byte[Settings.fakeCamera.bufferFromCamera.length];
                    }
                    System.arraycopy(Settings.fakeCamera.bufferFromCamera, 0, frameCamera.bufferFromCamera, 0, Settings.fakeCamera.bufferFromCamera.length);
                }
            }
            frameCamera.wereProcessed = false;
        }
        // we should add buffer to queue, dut to buffer
        mCamera.addCallbackBuffer(mBuffer);
    }

    public void disableView() {
        releaseCamera();
    }

    private void releaseCamera() {
        synchronized (frameCamera) {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                mCamera.release();
            }
            mCamera = null;
        }
    }

    public void swapCamera() {
        releaseCamera();
        cameraIndex++;
        if (cameraIndex >= numberOfCameras) {
            cameraIndex = 0;
        }
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraIndex, cameraInfo);
        cameraFacing = false;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            cameraFacing = true;
        }
        startCameraPreview(previewWidth, previewHeight);
    }

    public void enableView() {
        startCameraPreview(previewWidth, previewHeight);
    }
}
