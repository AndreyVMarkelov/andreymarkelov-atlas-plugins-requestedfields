package ru.andreymarkelov.atlas.plugins.requestedfiedls;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class XmlHttpRunnerTest {

    @Test
    public void oneTextNodeWorks() {
        assertXmlRunnerFunctionality("<foo>result</foo>", "//foo/text()", Arrays.asList("Default Value", "result"));
    }

    @Test
    public void moreTextNodesWork() {
        assertXmlRunnerFunctionality("<bar><foo>result</foo><foo>result2</foo></bar>", "//foo/text()", Arrays.asList("Default Value", "result"));
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
        System.out.printf("returned: %s\n", returned);
        assertEquals(returned.getVals(),expected);
    }
}
