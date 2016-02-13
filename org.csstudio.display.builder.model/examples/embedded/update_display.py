# Example script that updates the 'file' of
# an embedded display widget.
#
# Meant as an embedded display widget test.
# Unlikely to be useful in a production setup
# because displays should only change their fundamental
# content in response to operator input.
from org.csstudio.display.builder.runtime.script import PVUtil

if PVUtil.getDouble(pvs[0]) > 0.5:
    widget.setPropertyValue("file", "b.opi")
else:
    widget.setPropertyValue("file", "a.opi")
