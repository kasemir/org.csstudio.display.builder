#python script
""" Input:
    pvs[0] - Value around -5 .. 5
    pvs[1] - Default value for X
    pvs[2] - Scaling factor
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

    value = PVUtil.getDouble(pvs[0]);
    x0 = PVUtil.getDouble(pvs[1]);
    scale = PVUtil.getDouble(pvs[2]);
    widget.setPropertyValue("x", x0 + scale * value)
    
    shutdown(gateway)