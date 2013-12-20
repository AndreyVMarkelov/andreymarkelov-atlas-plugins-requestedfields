package ru.andreymarkelov.atlas.plugins.requestedfiedls;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.atlassian.annotations.PublicApi;
import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.AbstractInitializationCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.CustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.SimpleCustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.information.CustomFieldSearcherInformation;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.searchers.transformer.FreeTextCustomFieldSearchInputTransformer;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.statistics.TextFieldSorter;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.query.ActualValueCustomFieldClauseQueryFactory;
import com.atlassian.jira.jql.util.IndexValueConverter;
import com.atlassian.jira.jql.util.SimpleIndexValueConverter;
import com.atlassian.jira.jql.validator.ExactTextCustomFieldValidator;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.query.operator.Operator;
import com.atlassian.util.concurrent.atomic.AtomicReference;

@PublicSpi
@PublicApi
public class SimpleListSearcher  extends AbstractInitializationCustomFieldSearcher implements CustomFieldSearcher {
    private final FieldVisibilityManager fieldVisibilityManager;
    private final JqlOperandResolver jqlOperandResolver;
    private final CustomFieldInputHelper customFieldInputHelper;

    private volatile CustomFieldSearcherInformation searcherInformation;
    private volatile SearchInputTransformer searchInputTransformer;
    private volatile SearchRenderer searchRenderer;
    private volatile CustomFieldSearcherClauseHandler customFieldSearcherClauseHandler;

    public SimpleListSearcher(final JqlOperandResolver jqlOperandResolver, final CustomFieldInputHelper customFieldInputHelper) {
        this(jqlOperandResolver, customFieldInputHelper, ComponentAccessor.getComponent(FieldVisibilityManager.class));
    }

    public SimpleListSearcher(JqlOperandResolver jqlOperandResolver, CustomFieldInputHelper customFieldInputHelper, FieldVisibilityManager fieldVisibilityManager) {
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.jqlOperandResolver = jqlOperandResolver;
        this.customFieldInputHelper = notNull("customFieldInputHelper", customFieldInputHelper);
    }

    private List<FieldConfig> getConfigs(CustomField field) {
        List<FieldConfig> configs = new ArrayList<FieldConfig>();
        for (FieldConfigScheme cs : field.getConfigurationSchemes()) {
            configs.addAll(cs.getConfigs().values());
        }
        return configs;
    }

    public CustomFieldSearcherClauseHandler getCustomFieldSearcherClauseHandler() {
        if (customFieldSearcherClauseHandler == null) {
            throw new IllegalStateException("Attempt to retrieve customFieldSearcherClauseHandler off uninitialised custom field searcher.");
        }
        return customFieldSearcherClauseHandler;
    }

    public SearcherInformation<CustomField> getSearchInformation() {
        if (searcherInformation == null) {
            throw new IllegalStateException("Attempt to retrieve SearcherInformation off uninitialised custom field searcher.");
        }
        return searcherInformation;
    }

    public SearchInputTransformer getSearchInputTransformer() {
        if (searchInputTransformer == null) {
            throw new IllegalStateException("Attempt to retrieve searchInputTransformer off uninitialised custom field searcher.");
        }
        return searchInputTransformer;
    }

    public SearchRenderer getSearchRenderer() {
        if (searchRenderer == null) {
            throw new IllegalStateException("Attempt to retrieve searchRenderer off uninitialised custom field searcher.");
        }
        return searchRenderer;
    }

    public LuceneFieldSorter getSorter(CustomField customField) {
        return new TextFieldSorter(customField.getId());
    }

    public String getSortField(CustomField customField) {
        return customField.getId();
    }

    public void init(CustomField field) {
        final ClauseNames names = field.getClauseNames();
        final FieldIndexer indexer = new SimpleListIndexer(fieldVisibilityManager, field);
        final IndexValueConverter indexValueConverter = new SimpleIndexValueConverter(false);
        final Set<Operator> supportedOperators = OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY;

        boolean isXmlField = field.getCustomFieldType().getKey().equals("ru.andreymarkelov.atlas.plugins.requestedfields:xml-multi-request-custom-field");

        searcherInformation = new CustomFieldSearcherInformation(
                field.getId(),
                field.getNameKey(),
                Collections.<FieldIndexer>singletonList(indexer),
                new AtomicReference<CustomField>(field));
        searchInputTransformer = new FreeTextCustomFieldSearchInputTransformer(
                field,
                names,
                searcherInformation.getId(),
                customFieldInputHelper);
        searchRenderer = new SelectTextCustomFieldRenderer(
                names,
                getDescriptor(),
                field,
                new SelectTextCustomFieldValueProvider(getConfigs(field), isXmlField),
                fieldVisibilityManager);
        customFieldSearcherClauseHandler = new SimpleCustomFieldSearcherClauseHandler(
                new ExactTextCustomFieldValidator(),
                new ActualValueCustomFieldClauseQueryFactory(field.getId(), jqlOperandResolver, indexValueConverter, false),
                supportedOperators,
                JiraDataTypes.TEXT);
    }
}
