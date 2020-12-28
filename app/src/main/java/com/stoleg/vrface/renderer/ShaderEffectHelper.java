package com.stoleg.vrface.renderer;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.stoleg.vrface.model3d.Model;
import com.stoleg.vrface.model3d.ModelNew;
import com.stoleg.vrface.utils.PoseHelper;

/**
 * Created by sov on 13.02.2017.
 */

public class ShaderEffectHelper {

    private static final String TAG = "ShaderEffectHelper";

    public static void shaderEffect3d2(float[] matrixView, int texIn, int width, int height, final Model modelToDraw, int modelTextureId, float alpha, int programId, int vPos3d, int vTexFor3d, float[] ortho, int vTexFor3dortho, boolean flagOrtho, Mat initialParams) {
        Log.i(TAG, "shaderEffect3d" + modelToDraw.getClass().getName());
        if (modelToDraw instanceof ModelNew) {
            shaderEffect3d2(matrixView, texIn, width, height, (ModelNew) modelToDraw, modelTextureId, alpha, programId, vPos3d, vTexFor3d, ortho, vTexFor3dortho, flagOrtho, initialParams);
            return;
        }
        GLES20.glUseProgram(programId);
        int matrixMvp = GLES20.glGetUniformLocation(programId, "u_MVPMatrix");

        float[] mMatrix = new float[16];
        Matrix.multiplyMM(mMatrix, 0, PoseHelper.createProjectionMatrixThroughPerspective(width, height), 0, matrixView, 0);
        GLES20.glUniformMatrix4fv(matrixMvp, 1, false, mMatrix, 0);

        int fAlpha = GLES20.glGetUniformLocation(programId, "f_alpha");
        GLES20.glUniform1f(fAlpha, alpha);

        FloatBuffer mVertexBuffer = modelToDraw.getVertices();
        mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPos3d, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);

        FloatBuffer mTextureBuffer = modelToDraw.getTexCoords();
        mTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vTexFor3d,  2, GLES20.GL_FLOAT, false, 0, mTextureBuffer);

        GLES20.glUniform1i(GLES20.glGetUniformLocation(programId, "ss"), flagOrtho? 1 : 0);
        if (flagOrtho) {
            GLES20.glUniform1f(GLES20.glGetUniformLocation(programId, "s"), (float)initialParams.get(0, 0)[0]);
            GLES20.glUniform3f(GLES20.glGetUniformLocation(programId, "t"), (float)initialParams.get(4, 0)[0], (float)initialParams.get(5, 0)[0], 0);
            GLES20.glUniform2f(GLES20.glGetUniformLocation(programId, "wid"), width, height);
            Mat src = new Mat(3, 1, CvType.CV_64FC1);
            src.put(0, 0, initialParams.get(1, 0));
            src.put(1, 0, initialParams.get(2, 0));
            src.put(2, 0, initialParams.get(3, 0));
            Mat yac = new Mat();
            Calib3d.Rodrigues(src, yac);

            float[] matrixView3 = PoseHelper.convertToArray3(yac);
            GLES20.glUniformMatrix3fv(GLES20.glGetUniformLocation(programId, "u_OrthoMatrix"), 1, false, matrixView3, 0);

        }
        // points from ortho
        FloatBuffer mTextureBufferortho = convertArray(ortho);
        mTextureBufferortho.position(0);
        GLES20.glVertexAttribPointer(vTexFor3dortho,  2, GLES20.GL_FLOAT, false, 0, mTextureBufferortho);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, modelTextureId);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(programId, "u_Texture"), 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texIn);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(programId, "u_TextureOrig"), 1);

        ShortBuffer mIndices = modelToDraw.getIndices();
        mIndices.position(0);
        // FIXME with glDrawElements use can't use other texture coordinates
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, modelToDraw.getIndicesCount(), GLES20.GL_UNSIGNED_SHORT, mIndices);
        GLES20.glFlush();
    }

    public static void shaderEffect3d2(float[] matrixView, int texIn, int width, int height, final ModelNew modelToDraw, int modelTextureId, float alpha, int programId, int vPos3d, int vTexFor3d, float[] ortho, int vTexFor3dortho, boolean flagOrtho, Mat initialParams) {

        Log.i(TAG, "shaderEffect3d" + modelToDraw.getClass().getName());
        GLES20.glUseProgram(programId);
        int matrixMvp = GLES20.glGetUniformLocation(programId, "u_MVPMatrix");

        float[] mMatrix = new float[16];
        Matrix.multiplyMM(mMatrix, 0, PoseHelper.createProjectionMatrixThroughPerspective(width, height), 0, matrixView, 0);
        GLES20.glUniformMatrix4fv(matrixMvp, 1, false, mMatrix, 0);

        int fAlpha = GLES20.glGetUniformLocation(programId, "f_alpha");
        GLES20.glUniform1f(fAlpha, alpha);

        FloatBuffer mVertexBuffer = modelToDraw.getVertices();
        mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPos3d, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);

        FloatBuffer mTextureBuffer = modelToDraw.getTexCoords();
        mTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vTexFor3d,  2, GLES20.GL_FLOAT, false, 0, mTextureBuffer);

        GLES20.glUniform1i(GLES20.glGetUniformLocation(programId, "ss"), flagOrtho? 1 : 0);
        if (flagOrtho) {
            GLES20.glUniform1f(GLES20.glGetUniformLocation(programId, "s"), (float)initialParams.get(0, 0)[0]);
            GLES20.glUniform3f(GLES20.glGetUniformLocation(programId, "t"), (float)initialParams.get(4, 0)[0], (float)initialParams.get(5, 0)[0], 0);
            GLES20.glUniform2f(GLES20.glGetUniformLocation(programId, "wid"), width, height);
            Mat src = new Mat(3, 1, CvType.CV_64FC1);
            src.put(0, 0, initialParams.get(1, 0));
            src.put(1, 0, initialParams.get(2, 0));
            src.put(2, 0, initialParams.get(3, 0));
            Mat yac = new Mat();
            Calib3d.Rodrigues(src, yac);

            float[] matrixView3 = PoseHelper.convertToArray3(yac);
            GLES20.glUniformMatrix3fv(GLES20.glGetUniformLocation(programId, "u_OrthoMatrix"), 1, false, matrixView3, 0);

        }
        // points from ortho
        FloatBuffer mTextureBufferortho = convertArray(ortho);
        mTextureBufferortho.position(0);
        GLES20.glVertexAttribPointer(vTexFor3dortho,  2, GLES20.GL_FLOAT, false, 0, mTextureBufferortho);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, modelTextureId);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(programId, "u_Texture"), 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texIn);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(programId, "u_TextureOrig"), 1);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, modelToDraw.getVertexCount());
        GLES20.glFlush();
    }

    public static void shaderEffect3d(float[] matrixView, int texIn, int width, int height, final Model modelToDraw, int modelTextureId, float alpha, int programId, int vPos3d, int vTexFor3d) {
        Log.i(TAG, "shaderEffect3d" + modelToDraw.getClass().getName());
        if (modelToDraw instanceof  ModelNew) {
            shaderEffect3d(matrixView, texIn, width, height, (ModelNew)modelToDraw, modelTextureId, alpha, programId, vPos3d, vTexFor3d);
            return;
        }
        GLES20.glUseProgram(programId);
        int matrixMvp = GLES20.glGetUniformLocation(programId, "u_MVPMatrix");

        float[] mMatrix = new float[16];
        Matrix.multiplyMM(mMatrix, 0, PoseHelper.createProjectionMatrixThroughPerspective(width, height), 0, matrixView, 0);
        GLES20.glUniformMatrix4fv(matrixMvp, 1, false, mMatrix, 0);

        int fAlpha = GLES20.glGetUniformLocation(programId, "f_alpha");
        GLES20.glUniform1f(fAlpha, alpha);

        FloatBuffer mVertexBuffer = modelToDraw.getVertices();
        mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPos3d, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);

        FloatBuffer mTextureBuffer = modelToDraw.getTexCoords();
        mTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vTexFor3d, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, modelTextureId);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(programId, "u_Texture"), 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texIn);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(programId, "u_TextureOrig"), 1);

        ShortBuffer mIndices = modelToDraw.getIndices();
        mIndices.position(0);
        // FIXME with glDrawElements use can't use other texture coordinates
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, modelToDraw.getIndicesCount(), GLES20.GL_UNSIGNED_SHORT, mIndices);
        GLES20.glFlush();
    }

    public static void shaderEffect3d(float[] matrixView, int texIn, int width, int height, final ModelNew modelToDraw, int modelTextureId, float alpha, int programId, int vPos3d, int vTexFor3d) {
        Log.i(TAG, "shaderEffect3d modelnew");
        GLES20.glUseProgram(programId);
        int matrixMvp = GLES20.glGetUniformLocation(programId, "u_MVPMatrix");

        float[] mMatrix = new float[16];
        Matrix.multiplyMM(mMatrix, 0, PoseHelper.createProjectionMatrixThroughPerspective(width, height), 0, matrixView, 0);
        GLES20.glUniformMatrix4fv(matrixMvp, 1, false, mMatrix, 0);

        int fAlpha = GLES20.glGetUniformLocation(programId, "f_alpha");
        GLES20.glUniform1f(fAlpha, alpha);

        FloatBuffer mVertexBuffer = modelToDraw.getVertices();
        mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPos3d, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);

        FloatBuffer mTextureBuffer = modelToDraw.getTexCoords();
        mTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vTexFor3d, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, modelTextureId);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(programId, "u_Texture"), 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texIn);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(programId, "u_TextureOrig"), 1);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, modelToDraw.getVertexCount());
        GLES20.glFlush();
    }

    public static void effect2dParticle(int width, int height, int programId, int vPos, float[] verticesParticels, float[] color) {
        GLES20.glUseProgram(programId);
        GLES20.glUniform3f(GLES20.glGetUniformLocation(programId, "f_color"), color[0], color[1], color[2]);
        FloatBuffer vertexData = convertArray(verticesParticels);
        GLES20.glVertexAttribPointer(vPos, 2, GLES20.GL_FLOAT, false, 0, vertexData);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, verticesParticels.length / 2);
        GLES20.glFlush();
    }

    public static void effect2dLines(int width, int height, int programId, int vPos, float[] verticesParticels, float[] color) {
        GLES20.glUseProgram(programId);
        GLES20.glUniform3f(GLES20.glGetUniformLocation(programId, "f_color"), color[0], color[1], color[2]);
        FloatBuffer vertexData = convertArray(verticesParticels);
        GLES20.glVertexAttribPointer(vPos, 2, GLES20.GL_FLOAT, false, 0, vertexData);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, verticesParticels.length / 2);
        GLES20.glFlush();
    }

    public static void effect2dLinesFrom3dPoints(int width, int height, int programId, int vPos, float[] vertices3d, Mat glMatrix, float[] color) {
        GLES20.glUseProgram(programId);

        int matrixMvp = GLES20.glGetUniformLocation(programId, "u_MVPMatrix");
        float[] matrixView = PoseHelper.convertToArray(glMatrix);
        float[] mMatrix = new float[16];
        Matrix.multiplyMM(mMatrix, 0, PoseHelper.createProjectionMatrixThroughPerspective(width, height), 0, matrixView, 0);
        GLES20.glUniformMatrix4fv(matrixMvp, 1, false, mMatrix, 0);


        GLES20.glUniform3f(GLES20.glGetUniformLocation(programId, "f_color"), color[0], color[1], color[2]);
        FloatBuffer vertexData = convertArray(vertices3d);
        GLES20.glVertexAttribPointer(vPos, 3, GLES20.GL_FLOAT, false, 0, vertexData);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertices3d.length / 3);
        GLES20.glFlush();
    }

    public static void effect2dPointsFrom3dPoints(int width, int height, int programId, int vPos, float[] vertices3d, Mat glMatrix, float[] color) {
        GLES20.glUseProgram(programId);

        int matrixMvp = GLES20.glGetUniformLocation(programId, "u_MVPMatrix");
        float[] matrixView = PoseHelper.convertToArray(glMatrix);
        float[] mMatrix = new float[16];
        Matrix.multiplyMM(mMatrix, 0, PoseHelper.createProjectionMatrixThroughPerspective(width, height), 0, matrixView, 0);
        GLES20.glUniformMatrix4fv(matrixMvp, 1, false, mMatrix, 0);



        GLES20.glUniform3f(GLES20.glGetUniformLocation(programId, "f_color"), color[0], color[1], color[2]);
        FloatBuffer vertexData = convertArray(vertices3d);
        GLES20.glVertexAttribPointer(vPos, 3, GLES20.GL_FLOAT, false, 0, vertexData);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, vertices3d.length / 3);
        GLES20.glFlush();
    }

    public static void shaderEffect2dWholeScreen(Point center, Point center2, int texIn, int programId, int poss, int texx) {
        shaderEffect2dWholeScreen(center, center2, texIn, programId, poss, texx, null);
    }

    public static void effect2dTriangles(int programId, int textureForeground, int textureEffect[], float[] verticesOnForeground, float[] verticesOnTexture, int posForeground, int posOnTexture, short[] triangles, Integer texture2, Integer texture3, int[] useHsv, float[] color0, float[] color1, float[] color2, float alpha0, float alpha1, float alpha2, float[] addColors) {
        GLES20.glUseProgram(programId);

        int fAlpha = GLES20.glGetUniformLocation(programId, "f_alpha");
        GLES20.glUniform3f(fAlpha, alpha0, alpha1, alpha2);

        GLES20.glUniform1iv(GLES20.glGetUniformLocation(programId, "useHsv0"), 3, useHsv, 0);
        if (color0 != null) {
            int uColorLocation = GLES20.glGetUniformLocation(programId, "color0");
            GLES20.glUniform3f(uColorLocation, color0[0], color0[1], color0[2]);
        }
        if (color1 != null) {
            int uColorLocation = GLES20.glGetUniformLocation(programId, "color1");
            GLES20.glUniform3f(uColorLocation, color1[0], color1[1], color1[2]);
        }
        if (color2 != null) {
            int uColorLocation = GLES20.glGetUniformLocation(programId, "color2");
            GLES20.glUniform3f(uColorLocation, color2[0], color2[1], color2[2]);
        }

        FloatBuffer mVertexBuffer = convertArray(verticesOnForeground);
        GLES20.glVertexAttribPointer(posForeground, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer);

        FloatBuffer mTextureBuffer = convertArray(verticesOnTexture);
        GLES20.glVertexAttribPointer(posOnTexture,  2, GLES20.GL_FLOAT, false, 0, mTextureBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureEffect[0]);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(programId, "u_Texture"), 0);

        if (texture2 != null) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture2);
            GLES20.glUniform1i(GLES20.glGetUniformLocation(programId, "u_Texture1"), 2);
        }

        if (texture3 != null) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture3);
            GLES20.glUniform1i(GLES20.glGetUniformLocation(programId, "u_Texture2"), 3);
        }

        int[] useHsv1 = new int[]{-1, -1, -1, -1, -1};
        if (textureEffect[1] > 0) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE4);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureEffect[1]);
            GLES20.glUniform1i(GLES20.glGetUniformLocation(programId, "u_Texture3"), 4);
            useHsv1[0] = useHsv[0];
            GLES20.glUniform3f(GLES20.glGetUniformLocation(programId, "color3"), addColors[0], addColors[1], addColors[2]); // FIX to another color
        }
        if (textureEffect[2] > 0) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE5);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureEffect[2]);
            GLES20.glUniform1i(GLES20.glGetUniformLocation(programId, "u_Texture4"), 5);
            useHsv1[1] = useHsv[0];
            GLES20.glUniform3f(GLES20.glGetUniformLocation(programId, "color4"), addColors[3], addColors[4], addColors[5]); // FIX to another color
        }
        if (textureEffect[3] > 0) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE6);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureEffect[3]);
            GLES20.glUniform1i(GLES20.glGetUniformLocation(programId, "u_Texture5"), 6);
            useHsv1[2] = useHsv[0];
            GLES20.glUniform3f(GLES20.glGetUniformLocation(programId, "color5"), addColors[6], addColors[7], addColors[8]); // FIX to another color
        }
        if (textureEffect[4] > 0) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE7);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureEffect[4]);
            GLES20.glUniform1i(GLES20.glGetUniformLocation(programId, "u_Texture6"), 7);
            useHsv1[3] = useHsv[0];
            GLES20.glUniform3f(GLES20.glGetUniformLocation(programId, "color6"), addColors[9], addColors[10], addColors[11]); // FIX to another color
        }
        GLES20.glUniform1iv(GLES20.glGetUniformLocation(programId, "useHsv1"), 3, useHsv1, 0);
        GLES20.glUniform3f(GLES20.glGetUniformLocation(programId, "f_alpha1"), alpha0, alpha0, alpha0);

        GLES20.glUniform1iv(GLES20.glGetUniformLocation(programId, "useHsv2"), 3, useHsv1, 0);
        GLES20.glUniform3f(GLES20.glGetUniformLocation(programId, "f_alpha2"), alpha0, alpha0, alpha0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureForeground);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(programId, "u_TextureOrig"), 1);

        ShortBuffer mIndices = convertArray(triangles);
        // FIXME with glDrawElements use can't use other texture coordinates
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, triangles.length, GLES20.GL_UNSIGNED_SHORT, mIndices);
        GLES20.glFlush();
        GLES20.glFinish();

    }

    public static FloatBuffer convertArray(float[] vertices) {
        FloatBuffer vertexData;
        vertexData = ByteBuffer
                .allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);
        vertexData.position(0);
        return vertexData;
    }

    public static ShortBuffer convertArray(short[] indexes) {
        ShortBuffer indexArray;
        indexArray = ByteBuffer
                .allocateDirect(indexes.length * 2)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        indexArray.put(indexes);
        indexArray.position(0);
        return indexArray;
    }

    public static void shaderEffect2dWholeScreen(Point center, Point center2, int texIn, int programId, int poss, int texx, Integer texIn2) {
        GLES20.glUseProgram(programId);
        int uColorLocation = GLES20.glGetUniformLocation(programId, "u_Color");
        GLES20.glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f);

        int uCenter = GLES20.glGetUniformLocation(programId, "uCenter");
        GLES20.glUniform2f(uCenter, (float)center.x, (float)center.y);

        int uCenter2 = GLES20.glGetUniformLocation(programId, "uCenter2");
        GLES20.glUniform2f(uCenter2, (float)center2.x, (float)center2.y);

        FloatBuffer vertexData = convertArray(new float[]{
                -1, -1,
                -1,  1,
                1, -1,
                1,  1
        });

        FloatBuffer texData = convertArray(new float[] {
                0,  0,
                0,  1,
                1,  0,
                1,  1
        });

        GLES20.glVertexAttribPointer(poss, 2, GLES20.GL_FLOAT, false, 0, vertexData);
        GLES20.glVertexAttribPointer(texx,  2, GLES20.GL_FLOAT, false, 0, texData);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texIn);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(programId, "sTexture"), 0);

        if (texIn2 != null) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texIn2);
            GLES20.glUniform1i(GLES20.glGetUniformLocation(programId, "sTexture2"), 1);
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glFlush(); //?
        //GLES20.glFinish();
    }

    public static void shaderEffect2dWholeScreen(PoseHelper.PoseResult poseResult, int texIn, int programId, int poss, int texx, int width, int height) {
        GLES20.glUseProgram(programId);

        float[] points = new float[poseResult.foundLandmarks.length * 2];
        for (int i = 0; i < poseResult.foundLandmarks.length; i++) {
            Point p = poseResult.foundLandmarks[i];
            if (p != null) {
                points[i * 2] = (float)p.x / width;
                points[i * 2 + 1] = (1 - (float)p.y / height);
            }
        }
        GLES20.glUniform2fv(GLES20.glGetUniformLocation(programId, "feauturesFace"), points.length / 2, points, 0);
        GLES20.glUniform2f(GLES20.glGetUniformLocation(programId, "sizeScreen"), width, height);

        FloatBuffer vertexData = convertArray(new float[]{
                -1, -1,
                -1,  1,
                1, -1,
                1,  1
        });

        FloatBuffer texData = convertArray(new float[] {
                0,  0,
                0,  1,
                1,  0,
                1,  1
        });

        GLES20.glVertexAttribPointer(poss, 2, GLES20.GL_FLOAT, false, 0, vertexData);
        GLES20.glVertexAttribPointer(texx,  2, GLES20.GL_FLOAT, false, 0, texData);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texIn);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(programId, "sTexture"), 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glFlush(); //?
    }

}
