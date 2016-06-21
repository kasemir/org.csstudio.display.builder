from org.csstudio.display.builder.runtime.scriptUtil import ScriptUtil
from org.csstudio.display.builder.runtime.script import PVUtil

if PVUtil.getDouble(pvs[0])==1:
    ScriptUtil.infoDialog("Hello, information.", widget)
elif PVUtil.getDouble(pvs[0])==2:
    ScriptUtil.warningDialog("Hello, warning.", widget)