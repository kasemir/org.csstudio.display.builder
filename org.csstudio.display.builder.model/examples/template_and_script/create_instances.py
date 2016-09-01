# This script is attached to a display
# and triggered by a PV like this to
# execute once when that display is loaded:
# loc://initial_trigger$(DID)(1)

from org.csstudio.display.builder.model import WidgetFactory

embedded_width = 225
embedded_height = 65

def createInstance(x, y, name, pv):
    embedded = WidgetFactory.getInstance().getWidgetDescriptor("embedded").createWidget();
    embedded.setPropertyValue("x", x)
    embedded.setPropertyValue("y", y)
    embedded.setPropertyValue("width", embedded_width)
    embedded.setPropertyValue("height", embedded_height)
    embedded.getPropertyValue("macros").add("NAME", name)
    embedded.getPropertyValue("macros").add("PV", pv)
    embedded.setPropertyValue("file", "template.bob")
    return embedded

display = widget.getDisplayModel()
for i in range(50):
    instance = createInstance(0, 170 + embedded_height*i, "Device %d" % (i+1), "sim://noise")
    display.runtimeChildren().addChild(instance)
