# Script that receives a n image widget's "Cursor Info PV"
#
# Value is a VTable with columns X, Y, Value, one row of data

table = pvs[0].read()
try:
    x = table.getColumnData(0).getDouble(0)
    y = table.getColumnData(1).getDouble(0)
    v = table.getColumnData(2).getDouble(0)
    text = "X: %.1f Y: %.1f Value: %.1f" % (x, y, v)
except:
    text = ""

widget.setPropertyValue("text", text)
