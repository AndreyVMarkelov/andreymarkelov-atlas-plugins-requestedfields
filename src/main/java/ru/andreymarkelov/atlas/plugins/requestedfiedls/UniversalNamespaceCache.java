package ru.andreymarkelov.atlas.plugins.requestedfiedls;

//This is from http://www.ibm.com/developerworks/java/library/x-nmspccontext/
// written by Holger Kraus
// reworked for saxon interfaces by Árpád Magosányi

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.tree.NamespaceNode;

public class UniversalNamespaceCache implements NamespaceContext {
    public static final String DEFAULT_NS = "";
    private Map<String, String> prefix2Uri = new HashMap<String, String>();
    private Map<String, String> uri2Prefix = new HashMap<String, String>();

    /**
     * This constructor parses the document and stores all namespaces it can
     * find. If toplevelOnly is true, only namespaces in the root are used.
     * 
     * @param document
     *            source document
     * @param toplevelOnly
     *            restriction of the search to enhance performance
     */
    public UniversalNamespaceCache(XdmNode source, boolean toplevelOnly) {
    	
        examineNode(source, toplevelOnly);
    }

    /**
     * A single node is read, the namespace attributes are extracted and stored.
     * 
     * @param source
     *            to examine
     * @param attributesOnly,
     *            if true no recursion happens
     */
    private void examineNode(XdmNode source, boolean attributesOnly) {
    	XdmSequenceIterator attributes = source.axisIterator(Axis.NAMESPACE);
        while (attributes.hasNext()) {
            NamespaceNode attribute = (NamespaceNode) ((XdmNode) attributes.next()).getUnderlyingNode();
            storeAttribute(attribute);
        }

        if (!attributesOnly) {
            XdmSequenceIterator chields = source.axisIterator(Axis.CHILD);
            while (chields.hasNext()) {
            	XdmNode chield = (XdmNode) chields.next();
				if(chield.getNodeKind() == XdmNodeKind.ELEMENT)
                	examineNode((XdmNode) chield, false);
            }
        }
    }

    /**
     * This method looks at an attribute and stores it, if it is a namespace
     * attribute.
     * 
     * @param attribute
     *            to examine
     */
    private void storeAttribute(NamespaceNode attribute) {
        // examine the attributes in namespace xmlns
    	putInCache(attribute.getLocalPart(),attribute.getStringValue());
    }

    private void putInCache(String prefix, String uri) {
        prefix2Uri.put(prefix, uri);
        uri2Prefix.put(uri, prefix);
    }

    /**
     * This method is called by XPath. It returns the default namespace, if the
     * prefix is null or "".
     * 
     * @param prefix
     *            to search for
     * @return uri
     */
    public String getNamespaceURI(String prefix) {
        if (prefix == null || prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
            return prefix2Uri.get(DEFAULT_NS);
        } else {
            return prefix2Uri.get(prefix);
        }
    }

    /**
     * This method is not needed in this context, but can be implemented in a
     * similar way.
     */
    public String getPrefix(String namespaceURI) {
        return uri2Prefix.get(namespaceURI);
    }

    public Set<String> getAllPrefixes() {
        return prefix2Uri.keySet();
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
        throw new UnsupportedOperationException();
    }

}