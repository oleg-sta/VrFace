package com.stoleg.vrface.model3d;

import android.content.Context;

/**
 * Created by sov on 10.12.2016.
 */
// FIXME to many errrors
// FIXME textures loaded incorrectly, case format obj is slightlu different then we excect, whe should create new vertices
// 3d model holder
public class ModelNew extends Model {

    public ModelNew(int modelID, Context activity) {
        super(modelID, activity);

    }
    protected void fillInBuffers() {

        int facesSize = faces.size();

        vertexCount = facesSize * 3;
        tempV = new float[vertexCount * 3];
        tempVt = new float[vertexCount * 2];

        // stubs
        tempVn = new float[1];
        indices = new short[1];

        for (int i = 0; i < faces.size(); i++) {
            Face face = faces.get(i);
            tempV[i * 9] = vertices.get(face.indices.get(0)).getX();
            tempV[i * 9 + 1] = vertices.get(face.indices.get(0)).getY();
            tempV[i * 9 + 2] = vertices.get(face.indices.get(0)).getZ();
            tempV[i * 9 + 3] = vertices.get(face.indices.get(1)).getX();
            tempV[i * 9 + 4] = vertices.get(face.indices.get(1)).getY();
            tempV[i * 9 + 5] = vertices.get(face.indices.get(1)).getZ();
            tempV[i * 9 + 6] = vertices.get(face.indices.get(2)).getX();
            tempV[i * 9 + 7] = vertices.get(face.indices.get(2)).getY();
            tempV[i * 9 + 8] = vertices.get(face.indices.get(2)).getZ();

            // textures
            tempVt[i * 6] = vertexTexture.get(face.textures.get(0)).getX();
            tempVt[i * 6 + 1] = vertexTexture.get(face.textures.get(0)).getY();
            tempVt[i * 6 + 2] = vertexTexture.get(face.textures.get(1)).getX();
            tempVt[i * 6 + 3] = vertexTexture.get(face.textures.get(1)).getY();
            tempVt[i * 6 + 4] = vertexTexture.get(face.textures.get(2)).getX();
            tempVt[i * 6 + 5] = vertexTexture.get(face.textures.get(2)).getY();
        }


        recalcV();
        calcBuffer();

    }


}
