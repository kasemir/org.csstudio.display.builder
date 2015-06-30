# Demo python script that changes the 'width' of a widget
from time import clock
from math import pi, sin

orig_width = 80
amp = 10
period = 2

width = int(orig_width + amp*sin(2.0*pi * clock()/period))

widget.setPropertyValue('x', amp + orig_width-width)
widget.setPropertyValue('width', width)
