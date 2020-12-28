package com.stoleg.vrface.totriangle;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.MatOfFloat6;
import org.opencv.core.Rect;
import org.opencv.imgproc.Subdiv2D;

import com.stoleg.vrface.model.primitives.Line;
import com.stoleg.vrface.model.primitives.Point;

public class DelaunayTriangulation implements Triangulation {
    
    
    // FIXME
    public Line[] convertToTriangle(Point[] points, Line[] lines) {
        List<Line> res = new ArrayList<Line>(); 
        Subdiv2D dc = new Subdiv2D(new Rect(0, 0, 1024, 1024));
        for (int i = 0; i < points.length; i++) {
            dc.insert(new org.opencv.core.Point(points[i].x, points[i].y));
        }
        MatOfFloat6 triangleList = new MatOfFloat6();
        dc.getTriangleList(triangleList);
        float[] floats = triangleList.toArray();
        for (int indexTriangle = 0; indexTriangle < floats.length; indexTriangle = indexTriangle + 6) {
            int point1 = getPoint(points, floats[indexTriangle], floats[indexTriangle + 1]);
            int point2 = getPoint(points, floats[indexTriangle + 2], floats[indexTriangle + 3]);
            int point3 = getPoint(points, floats[indexTriangle + 4], floats[indexTriangle + 5]);
            if (point1 < 0 || point2 < 0 || point3 < 0) {
                continue;
            }
            addIfNeeded(res, point1, point2);
            addIfNeeded(res, point2, point3);
            addIfNeeded(res, point3, point1);
        }
        
        
        return res.toArray(new Line[0]);
    }

    private static int getPoint(Point[] points, float f, float g) {
        for (int i = 0; i < points.length; i++) {
            if (points[i].x == f && points[i].y == g) {
                return i;
            }
        }
        return -1;
    }

    private static void addIfNeeded(List<Line> res, int p1, int p2) {
        Line newLine = new Line(p1, p2);
        for (Line line : res) {
            if (line.same(newLine)) {
                return;
            }
        }
        res.add(newLine);
    }

}

