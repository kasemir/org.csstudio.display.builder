import sys

from connect2j import updateMap, shutdown, connectToJava, getMap

if (len(sys.argv) > 1):
    gateway = connectToJava(sys.argv[1])
    map = getMap(gateway)
    map['1'] = 1
    updateMap(gateway, map)
    map["obj"].setValue("Hello")
    shutdown(gateway)