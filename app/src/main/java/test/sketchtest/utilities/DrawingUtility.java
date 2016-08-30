package test.sketchtest.utilities;

import java.util.ArrayList;
import java.util.Arrays;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

public class DrawingUtility {

    public static double DistanceToPoint(Point v, Point w) {
        return Math.sqrt((Math.pow(v.x - w.x, 2) + Math.pow(v.y - w.y, 2)));
    }

    public static ArrayList<Point> GetPointForRect(Point p1, Point p2) {
        if (p1 == null || p2 == null)
            throw new IllegalArgumentException("parameters can not be null");

        return new ArrayList<Point>(Arrays.asList(p1, new Point(p2.x, p1.y), p2, new Point(p1.x, p2.y))); // (top left, top right, bottom right, bottom left)
    }

    static void logVec(String name, Point v) {
        Log.d("TEST POINT " + name, v.x + ", " + v.y);
    }

    public static double DistanceToSegmentSq(Point p1, Point p2,
                                             Point touchPoint) {
        Point w = subPoint(p1, p2);
        double wLenSqInv = 1 / dot(w, w);

        Point p1ToTouchPoint = subPoint(p1, touchPoint);
        Point p2ToTouchPoint = subPoint(p2, touchPoint);
        double gm1 = dot(p1ToTouchPoint, w) * wLenSqInv;
        double gm2 = dot(p2ToTouchPoint, w) * wLenSqInv;

        if (gm1 < 0) {
            return dot(p1ToTouchPoint, p1ToTouchPoint);
        } else if (gm2 > 0) {
            return dot(p2ToTouchPoint, p2ToTouchPoint);
        }

        Point tpPrj = addPoint(p1, scalePoint(w, gm1));

        Point v = subPoint(tpPrj, touchPoint);
        return dot(v, v);
    }

    static Point addPoint(Point p1, Point p2) {
        return new Point((int) p1.x + p2.x, (int) p1.y + p2.y);
    }

    static Point scalePoint(Point p, double d) {
        Point retVal = new Point();

        retVal.x = (int) (p.x * d);
        retVal.y = (int) (p.y * d);

        return retVal;
    }

    public static ArrayList<Point> getTextPoints(Point origin, Rect bounds) {
        ArrayList<Point> arrayList = new ArrayList<Point>();

        arrayList.add(origin);// origin
        arrayList.add(new Point(origin.x + bounds.width(), origin.y));// top
        // right
        arrayList.add(new Point(origin.x + bounds.width(), origin.y
                + bounds.height()));// bottom right
        arrayList.add(new Point(origin.x, origin.y + bounds.height()));// bottom
        // left

        return arrayList;
    }

    static double dot(Point p1, Point p2) {
        return p1.x * p2.x + p1.y * p2.y;
    }

    static Point subPoint(Point p1, Point p2) {
        Point retVal = new Point();

        retVal.x = p2.x - p1.x;
        retVal.y = p2.y - p1.y;

        return retVal;
    }
}
