package ru.andreymarkelov.atlas.plugins.requestedfiedls;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

public class XmlHttpRunner {
    private static final Logger log = LoggerFactory.getLogger(XmlHttpRunner.class);

    private JSONFieldData data;
    private Object defValue;

    private HttpSender httpService;

    public XmlHttpRunner(JSONFieldData data, Object defValue) {
        this.data = data;
        this.defValue = defValue;
        httpService = new HttpSender(data.getUrl(), data.getReqType(), data.getReqDataType(), data.getUser(), data.getPassword());
    }

    public XmlHttpRunner(JSONFieldData data, Object defValue, HttpSender httpService) {
        this.data = data;
        this.defValue = defValue;
        this.httpService = httpService;
    }

    public HttpRunnerData getData() {
        HttpRunnerData res = new HttpRunnerData();

        try {
            String xml = httpService.call(data.getReqData());
            List<String> vals = new ArrayList<String>();
            Processor proc = new Processor(false);
            DocumentBuilder docbuilder = proc.newDocumentBuilder();
            InputStream inputStream = IOUtils.toInputStream(xml);
            InputSource inputSource = new InputSource(inputStream);
            SAXSource streamSource = new SAXSource(inputSource);
            XdmNode source = docbuilder.build(streamSource);

            XPathCompiler compiler = proc.newXPathCompiler();
            
 
            UniversalNamespaceCache nsCache = new UniversalNamespaceCache(source,false);
            
            for(String nsname : nsCache.getAllPrefixes()) {
                compiler.declareNamespace(nsname, nsCache.getNamespaceURI(nsname));
            }
            XPathExecutable expr = compiler.compile(data.getReqPath());
            XPathSelector selector = expr.load();
            selector.setContextItem(source);
            selector.evaluate();
            for (XdmItem item : selector) {
                String nodeText = item.getStringValue();
                if (!StringUtils.isEmpty(nodeText)) {
                    vals.add(nodeText);
                }
            }
            
            if (defValue != null) {
                vals.add(0, defValue.toString());
            }

            res.setRawData(xml);
            if (vals != null) {
                if (!vals.isEmpty()) {
                    Collections.sort(vals);
                }
                res.setVals(vals);
            }
        } catch (Throwable th) {
            log.error("XmlHttpRunner::getData - error rendering", th);
            res.setError(th.getMessage());
            th.printStackTrace();
        }

        return res;
    }
}
