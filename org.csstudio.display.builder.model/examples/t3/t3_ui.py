"""T3 UI

   Script is compiled once, but
   invoked each time user enters a move.
   
   Widget's user data tracks
   the T3 board across invocations.
   
   @author Kay Kasemir
"""

# "widget" is really the display model,
# because script is attached to display
display = widget


# TODO Should Runtime add directory of the *.opi to path?
from java.io import File
file = File(display.getUserData("_input_file"))
dir = file.getParent()
import sys
sys.path.append(dir)


from t3 import T3
from org.csstudio.display.builder.runtime.script import PVUtil

def showBoard(t3):
    for pos in range(9):
        w = display.getChildByName("ttt%d" % pos)
        piece = t3.board[pos]
        if piece == ' ':
            w.setPropertyValue("text", str(pos))
        else:
            w.setPropertyValue("text", str(piece).upper())


def showInfo(text):
    w = display.getChildByName("info")
    w.setPropertyValue("text", str(text))


t3 = display.getUserData("T3")
if t3 is None:
    # Initialize new board
    t3 = T3()
    display.setUserData("T3", t3)
    showBoard(t3)
else:
    # User entered a move
    pos = int(PVUtil.getString(pvs[0]))
    if not (0 <= pos < 9):
        showInfo("Field must be 0 .. 8")
    elif t3.board[pos] != ' ':
        showInfo("Field %d already occupied!" % pos)
    else:
        # Perform user's move
        t3.board[pos] = 'x'
        showBoard(t3)
        
        # Compute computer's move
        test = T3(t3.board)
        pos = test.makeMove('o')
        if pos is not None:
            showInfo("I set " + str(pos))
            t3.board[pos] = 'o'
            showBoard(t3)
        
        winner = t3.determineWinner()
        if winner == 'x':
            showInfo("You're the Weener!!")
        elif winner == 'o':
            showInfo("You snooze, you loose!")
        elif t3.isDone():
            showInfo("It's a draw...")
