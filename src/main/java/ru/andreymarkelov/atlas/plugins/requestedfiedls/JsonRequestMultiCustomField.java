package ru.andreymarkelov.atlas.plugins.requestedfiedls;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.TextCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.templaterenderer.TemplateRenderer;

public class JsonRequestMultiCustomField extends TextCFType {
    private final PluginData pluginData;
    private final TemplateRenderer renderer;

    public JsonRequestMultiCustomField(
            CustomFieldValuePersister customFieldValuePersister,
            GenericConfigManager genericConfigManager,
            PluginData pluginData,
            TemplateRenderer renderer) {
        super(customFieldValuePersister, genericConfigManager);
        this.pluginData = pluginData;
        this.renderer = renderer;
    }

    @Override
    public String getChangelogValue(CustomField field, Object value) {
        String str = super.getChangelogValue(field, value);
        List<String> data = parseData(str);
        StringBuilder sb = new StringBuilder();
        for (String s : data) {
            sb.append(s).append("\n");
        }
        return sb.toString();
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

        map.put("list", parseData(field.getValue(issue)));
        FieldConfig fieldConfig = field.getRelevantConfig(issue);
        JSONFieldData data = pluginData.getJSONFieldData(fieldConfig);
        if (data != null) {
            JsonHttpRunner runner = new JsonHttpRunner(data, field.getDefaultValue(issue));
            map.put("runner", runner);
        } else {
            map.put("notconfigured", Boolean.TRUE);
        }

        return map;
    }

    private List<String> parseData(Object obj) {
        List<String> list = new ArrayList<String>();

        if (obj != null) {
            try {
                JSONArray jsonArray = new JSONArray(obj.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    list.add(jsonArray.getString(i));
                }
            } catch (JSONException e) {
                //do nothing
            }
        }
        return list;
    }
}
