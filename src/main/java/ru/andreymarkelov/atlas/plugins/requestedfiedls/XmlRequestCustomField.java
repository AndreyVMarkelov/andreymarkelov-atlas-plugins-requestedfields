package ru.andreymarkelov.atlas.plugins.requestedfiedls;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.TextCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.templaterenderer.TemplateRenderer;

public class XmlRequestCustomField extends TextCFType  {
	private static final Logger log = LoggerFactory.getLogger(XmlRequestCustomField.class);

    private final PluginData pluginData;

    private final TemplateRenderer renderer;

    public XmlRequestCustomField(
            CustomFieldValuePersister customFieldValuePersister,
            GenericConfigManager genericConfigManager,
            PluginData pluginData,
            TemplateRenderer renderer) {
        super(customFieldValuePersister, genericConfigManager);
        this.pluginData = pluginData;
        this.renderer = renderer;
    }

    @Override
    public List<FieldConfigItemType> getConfigurationItemTypes() {
        final List<FieldConfigItemType> configurationItemTypes = super.getConfigurationItemTypes();
        configurationItemTypes.add(new SimpleHttpConfig(renderer, pluginData, true));
        return configurationItemTypes;
    }

    @Override
    public Map<String, Object> getVelocityParameters(
            final Issue issue,
            final CustomField field,
            final FieldLayoutItem fieldLayoutItem) {
        final Map<String, Object> map = super.getVelocityParameters(issue, field, fieldLayoutItem);

        if (issue == null) {
            return map;
        }

        FieldConfig fieldConfig = field.getRelevantConfig(issue);
        JSONFieldData data = pluginData.getJSONFieldData(fieldConfig);
        if (data != null) {
            try {
                //--> http request
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

                //<-- http request
                Object defaultValue = field.getDefaultValue(issue);
                if (defaultValue != null) {
                    vals.add(0, defaultValue.toString());
                }

                map.put("xml", xml);
                if (vals != null) {
                    if (!vals.isEmpty()) {
                        Collections.sort(vals);
                    }
                    map.put("vals", vals);
                }
            } catch (Throwable th) {
                log.error("XmlRequestCustomField::getVelocityParameters - error renderring", th);
                map.put("error", th.getMessage());
            }
        } else {
            map.put("notconfigured", Boolean.TRUE);
        }

        return map;
    }
}
