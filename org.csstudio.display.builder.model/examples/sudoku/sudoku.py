"""
Sudoku board creator.

@author Amanda Carpenter
"""

from org.csstudio.display.builder.runtime.script import PVUtil

from java.util.logging import Logger
logger = Logger.getLogger("script")

pvVal = int(PVUtil.getDouble(pvs[0]))
if pvVal == 3:
    board = [ [0,2,0, 0,0,4, 3,0,0],
              [9,0,0, 0,2,0, 0,0,8],
              [0,0,0, 6,0,9, 0,5,0],
              
              [0,0,0, 0,0,0, 0,0,1],
              [0,7,2, 5,0,3, 6,8,0],
              [6,0,0, 0,0,0, 0,0,0],

              [0,8,0, 2,0,5, 0,0,0],
              [1,0,0, 0,9,0, 0,0,3],
              [0,0,9, 8,0,0, 0,6,0] ]
elif pvVal == 2:
    board = [ [5,3,0, 0,7,0, 0,0,0],
              [6,0,0, 1,9,5, 0,0,0],
              [0,9,8, 0,0,0, 0,6,0],
              
              [8,0,0, 0,6,0, 0,0,3],
              [4,0,0, 8,0,3, 0,0,1],
              [7,0,0, 0,2,0, 0,0,6],
              
              [0,6,0, 0,0,0, 2,8,0],
              [0,0,0, 4,1,9, 0,0,5],
              [0,0,0, 0,8,0, 0,7,9] ]
else:
    board = [ [0,0,0, 0,9,0, 4,0,3],
              [0,0,3, 0,1,0, 0,9,6],
              [2,0,0, 6,4,0, 0,0,7],
              
              [4,0,0, 5,0,0, 0,6,0],
              [0,0,1, 0,0,0, 8,0,0],
              [0,6,0, 0,0,1, 0,0,2],
              
              [1,0,0, 0,7,4, 0,0,5],
              [8,2,0, 0,6,0, 7,0,0],
              [7,0,4, 0,5,0, 0,0,0] ]

def writePVValue(value, cell):
    cell.setPropertyValue("pv_name", 'loc://cell("%s")' % value)


from org.csstudio.display.builder.model.persist import WidgetColorService
from org.csstudio.display.builder.model.properties import WidgetColor
from org.csstudio.display.builder.model.persist import NamedWidgetColors

display = widget.getDisplayModel()
text = WidgetColorService.getColor(NamedWidgetColors.TEXT)

def createBoard():
    background = display.displayBackgroundColor().getValue()
    index = 0
    for row in range(9):
        for col in range(9):
            cell = display.runtimeChildren().getChildByName("cell%d%d" % (row,col))
            value = board[row][col]
            if value != 0:
                cell.setPropertyValue("background_color", background)
                writePVValue(value, cell)
            else:
                writePVValue(" ", cell)
                cell.setPropertyValue("background_color", cell.displayBackgroundColor().getDefaultValue())
            cell.setPropertyValue("foreground_color", text)
    pvs[1].write(0) #do not auto-solve

createBoard()