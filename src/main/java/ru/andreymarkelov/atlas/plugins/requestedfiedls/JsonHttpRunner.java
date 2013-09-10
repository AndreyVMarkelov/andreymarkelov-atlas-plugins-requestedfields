package ru.andreymarkelov.atlas.plugins.requestedfiedls;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nebhale.jsonpath.JsonPath;

public class JsonHttpRunner {
    private static final Logger log = LoggerFactory.getLogger(JsonHttpRunner.class);

    private JSONFieldData data;
    private Object defValue;

    public JsonHttpRunner(JSONFieldData data, Object defValue) {
        this.data = data;
        this.defValue = defValue;
    }

    public HttpRunnerData getData() {
        HttpRunnerData res = new HttpRunnerData();

        try {
            HttpSender httpService = new HttpSender(data.getUrl(), data.getReqType(), data.getReqDataType(), data.getUser(), data.getPassword());
            String json = httpService.call(data.getReqData());
            JsonPath namePath = JsonPath.compile(data.getReqPath());
            List<String> vals = namePath.read(json, List.class);

            if (defValue != null) {
                vals.add(0, defValue.toString());
            }

            res.setRawData(json);
            if (vals != null) {
                if (!vals.isEmpty()) {
                    Collections.sort(vals);
                }
                res.setVals(vals);
            }
        } catch (Throwable th) {
            log.error("JsonHttpRunner::getData - error renderring", th);
            res.setError(th.getMessage());
        }

        return res;
    }
}