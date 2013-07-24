package ru.andreymarkelov.atlas.plugins.requestedfiedls;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.TextCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.nebhale.jsonpath.JsonPath;

public class JsonRequestCustomField extends TextCFType {
    private static final Logger log = LoggerFactory.getLogger(JsonRequestCustomField.class);

    private final PluginData pluginData;

    private final TemplateRenderer renderer;

    public JsonRequestCustomField(
            CustomFieldValuePersister customFieldValuePersister,
            GenericConfigManager genericConfigManager,
            PluginData pluginData,
            TemplateRenderer renderer) {
        super(customFieldValuePersister, genericConfigManager);
        this.pluginData = pluginData;
        this.renderer = renderer;
    }

    @Override
    public List<FieldConfigItemType> getConfigurationItemTypes() {
        final List<FieldConfigItemType> configurationItemTypes = super.getConfigurationItemTypes();
        configurationItemTypes.add(new SimpleHttpConfig(renderer, pluginData, false));
        return configurationItemTypes;
    }

    @Override
    public Map<String, Object> getVelocityParameters(
            final Issue issue,
            final CustomField field,
            final FieldLayoutItem fieldLayoutItem) {
        final Map<String, Object> map = super.getVelocityParameters(issue, field, fieldLayoutItem);

        if (issue == null) {
            return map;
        }

        FieldConfig fieldConfig = field.getRelevantConfig(issue);
        JSONFieldData data = pluginData.getJSONFieldData(fieldConfig);
        if (data != null) {
            try {
                //--> http request
                HttpSender httpService = new HttpSender(data.getUrl(), data.getReqType(), data.getReqDataType(), data.getUser(), data.getPassword());
                String json = httpService.call(data.getReqData());
                JsonPath namePath = JsonPath.compile(data.getReqPath());
                List<String> vals = namePath.read(json, List.class);
                //<-- http request
                Object defaultValue = field.getDefaultValue(issue);
                if (defaultValue != null) {
                    vals.add(0, defaultValue.toString());
                }

                map.put("json", json);
                if (vals != null) {
                    if (!vals.isEmpty()) {
                        Collections.sort(vals);
                    }
                    map.put("vals", vals);
                }
            } catch (Throwable th) {
                log.error("JsonRequestCustomField::getVelocityParameters - error renderring", th);
                map.put("error", th.getMessage());
            }
        } else {
            map.put("notconfigured", Boolean.TRUE);
        }

        return map;
    }
}