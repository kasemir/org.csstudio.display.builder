"""
Convenience module for connecting to Java

Important: Scripts should call shutdown(gateway) before ending,
otherwise gateway could remain open, and script execution would
not complete

Author: Amanda Carpenter
"""

from py4j.java_gateway import JavaGateway, GatewayParameters, CallbackServerParameters

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
        gateway.java_gateway_server.resetCallbackClient(
                            gateway.java_gateway_server.getCallbackClient().getAddress(),
                            python_port)
        return gateway
    else:
        return None
        #raise connect2jException

"""
Get a map from the gateway. (Gateway must be connected, and
its entry point must have a getMap() method.)
"""
def getMap(gateway):
    if gateway != None:
        return gateway.entry_point.getMap()
    else:
        #raise connect2jException
        return None

"""
Update java's map object with new values. Only needed if map key was
assigned a new value or new key-value pairs were removed or added.
Expects a converted Java map object, not one created in Python.
"""
def updateMap(gateway, map):
    if gateway != None:
        gateway.entry_point.setMap(map)
    #else:
        #raise c2jException

def shutdown(gateway):
    if gateway != None:
        gateway.shutdown_callback_server()
        gateway.shutdown()