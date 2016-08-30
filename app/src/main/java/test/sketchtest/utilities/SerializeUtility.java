package test.sketchtest.utilities;

import android.graphics.Point;
import org.json.JSONArray;

/**
 * Created by karimmastrobuono on 8/27/14.
 */
public class SerializeUtility {

    public static JSONArray getJsonArray(Point p) {
        
        JSONArray retVal = new JSONArray();

        retVal.put(p.x);
        retVal.put(p.y);

        return retVal;
    }
}
