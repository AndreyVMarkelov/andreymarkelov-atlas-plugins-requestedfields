package ru.andreymarkelov.atlas.plugins.requestedfiedls;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.GenericTextCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.TextFieldCharacterLengthValidator;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.templaterenderer.TemplateRenderer;
import ru.andreymarkelov.atlas.plugins.requestedfiedls.field.SimpleHttpConfig;
import ru.andreymarkelov.atlas.plugins.requestedfiedls.manager.PluginData;
import ru.andreymarkelov.atlas.plugins.requestedfiedls.model.JSONFieldData;
import ru.andreymarkelov.atlas.plugins.requestedfiedls.util.JsonHttpRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonRequestMultiCustomField extends GenericTextCFType {
    private final PluginData pluginData;
    private final TemplateRenderer renderer;

    public JsonRequestMultiCustomField(
            CustomFieldValuePersister customFieldValuePersister,
            GenericConfigManager genericConfigManager,
            TextFieldCharacterLengthValidator textFieldCharacterLengthValidator,
            JiraAuthenticationContext jiraAuthenticationContext,
            PluginData pluginData,
            TemplateRenderer renderer) {
        super(customFieldValuePersister, genericConfigManager, textFieldCharacterLengthValidator, jiraAuthenticationContext);
        this.pluginData = pluginData;
        this.renderer = renderer;
    }

    @Override
    public String getChangelogValue(CustomField field, String value) {
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
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField customField, FieldLayoutItem fieldLayoutItem) {
        Map<String, Object> map = super.getVelocityParameters(issue, customField, fieldLayoutItem);
        if (issue == null) {
            return map;
        }

        map.put("list", parseData(customField.getValue(issue)));

        FieldConfig fieldConfig = customField.getRelevantConfig(issue);
        if (fieldConfig != null) {
            JSONFieldData data = pluginData.getJSONFieldData(fieldConfig);
            if (data != null) {
                map.put("runner", new JsonHttpRunner(data, customField.getDefaultValue(issue)));
            } else {
                map.put("notconfigured", Boolean.TRUE);
            }
        }

        return map;
    }

    private List<String> parseData(Object obj) {
        List<String> list = new ArrayList<>();

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
