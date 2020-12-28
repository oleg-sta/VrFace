package com.stoleg.vrface.utils;

/**
 * Created by sov on 04.01.2017.
 */

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.stoleg.vrface.Static;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;


public class ShaderUtils {

    public static int createProgram(int vertexShaderId, int fragmentShaderId) {

        final int programId = glCreateProgram();
        if (programId == 0) {
            return 0;
        }

        glAttachShader(programId, vertexShaderId);
        glAttachShader(programId, fragmentShaderId);

        glLinkProgram(programId);
        final int[] linkStatus = new int[1];
        glGetProgramiv(programId, GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            glDeleteProgram(programId);
            throw new RuntimeException("couldnt' create program");
        }
        return programId;

    }

    public static int createShader(Context context, int type, int shaderRawId) {
        String shaderText = FileUtils
                .readTextFromRaw(context, shaderRawId);
        return ShaderUtils.createShader(type, shaderText);
    }

    public static int createShader(int type, String shaderText) {
        if (Static.LOG_MODE) Log.i("ShaderUtils", "createShader");
        final int shaderId = glCreateShader(type);
        if (shaderId == 0) {
            return 0;
        }
        glShaderSource(shaderId, shaderText);
        glCompileShader(shaderId);
        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderId, GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            if (Static.LOG_MODE) Log.i("ShaderUtils", "error '"  + GLES20.glGetShaderInfoLog(shaderId) + "' compile shader " + shaderText);
            glDeleteShader(shaderId);
            throw new RuntimeException("error while compile shader");
        }
        return shaderId;
    }

}
