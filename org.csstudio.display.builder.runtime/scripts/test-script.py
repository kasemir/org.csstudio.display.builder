import sys

from connect2j import updateMap, connectToJava, getMap

if (len(sys.argv) > 1):
    gateway = None
    try:
        gateway = connectToJava(sys.argv[1])
        map = getMap(gateway)
        map['1'] = 1
        updateMap(gateway, map)
        map["obj"].setValue("Hello")
    except Exception as inst:
        if gateway != None:
            gateway.shutdown()
        raise
    gateway.shutdown()