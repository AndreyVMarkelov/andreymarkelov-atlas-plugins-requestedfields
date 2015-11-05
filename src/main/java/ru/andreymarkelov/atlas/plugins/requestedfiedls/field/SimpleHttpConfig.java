package ru.andreymarkelov.atlas.plugins.requestedfiedls.field;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ru.andreymarkelov.atlas.plugins.requestedfiedls.manager.PluginData;
import ru.andreymarkelov.atlas.plugins.requestedfiedls.model.JSONFieldData;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.templaterenderer.TemplateRenderer;

public class SimpleHttpConfig implements FieldConfigItemType {
    private static final Logger logger = Logger.getLogger(SimpleHttpConfig.class);

    private final TemplateRenderer renderer;
    private final PluginData pluginData;
    private final boolean isXmlField;

    public SimpleHttpConfig(TemplateRenderer renderer, PluginData pluginData, boolean isXmlField) {
        this.renderer = renderer;
        this.pluginData = pluginData;
        this.isXmlField = isXmlField;
    }

    @Override
    public String getBaseEditUrl() {
        return "EditRequestFieldConfig!default.jspa";
    }

    @Override
    public Object getConfigurationObject(Issue issue, FieldConfig config) {
        Map<String, Object> parms = new HashMap<String, Object>();
        JSONFieldData data = pluginData.getJSONFieldData(config);
        if (data != null) {
            parms.put("url", data.getUrl());
            parms.put("user", data.getUser());
            parms.put("password", data.getPassword());
            parms.put("reqType", data.getReqType());
            parms.put("reqData", data.getReqData());
            parms.put("reqPath", data.getReqPath());
        }
        return parms;
    }

    @Override
    public String getDisplayName() {
        return isXmlField ? "XML Http Settings" : "JSON Http Settings";
    }

    @Override
    public String getDisplayNameKey() {
        return isXmlField ? "XML Http Settings" : "JSON Http Settings";
    }

    @Override
    public String getObjectKey() {
        return "SimpleHttpConfig";
    }

    @Override
    public String getViewHtml(FieldConfig config, FieldLayoutItem fieldLayoutItem) {
        Map<String, Object> parms = new HashMap<String, Object>();
        JSONFieldData data = pluginData.getJSONFieldData(config);
        if (data != null) {
            parms.put("i18n", ComponentAccessor.getJiraAuthenticationContext().getI18nHelper());
            parms.put("url", data.getUrl());
            parms.put("user", data.getUser());
            parms.put("password", data.getPassword());
            parms.put("reqType", data.getReqType());
            parms.put("reqData", data.getReqData());
            parms.put("reqPath", data.getReqPath());
            parms.put("isXmlField", isXmlField);
        }

        StringWriter sw = new StringWriter();
        try {
            renderer.render("/ru/andreymarkelov/atlas/plugins/requestedfiedls/templates/action/view-config.vm", parms, sw);
        } catch (Exception e) {
            logger.error("Render exception", e);
            sw.append("Render exception");
        }
        return sw.toString();
    }
}
