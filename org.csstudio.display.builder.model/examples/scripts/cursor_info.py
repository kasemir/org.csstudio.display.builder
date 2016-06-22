# Script that receives a n image widget's "Cursor Info PV"
#
# Value is a VTable with columns X, Y, Value, one row of data

table = pvs[0].read()
try:
    x = table.getColumnData(0).getDouble(0)
    xt = "left" if x < 50 else "right"
    y = table.getColumnData(1).getDouble(0)
    yt = "top" if y > 40 else "bottom"
    v = table.getColumnData(2).getDouble(0)
    text = "X: %.1f (%s) Y: %.1f (%s) Value: %.1f" % (x, xt, y, yt, v)
except:
    text = ""

widget.setPropertyValue("text", text)
