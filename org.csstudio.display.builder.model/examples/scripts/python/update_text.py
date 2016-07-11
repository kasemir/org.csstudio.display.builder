# python (not jython) script
""" Input:
    pvs[0] - Value around -5 .. 5
"""
from connect2j import connectToJava, shutdown, getMap
from py4j.java_gateway import java_import
from sys import argv

if len(argv) > 1:
    gateway = connectToJava(argv[1])
    map = getMap(gateway)

    #PVUtilClass = gateway.jvm.org.csstudio.display.builder.runtime.script.PVUtil #doesn't get recognized as a class

    widget = map['widget']
    pvs = map['pvs']
    PVUtil = map['PVUtil']

    try:
        value = pvs[0] #temp line; kill later
        value = PVUtil.getDouble(value);
        if value >= 0:
            widget.setPropertyValue("text", "Positive")
        else:
            widget.setPropertyValue("text", "Negative")
    except Exception:
        None

    shutdown(gateway)
