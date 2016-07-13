package org.csstudio.display.builder.runtime.script.internal;

import java.util.Collections;
import java.util.Map;

import py4j.GatewayServer;

/**
 * Provides a gateway through which to run Python scripts. Java objects are made
 * accessible to Python through the gateway using a map.
 * 
 * @author Amanda Carpenter
 */
@SuppressWarnings("nls")
public class PythonGatewaySupport implements AutoCloseable
{
    private GatewayServer server;
    private MapWrapper entryPoint;
    private int port;

    /**
     * Creates a PythonGatewaySupport() object, starting a Py4J GatewayServer
     * that has a default {@link PythonGatewaySupport.MapWrapper} (containing an
     * empty map) as its entry point.
     * 
     * @throws Exception If GatewayServer starts but is not listening to Python (unlikely)
     */
    public PythonGatewaySupport() throws Exception
    {
        entryPoint = new MapWrapper();
        server = new GatewayServer(entryPoint, 0);
        server.start();
        port = server.getListeningPort();
        if (port == -1)
        {
            server.shutdown();
            throw new Exception("Exception instantiating PythonGatewaySupport: GatewayServer not listening");
        }
    }

    /**
     * Run a Python script, with a map of Java objects made accessible through a
     * gateway to Python.
     * 
     * @param map Map which is to be accessed by the script
     * @param script Path (including name) of script which is to be run
     * @throws Exception If unable to execute command to run script
     */
    public void run(Map<String, Object> map, String script) throws Exception
    {
        entryPoint.setMap(map);
        Runtime.getRuntime().exec("python " + script + " " + port).waitFor();
    }

    /**
     * Shutdown the gateway and release resources
     */
    @Override
    public void close()
    {
        port = -1;
        server.shutdown();
    }

    /** Wrapper class which allows access to map for PythonGatewaySupport */
    @SuppressWarnings("unused")
    private static class MapWrapper
    {
        private Map<String, Object> map;
        public MapWrapper()
        {
            this.map = Collections.emptyMap();
        }
        public MapWrapper(Map<String, Object> map)
        {
            this.map = map;
        }
        public Map<String, Object> getMap()
        {
            return map;
        }
        public void setMap(Map<String, Object> map)
        {
            this.map = map;
        }

        //print messages for debugging purposes
        public void printJava(String str)
        {
            System.out.print(str);
        }
    }
}