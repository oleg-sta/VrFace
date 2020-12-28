package com.stoleg.vrface.model;

import com.stoleg.vrface.model.primitives.Line;
import com.stoleg.vrface.model.primitives.Point;

public interface SimpleModel {
    Point[] getPointsWas();
    Point[] getPointsTo();
    Line[] getLines();
    

}
