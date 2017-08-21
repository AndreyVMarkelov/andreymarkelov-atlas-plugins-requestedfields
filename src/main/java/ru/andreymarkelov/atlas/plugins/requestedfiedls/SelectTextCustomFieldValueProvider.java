package ru.andreymarkelov.atlas.plugins.requestedfiedls;

import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.nebhale.jsonpath.JsonPath;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.andreymarkelov.atlas.plugins.requestedfiedls.manager.PluginData;
import ru.andreymarkelov.atlas.plugins.requestedfiedls.model.JSONFieldData;
import ru.andreymarkelov.atlas.plugins.requestedfiedls.util.HttpSender;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.component.ComponentAccessor.getOSGiComponentInstanceOfType;

public class SelectTextCustomFieldValueProvider implements CustomFieldValueProvider {
    private List<FieldConfig> configs;
    private PluginData pluginData;
    private boolean isXmlField;

    public SelectTextCustomFieldValueProvider(List<FieldConfig> configs, boolean isXmlField) {
        this.configs = configs;
        this.isXmlField = isXmlField;
        this.pluginData = getOSGiComponentInstanceOfType(PluginData.class);
    }

    @SuppressWarnings("unchecked")
    private List<String> getJsonData(JSONFieldData data) {
        try {
            HttpSender httpService = new HttpSender(data.getUrl(), data.getReqType(), data.getReqDataType(), data.getUser(), data.getPassword());
            String json = httpService.call(data.getReqHeaders(), data.getReqData());

            JsonPath namePath = JsonPath.compile(data.getReqPath());
            List<String> values = namePath.read(json, List.class);
            if (values != null) {
                if (!values.isEmpty()) {
                    Collections.sort(values);
                }
            }

            return values;
        } catch (Throwable th) {
            return new ArrayList<>();
        }
    }

    public List<String> getStringValue(CustomField customField, FieldValuesHolder fieldValuesHolder) {
        List<String> values = new ArrayList<>();
        for (FieldConfig fieldConfig : configs) {
            JSONFieldData data = pluginData.getJSONFieldData(fieldConfig);
            if (data != null) {
                if (isXmlField) {
                    values.addAll(getXmlData(data));
                } else {
                    values.addAll(getJsonData(data));
                }
            }
        }
        return values;
    }

    public Object getValue(CustomField customField, FieldValuesHolder fieldValuesHolder) {
        CustomFieldType<?, ?> customFieldType = customField.getCustomFieldType();
        final CustomFieldParams customFieldParams = customField.getCustomFieldValues(fieldValuesHolder);
        return customFieldType.getValueFromCustomFieldParams(customFieldParams);
    }

    private List<String> getXmlData(JSONFieldData data) {
        try {
            HttpSender httpService = new HttpSender(data.getUrl(), data.getReqType(), data.getReqDataType(), data.getUser(), data.getPassword());
            String xml = httpService.call(data.getReqHeaders(), data.getReqData());

            List<String> values = new ArrayList<>();
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
                        values.add(nodes.item(i).getNodeValue());
                    }
                }
            }

            if (values != null) {
                if (!values.isEmpty()) {
                    Collections.sort(values);
                }
            }

            return values;
        } catch (Throwable th) {
            return new ArrayList<>();
        }
    }
}
