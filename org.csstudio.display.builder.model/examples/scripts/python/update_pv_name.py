#python (not jython) script
""" Input:
    pvs[0] - PV name to use
"""
from connect2j import connectToJava, shutdown, getMap
from py4j.java_gateway import java_import
from sys import argv

if len(argv) > 1:
    gateway = connectToJava(argv[1])
    map = getMap(gateway)
    widget = map['widget']
    pvs = map['pvs']
    PVUtil = map['PVUtil']
    
	value = PVUtil.getString(pvs[0]);
	widget.setPropertyValue("pv_name", value)

    shutdown(gateway)