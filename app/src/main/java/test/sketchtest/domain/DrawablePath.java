package test.sketchtest.domain;

import java.util.ArrayList;

import test.sketchtest.utilities.DrawingUtility;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;

public class DrawablePath extends Path {

    public static final int CURVED = 0;
    public static final int STRAIGHT_LINE = 1;
    public static final int RECT = 2;
    public static final int TEXT = 3;

    int pathType = CURVED;

    ArrayList<Point> points = new ArrayList<Point>();
    ArrayList<Point> bounds = new ArrayList<Point>();
    String text = null;
    int fontSize = 0;

    public DrawablePath() {
        super();
    }

    public DrawablePath(DrawablePath p) {
        super(p);
        this.text = p.getText();
        this.points = new ArrayList<Point>(p.getPoints());
        this.pathType = new Integer(p.getPathType());
    }

    public DrawablePath(int pathtype) {
        super();

        pathType = pathtype;
    }

    public int getPathType() {
        return pathType;
    }

    public void setPathType(int pathtype) {
        pathType = pathtype;
    }

    @Override
    public void lineTo(float x, float y) {
        super.lineTo(x, y);
    }

    @Override
    public void reset() {
        super.reset();
        points.clear();
    }

    public ArrayList<Point> getPoints() {
        return points;
    }

    public String getText() {
        return text;
    }

    public int getFontSize() {
        return this.fontSize;
    }

    public void setRectangle(Point p1, Point p2, Paint paint) {
        if (paint == null || p2 == null || paint == null)
            throw new IllegalArgumentException("parameters can not be null");

        this.setPoints(true, DrawingUtility.GetPointForRect(p1, p2));

        this.addPoints(p1);

        pathType = RECT;
    }

    public void setText(String _text, Point origin, Paint paint) {
        if (paint == null || _text == null || origin == null)
            throw new IllegalArgumentException("parameters can not be null");

        this.text = _text;
        this.fontSize = (int) paint.getTextSize();
        this.pathType = TEXT;

        Rect rect = new Rect();
        paint.getTextBounds(_text, 0, _text.length(), rect);
        setPoints(true, DrawingUtility.getTextPoints(origin, rect));
        this.bounds = DrawingUtility.getTextPoints(origin, rect);
    }

    public void addPoints(Point... _points) {
        for (Point p : _points) {
            points.add(p);
        }
    }

    public void setPoints(Boolean clear, ArrayList<Point> pts) {
        points = pts;
    }
}
