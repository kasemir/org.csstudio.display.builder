import sys
sys.path.append('/Users/cj5/git/org.csstudio.display.builder/org.csstudio.display.builder.model/examples/sudoku')

from sudoku_lib import SudokuSolver

display = widget.getDisplayModel()

def writePVValue(value, cell):
    cell.setPropertyValue("pv_name", 'loc://cell("%s")' % value)

def getWidget(row, col):
    return display.runtimeChildren().getChildByName("cell%d%d" % (row, col))

def getValue(widget):
    #since values written using loc://cell("*")
    return widget.getPropertyValue("pv_name")[12:13]

def readBoard():
    board = [[0] * 9 for x in range(9)]
    for row in range(9):
        for col in range(9):
            value = getValue(getWidget(row,col))
            if value == " " or value == "#":
                value = 0
            else:
                value = int(value)
            board[row][col] = value
    return board

def writeBoard(board):
    for row in range(9):
        for col in range(9):
            pv = pvs[1+row*9+col]
            cell = getWidget(row, col)
            color = display.displayBackgroundColor().getValue()
            #only write into non-preset cells
            value = board[row][col]
            if value > 0 and cell.getPropertyValue("background_color") != color:
                pv.write(value)
            #else: deliberately ignored
def clearBoard():
    for index in range (1,82):
        pv = pvs[index]
        if PVUtil.getDouble(pv) != -1:
            pv.write(0)

#------ begin execution ------
if int(PVUtil.getDouble(pvs[0])) == 1:
    board = readBoard()
    solver = SudokuSolver(board)
    board = solver.solve()
    writeBoard(board)