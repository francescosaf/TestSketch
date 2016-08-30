package test.sketchtest.domain;

import test.sketchtest.domain.vectors.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by karimmastrobuono on 8/26/14.
 */
public class Sketch implements SerializableToJson {

//    public ArrayList<StraightLineVector> straightLines = new ArrayList<StraightLineVector>();
//    public ArrayList<SerializableToJson> lines = new ArrayList<SerializableToJson>();
//    public ArrayList<SerializableToJson> squares = new ArrayList<SerializableToJson>();
//    public ArrayList<TextVector> texts = new ArrayList<TextVector>();

    public ArrayList<SerializableToJson> vectors = new ArrayList<SerializableToJson>();

    public Sketch(ArrayList<DrawablePath> paths) {

        if (paths == null)
            throw new IllegalArgumentException();

        for (DrawablePath path : paths) {
            switch (path.getPathType()) {
                case DrawablePath.TEXT:
                    this.vectors.add(new TextVector(path));
                    break;
                case DrawablePath.RECT:
                    this.vectors.add(new RectVector(path));
                    break;
                case DrawablePath.STRAIGHT_LINE:
                case DrawablePath.CURVED:
                    this.vectors.add(new LineVector(path));
                    break;
            }
        }
    }

    public JSONObject serialize() throws JSONException {

        JSONObject retVal = new JSONObject();

        JSONArray textsJson = new JSONArray();
        JSONArray linesJson = new JSONArray();
        JSONArray rectsJson = new JSONArray();

        for (SerializableToJson s : this.vectors) {
            if (s instanceof LineVector) {
                linesJson.put(s.serialize());
            } else if (s instanceof TextVector) {
                textsJson.put(s.serialize());
            } else if (s instanceof RectVector) {
                rectsJson.put(s.serialize());
            }
        }

        retVal.put("texts", textsJson);
        retVal.put("paths", linesJson);
        retVal.put("rects", rectsJson);

        return retVal;
    }
}
