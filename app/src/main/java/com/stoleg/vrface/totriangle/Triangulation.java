package com.stoleg.vrface.totriangle;

import com.stoleg.vrface.model.primitives.Line;
import com.stoleg.vrface.model.primitives.Point;

public interface Triangulation {
    
    public Line[] convertToTriangle(Point[] points, Line[] lines);

}
