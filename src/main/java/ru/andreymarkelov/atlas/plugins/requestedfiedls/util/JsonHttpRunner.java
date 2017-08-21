package ru.andreymarkelov.atlas.plugins.requestedfiedls.util;

import com.nebhale.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.andreymarkelov.atlas.plugins.requestedfiedls.model.HttpRunnerData;
import ru.andreymarkelov.atlas.plugins.requestedfiedls.model.JSONFieldData;

import java.util.Collections;
import java.util.List;

public class JsonHttpRunner {
    private static final Logger log = LoggerFactory.getLogger(JsonHttpRunner.class);

    private JSONFieldData data;
    private Object defValue;

    public JsonHttpRunner(JSONFieldData data, Object defValue) {
        this.data = data;
        this.defValue = defValue;
    }

    @SuppressWarnings("unchecked")
    public HttpRunnerData getData() {
        HttpRunnerData res = new HttpRunnerData();
        try {
            HttpSender httpService = new HttpSender(data.getUrl(), data.getReqType(), data.getReqDataType(), data.getUser(), data.getPassword());
            String json = httpService.call(data.getReqHeaders(), data.getReqData());
            JsonPath namePath = JsonPath.compile(data.getReqPath());
            List<String> values = namePath.read(json, List.class);

            if (defValue != null) {
                values.add(0, defValue.toString());
            }

            res.setRawData(json);
            if (values != null) {
                if (!values.isEmpty()) {
                    Collections.sort(values);
                }
                res.setVals(values);
            }
        } catch (Throwable th) {
            log.error("JsonHttpRunner::getData - error renderring", th);
            res.setError(th.getMessage());
        }

        return res;
    }
}
