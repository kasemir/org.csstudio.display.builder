"""
Convenience module for connecting to Java

Important: Always call shutdown() before program is closed,
otherwise gateway remain open and python command never completes

Author: Amanda Carpenter
based on py4j tutorial code at:
    https://www.py4j.org/advanced_topics.html#using-py4j-without-pre-determined-ports-dynamic-port-number
"""

"""temporary path setting code:
import sys
c2jpath = "C:\\Users\\cj5\\workspace_css\\org.csstudio.display.builder.python-support"
if c2jpath not in sys.path:
    sys.path.append(c2jpath)
"""

from py4j.java_gateway import JavaGateway, GatewayParameters, CallbackServerParameters
gateway = None

"""
Connect to Java using the given port. (Connect to a GatewayServer listening to the port.)
"""
def connectToJava(port):
    global gateway
    if gateway == None and port > 0:
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
        gateway.java_gateway_server.resetCallbackClient(
                            gateway.java_gateway_server.getCallbackClient().getAddress(),
                            python_port)
    #else:
        #raise c2jException

"""
Get a map from the gateway. (Gateway must be connected, and
its entry point must have a getMap() method.)
"""
def getMap():
    global gateway
    if gateway != None:
        return gateway.entry_point.getMap()
    else:
        #raise c2jException
        return None

"""
Connect to Java (connectToJava(port)) and returns the map (getMap()).
"""
def connectToMap(port):
    connectToJava(port)
    return getMap()

"""
Update java's map-item with new values. Only needed if map key is
assigned a new value or new key-value pairs are removed or added.
"""
def updateMap(map):
    global gateway
    if gateway != None:
        gateway.entry_point.setMap(map)
    #else:
        #raise c2jException

def shutdown():
    global gateway
    if gateway != None:
        gateway.shutdown_callback_server()
        gateway.shutdown()