from org.csstudio.display.builder.runtime.script import PVUtil, ScriptUtil

secs = PVUtil.getDouble(pvs[0])
max_time = PVUtil.getDouble(pvs[1])

fraction = secs/max_time
widget.setPropertyValue("total_angle", 360.0*fraction)

if fraction < 0.25:
    color = [ 255, 0, 0 ]
elif fraction < 0.4:
    color = [ 255, 255, 0 ]
else:
    color = [ 0, 255, 0 ] 
widget.setPropertyValue("background_color", color)

