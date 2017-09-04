package ru.andreymarkelov.atlas.plugins.requestedfiedls.manager;

import ru.andreymarkelov.atlas.plugins.requestedfiedls.model.JSONFieldData;

import com.atlassian.jira.issue.fields.config.FieldConfig;

public interface RequestFieldDataManager {
    JSONFieldData getJSONFieldData(FieldConfig config);
    void storeJSONFieldData(FieldConfig config, JSONFieldData data);
}
