#from sqlite3 import collections
import sys

from connect2j import connectToMap, updateMap, shutdown

if (len(sys.argv) > 1):
    map = connectToMap(int(sys.argv[1]))
    map['1'] = 1 #change
    updateMap(map)
    map["obj"].setValue("Hello")
    shutdown()