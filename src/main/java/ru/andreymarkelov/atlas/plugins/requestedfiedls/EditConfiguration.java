package ru.andreymarkelov.atlas.plugins.requestedfiedls;

//import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.action.admin.customfields.AbstractEditConfigurationItemAction;

public class EditConfiguration extends AbstractEditConfigurationItemAction {
    private static final long serialVersionUID = -4644319955468389371L;

    private final PluginData pluginData;

    private String url;
    private String user;
    private String password;
    private String reqType;
    private String reqData;
    private String reqPath;

    /*
    public EditConfiguration(ManagedConfigurationItemService managedConfigurationItemService, PluginData pluginData) {
        super(managedConfigurationItemService);
        this.pluginData = pluginData;
    }
    */

    public EditConfiguration(PluginData pluginData) {
        this.pluginData = pluginData;
    }

    @Override
    public String doDefault() throws Exception {
        JSONFieldData data = pluginData.getJSONFieldData(getFieldConfig());
        if (data != null) {
            this.url = data.getUrl();
            this.user = data.getUser();
            this.password = data.getPassword();
            this.reqType = data.getReqType();
            this.reqData = data.getReqData();
            this.reqPath = data.getReqPath();
        }

        return INPUT;
    }

    @Override
    @com.atlassian.jira.security.xsrf.RequiresXsrfCheck
    protected String doExecute() throws Exception {
        if (!isHasPermission(Permissions.ADMINISTER)) {
            return "securitybreach";
        }

        String reqDataType = isXmlField() ? "xml" : "json";
        pluginData.storeJSONFieldData(getFieldConfig(), new JSONFieldData(url, user, password, reqType, reqDataType, reqData, reqPath));
        return getRedirect("/secure/admin/ConfigureCustomField!default.jspa?customFieldId=" + getFieldConfig().getCustomField().getIdAsLong().toString());
    }

    @Override
    protected void doValidation() {
        if (url ==null || url.length() == 0) {
            addErrorMessage(getText("requestedfields.config.error.url"));
        }

        if (reqPath ==null || reqPath.length() == 0) {
            addErrorMessage(getText(isXmlField() ? "requestedfields.config.error.reqPathXML" : "requestedfields.config.error.reqPathJSON"));
        }
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

    public boolean isXmlField() {
        return (getCustomField().getCustomFieldType().getKey().equals("ru.andreymarkelov.atlas.plugins.requestedfields:xml-request-custom-field") ||
                getCustomField().getCustomFieldType().getKey().equals("ru.andreymarkelov.atlas.plugins.requestedfields:xml-multi-request-custom-field"));
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
}
