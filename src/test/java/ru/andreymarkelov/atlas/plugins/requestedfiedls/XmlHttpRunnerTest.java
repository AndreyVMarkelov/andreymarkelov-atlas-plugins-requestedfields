package ru.andreymarkelov.atlas.plugins.requestedfiedls;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class XmlHttpRunnerTest {

    @Test
    public void oneTextNodeWorks() {
        assertXmlRunnerFunctionality(
                "<foo>result</foo>",
                "//foo/text()",
                Arrays.asList(
                        "Default Value",
                        "result"
                )
        );
    }

    @Test
    public void moreTextNodesWork() {
        assertXmlRunnerFunctionality(
                "<bar><foo>result</foo><foo>result2</foo></bar>",
                "//foo/text()",
                Arrays.asList(
                        "Default Value",
                        "result",
                        "result2"
                )
        );
    }

    @Test
    public void Attribute_values_can_be_extracted() {
        assertXmlRunnerFunctionality(
                "<bar><foo id=\"id1\">result</foo><foo id=\"id2\">result2</foo></bar>",
                "//@id",
                Arrays.asList(
                        "Default Value",
                        "id1",
                        "id2"
                )
        );
    }
    
    @Test
    public void No_need_to_convert_node_contents_to_text() {
        assertXmlRunnerFunctionality(
                "<bar><foo>result</foo><foo>result2</foo></bar>",
                "//foo",
                Arrays.asList(
                        "Default Value",
                        "result",
                        "result2"
                )
        );
    }

    @Test
    public void Xpath_can_use_doc_namespaces() {
        assertXmlRunnerFunctionality(
                "<bar xmlns:baz=\"http://foo.dom/bar\"><baz:foo>result</baz:foo><foo>thisnot</foo><baz:foo>result2</baz:foo></bar>",
                "//baz:foo",
                Arrays.asList(
                        "Default Value",
                        "result",
                        "result2"
                )
        );
    }

    @Test
    public void Xpath_uses_doc_default_namespace() {
        assertXmlRunnerFunctionality(
                "<bar xmlns=\"http://foo.dom/bar\"><foo>result</foo><foo>result2</foo></bar>",
                "//foo",
                Arrays.asList(
                        "Default Value",
                        "result",
                        "result2"
                )
        );
    }

    @Test
    public void Xpath_processor_is_version_2() {
        assertXmlRunnerFunctionality(
                "<bar xmlns=\"http://foo.dom/bar\"><foo>result</foo><foo>result2</foo></bar>",
                "for $i in //foo return concat($i,'--')",
                Arrays.asList(
                        "Default Value",
                        "result--",
                        "result2--"
                )
        );
    }


    private void assertXmlRunnerFunctionality(String xml, String path,
            List<String> expected) {
        HttpSender mockSender = mock(HttpSender.class);
        JSONFieldData data = new JSONFieldData();
        when(mockSender.call(null)).thenReturn(xml);
        data.setReqPath(path);
        Object defValue = "Default Value";
        XmlHttpRunner runner = new XmlHttpRunner(data, defValue, mockSender);
        HttpRunnerData returned = runner.getData();
        //System.out.printf("returned: %s\n", returned);
        assertEquals(expected, returned.getVals());
    }
}
