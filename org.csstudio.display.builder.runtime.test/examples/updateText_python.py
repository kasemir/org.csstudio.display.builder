#python script
from connect2j import scriptContext
from sys import argv

with scriptContext('widget', dict=globals()):
    print("Updating widget %s" % widget.getName())
    widget.setPropertyValue("text", "Hello");