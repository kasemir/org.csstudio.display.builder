package org.csstudio.display.builder.runtime.script.internal;

import static org.csstudio.display.builder.runtime.RuntimePlugin.logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;

import py4j.GatewayServer;

/**
 * Provides a gateway through which to run Python scripts. Java objects are made
 * accessible to Python through the gateway using a map.
 * 
 * Error logging written with suggestions from Kay Kasemir.
 * 
 * @author Amanda Carpenter
 */
@SuppressWarnings("nls")
public class PythonGatewaySupport
{

    /**
     * Run a Python script, with a map of Java objects made accessible through a
     * gateway to Python. The port to which the gateway server is listening is
     * passed as an argument to the script.
     * 
     * @param map Map which is to be accessed by the script
     * @param script Path (including name) of script which is to be run
     * @throws Exception If unable to execute command to run script
     */
    public static void run(Map<String, Object> map, String script) throws Exception
    {
        GatewayServer server = new GatewayServer(new MapWrapper(map), 0);
        server.start();
        int port = server.getListeningPort();
        if (port == -1)
        {
            server.shutdown();
            throw new Exception("Exception instantiating PythonGatewaySupport: GatewayServer not listening");
        }

        //start Python process, passing port used to connect to Py4J Java Gateway
        final ProcessBuilder process_builder = new ProcessBuilder(new String[] {
                "python", script, Integer.toString(port)
        });
        final Process process = process_builder.start();

        final Thread error_log = new StreamLogger("PythonErrors", process.getErrorStream(), Level.WARNING);
        error_log.start();
        final Thread python_out = new StreamLogger("PythonPrint", process.getInputStream(), Level.INFO);
        python_out.start();

        try
        {
            process.waitFor();
        }
        catch (InterruptedException ex)
        {
            //ignore; closing display creates interruption
        }

        error_log.join();
        python_out.join();
        server.shutdown();
    }

    static class StreamLogger extends Thread
    {
        private final InputStream stream;
        private final Level log_level;

        public StreamLogger(final String name, final InputStream stream, final Level log_level)
        {
            super(name);
            setDaemon(true);
            this.stream = stream;
            this.log_level = log_level;
        }

        @Override
        public void run()
        {
            try (
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                )
            {
                String line;
                while ((line = reader.readLine()) != null)
                    logger.log(log_level, "Python: " + line);
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, this.getName() + " stream failed", ex);
            }
        }
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
    }
}