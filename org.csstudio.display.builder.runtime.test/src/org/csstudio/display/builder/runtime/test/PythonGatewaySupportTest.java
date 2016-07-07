package org.csstudio.display.builder.runtime.test;

import java.util.HashMap;
import java.util.Map;

import org.csstudio.display.builder.runtime.script.internal.PythonGatewaySupport;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class PythonGatewaySupportTest
{
    @Test
    public void testScriptSupport() throws Exception
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("0", 0);
        map.put("1", -1);
        map.put("obj", new TestObject());

        String testPath = "../org.csstudio.display.builder.runtime/scripts/";
        PythonGatewaySupport.run(map, testPath + "test-script.py");

        Assert.assertEquals(0, map.get("0")); //map does not change unchanged elements
        Assert.assertEquals(1, map.get("1")); //map updates with different elements
        //methods called on map elements affect map without explicit update
        TestObject obj = (TestObject) map.get("obj");
        Assert.assertEquals("Hello", obj.getValue());
    }

    @SuppressWarnings("unused")
    private class TestObject
    {
        private Object value;

        public Object getValue()
        {
            return value;
        }

        public void setValue(Object value)
        {
            this.value = value;
        }

        public TestObject()
        {
            value = null;
        }
    }
}