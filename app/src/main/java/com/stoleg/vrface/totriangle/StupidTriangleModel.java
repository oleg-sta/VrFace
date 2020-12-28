package com.stoleg.vrface.totriangle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.stoleg.vrface.model.primitives.Line;
import com.stoleg.vrface.model.primitives.Point;
import com.stoleg.vrface.model.primitives.Triangle;

/**
 *
 * @author sov
 * 
 */
public class StupidTriangleModel implements Triangulation {

    /**
     *
     * @param points
     * @param lines
     */
    public Line[] convertToTriangle(Point[] points, Line[] lines) {
        int i1 = 0, j1 = 0;
        boolean wasLine = true;
        while (wasLine) {
            wasLine = false;
            for (int i = 0; i < points.length; i++) {
                Point pointCheck = points[i];
                for (int j = 0; j < points.length; j++) {
                    Point toPoint = points[j];
                    if (i != j) {
                        boolean needJoin = true;
                        for (Line line : lines) {
                            if ((line.pointStart == i && line.pointEnd == j)
                                    || (line.pointStart == j && line.pointEnd == i)) {
                                needJoin = false;
                                continue;
                            }
                            Point poL1 = points[line.pointStart];
                            Point poL2 = points[line.pointEnd];
                            double divider1 = (poL2.x - poL1.x) * (toPoint.y - pointCheck.y) - (poL2.y - poL1.y)
                                    * (toPoint.x - pointCheck.x);
                            if (divider1 != 0) {
                                double divident1 = (poL2.y - poL1.y) * (pointCheck.x - poL1.x)
                                        + (poL1.y - pointCheck.y) * (poL2.x - poL1.x);
                                double divident2 = (toPoint.y - pointCheck.y) * (poL1.x - pointCheck.x)
                                        + (pointCheck.y - poL1.y) * (toPoint.x - pointCheck.x);
                                double k1 = divident1 / divider1;
                                double k2 = -divident2 / divider1;
                                if (k1 > 0 && k1 < 1 && k2 > 0 && k2 < 1) {
                                    needJoin = false;
                                }
                            } else {
                                needJoin = false;
                            }
                        }
                        if (needJoin) {
                            if (!wasLine) {
                                i1 = i;
                                j1 = j;
                                wasLine = true;
                            } else {
                                if ((Math.pow(points[i].x - points[j].x, 2) + Math.pow(points[i].y - points[j].y, 2)) < (Math.pow(points[i1].x - points[j1].x, 2) + Math.pow(points[i1].y - points[j1].y, 2))) {
                                    i1 = i;
                                    j1 = j;
                                    wasLine = true;
                                }
                            }
                        }
                    }
                }
            }
            if (wasLine) {
                Line[] lineTemp = lines;
                lines = new Line[lineTemp.length + 1];
                for (int k = 0; k < lineTemp.length; k++) {
                    lines[k] = lineTemp[k];
                }
                lines[lineTemp.length] = new Line(i1, j1);
            }
        }
        return lines;
    }
    
    public static Triangle[] getTriagles(Point[] points, Line[] lines) {
        List<Triangle> triangles = new ArrayList<Triangle>();
        for (int i = 0; i < lines.length; i++) {
            for (int j = i + 1; j < lines.length; j++) {
                for (int k = j + 1; k < lines.length; k++) {
                    Set<Integer> pointsTr = new HashSet<Integer>();
                    pointsTr.add(lines[i].pointStart);
                    pointsTr.add(lines[i].pointEnd);
                    pointsTr.add(lines[j].pointStart);
                    pointsTr.add(lines[j].pointEnd);
                    pointsTr.add(lines[k].pointStart);
                    pointsTr.add(lines[k].pointEnd);
                    if (pointsTr.size() == 3) {
                        Integer[] po = pointsTr.toArray(new Integer[0]);
                        triangles.add(new Triangle(po[0], po[1], po[2]));
                    }
                }
            }
        }
        return triangles.toArray(new Triangle[0]);
    }
}
