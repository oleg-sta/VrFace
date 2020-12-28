package com.stoleg.vrface.renderer;

import android.app.Activity;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import org.m4m.IProgressListener;
import org.m4m.StreamingParameters;
import org.m4m.samples.VideoCapture;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.stoleg.vrface.commonlib.R;
import com.stoleg.vrface.commonlib.Settings;
import com.stoleg.vrface.CompModel;
import com.stoleg.vrface.Static;
import com.stoleg.vrface.camera.FrameCamera;
import com.stoleg.vrface.utils.FileUtils;
import com.stoleg.vrface.utils.OpencvUtils;
import com.stoleg.vrface.utils.PhotoMaker;
import com.stoleg.vrface.utils.PointsConverter;
import com.stoleg.vrface.utils.PoseHelper;
import com.stoleg.vrface.utils.ShaderUtils;

/**
 * Created by sov on 06.02.2017.
 */

public class MaskRenderer implements GLSurfaceView.Renderer {

    public static int VIDEO_HEIGHT = 480;
    public static int VIDEO_WIDTH = 360;

    public VideoCapture videoCapture;
    int widthSurf;
    int heightSurf;

    int iGlobTime = 0;
    Activity context;
    Callback callback;

    int programNv21ToRgba;
    int texNV21FromCamera[] = new int[2];
    int programId2dParticle;
    int programId3dParticle;
    int program2dTriangles;
    int program2dJustCopy;

    int texRgba[] = new int[2];
    int fboRgba[] = new int[2];
    int[] renId = new int[1]; // depth for 3d

    ByteBuffer bufferY;
    ByteBuffer bufferUV;

    ByteBuffer buffer22;

    Mat greyTemp;
    Mat grey;
    Mat mRgbaDummy;
    CompModel compModel;
    PoseHelper poseHelper;
    ShaderEffect shaderHelper;
    public boolean staticView = false; // it means frame is fixed
    public static PoseHelper.PoseResult poseResult;

    private static final String TAG = "MaskRenderer";
    public FrameCamera frameCamera;

    public IProgressListener progressListener = new IProgressListener() {
        @Override
        public void onMediaStart() {
        }

        @Override
        public void onMediaProgress(float progress) {
        }

        @Override
        public void onMediaDone() {
        }

        @Override
        public void onMediaPause() {
        }

        @Override
        public void onMediaStop() {
        }

        @Override
        public void onError(Exception exception) {
            if (Static.LOG_MODE) Log.i(TAG, "onError progressListener " + exception.getMessage());
        }
    };

    public MaskRenderer(Activity context, CompModel compModel, ShaderEffect shaderHelper, Callback callback) {
        this.context = context;
        this.compModel = compModel;
        this.shaderHelper = shaderHelper;
        this.callback = callback;
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        if (Static.LOG_MODE) Log.i(TAG, "onSurfaceCreated");
        initShaders();
        GLES20.glGenTextures(2, texNV21FromCamera, 0);
        if (Static.LOG_MODE) Log.i(TAG, "onSurfaceCreated2 " + texNV21FromCamera[0]);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texNV21FromCamera[0]);
        // FIXME use pixel to pixel, not average neighbours
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        if (Static.LOG_MODE) Log.i(TAG, "onSurfaceCreated2 " + texNV21FromCamera[1]);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texNV21FromCamera[1]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        shaderHelper.init();
    }

    private void initShaders() {
        int vertexShaderId = ShaderUtils.createShader(GLES20.GL_VERTEX_SHADER, FileUtils.getStringFromAsset(context.getAssets(), "shaders/vss_2d.glsl"));
        int fragmentShaderId = ShaderUtils.createShader(GLES20.GL_FRAGMENT_SHADER, FileUtils.getStringFromAsset(context.getAssets(), "shaders/fss_n21_to_rgba.glsl"));
        programNv21ToRgba = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId);

        programId2dParticle = ShaderUtils.createProgram(ShaderUtils.createShader(GLES20.GL_VERTEX_SHADER, FileUtils.getStringFromAsset(context.getAssets(), "shaders/vss_2d.glsl")), ShaderUtils.createShader(GLES20.GL_FRAGMENT_SHADER, FileUtils.getStringFromAsset(context.getAssets(), "shaders/fss_particle.glsl")));
        program2dTriangles = ShaderUtils.createProgram(ShaderUtils.createShader(GLES20.GL_VERTEX_SHADER, FileUtils.getStringFromAsset(context.getAssets(), "shaders/vss_2d.glsl")), ShaderUtils.createShader(GLES20.GL_FRAGMENT_SHADER, FileUtils.getStringFromAsset(context.getAssets(), "shaders/fss_solid.glsl")));
        program2dJustCopy = ShaderUtils.createProgram(ShaderUtils.createShader(GLES20.GL_VERTEX_SHADER, FileUtils.getStringFromAsset(context.getAssets(), "shaders/vss_2d.glsl")), ShaderUtils.createShader(GLES20.GL_FRAGMENT_SHADER, FileUtils.getStringFromAsset(context.getAssets(), "shaders/fss_2d_simple.glsl")));

        programId3dParticle = ShaderUtils.createProgram(ShaderUtils.createShader(GLES20.GL_VERTEX_SHADER, FileUtils.getStringFromAsset(context.getAssets(), "shaders/vss3d.glsl")), ShaderUtils.createShader(GLES20.GL_FRAGMENT_SHADER, FileUtils.getStringFromAsset(context.getAssets(), "shaders/fss_particle.glsl")));

    }

    public void onDrawFrame(GL10 gl) {
        if (Static.LOG_MODE) Log.i(TAG, "onDrawFrame");
        long time = System.currentTimeMillis();
        iGlobTime++;
        if (iGlobTime % 100 == 0) {
            iGlobTime = 0;
        }
        // TODO synchronize size
        int mCameraWidth;
        int mCameraHeight;

        if (frameCamera != null && Static.libsLoaded) {
            // повторно вытаскивая карды из буфера мы решаем проблему двойной буферизации, т.к. если тащить кадр из буфера, то их будет два
            boolean facing1;
            boolean wereProcessed; // FIXME frame should be previous to be static, not future
            synchronized (frameCamera) {
                wereProcessed = frameCamera.wereProcessed;
                frameCamera.wereProcessed =  true;
                facing1 = frameCamera.facing;
                mCameraWidth = frameCamera.cameraWidth;
                mCameraHeight = frameCamera.cameraHeight;
                if (Static.LOG_MODE) Log.i(TAG, "onDrawFrame size " + mCameraWidth + " " + mCameraHeight + " " + widthSurf + " " + heightSurf);
                if (frameCamera.bufferFromCamera == null) return;
                int cameraSize = mCameraWidth * mCameraHeight;
                if (greyTemp == null) {
                    greyTemp = new Mat(mCameraHeight, mCameraWidth, CvType.CV_8UC1);
                    grey = new Mat(mCameraWidth, mCameraHeight, CvType.CV_8UC1);
                    mRgbaDummy = new Mat(heightSurf, widthSurf, CvType.CV_8UC4);
                    bufferY = ByteBuffer.allocateDirect(cameraSize);
                    bufferUV = ByteBuffer.allocateDirect(cameraSize / 2);
                } else if (greyTemp.rows() != mCameraHeight || greyTemp.cols() != mCameraWidth) {
                    if (Static.LOG_MODE) Log.i(TAG, "onDrawFrame change size");
                    greyTemp.release();
                    grey.release();
                    mRgbaDummy.release();
                    greyTemp = new Mat(mCameraHeight, mCameraWidth, CvType.CV_8UC1);
                    grey = new Mat(mCameraWidth, mCameraHeight, CvType.CV_8UC1);
                    mRgbaDummy = new Mat(heightSurf, widthSurf, CvType.CV_8UC4);
                    bufferY = ByteBuffer.allocateDirect(cameraSize);
                    bufferUV = ByteBuffer.allocateDirect(cameraSize / 2);
                }
                greyTemp.put(0, 0, frameCamera.bufferFromCamera);
                bufferY.put(frameCamera.bufferFromCamera, 0, cameraSize);
                bufferY.position(0);
                bufferUV.put(frameCamera.bufferFromCamera, cameraSize, cameraSize / 2);
                bufferUV.position(0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texNV21FromCamera[0]);
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, mCameraWidth, (int) (mCameraHeight), 0,
                        GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, bufferY);
                GLES20.glFlush();
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texNV21FromCamera[1]);
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE_ALPHA, mCameraWidth / 2, (int) (mCameraHeight * 0.5), 0,
                        GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, bufferUV);
                GLES20.glFlush();
                if (Static.LOG_MODE) Log.i(TAG, "onDrawFrame3");
            }



            // if back camera
            //Mat grey = greyTemp.t();
            Core.transpose(greyTemp, grey);
            if (!facing1) {
                Core.flip(grey, grey, 1);
            } else {
                Core.flip(grey, grey, -1);
            }
            // convert from NV21 to RGBA
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboRgba[0]);
            GLES20.glViewport(0, 0, widthSurf, heightSurf);
            GLES20.glUseProgram(programNv21ToRgba);
            int vPos = GLES20.glGetAttribLocation(programNv21ToRgba, "vPosition");
            int vTex = GLES20.glGetAttribLocation(programNv21ToRgba, "vTexCoord");
            GLES20.glEnableVertexAttribArray(vPos);
            GLES20.glEnableVertexAttribArray(vTex);
            int ufacing = GLES20.glGetUniformLocation(programNv21ToRgba, "u_facing");
            GLES20.glUniform1i(ufacing, facing1 ? 1 : 0);
            if (Static.LOG_MODE) Log.i(TAG, "onDrawFrame size22 " + 1f * widthSurf / heightSurf + " " + 1f * mCameraHeight / mCameraWidth);
            GLES20.glUniform1f(GLES20.glGetUniformLocation(programNv21ToRgba, "cameraWidth"), mCameraWidth);
            GLES20.glUniform1f(GLES20.glGetUniformLocation(programNv21ToRgba, "cameraHeight"), mCameraHeight);
            GLES20.glUniform1f(GLES20.glGetUniformLocation(programNv21ToRgba, "previewWidth"), widthSurf);
            GLES20.glUniform1f(GLES20.glGetUniformLocation(programNv21ToRgba, "previewHeight"), heightSurf);
            if (Static.LOG_MODE) Log.i(TAG, "onDrawFrame5");
            ShaderEffectHelper.shaderEffect2dWholeScreen(new Point(0, 0), new Point(widthSurf, heightSurf), texNV21FromCamera[0], programNv21ToRgba, vPos, vTex, texNV21FromCamera[1]);
            if (Static.LOG_MODE) Log.i(TAG, "onDrawFrame6");

            if (Settings.superDebugMode) {
                if (buffer22 == null) {
                    buffer22 = ByteBuffer.allocateDirect(widthSurf * heightSurf * 4);
                    buffer22.order(ByteOrder.LITTLE_ENDIAN);
                }
                GLES20.glReadPixels(0, 0, widthSurf, heightSurf, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer22);
                buffer22.rewind();
                mRgbaDummy.put(0, 0, buffer22.array());
                //Core.transpose(mRgbaDummy, mRgbaDummy);
                Core.flip(mRgbaDummy, mRgbaDummy, 0);
            }


            if (!wereProcessed) {
                int mAbsoluteFaceSize = Math.round((int) (mCameraWidth * 0.33));
                boolean shapeBlends = shaderHelper.needBlend();
                poseResult = poseHelper.findShapeAndPose(grey, mAbsoluteFaceSize, mRgbaDummy, widthSurf, heightSurf, shapeBlends, shaderHelper.model, context, mCameraHeight, mCameraWidth);
            }

            if (Settings.superDebugMode) {
                // super debug ///////1!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                Imgproc.line(mRgbaDummy, new Point(0, 0), new Point(100, 200), new Scalar(255, 0, 0), 3);
                Core.flip(mRgbaDummy, mRgbaDummy, 0);
                //Core.transpose(mRgbaDummy, mRgbaDummy);
                buffer22.rewind();
                mRgbaDummy.get(0, 0, buffer22.array());
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texRgba[0]);
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, widthSurf, heightSurf, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer22);
                ///////////////////////!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            }

            // TODO draw debug with shaders
            if (Settings.debugMode && poseResult.foundLandmarks != null) {
                int vPos2 = GLES20.glGetAttribLocation(programId2dParticle, "vPosition");
                GLES20.glEnableVertexAttribArray(vPos2);
                ShaderEffectHelper.effect2dParticle(widthSurf, heightSurf, programId2dParticle, vPos2, PointsConverter.convertFromPointsGlCoord(poseResult.foundLandmarks, widthSurf, heightSurf), new float[]{1,1,1});

                if (Settings.addDebug) {
                    // draw 3d morph model
                    int vPos3 = GLES20.glGetAttribLocation(programId3dParticle, "vPosition");
                    GLES20.glEnableVertexAttribArray(vPos3);
                    ShaderEffectHelper.effect2dLinesFrom3dPoints(widthSurf, heightSurf, programId3dParticle, vPos3, PointsConverter.getLinesFromModel(shaderHelper.model), poseResult.glMatrix, new float[]{1, 0, 0});


                    String[] p3d = context.getResources().getStringArray(R.array.points2DTo3D);
                    int[] p3d1 = new int[p3d.length];
                    for (int i = 0; i < p3d.length; i++) {
                        String p = p3d[i];
                        String[] w2 = p.split(";");
                        p3d1[i] = Integer.parseInt(w2[1]);
                    }
                    // draw 3d2d 2d points
                    vPos2 = GLES20.glGetAttribLocation(programId3dParticle, "vPosition");
                    GLES20.glEnableVertexAttribArray(vPos2);
                    ShaderEffectHelper.effect2dPointsFrom3dPoints(widthSurf, heightSurf, programId3dParticle, vPos3, PointsConverter.getOnlyByNumbder(shaderHelper.model.tempV, p3d1), poseResult.glMatrix, new float[]{0, 1, 0});


                    // draw projected
                    float[] projected2dpoints = PointsConverter.convertFromProjectedTo2dPoints(poseResult.projected, widthSurf, heightSurf);
                    vPos2 = GLES20.glGetAttribLocation(programId2dParticle, "vPosition");
                    GLES20.glEnableVertexAttribArray(vPos2);
                    ShaderEffectHelper.effect2dParticle(widthSurf, heightSurf, programId2dParticle, vPos2, projected2dpoints, new float[]{0, 0, 1});

                    if (Static.LOG_MODE) Log.i(TAG, "onDrawFrame size22 " + projected2dpoints.length);
                    vPos2 = GLES20.glGetAttribLocation(programId2dParticle, "vPosition");
                    GLES20.glEnableVertexAttribArray(vPos2);
                    ShaderEffectHelper.effect2dLines(widthSurf, heightSurf, programId2dParticle, vPos2, PointsConverter.getLinesFromModel2(projected2dpoints, shaderHelper.model), new float[]{0, 0, 1});
                }

            }
            // draw effect on rgba
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboRgba[1]);
            GLES20.glViewport(0, 0, widthSurf, heightSurf);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            shaderHelper.makeShaderMask(Static.newIndexEye, poseResult, widthSurf, heightSurf, texRgba[0], time, iGlobTime);

            // copy from middle buffer
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            GLES20.glViewport(0, 0, widthSurf, heightSurf);
            vPos = GLES20.glGetAttribLocation(program2dJustCopy, "vPosition");
            vTex = GLES20.glGetAttribLocation(program2dJustCopy, "vTexCoord");
            GLES20.glEnableVertexAttribArray(vPos);
            GLES20.glEnableVertexAttribArray(vTex);
            ShaderEffectHelper.shaderEffect2dWholeScreen(poseResult.leftEye, poseResult.rightEye, texRgba[1], program2dJustCopy, vPos, vTex);

            if (Static.LOG_MODE) Log.i(TAG, "check for make photo");
            if (Static.makePhoto) {
                Static.makePhoto = false;
                ByteBuffer m_bbPixels = ByteBuffer.wrap(new byte[widthSurf * heightSurf * 4]);
                Mat rgba = new Mat(heightSurf, widthSurf, CvType.CV_8UC4);
                m_bbPixels.order(ByteOrder.LITTLE_ENDIAN);
                GLES20.glReadPixels(0, 0, widthSurf, heightSurf, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, m_bbPixels);
                m_bbPixels.rewind();
                rgba.put(0, 0, m_bbPixels.array());
                Core.flip(rgba, rgba, 0);

                OpencvUtils.makeLogo(rgba, context);

                final String fileName = PhotoMaker.makePhoto(rgba, context);
                rgba.release();
                // TODO change view
                callback.photoSaved();
            }
        }

        if (Static.LOG_MODE) Log.i(TAG, "sync for video");
        if (Static.LOG_MODE) Log.i(TAG, "enter sync for video");
        synchronized (videoCapture) {
            if (videoCapture.isStarted()) {
                if (Static.LOG_MODE) Log.i(TAG, "isStarted1111");
                if (videoCapture.beginCaptureFrame()) {
                    Log.i(TAG, "videoCapture1");
                    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                    GLES20.glViewport(0, 0, VIDEO_WIDTH, VIDEO_HEIGHT);

                    int vPos = GLES20.glGetAttribLocation(program2dJustCopy, "vPosition");
                    int vTex = GLES20.glGetAttribLocation(program2dJustCopy, "vTexCoord");
                    GLES20.glEnableVertexAttribArray(vPos);
                    GLES20.glEnableVertexAttribArray(vTex);
                    Log.i(TAG, "videoCapture2");
                    ShaderEffectHelper.shaderEffect2dWholeScreen(poseResult.leftEye, poseResult.rightEye, texRgba[1], program2dJustCopy, vPos, vTex);
                    Log.i(TAG, "videoCapture3");
                    videoCapture.endCaptureFrame();
                }
            }
        }

    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        VIDEO_HEIGHT = (height / 2) * 2;
        VIDEO_WIDTH = (width / 2) * 2;

//        videoCapture = new VideoCapture(context.getApplicationContext(), progressListener, height, width);
        videoCapture = new VideoCapture(context.getApplicationContext(), progressListener, VIDEO_WIDTH, VIDEO_HEIGHT);
        if (Static.LOG_MODE) Log.i(TAG, "onSurfaceChanged " + width + " " + height);
        GLES20.glGenTextures(2, texRgba, 0);
        if (Static.LOG_MODE) Log.i(TAG, "onSurfaceCreated3 " + texRgba[0]);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texRgba[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        GLES20.glGenFramebuffers(2, fboRgba, 0);
        GLES20.glGenRenderbuffers(1, renId, 0);
        if (Static.LOG_MODE) Log.i(TAG, "onSurfaceCreated4 " + fboRgba[0]);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboRgba[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texRgba[0], 0);

        // second frame buffer and texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texRgba[1]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboRgba[1]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texRgba[1], 0);

        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renId[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, renId[0]);


        if (Static.LOG_MODE) Log.i(TAG, " fbo status " + GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER));
        if (Static.LOG_MODE) Log.i(TAG, "onSurfaceCreated5");
        poseHelper = new PoseHelper(compModel);
        poseHelper.init(context, width, height); // FIXME

        this.widthSurf = width;
        this.heightSurf = height;
    }

    public void startCapturing(StreamingParameters params) throws IOException {
        if (videoCapture == null) {
            return;
        }
        synchronized (videoCapture) {
            videoCapture.start(params);
        }
    }

    public void startCapturing(String videoPath) throws IOException {
        if (videoCapture == null) {
            return;
        }
        synchronized (videoCapture) {
            videoCapture.start(videoPath);
        }
    }

    public void stopCapturing() {
        if (videoCapture == null) {
            return;
        }
        synchronized (videoCapture) {
            if (videoCapture.isStarted()) {
                videoCapture.stop();
            }
        }
    }

    public boolean isCapturingStarted() {
        return videoCapture.isStarted();
    }

    public interface Callback {
        void photoSaved();
    }
}
