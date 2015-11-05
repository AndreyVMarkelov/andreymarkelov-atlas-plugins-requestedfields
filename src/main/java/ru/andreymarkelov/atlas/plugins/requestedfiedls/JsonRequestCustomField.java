package ru.andreymarkelov.atlas.plugins.requestedfiedls;

import java.util.List;
import java.util.Map;

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
import com.atlassian.templaterenderer.TemplateRenderer;

import ru.andreymarkelov.atlas.plugins.requestedfiedls.field.SimpleHttpConfig;
import ru.andreymarkelov.atlas.plugins.requestedfiedls.manager.PluginData;
import ru.andreymarkelov.atlas.plugins.requestedfiedls.model.JSONFieldData;

public class JsonRequestCustomField extends GenericTextCFType {
    private final PluginData pluginData;
    private final TemplateRenderer renderer;

    public JsonRequestCustomField(
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
    public List<FieldConfigItemType> getConfigurationItemTypes() {
        final List<FieldConfigItemType> configurationItemTypes = super.getConfigurationItemTypes();
        configurationItemTypes.add(new SimpleHttpConfig(renderer, pluginData, false));
        return configurationItemTypes;
    }

    @Override
    public Map<String, Object> getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem) {
        final Map<String, Object> map = super.getVelocityParameters(issue, field, fieldLayoutItem);

        if (issue == null) {
            return map;
        }

        FieldConfig fieldConfig = field.getRelevantConfig(issue);
        if (fieldConfig != null) {
            JSONFieldData data = pluginData.getJSONFieldData(fieldConfig);
            if (data != null) {
                JsonHttpRunner runner = new JsonHttpRunner(data, field.getDefaultValue(issue));
                map.put("runner", runner);
            } else {
                map.put("notconfigured", Boolean.TRUE);
            }
        }

        return map;
    }
}