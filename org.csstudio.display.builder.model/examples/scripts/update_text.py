""" Input:
    pvs[0] - Value around -5 .. 5
"""
from org.csstudio.display.builder.runtime.script import PVUtil

value = PVUtil.getDouble(pvs[0]);
if value >= 0:
    widget.setPropertyValue("text", "Positive")
else:
    widget.setPropertyValue("text", "Negative")

