"""
Convenience module for connecting to Java

Author: Amanda Carpenter
"""

import sys
from contextlib import contextmanager

try:
    from py4j.java_gateway import JavaGateway, GatewayParameters, CallbackServerParameters
except ImportError:
    logging.error(" Please install py4j.")
    raise

widget = None
pvs = None
PVUtil = None
ScriptUtil = None

"""
Connect to Java using the given port. (Connect to a GatewayServer listening to the port.)
based on py4j tutorial code at:
    https://www.py4j.org/advanced_topics.html#using-py4j-without-pre-determined-ports-dynamic-port-number
"""
def connectToJava(port):
    port = int(port)
    if port > 0:
        # connect python side to Java side with Java dynamic port and start python
        # callback server with a dynamic port
        gateway = JavaGateway(
                          gateway_parameters=GatewayParameters(port=port),
                          callback_server_parameters=CallbackServerParameters(port=0))

        # retrieve the port to which the python callback server was bound
        python_port = gateway.get_callback_server().get_listening_port()

        # tell the Java side to connect to the python callback server with the new
        # python port, using the java_gateway_server attribute that retrieves the
        # GatewayServer instance
        addr = gateway.java_gateway_server.getCallbackClient().getAddress()
        gateway.java_gateway_server.resetCallbackClient(addr, python_port)
    else:
        return None
        #raise connect2jException
    return gateway

"""
Initializes and connects the variables 'widgets' and 'pvs' for native Python scripts and
initializes and connects 'PVUtil' and/or 'ScriptUtil', (or, for Jython, imports them) as
configured by the parameters.

In greater detail:
native Python:
Creates a context for the script, where the global variables
'widget' and 'pvs' are connected to the widget and array of pvs
associated with the script. If the InitPVUtil parameter is True,
the global variable PVUtil is connected to an instance of the PVUtil
class, and likewise ScriptUtil if InitScriptUtil is True. Yields a
Py4J JavaGateway, which gives access to static Java classes and methods
through the gateway's jvm attribute.

Jython:
Imports PVUtil and/or ScriptUtil from org.csstudio.display.builder.runtime.script if
InitPVUtil and/or InitScriptUtil is/are True, respectively.
"""
@contextmanager
def scriptContext(InitPVUtil=True, InitScriptUtil=True):
    if len(sys.argv) > 1: #treat as native Python script
        global widget; global pvs;
        gateway = None
        try:
            #logging.info("connecting to Java...")
            #connect the gateway
            gateway = connectToJava(sys.argv[1])
            #logging.info("connected on port %s" % sys.argv[1])
            #get the map of Java objects from the gateway
            map = gateway.getMap()
            #initialize the global variables
            widget = map['widget']
            pvs = map['pvs']
            if InitPVUtil:
                global PVUtil; PVUtil = map['PVUtil']
            if InitScriptUtil:
                global ScriptUtil; ScriptUtil = map['ScriptUtil']
            #yield to the caller
            yield gateway
        finally:
            #shutdown the gateway
            if gateway != None:
                #logging.info("shutting down gateway")
                gateway.shutdown(True)
                #logging.info("gateway shutdown")
                gateway = None
    elif sys.platform.lower().startswith('java'):
        if PVUtil:
            from org.csstudio.display.builder.runtime.script import PVUtil
        if ScriptUtil:
            from org.csstudio.display.builder.runtime.script import ScriptUtil
        yield