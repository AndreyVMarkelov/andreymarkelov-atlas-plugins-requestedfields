package ru.andreymarkelov.atlas.plugins.requestedfiedls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.searchers.renderer.CustomFieldRenderer;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.KickassSearchContext;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.web.FieldVisibilityManager;
import webwork.action.Action;

public class JsonRequestMultiCustomFieldRenderer extends CustomFieldRenderer {

    public JsonRequestMultiCustomFieldRenderer(
            ClauseNames clauseNames,
            CustomFieldSearcherModuleDescriptor customFieldSearcherModuleDescriptor,
            CustomField field,
            CustomFieldValueProvider customFieldValueProvider,
            FieldVisibilityManager fieldVisibilityManager) {
        super(clauseNames, customFieldSearcherModuleDescriptor, field, customFieldValueProvider, fieldVisibilityManager);
    }

    @Override
    public String getEditHtml(final User user, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map<?, ?> displayParameters, final Action action)
    {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("isKickass", isKickass());
        final CustomFieldParams customFieldParams = (CustomFieldParams) fieldValuesHolder.get(getField().getId());
        if (customFieldParams != null)
        {
            final Collection<String> groupNames = customFieldParams.getAllValues();
            final List<String> strings = transformUserInput(groupNames);
            params.put("values", strings);
        }
        return super.getEditHtml(searchContext, fieldValuesHolder, displayParameters, action, params);
    }

    @Override
    public String getViewHtml(User user, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map<?, ?> displayParameters, Action action) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        boolean kickass = isKickass();
        params.put("isKickass", kickass);
        if (kickass) {
            final CustomFieldParams customFieldParams = (CustomFieldParams) fieldValuesHolder.get(getField().getId());
            if (customFieldParams != null)
            {
                final Collection<String> allString = customFieldParams.getAllValues();
                final List<String> groups = transformUserInput(allString);
                params.put("values", groups);
            }

            return super.getViewHtml(searchContext, fieldValuesHolder, displayParameters, action, params);
        }
        return super.getViewHtml(user, searchContext, fieldValuesHolder, displayParameters, action);
    }

    public boolean isKickass() {
        KickassSearchContext kickassSearchContext = ComponentAccessor.getComponent(KickassSearchContext.class);
        return kickassSearchContext.isEnabled();
    }

    public List<String> transformUserInput(Collection<String> values) {
        List<String> strings = new ArrayList<String>();
        for (String string : values) {
            if (string != null) {
                strings.add(string);
            }
        }
        Collections.sort(strings);
        return strings;
    }
}
