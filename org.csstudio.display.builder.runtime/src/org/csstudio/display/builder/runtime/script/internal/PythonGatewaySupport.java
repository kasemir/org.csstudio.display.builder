package org.csstudio.display.builder.runtime.script.internal;

import java.util.Map;

import py4j.GatewayServer;

/**
 * Provides a gateway through which to run Python scripts. Java objects are made
 * accessible to Python through the gateway using a map.
 * 
 * @author Amanda Carpenter
 */
public class PythonGatewaySupport
{
    /**
     * Runs a Python script, with the given map accessible within the script
     * 
     * @param map Map which is to be accessed by script
     * @param script Path (including name) of script which is to be run
     * @throws Exception if unable to execute command to run script, or if
     *             gateway fails to start such that it is listening to Python.
     */
    @SuppressWarnings("nls")
    public static void run(Map<String, Object> map, String script) throws Exception
    {
        GatewayServer server = new GatewayServer(new MapWrapper(map), 0);
        server.start();
        
        int port = server.getListeningPort();
        System.out.println("port= " + port);
        if (port == -1)
        {
            server.shutdown();
            throw new Exception("GatewayServer socket not listening");
        }

        Runtime.getRuntime().exec("python " + script + " " + port).waitFor();

        server.shutdown();
    }

    /** Wrapper class which allows access to map */
    @SuppressWarnings("unused")
    private static class MapWrapper
    {
        private Map<String, Object> map;
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