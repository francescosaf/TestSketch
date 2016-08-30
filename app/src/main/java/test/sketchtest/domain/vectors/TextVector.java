package test.sketchtest.domain.vectors;

import android.graphics.Point;
import test.sketchtest.domain.DrawablePath;
import test.sketchtest.domain.SerializableToJson;
import test.sketchtest.utilities.SerializeUtility;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by karimmastrobuono on 8/26/14.
 */
public class TextVector implements SerializableToJson {
    String text;
    int fontSize;
    Point topLeft;

    public TextVector(DrawablePath path) {

        if (path.getPathType() != DrawablePath.TEXT)
            throw new IllegalArgumentException();

        this.topLeft = path.getPoints().get(0);
        this.text = path.getText();
        this.fontSize = path.getFontSize();
    }

    public JSONObject serialize() throws JSONException {

        JSONObject retVal = new JSONObject();

        retVal.put("text", this.text);
        retVal.put("fontSize", this.fontSize);
        retVal.put("topLeft", SerializeUtility.getJsonArray(this.topLeft));

        return retVal;
    }
}
