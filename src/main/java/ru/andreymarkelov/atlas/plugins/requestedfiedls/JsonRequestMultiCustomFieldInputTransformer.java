package ru.andreymarkelov.atlas.plugins.requestedfiedls;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.QueryContextConverter;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.searchers.transformer.MultiSelectCustomFieldSearchInputTransformer;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.SimpleNavigatorCollectorVisitor;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

public class JsonRequestMultiCustomFieldInputTransformer
        extends MultiSelectCustomFieldSearchInputTransformer
        implements SearchInputTransformer {
    private JqlOperandResolver jqlOperandResolver;

    public JsonRequestMultiCustomFieldInputTransformer(
            String urlParameterName,
            ClauseNames clauseNames,
            CustomField field,
            JqlOperandResolver jqlOperandResolver,
            JqlSelectOptionsUtil jqlSelectOptionsUtil,
            QueryContextConverter queryContextConverter,
            CustomFieldInputHelper customFieldInputHelper) {
        super(urlParameterName, clauseNames, field, jqlOperandResolver, jqlSelectOptionsUtil, queryContextConverter, customFieldInputHelper);
        this.jqlOperandResolver = jqlOperandResolver;
    }

    public boolean doRelevantClausesFitFilterForm(User searcher, Query query, SearchContext searchContext) {
        if(query != null && query.getWhereClause() != null) {
            boolean result = true;
            Clause whereClause = query.getWhereClause();
            SimpleNavigatorCollectorVisitor collector = new SimpleNavigatorCollectorVisitor(getCustomField().getClauseNames().getJqlFieldNames());
            whereClause.accept(collector);

            List<TerminalClause> clauseList = collector.getClauses();
            for(TerminalClause clause : clauseList) {
                if(!hasValidSimpleOperators(clause.getOperator()) || !hasValidSimpleOperand(clause.getOperand())) {
                    result = false;
                }
            }

            return result;
        }

        return false;
    }

    protected CustomFieldParams getParamsFromSearchRequest(User searcher, Query query, SearchContext searchContext) {
        List<String> params = new ArrayList<String>();
        if(query != null && query.getWhereClause() != null) {
            Clause whereClause = query.getWhereClause();
            SimpleNavigatorCollectorVisitor collector = new SimpleNavigatorCollectorVisitor(getCustomField().getClauseNames().getJqlFieldNames());
            whereClause.accept(collector);

            List<TerminalClause> clauseList = collector.getClauses();
            for(TerminalClause clause : clauseList) {
                Operand operand = clause.getOperand();
                if(jqlOperandResolver.isValidOperand(operand)) {
                    if(jqlOperandResolver.isFunctionOperand(operand)) {
                        jqlOperandResolver.sanitiseFunctionOperand(searcher, (FunctionOperand)operand);
                    }

                    List<QueryLiteral> operandValues = jqlOperandResolver.getValues(searcher, operand, clause);
                    for(QueryLiteral value : operandValues) {
                        params.add(value.getStringValue());
                    }
                }
            }
        }

        return new CustomFieldParamsImpl(getCustomField(), params);
    }

    protected boolean hasValidSimpleOperand(Operand operand) {
        if(operand instanceof SingleValueOperand || operand instanceof MultiValueOperand) {
            return true;
        }

        return false;
    }

    protected boolean hasValidSimpleOperators(Operator operator) {
        if(operator == Operator.EQUALS || operator == Operator.IN) {
            return true;
        }

        return false;
    }
}
