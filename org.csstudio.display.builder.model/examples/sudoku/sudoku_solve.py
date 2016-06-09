display = widget.getDisplayModel()

def writePVValue(value, cell):
    cell.setPropertyValue("pv_name", 'loc://cell("%s")' % value)

def getWidget(row, col):
    return display.runtimeChildren().getChildByName("cell%d%d" % (row, col))

def getValue(widget):
    #since values written using loc://cell("*")
    return widget.getPropertyValue("pv_name")[12:13]

def readBoard():
    global board
    global poss
    #for index in range(?, ?):
        #value = PVUtil.getString(pvs[index])
    for row in range(9):
        for col in range(9):
            value = getValue(getWidget(row,col))
            if value == " " or value == "#":
                value = 0
            else:
                value = int(value)
            if value != 0:
                solveCell(row, col, value)
            else:
                board[row*9+col] = value

from org.csstudio.display.builder.model.persist import WidgetColorService
from org.csstudio.display.builder.model.properties import WidgetColor
from org.csstudio.display.builder.model.persist import NamedWidgetColors
def writeBoard():
    for row in range(9):
        for col in range(9):
            index = row*9+col
            pv = pvs[1+index]
            cell = getWidget(row, col)
            color = display.displayBackgroundColor().getValue()
            #only write into non-preset cells
            value = board[index]
            if value > 0 and cell.getPropertyValue("background_color") != color:
                pv.write(value)
            #else: deliberately ignored
def clearBoard():
    for index in range (1,82):
        pv = pvs[index]
        if PVUtil.getDouble(pv) != -1:
            pv.write(0)
def printBoard():
    for x in range(9):
        logger.warning("board:%r" % board[x*9:x*9+9])

#------ for solving ------
numSolvedCells = 0
isChangedPoss = True
board = [0] * 81
#poss: possibilities for values
poss = [ [x for x in range(1,10)] for i in range(81)]
    #using [] * 81 syntax results in all cells referencing the same array

def calcBlockRange(val):
    return range(val-val%3, val-val%3+3)

def solveCell(row, col, val):
    if board[row*9+col] != 0 or val == 0: return
    global isChangedPoss
    global numSolvedCells
    board[row*9+col] = val
    poss[row*9+col] = [val]
    isChangedPoss = True
    for currRow in range(9):
        if currRow != row:
            removeValsFromCell(currRow, col, val)
    for currCol in range(9):
        if currCol != col:
            removeValsFromCell(row, currCol, val)
    for currRow in calcBlockRange(row):
        for currCol in calcBlockRange(col):
            if currRow != row or currCol != col:
                removeValsFromCell(currRow, currCol, val)
    numSolvedCells+=1
    
def removeValsFromCell(row, col, *vals):
    for val in vals:
        if poss[row*9+col].count(val) > 0:
            poss[row*9+col].remove(val)

def removePairFromRow(row, col1, col2, val1, val2):
    for col in range(9):
        if col != col1 and col != col2:
            removeValsFromCell(row, col, val1, val2)

def removePairFromCol(row1, row2, col, val1, val2):
    for row in range(9):
        if row != row1 and row != row2:
            removeValsFromCell(row, col, val1, val2)

def removePairFromBlock(pos1, pos2, val1, val2):
    for row in calcBlockRange(pos1[0]):
        for col in calcBlockRange(pos1[1]):
            if (row != pos1[0] or col != pos1[1]) and \
                    (row != pos2[0] or col != pos2[1]):
                removeValsFromCell(row, col, val1, val2)

def scanUnit(numFoundOf, lastFoundAt):
    global isChangedPoss
    for i in range(9):
        numFound = numFoundOf[i]
        if numFound == 1:
            row = lastFoundAt[i][0]; col = lastFoundAt[i][1]
            solveCell(row, col, i+1)

def isSolvableRow(row):
    numFoundOf = [0] * 9
    lastFoundAt = [(-1,-1)] * 9
    for col in range(9):
        if not isSolvableCell(row, col, numFoundOf, lastFoundAt):
            return False
    scanUnit(numFoundOf, lastFoundAt)
    return True
def isSolvableCol(col):
    numFoundOf = [0] * 9
    lastFoundAt = [(-1,-1)] * 9
    for row in range(9):
        if not isSolvableCell(row, col, numFoundOf, lastFoundAt):
            return False
    scanUnit(numFoundOf, lastFoundAt)
    return True
def isSolvableBlock(rowStart, colStart):
    numFoundOf = [0] * 9
    lastFoundAt = [(-1,-1)] * 9
    for row in range(rowStart, rowStart+3):
        for col in range(colStart, colStart+3):
            if not isSolvableCell(row, col, numFoundOf, lastFoundAt):
                return False
    scanUnit(numFoundOf, lastFoundAt)
    return True
def isSolvableCell(row, col, numFoundOf, lastFoundAt):
    global isChangedPoss
    currPoss = poss[row*9+col]
    numPoss = len(currPoss)
    if numPoss==0:
        board[row*9+col] = 10
        return False
    elif numPoss==1:
        value = poss[row*9+col][0]
        solveCell(row, col, value)
        numFoundOf[value-1] = -1
    else:
        for possVal in currPoss:
             if numFoundOf[possVal-1] != -1:
                 numFoundOf[possVal-1] += 1
                 lastFoundAt[possVal-1] = (row, col)
    return True

def solveBoard():
    global numSolvedCells
    global isChangedPoss
    while numSolvedCells < 81 and isChangedPoss:
        isChangedPoss = False
        for x in range(9):
            if not isSolvableRow(x) or not isSolvableCol(x) or not isSolvableBlock(x-x%3, x%3*3):
                isChangedPoss = False

#------ begin execution ------
if int(PVUtil.getDouble(pvs[0])) == 1:
    readBoard()
    solveBoard()
    writeBoard()