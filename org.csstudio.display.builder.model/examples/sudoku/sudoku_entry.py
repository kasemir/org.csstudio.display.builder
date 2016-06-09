display = widget.getDisplayModel()


conflicts = []

def getWidget(row, col):
    return display.runtimeChildren().getChildByName("cell%d%d" % (row, col))

def getValue(widget):
    #since values written using loc://cell("*")
    return widget.getPropertyValue("pv_name")[12:13]

def writePVValue(value, cell):
    cell.setPropertyValue("pv_name", 'loc://cell("%s")' % value)

def calcBlockRange(val):
    return range(val-val%3, val-val%3+3)

def clearConflicts():
    if PVUtil.getDouble(pvs[1]) != 0: return #if auto-solve was called
    for pos in [(x,y) for x in range(9) for y in range(9)]:
        row = pos[0]; col = pos[1]
        cell = getWidget(row, col)
        if getValue(cell) == "#":
            writePVValue(" ", cell)
        cell.setPropertyValue("foreground_color",
                              WidgetColorService.getColor(NamedWidgetColors.TEXT))

def findConflicts(row, col, value):
    for currCol in range(9):
        if currCol != col and value == getValue(getWidget(row, currCol)):
            conflicts.append((row, currCol))
    for currRow in range(9):
        if currRow != row and value == getValue(getWidget(currRow, col)):
            conflicts.append((currRow, col))
    for currRow in calcBlockRange(row):
        for currCol in calcBlockRange(col):
            if currRow != row and \
                   currCol != col and \
                   value == getValue(getWidget(currRow, currCol)):
                conflicts.append((currRow,currCol))
    if len(conflicts) > 0:
        conflicts.append((row,col))

from org.csstudio.display.builder.model.persist import WidgetColorService
from org.csstudio.display.builder.model.properties import WidgetColor
from org.csstudio.display.builder.model.persist import NamedWidgetColors

selected = int(widget.getPropertyValue("name")[4:])
value = int(PVUtil.getDouble(pvs[0]))
if value != -1: #initial value ==> ignore
    col = selected%10
    row = (selected-col)/10
    if value==0:
        value = " "
        clearConflicts()
    elif value==10: #used by solver for cells with no possible values
        conflicts.append((row,col))
    else:
        value = str(value)
        clearConflicts()
        findConflicts(row, col, value)
    if len(conflicts) > 0:
        value = "#"
        for pos in conflicts:
            cell = getWidget(pos[0], pos[1])
            cell.setPropertyValue("foreground_color",
                                  WidgetColorService.getColor(NamedWidgetColors.ALARM_MAJOR))
    writePVValue(value, widget)
    #resume normal behavior after auto-solve
    if selected == 88:
        pvs[1].write(0)
