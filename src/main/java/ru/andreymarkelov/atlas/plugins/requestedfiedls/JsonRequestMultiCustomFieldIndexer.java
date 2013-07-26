package ru.andreymarkelov.atlas.plugins.requestedfiedls;

import java.util.Collection;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.impl.AbstractCustomFieldIndexer;
import com.atlassian.jira.web.FieldVisibilityManager;

public class JsonRequestMultiCustomFieldIndexer extends AbstractCustomFieldIndexer {

    private final CustomField customField;

    public JsonRequestMultiCustomFieldIndexer(
            FieldVisibilityManager fieldVisibilityManager,
            CustomField customField) {
        super(fieldVisibilityManager, customField);
        this.customField = customField;
    }

    public void addDocumentFields(final Document doc, final Issue issue, final Field.Index indexType) {
        final Object value = customField.getValue(issue);
        if (value != null && value instanceof Collection) {
            Collection<String> versions = (Collection<String>) value;
            for (final String string : versions) {
                doc.add(new Field(getDocumentFieldId(), string, Field.Store.YES, indexType));
            }
        }
    }

    @Override
    public void addDocumentFieldsNotSearchable(final Document doc, final Issue issue) {
        addDocumentFields(doc, issue, Field.Index.NO);
    }

    @Override
    public void addDocumentFieldsSearchable(final Document doc, final Issue issue) {
        addDocumentFields(doc, issue, Field.Index.NOT_ANALYZED_NO_NORMS);
    }
}
