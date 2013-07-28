package ru.andreymarkelov.atlas.plugins.requestedfiedls;

import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.impl.AbstractCustomFieldIndexer;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.web.FieldVisibilityManager;

public class SimpleListIndexer extends AbstractCustomFieldIndexer {

    private final CustomField customField;

    public SimpleListIndexer(
            FieldVisibilityManager fieldVisibilityManager,
            CustomField customField) {
        super(fieldVisibilityManager, customField);
        this.customField = customField;
    }

    public void addDocumentFields(final Document doc, final Issue issue, final Field.Index indexType) {
        final Object value = customField.getValue(issue);
        if (value != null) {
            List<String> data = parseData(value);
            for (String s : data) {
                doc.add(new Field(getDocumentFieldId(), s, Field.Store.YES, indexType));
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

    private List<String> parseData(Object obj) {
        List<String> list = new ArrayList<String>();

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
