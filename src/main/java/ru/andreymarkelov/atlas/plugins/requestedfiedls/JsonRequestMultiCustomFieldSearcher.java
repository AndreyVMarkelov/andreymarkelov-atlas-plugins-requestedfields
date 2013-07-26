package ru.andreymarkelov.atlas.plugins.requestedfiedls;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.bc.issue.search.QueryContextConverter;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.MultiSelectCustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.SortableCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.AbstractInitializationCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.CustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.SimpleCustomFieldValueGeneratingClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.information.CustomFieldSearcherInformation;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.issue.statistics.TextFieldSorter;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.jql.validator.UserCustomFieldValidator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.FieldVisibilityManager;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class JsonRequestMultiCustomFieldSearcher
        extends AbstractInitializationCustomFieldSearcher
        implements CustomFieldSearcher, SortableCustomFieldSearcher, CustomFieldStattable {

    private final JqlOperandResolver operandResolver;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final JiraAuthenticationContext context;

    private volatile CustomFieldSearcherInformation searcherInformation;
    private volatile SearchInputTransformer searchInputTransformer;
    private volatile SearchRenderer searchRenderer;
    private volatile CustomFieldSearcherClauseHandler customFieldSearcherClauseHandler;

    private JqlOperandResolver jqlOperandResolver;
    private CustomFieldInputHelper customFieldInputHelper;
    private I18nHelper.BeanFactory beanFactory;

    @Override
    public SearcherInformation<CustomField> getSearchInformation() {
        if (searcherInformation == null) {
            throw new IllegalStateException("Attempt to retrieve SearcherInformation off uninitialised custom field searcher.");
        }
        return searcherInformation;
    }

    @Override
    public SearchInputTransformer getSearchInputTransformer() {
        return searchInputTransformer;
    }

    @Override
    public SearchRenderer getSearchRenderer() {
        return searchRenderer;
    }

    public JsonRequestMultiCustomFieldSearcher(
            final JqlOperandResolver operandResolver,
            final JiraAuthenticationContext context,
            final CustomFieldInputHelper customFieldInputHelper,
            final FieldVisibilityManager fieldVisibilityManager,
            final JqlOperandResolver jqlOperandResolver) {
        this.beanFactory = ComponentAccessor.getI18nHelperFactory();
        this.context = notNull("context", context);
        this.operandResolver = notNull("operandResolver", operandResolver);
        this.fieldVisibilityManager = notNull("fieldVisibilityManager", fieldVisibilityManager);
        this.customFieldInputHelper = notNull("customFieldInputHelper", customFieldInputHelper);
        this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
    }

    public void init(final CustomField field) {
        final JsonRequestMultiCustomFieldIndexer indexer = new JsonRequestMultiCustomFieldIndexer(fieldVisibilityManager, field);
        JqlSelectOptionsUtil jqlSelectOptionsUtil = ComponentManager.getComponentInstanceOfType(JqlSelectOptionsUtil.class);
        QueryContextConverter queryContextConverter = new QueryContextConverter();
        final ClauseNames names = field.getClauseNames();

        final CustomFieldValueProvider customFieldValueProvider = new MultiSelectCustomFieldValueProvider();

        this.searcherInformation = new CustomFieldSearcherInformation(
                field.getId(),
                field.getNameKey(),
                Collections.<FieldIndexer>singletonList(indexer),
                new AtomicReference<CustomField>(field));

        this.searchRenderer = new JsonRequestMultiCustomFieldRenderer(
                names,
                getDescriptor(),
                field,
                customFieldValueProvider,
                fieldVisibilityManager);

        this.searchInputTransformer = new JsonRequestMultiCustomFieldInputTransformer(
                searcherInformation.getId(),
                names,
                field,
                jqlOperandResolver,
                jqlSelectOptionsUtil,
                queryContextConverter,
                customFieldInputHelper);

        this.customFieldSearcherClauseHandler = new SimpleCustomFieldValueGeneratingClauseHandler(
                new UserCustomFieldValidator(userResolver, operandResolver, beanFactory),
                new UserCustomFieldClauseQueryFactory(field.getId(), userResolver, operandResolver),
                new UserClauseValuesGenerator(userPickerSearchService),
                OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY,
                JiraDataTypes.TEXT);
    }

    @Override
    public StatisticsMapper getStatisticsMapper(CustomField customField) {
        return null;
    }

    @Override
    public LuceneFieldSorter getSorter(CustomField customField) {
        return null;
    }

    @Override
    public CustomFieldSearcherClauseHandler getCustomFieldSearcherClauseHandler() {
        return customFieldSearcherClauseHandler;
    }
}
