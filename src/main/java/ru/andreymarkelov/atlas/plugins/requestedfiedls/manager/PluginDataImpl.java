package ru.andreymarkelov.atlas.plugins.requestedfiedls.manager;

import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import ru.andreymarkelov.atlas.plugins.requestedfiedls.model.JSONFieldData;

import static ru.andreymarkelov.atlas.plugins.requestedfiedls.util.JSONFieldDataTranslator.JSONFieldDataFromString;
import static ru.andreymarkelov.atlas.plugins.requestedfiedls.util.JSONFieldDataTranslator.JSONFieldDataToString;

public class PluginDataImpl implements PluginData {
    private static final String PLUGIN_KEY = "RequestedFields";

    private final PluginSettings pluginSettings;

    public PluginDataImpl(PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettings = pluginSettingsFactory.createSettingsForKey(PLUGIN_KEY);
    }

    @Override
    public JSONFieldData getJSONFieldData(FieldConfig config) {
        Object obj = getPluginSettings().get(getKey(config));
        return obj != null ? JSONFieldDataFromString(obj.toString()) : null;
    }

    private String getKey(FieldConfig config) {
        return config.getFieldId().concat("_").concat(config.getId().toString()).concat("_").concat("config");
    }

    @Override
    public void storeJSONFieldData(FieldConfig config, JSONFieldData data) {
        getPluginSettings().put(getKey(config), JSONFieldDataToString(data));
    }

    private synchronized PluginSettings getPluginSettings() {
        return pluginSettings;
    }
}
