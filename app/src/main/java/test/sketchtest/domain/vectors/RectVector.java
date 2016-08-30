package test.sketchtest.domain.vectors;

import java.util.ArrayList;
import android.graphics.Point;
import test.sketchtest.domain.DrawablePath;
import test.sketchtest.domain.SerializableToJson;
import test.sketchtest.utilities.SerializeUtility;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by karimmastrobuono on 8/26/14.
 */
public class RectVector implements SerializableToJson {

    Point topLeft;
    Point bottomRight;

    public RectVector(DrawablePath p) {

        if (p == null)
            throw new IllegalArgumentException();

        topLeft = findTopLeft(p.getPoints());
        bottomRight = this.findBottomRight(p.getPoints());
    }

    public JSONObject serialize() throws JSONException {

        JSONObject retVal = new JSONObject();

        retVal.put("topLeft", SerializeUtility.getJsonArray(topLeft));
        retVal.put("bottomRight", SerializeUtility.getJsonArray(bottomRight));

        return retVal;
    }

    private Point findTopLeft(ArrayList<Point> points) {
        Point tl = points.get(0);
        for (Point p : points) {
            if (p.x < tl.x && p.y < tl.y) {
                tl = p;
            }
        }
        return tl;
    }

    private Point findBottomRight(ArrayList<Point> points) {
        Point br = points.get(0);
        for (Point p : points) {
            if (p.x > br.x && p.y > br.y) {
                br = p;
            }
        }
        return br;
    }
}
