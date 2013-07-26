package ru.andreymarkelov.atlas.plugins.requestedfiedls;

import java.util.Comparator;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.statistics.StatisticsMapper;

public class JsonRequestMultiCustomFieldStatisticsMapper implements StatisticsMapper<String> {

    private final SimpleFieldSearchConstantsWithEmpty searchConstants = SystemSearchConstants.forComponent();

	@Override
	public Comparator<String> getComparator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDocumentConstant() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getValueFromLuceneField(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SearchRequest getSearchUrlSuffix(String arg0, SearchRequest arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFieldAlwaysPartOfAnIssue() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isValidValue(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
