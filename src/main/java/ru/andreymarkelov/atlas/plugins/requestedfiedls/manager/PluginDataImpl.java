package ru.andreymarkelov.atlas.plugins.requestedfiedls.manager;

import ru.andreymarkelov.atlas.plugins.requestedfiedls.model.JSONFieldData;
import ru.andreymarkelov.atlas.plugins.requestedfiedls.util.JSONFieldDataTranslator;

import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

public class PluginDataImpl implements PluginData {
    private final String PLUGIN_KEY = "RequestedFields";

    private final PluginSettings pluginSettings;

    public PluginDataImpl(PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettings = pluginSettingsFactory.createSettingsForKey(PLUGIN_KEY);
    }

    @Override
    public JSONFieldData getJSONFieldData(FieldConfig config) {
        Object obj = getPluginSettings().get(getKey(config));
        if (obj != null) {
            return JSONFieldDataTranslator.JSONFieldDataFromString(obj.toString());
        } else {
            return null;
        }
    }

    private String getKey(FieldConfig config) {
        return config.getFieldId().concat("_").concat(config.getId().toString()).concat("_").concat("config");
    }

    private synchronized PluginSettings getPluginSettings() {
        return pluginSettings;
    }

    @Override
    public void storeJSONFieldData(FieldConfig config, JSONFieldData data) {
        getPluginSettings().put(getKey(config), JSONFieldDataTranslator.JSONFieldDataToString(data));
    }
}
