# Set color map
from org.csstudio.display.builder.runtime.script import PVUtil, ScriptUtil
from org.csstudio.display.builder.model.properties import ColorMap

mapname = PVUtil.getString(pvs[0])

colormap = None

for map in ColorMap.PREDEFINED:
    if mapname == map.getName():
        colormap = map
        break

if colormap is None:
    ScriptUtil.getLogger().warning("Unknown color map " + mapname)
else:
    widget.setPropertyValue("color_map", colormap)

