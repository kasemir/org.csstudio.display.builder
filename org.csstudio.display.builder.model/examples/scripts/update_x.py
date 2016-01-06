""" Input:
    pvs[0] - Value around -5 .. 5
    pvs[1] - Default value for X
    pvs[2] - Scaling factor
"""
from org.csstudio.display.builder.runtime.script import PVUtil

value = PVUtil.getDouble(pvs[0]);
x0 = PVUtil.getDouble(pvs[1]);
scale = PVUtil.getDouble(pvs[2]);
widget.setPropertyValue("x", x0 + scale * value)

