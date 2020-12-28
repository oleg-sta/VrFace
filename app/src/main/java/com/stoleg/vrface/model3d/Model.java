package com.stoleg.vrface.model3d;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import com.stoleg.vrface.Static;

/**
 * Created by sov on 10.12.2016.
 */
// FIXME to many errrors
// FIXME textures loaded incorrectly, case format obj is slightlu different then we excect, whe should create new vertices
// 3d model holder
public class Model {

    // Constants
    public static final int FLOAT_SIZE_BYTES = 4;
    public static final int SHORT_SIZE_BYTES = 2;

    private FloatBuffer _vb;
    private FloatBuffer _nb;
    private ShortBuffer _ib;
    private FloatBuffer _tcb;

    public short[] indices;

    public float[] tempV;
    protected float[] tempVt;
    protected float[] tempVn;

    protected ArrayList<Vector3D> vertices;
    protected ArrayList<Vector3D> vertexTexture;
    private ArrayList<Vector3D> vertexNormal;
    protected ArrayList<Face> faces;

    public int getVertexCount() {
        return vertexCount;
    }

    protected int vertexCount;

    private ArrayList<GroupObject> groupObjects;

    class Vector3D {

        float x, y, z;

        public Vector3D(float parseFloat, float parseFloat2, float parseFloat3) {
            x = parseFloat;
            y = parseFloat2;
            z = parseFloat3;
        }

        public float getX() {

            return x;
        }

        public float getY() {

            return y;
        }

        public float getZ() {

            return z;
        }



    }

    class Face {

        public ArrayList<Integer> indices = new ArrayList<Integer>();
        public ArrayList<Integer> textures = new ArrayList<Integer>();

        public ArrayList<Vector3D> getVertices() {

            return vertices;
        }

        public ArrayList<Vector3D> getUvws() {

            return vertexTexture;
        }

        public ArrayList<Vector3D> getNormals() {

            return vertexNormal;
        }

    }

    class GroupObject {

        String objectName;

        public void setObjectName(String string) {
            this.objectName = string;
        }

        public String getObjectName() {
            return objectName;
        }

    }

    // Android Stuff!
    private Context context;
    private int modelID;

    public Model(int modelID, Context activity) {
        this.vertices = new ArrayList<Vector3D>();
        this.vertexTexture = new ArrayList<Vector3D>();
        this.vertexNormal = new ArrayList<Vector3D>();
        this.faces = new ArrayList<Face>();

        this.groupObjects = new ArrayList<GroupObject>();

        this.modelID = modelID;
        this.context = activity;

        loadFile();
    }

    public void saveModel(String fileName) {
        try {
            OutputStream out = new FileOutputStream(fileName);
            out.write(("o MeanShape222\r\n").getBytes("cp866"));
            for (int i = 0; i < tempV.length / 3; i++) {
                out.write(("v " + String.format("%.5f", tempV[i * 3]) + " "  + String.format("%.5f", tempV[i * 3 + 1]) + " "  + String.format("%.5f", tempV[i * 3 + 2]) + "\r\n").getBytes("cp866"));
            }
            for (int i = 0; i < indices.length / 3; i++) {
                out.write(("f " + indices[i * 3] + " "  + indices[i * 3 + 1] + " "  + indices[i * 3 + 2] + "\r\n").getBytes("cp866"));
            }
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private int loadFile() {
        InputStream inputStream = context.getResources().openRawResource(
                modelID);

        BufferedReader in = new BufferedReader(new InputStreamReader(
                inputStream));

        try {
            loadOBJ(in);
            Log.d("LOADING FILE", "FILE LOADED SUCESSFULLY====================");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 1;
    }

    private void loadOBJ(BufferedReader in) throws IOException {
        if (Static.LOG_MODE) Log.d("LOADING FILE", "STARTING!====================");
        GroupObject defaultObject = new GroupObject();
        GroupObject currentObject = defaultObject;

        groupObjects.add(defaultObject);

        String Line; // Stores ever line we read!
        String[] Blocks; // Stores string fragments after the split!!
        String CommandBlock; // Stores Command Blocks such as: v, vt, vn, g,
        // etc...

        while ((Line = in.readLine()) != null) {
            Blocks = Line.split(" ");
            CommandBlock = Blocks[0];

            // Log.d("COMMAND BLOCK" , "---------- " + CommandBlock +
            // " ----------");

            if (CommandBlock.equals("g")) {
                if (Blocks[1] == "default")
                    currentObject = defaultObject;
                else {
                    GroupObject groupObject = new GroupObject();
                    groupObject.setObjectName(Blocks[1]);
                    currentObject = groupObject;
                    groupObjects.add(groupObject);
                }
            }

            if (CommandBlock.equals("v")) {
                Vector3D vertex = new Vector3D(Float.parseFloat(Blocks[1]),
                        Float.parseFloat(Blocks[2]),
                        Float.parseFloat(Blocks[3]));
                this.vertices.add(vertex);
                // Log.d("VERTEX DATA", " " + vertex.getX() + ", " +
                // vertex.getY() + ", " + vertex.getZ());
            }

            if (CommandBlock.equals("vt")) {
                Vector3D vertexTex = new Vector3D(Float.parseFloat(Blocks[1]),
                        1 - Float.parseFloat(Blocks[2]), 0.0f);
                this.vertexTexture.add(vertexTex);
                // Log.d("TEXTURE DATA", " " + vertexTex.getX() + ", " +
                // vertexTex.getY() + ", " + vertexTex.getZ());
            }

            if (CommandBlock.equals("vn")) {
                Vector3D vertexNorm = new Vector3D(Float.parseFloat(Blocks[1]),
                        Float.parseFloat(Blocks[2]),
                        Float.parseFloat(Blocks[3]));
                this.vertexNormal.add(vertexNorm);
                // Log.d("NORMAL DATA", " " + vertexNorm.getX() + ", " +
                // vertexNorm.getY() + ", " + vertexNorm.getZ());
            }

            if (CommandBlock.equals("f")) {
                Face face = new Face();
                faces.add(face);

                String[] faceParams;

                for (int i = 1; i < Blocks.length; i++) {
                    faceParams = Blocks[i].split("/");

                    face.indices.add(Integer
                            .parseInt(faceParams[0]) - 1);
//                    face.getVertices()
//                            .add(this.vertices.get(Integer
//                                    .parseInt(faceParams[0]) - 1));

                    if ("".equals(faceParams[1])) {
                    } else {
                        face.textures.add(Integer
                                .parseInt(faceParams[1]) - 1);

                        // some bug
//                        face.getUvws().add(
//                                this.vertexTexture.get(Integer
//                                        .parseInt(faceParams[1]) - 1));
                        face.getNormals().add(
                                this.vertexNormal.get(Integer
                                        .parseInt(faceParams[2]) - 1));
                    }
                }
            }
        }

         fillInBuffers();
//        fillInBuffersWithNormals();

        Log.d("OBJ OBJECT DATA", "V = " + vertices.size() + " VN = "
                + vertexTexture.size() + " VT = " + vertexNormal.size());

    }

    protected void fillInBuffers() {

        int facesSize = faces.size();

        vertexCount = facesSize * 3;

        tempV = new float[vertices.size() * 3];
        tempVt = new float[vertices.size() * 2]; // FIXME textured coordinates should have their own size
        indices = new short[facesSize * 3];

        for (int i = 0; i < vertices.size(); i++) {
            tempV[i * 3] = vertices.get(i).getX();
            tempV[i * 3 + 1] = vertices.get(i).getY();
            tempV[i * 3 + 2] = vertices.get(i).getZ();
        }
        for (int i = 0; i < vertexTexture.size(); i++) {
            // FIXME textures are not the same number as vertices
//            tempVt[i * 2] = vertexTexture.get(i).getX();
//            tempVt[i * 2 + 1] = vertexTexture.get(i).getY();
        }

        for (int i = 0; i < facesSize; i++) {
            Face face = faces.get(i);
            indices[i * 3] = (short) (face.indices.get(0).intValue());
            indices[i * 3 + 1] = (short) (face.indices.get(1).intValue());
            indices[i * 3 + 2] = (short) (face.indices.get(2).intValue());

            // FIXME fix textures coordinates
            tempVt[indices[i * 3] * 2] = getX(vertexTexture, face.textures.get(0).intValue());
            tempVt[indices[i * 3] * 2 + 1] = getY(vertexTexture, face.textures.get(0).intValue());
            tempVt[indices[i * 3 + 1] * 2] = getX(vertexTexture, face.textures.get(1).intValue());
            tempVt[indices[i * 3 + 1] * 2 + 1] = getY(vertexTexture, face.textures.get(1).intValue());
            tempVt[indices[i * 3 + 2] * 2] = getX(vertexTexture, face.textures.get(2).intValue());
            tempVt[indices[i * 3 + 2] * 2 + 1] = getY(vertexTexture, face.textures.get(2).intValue());
        }

        for (int i = 0; i < facesSize && false; i++) {
            if (Static.LOG_MODE) Log.d("OBJ OBJECT DATA", "fillInBuffers" + i);
            Face face = faces.get(i);
            tempV[i * 9] = face.getVertices().get(0).getX();
            tempV[i * 9 + 1] = face.getVertices().get(0).getY();
            tempV[i * 9 + 2] = face.getVertices().get(0).getZ();
            tempV[i * 9 + 3] = face.getVertices().get(1).getX();
            tempV[i * 9 + 4] = face.getVertices().get(1).getY();
            tempV[i * 9 + 5] = face.getVertices().get(1).getZ();
            tempV[i * 9 + 6] = face.getVertices().get(2).getX();
            tempV[i * 9 + 7] = face.getVertices().get(2).getY();
            tempV[i * 9 + 8] = face.getVertices().get(2).getZ();
            tempVt[i * 6] = face.getUvws().get(0).getX();
            tempVt[i * 6 + 1] = face.getUvws().get(0).getY();
            tempVt[i * 6 + 2] = face.getUvws().get(1).getX();
            tempVt[i * 6 + 3] = face.getUvws().get(1).getY();
            tempVt[i * 6 + 4] = face.getUvws().get(2).getX();
            tempVt[i * 6 + 5] = face.getUvws().get(2).getY();
            indices[i * 3] = (short) (i * 3);
            indices[i * 3 + 1] = (short) (i * 3 + 1);
            indices[i * 3 + 2] = (short) (i * 3 + 2);
        }

        recalcV();
        calcBuffer();
    }

    // little hack while textures not fixed
    private float getX(ArrayList<Vector3D> vertexes, int faceNum) {
        Vector3D e = vertexTexture.get(faceNum);
        if (e != null) {
            return e.getX();
        }
        return 0;
    }
    private float getY(ArrayList<Vector3D> vertexes, int faceNum) {
        Vector3D e = vertexTexture.get(faceNum);
        if (e != null) {
            return e.getY();
        }
        return 0;
    }

    private void fillInBuffersWithNormals() {

        int facesSize = faces.size();

        vertexCount = facesSize * 3;

        tempV = new float[facesSize * 3 * 3];
        tempVt = new float[facesSize * 2 * 3];
        tempVn = new float[facesSize * 3 * 3];
        indices = new short[facesSize * 3];

        for (int i = 0; i < facesSize; i++) {
            if (Static.LOG_MODE) Log.d("OBJ OBJECT DATA", "fillInBuffers" + i);
            Face face = faces.get(i);
            tempV[i * 9] = face.getVertices().get(0).getX();
            tempV[i * 9 + 1] = face.getVertices().get(0).getY();
            tempV[i * 9 + 2] = face.getVertices().get(0).getZ();
            tempV[i * 9 + 3] = face.getVertices().get(1).getX();
            tempV[i * 9 + 4] = face.getVertices().get(1).getY();
            tempV[i * 9 + 5] = face.getVertices().get(1).getZ();
            tempV[i * 9 + 6] = face.getVertices().get(2).getX();
            tempV[i * 9 + 7] = face.getVertices().get(2).getY();
            tempV[i * 9 + 8] = face.getVertices().get(2).getZ();

            tempVn[i * 9] = face.getNormals().get(0).getX();
            tempVn[i * 9 + 1] = face.getNormals().get(0).getY();
            tempVn[i * 9 + 2] = face.getNormals().get(0).getZ();
            tempVn[i * 9 + 3] = face.getNormals().get(1).getX();
            tempVn[i * 9 + 4] = face.getNormals().get(1).getY();
            tempVn[i * 9 + 5] = face.getNormals().get(1).getZ();
            tempVn[i * 9 + 6] = face.getNormals().get(2).getX();
            tempVn[i * 9 + 7] = face.getNormals().get(2).getY();
            tempVn[i * 9 + 8] = face.getNormals().get(2).getZ();

//            tempVt[i * 6] = face.getUvws().get(0).getX();
//            tempVt[i * 6 + 1] = face.getUvws().get(0).getY();
//            tempVt[i * 6 + 2] = face.getUvws().get(1).getX();
//            tempVt[i * 6 + 3] = face.getUvws().get(1).getY();
//            tempVt[i * 6 + 4] = face.getUvws().get(2).getX();
//            tempVt[i * 6 + 5] = face.getUvws().get(2).getY();
            tempVt[i * 6] = 0;
            tempVt[i * 6 + 1] = 0;
            tempVt[i * 6 + 2] = 0;
            tempVt[i * 6 + 3] = 0;
            tempVt[i * 6 + 4] = 0;
            tempVt[i * 6 + 5] = 0;

            indices[i * 3] = (short) (i * 3);
            indices[i * 3 + 1] = (short) (i * 3 + 1);
            indices[i * 3 + 2] = (short) (i * 3 + 2);
        }

        recalcV();
        calcBufferN();
    }

    protected void calcBuffer() {
        _tcb = ByteBuffer.allocateDirect(tempVt.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        _tcb.put(tempVt);
        _tcb.position(0);

        _ib = ByteBuffer.allocateDirect(indices.length * SHORT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        _ib.put(indices);
        _ib.position(0);
    }
    protected void calcBufferN() {
        _tcb = ByteBuffer.allocateDirect(tempVt.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        _tcb.put(tempVt);
        _tcb.position(0);

        _nb = ByteBuffer.allocateDirect(tempVn.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        _nb.put(tempVn);
        _nb.position(0);

        _ib = ByteBuffer.allocateDirect(indices.length * SHORT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        _ib.put(indices);
        _ib.position(0);
    }

    public void recalcV() {
        _vb = ByteBuffer.allocateDirect(tempV.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        _vb.put(tempV);
        _vb.position(0);
    }

    public FloatBuffer getVertices() {
        return _vb;
    }

    public FloatBuffer getTexCoords() {
        return _tcb;
    }

    public ShortBuffer getIndices() {
        return _ib;
    }

    public int getIndicesCount() {
        return indices.length;
    }

    public FloatBuffer getNormals() {
        return _nb;
    }

}
