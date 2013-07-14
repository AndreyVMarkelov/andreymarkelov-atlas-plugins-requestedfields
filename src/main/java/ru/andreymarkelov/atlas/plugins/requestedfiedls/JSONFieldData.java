package ru.andreymarkelov.atlas.plugins.requestedfiedls;

public class JSONFieldData {
    private String url;

    private String user;

    private String password;

    private String reqType;

    private String reqData;

    private String reqPath;

    public JSONFieldData() {
    }

    public JSONFieldData(
            String url,
            String user,
            String password,
            String reqType,
            String reqData,
            String reqPath) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.reqType = reqType;
        this.reqData = reqData;
        this.reqPath = reqPath;
    }

    public String getPassword() {
        return password;
    }

    public String getReqData() {
        return reqData;
    }

    public String getReqPath() {
        return reqPath;
    }

    public String getReqType() {
        return reqType;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setReqData(String reqData) {
        this.reqData = reqData;
    }

    public void setReqPath(String reqPath) {
        this.reqPath = reqPath;
    }

    public void setReqType(String reqType) {
        this.reqType = reqType;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "JSONFieldData[url=" + url + ", user=" + user + ", password="
                + password + ", reqType=" + reqType + ", reqData=" + reqData + ", reqPath=" + reqPath + "]";
    }
}
