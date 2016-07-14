# python (not jython) script
""" Input:
    pvs[0] - Value around -5 .. 5
"""
from connect2j import scriptContext

with scriptContext(True, False):
    from connect2j import widget, pvs, PVUtil
    value = PVUtil.getDouble(pvs[0]);
    if value >= 0:
        widget.setPropertyValue("text", "Positive")
    else:
        widget.setPropertyValue("text", "Negative")