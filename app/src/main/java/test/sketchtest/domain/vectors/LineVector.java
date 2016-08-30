package test.sketchtest.domain.vectors;

import android.graphics.Point;
import test.sketchtest.domain.DrawablePath;
import test.sketchtest.domain.SerializableToJson;
import test.sketchtest.utilities.SerializeUtility;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by karimmastrobuono on 8/26/14.
 */
public class LineVector implements SerializableToJson {

    ArrayList<Point> points = new ArrayList<Point>();

    public LineVector(DrawablePath p) {
        this.points.addAll(p.getPoints());
    }

    public JSONObject serialize() throws JSONException {

        JSONObject retVal = new JSONObject();
        JSONArray pointsArray = new JSONArray();

        for (int i = 0; i < points.size(); i++) {
            pointsArray.put(SerializeUtility.getJsonArray(points.get(i)));
        }

        retVal.put("points", pointsArray);

        return retVal;
    }
}
