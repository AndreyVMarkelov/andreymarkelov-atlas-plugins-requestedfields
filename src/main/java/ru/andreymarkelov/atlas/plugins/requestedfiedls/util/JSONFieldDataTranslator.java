package ru.andreymarkelov.atlas.plugins.requestedfiedls.util;

import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import org.apache.log4j.Logger;
import ru.andreymarkelov.atlas.plugins.requestedfiedls.model.JSONFieldData;

public class JSONFieldDataTranslator {
    private static final Logger logger = Logger.getLogger(JSONFieldDataTranslator.class);

    public static JSONFieldData JSONFieldDataFromString(String json) {
        try {
            JSONObject jsonObj = new JSONObject(json);
            JSONFieldData data = new JSONFieldData();
            data.setUrl(jsonObj.getString("url"));
            data.setUser(jsonObj.getString("user"));
            data.setPassword(jsonObj.getString("password"));
            data.setReqType(jsonObj.getString("reqType"));
            if (jsonObj.has("reqHeaders")) {
                data.setReqHeaders(jsonObj.getString("reqHeaders"));
            }
            data.setReqData(jsonObj.getString("reqData"));
            data.setReqPath(jsonObj.getString("reqPath"));
            data.setReqDataType(jsonObj.getString("reqDataType"));
            return data;
        } catch (JSONException e) {
            logger.error("Error parse JSON", e);
            return null;
        }
    }

    public static String JSONFieldDataToString(JSONFieldData obj) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("url", obj.getUrl());
            jsonObj.put("user", obj.getUser());
            jsonObj.put("password", obj.getPassword());
            jsonObj.put("reqType", obj.getReqType());
            jsonObj.put("reqHeaders", obj.getReqHeaders());
            jsonObj.put("reqData", obj.getReqData());
            jsonObj.put("reqPath", obj.getReqPath());
            jsonObj.put("reqDataType", obj.getReqDataType());
        } catch (JSONException e) {
            logger.error("Error write JSON", e);
            return null;
        }
        return jsonObj.toString();
    }

    private JSONFieldDataTranslator() {
    }
}
