package ru.andreymarkelov.atlas.plugins.requestedfiedls.action;

import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.admin.customfields.AbstractEditConfigurationItemAction;
import ru.andreymarkelov.atlas.plugins.requestedfiedls.manager.RequestFieldDataManager;
import ru.andreymarkelov.atlas.plugins.requestedfiedls.model.JSONFieldData;

import static com.atlassian.jira.permission.GlobalPermissionKey.ADMINISTER;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class EditRequestFieldConfig extends AbstractEditConfigurationItemAction {
    private static final long serialVersionUID = -4644319955468389371L;

    private final RequestFieldDataManager requestFieldDataManager;
    private final JiraAuthenticationContext authenticationContext;
    private final GlobalPermissionManager globalPermissionManager;

    private String url;
    private String user;
    private String password;
    private String reqType;
    private String reqHeaders;
    private String reqData;
    private String reqPath;
    private String sortOrder;

    public EditRequestFieldConfig(
            ManagedConfigurationItemService managedConfigurationItemService,
            RequestFieldDataManager requestFieldDataManager,
            JiraAuthenticationContext authenticationContext,
            GlobalPermissionManager globalPermissionManager) {
        super(managedConfigurationItemService);
        this.requestFieldDataManager = requestFieldDataManager;
        this.authenticationContext = authenticationContext;
        this.globalPermissionManager = globalPermissionManager;
    }

    @Override
    public String doDefault() throws Exception {
        JSONFieldData data = requestFieldDataManager.getJSONFieldData(getFieldConfig());
        if (data != null) {
            this.url = data.getUrl();
            this.user = data.getUser();
            this.password = data.getPassword();
            this.reqType = data.getReqType();
            this.reqHeaders = data.getReqHeaders();
            this.reqData = data.getReqData();
            this.reqPath = data.getReqPath();
            this.sortOrder = data.getSortOrder();
        }
        return INPUT;
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception {
        if (!globalPermissionManager.hasPermission(ADMINISTER, getLoggedInUser())) {
            return "securitybreach";
        }

        requestFieldDataManager.storeJSONFieldData(
                getFieldConfig(),
                new JSONFieldData(url, user, password, reqType, reqHeaders, isXmlField() ? "xml" : "json", reqData, reqPath, sortOrder)
        );
        return getRedirect("/secure/admin/ConfigureCustomField!default.jspa?customFieldId=" + getFieldConfig().getCustomField().getIdAsLong().toString());
    }

    @Override
    protected void doValidation() {
        super.doValidation();

        if (isBlank(url)) {
            addError("url", authenticationContext.getI18nHelper().getText("ru.andreymarkelov.atlas.plugins.requestedfields.fieldconfig.url.error.empty"));
        }

        if (isBlank(reqPath)) {
            addError(
                    "reqPath",
                    authenticationContext.getI18nHelper().getText(isXmlField() ?
                            "ru.andreymarkelov.atlas.plugins.requestedfields.fieldconfig.reqPath.xml.error.empty" :
                            "ru.andreymarkelov.atlas.plugins.requestedfields.fieldconfig.reqPath.json.error.empty"));
        }

        if (isNotBlank(reqHeaders)) {
            String[] headerLines = reqHeaders.split("\\r?\\n");
            for (String headerLine : headerLines) {
                if (!headerLine.contains("=")) {
                    addError("reqHeaders", authenticationContext.getI18nHelper().getText("ru.andreymarkelov.atlas.plugins.requestedfields.fieldconfig.headers.error.format"));
                }
            }
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

    public String getReqHeaders() {
        return reqHeaders;
    }

    public void setReqHeaders(String reqHeaders) {
        this.reqHeaders = reqHeaders;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
}
