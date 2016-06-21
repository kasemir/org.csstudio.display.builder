"""
A demonstration script for the getPV and getPVByName script utilities.
Not the best implementation; deliberately obtains PV from widgets whenever possible.

Writes the value in the textentry to the PV whose name is selected in the combo box.
"""

from org.csstudio.display.builder.runtime.scriptUtil import ScriptUtil
from org.csstudio.display.builder.runtime.script import PVUtil

ScriptUtil.logWarning("Writing PV: %d" % PVUtil.getDouble(pvs[0]))

if PVUtil.getDouble(pvs[0])==1:
    ScriptUtil.logWarning("(1)")
    textentry = widget.getDisplayModel().runtimeChildren().getChildByName("textentry")
    combo = widget.getDisplayModel().runtimeChildren().getChildByName("Combo Box")
    
    ScriptUtil.logWarning("(2)")
    pvName = PVUtil.getString(ScriptUtil.getPV(combo))
    pv = ScriptUtil.getPVByName(widget, pvName)
    ScriptUtil.logWarning("(3)")
    if pv != None:
        ScriptUtil.logWarning("(3.1)")
        entryPV = ScriptUtil.getPV(textentry)
        pv.write(PVUtil.getString(entryPV))
    ScriptUtil.logWarning("(4)")
elif PVUtil.getDouble(pvs[0])==-1: #cycle through PVs by name
    for pvName in ["loc://getPV<String>", "loc://test1", "loc://test2", "loc://test3", "loc://test4"]:
        pv = ScriptUtil.getPVByName(widget, pvName)
        if pv != None:
            ScriptUtil.logWarning("Script Util getting PV %r=%d" % (pv, PVUtil.getDouble(pv)))