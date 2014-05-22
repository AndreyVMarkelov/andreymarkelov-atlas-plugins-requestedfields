package ru.andreymarkelov.atlas.plugins.requestedfiedls;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.nebhale.jsonpath.JsonPath;

public class SelectTextCustomFieldValueProvider implements CustomFieldValueProvider {
    private List<FieldConfig> configs;
    private PluginData pluginData;
    private boolean isXmlField;

    public SelectTextCustomFieldValueProvider(List<FieldConfig> configs, boolean isXmlField) {
        this.configs = configs;
        this.isXmlField = isXmlField;
        this.pluginData = ComponentAccessor.getOSGiComponentInstanceOfType(PluginData.class);
    }

    private List<String> getJsonData(JSONFieldData data) {
        try {
            HttpSender httpService = new HttpSender(data.getUrl(), data.getReqType(), data.getReqDataType(), data.getUser(), data.getPassword());
            String json = httpService.call(data.getReqData());

            JsonPath namePath = JsonPath.compile(data.getReqPath());
            @SuppressWarnings("unchecked")
            List<String> vals = namePath.read(json, List.class);
            if (vals != null) {
                if (!vals.isEmpty()) {
                    Collections.sort(vals);
                }
            }

            return vals;
        } catch (Throwable th) {
            return new ArrayList<String>();
        }
    }

    public List<String> getStringValue(CustomField customField, FieldValuesHolder fieldValuesHolder) {
        List<String> vals = new ArrayList<String>();
        for (FieldConfig fieldConfig : configs) {
            JSONFieldData data = pluginData.getJSONFieldData(fieldConfig);
            if (data != null) {
                if (isXmlField) {
                    vals.addAll(getXmlData(data));
                } else {
                    vals.addAll(getJsonData(data));
                }
            }
        }
        return vals;
    }

    public Object getValue(CustomField customField, FieldValuesHolder fieldValuesHolder) {
        @SuppressWarnings("rawtypes")
        CustomFieldType customFieldType = customField.getCustomFieldType();
        final CustomFieldParams customFieldParams = customField.getCustomFieldValues(fieldValuesHolder);
        return customFieldType.getValueFromCustomFieldParams(customFieldParams);
    }

    private List<String> getXmlData(JSONFieldData data) {
        try {
            HttpSender httpService = new HttpSender(data.getUrl(), data.getReqType(), data.getReqDataType(), data.getUser(), data.getPassword());
            String xml = httpService.call(data.getReqData());

            List<String> vals = new ArrayList<String>();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
            XPathFactory xpathfactory = XPathFactory.newInstance();
            XPath xpath = xpathfactory.newXPath();
            XPathExpression expr = xpath.compile(data.getReqPath());
            Object result = expr.evaluate(doc, XPathConstants.NODESET);
            NodeList nodes = (NodeList) result;
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.TEXT_NODE) {
                    String nodeText = node.getTextContent();
                    if (!StringUtils.isEmpty(nodeText)) {
                        vals.add(nodes.item(i).getNodeValue());
                    }
                }
            }

            if (vals != null) {
                if (!vals.isEmpty()) {
                    Collections.sort(vals);
                }
            }

            return vals;
        } catch (Throwable th) {
            return new ArrayList<String>();
        }
    }
}
