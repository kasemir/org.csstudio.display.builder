#python script
from connect2j import shutdown, connectToJava, getMap
from sys import argv

if len(argv) > 1:
    gateway = connectToJava(argv[1])
    map = getMap(gateway)
    widget = map['widget']
    print("Updating widget %s" % widget.getName())
    widget.setPropertyValue("text", "Hello");
    shutdown(gateway)