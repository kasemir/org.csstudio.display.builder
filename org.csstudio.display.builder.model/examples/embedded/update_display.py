# Example script that updates the 'file' of
# an embedded display widget.
#
# Meant as an embedded display widget test.
# Unlikely to be useful in a production setup
# because displays should only change their fundamental
# content in response to operator input.
from org.csstudio.display.builder.runtime.script import PVUtil

sel = PVUtil.getDouble(pvs[0])
if sel > 1.5:
    widget.setPropertyValue("file", "missing.bob")
elif sel > 0.5:
    widget.setPropertyValue("file", "b.bob")
else:
    widget.setPropertyValue("file", "a.bob")
