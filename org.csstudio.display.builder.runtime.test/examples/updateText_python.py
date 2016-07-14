#python script
from connect2j import scriptContext
from sys import argv

with scriptContext(False, False):
    from connect2j import widget
    print("Updating widget %s" % widget.getName())
    widget.setPropertyValue("text", "Hello");