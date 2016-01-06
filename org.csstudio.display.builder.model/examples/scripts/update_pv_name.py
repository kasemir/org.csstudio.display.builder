""" Input:
    pvs[0] - PV name to use
"""
from org.csstudio.display.builder.runtime.script import PVUtil

value = PVUtil.getString(pvs[0]);
# print "update_pv_name.py: Name is %s" % value
widget.setPropertyValue("pv_name", value)

