package ru.andreymarkelov.atlas.plugins.requestedfiedls;

import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.MultiSelectCustomFieldValueProvider;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.transport.FieldValuesHolder;

/**
 * Custom field value provider.
 * 
 * @author Andrey Markelov
 */
public class SelectTextCustomFieldValueProvider implements CustomFieldValueProvider {
    private final CustomFieldValueProvider customFieldValueProvider = new MultiSelectCustomFieldValueProvider();

    /**
     * This method uses the MultiSelectCustomFieldValueProvider class so that an array of values is returned
     */
    public Object getStringValue(CustomField customField, FieldValuesHolder fieldValuesHolder) {
        Object values = customFieldValueProvider.getStringValue(customField, fieldValuesHolder);
        return values;
    }

    public Object getValue(CustomField customField, FieldValuesHolder fieldValuesHolder) {
        return getStringValue(customField, fieldValuesHolder);
    }
}
