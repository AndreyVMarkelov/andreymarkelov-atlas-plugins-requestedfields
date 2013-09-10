package ru.andreymarkelov.atlas.plugins.requestedfiedls;

import java.util.List;

public class HttpRunnerData {
    private List<String> vals;
    private String error;
    private String rawData;

    public String getError() {
        return error;
    }

    public String getRawData() {
        return rawData;
    }

    public List<String> getVals() {
        return vals;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public void setVals(List<String> vals) {
        this.vals = vals;
    }

    @Override
    
    public String toString() {
        return "HttpRunnerData[vals=" + vals + ", error=" + error + ", rawData=" + rawData + "]";
    }
}
