package ru.andreymarkelov.atlas.plugins.requestedfiedls;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.searchers.renderer.CustomFieldRenderer;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.searchers.impl.NamedTerminalClauseCollectingVisitor;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.query.Query;

/**
 * Custom field renderer.
 * 
 * @author Andrey Markelov
 */
public class SelectTextCustomFieldRenderer extends CustomFieldRenderer {
    private ClauseNames clauseNames;

    /**
     * Constructor.
     */
    public SelectTextCustomFieldRenderer(
            ClauseNames clauseNames,
            CustomFieldSearcherModuleDescriptor customFieldSearcherModuleDescriptor,
            CustomField field,
            CustomFieldValueProvider customFieldValueProvider,
            FieldVisibilityManager fieldVisibilityManager) {
        super(clauseNames, customFieldSearcherModuleDescriptor, field, customFieldValueProvider, fieldVisibilityManager);
        this.clauseNames = clauseNames;
    }

    public boolean isRelevantForQuery(User searcher, Query query) {
        final NamedTerminalClauseCollectingVisitor clauseVisitor = new NamedTerminalClauseCollectingVisitor(clauseNames.getJqlFieldNames());
        if(query != null && query.getWhereClause() != null) {
            query.getWhereClause().accept(clauseVisitor);
        }

        return clauseVisitor.containsNamedClause();
    }
}
