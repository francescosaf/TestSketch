package test.sketchtest.domain;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by karimmastrobuono on 8/26/14.
 */
public interface SerializableToJson {
    public JSONObject serialize() throws JSONException;
}
