package ru.andreymarkelov.atlas.plugins.requestedfiedls;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.AbstractMultiCFType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.NotNull;

public class JsonRequestMultiCustomField extends AbstractMultiCFType<String> {

    public JsonRequestMultiCustomField(
            CustomFieldValuePersister customFieldValuePersister,
            GenericConfigManager genericConfigManager) {
        super(customFieldValuePersister, genericConfigManager);
    }

    @Override
    @Nullable
    protected String convertDbValueToType(@Nullable Object obj) {
        return null;
    }

    @Override
    @Nullable
    protected Object convertTypeToDbValue(@Nullable String arg0) {
        return null;
    }

    @Override
    @NotNull
    protected PersistenceFieldType getDatabaseType() {
        return null;
    }

    @Override
    public String getSingularObjectFromString(String str) throws FieldValidationException {
        return null;
    }

    @Override
    public String getStringFromSingularObject(String str) {
        return null;
    }

    @Override
    public Object getStringValueFromCustomFieldParams(CustomFieldParams arg0) {
        return null;
    }

    @Override
    @Nullable
    protected Comparator<String> getTypeComparator() {
        return null;
    }

    @Override
    public Collection<String> getValueFromCustomFieldParams(CustomFieldParams arg0) throws FieldValidationException {
        return null;
    }

    @Override
    @NotNull
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem) {
        return super.getVelocityParameters(issue, field, fieldLayoutItem);
    }

    @Override
    public void validateFromParams(CustomFieldParams cfp, ErrorCollection err, FieldConfig config) {
        
    }
}
