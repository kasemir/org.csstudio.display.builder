#python (not jython) script
""" Input:
    pvs[0] - PV name to use
"""
from connect2j import scriptContext

with scriptContext(True, False):
    from connect2j import widget, pvs, PVUtil
    value = PVUtil.getString(pvs[0]);
    widget.setPropertyValue("pv_name", value)