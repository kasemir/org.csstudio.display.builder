# import sys
# print("Search path:\n" + "\n".join(sys.path))

from my_service import read_html, format_html

text = format_html(read_html())
widget.setPropertyValue("text",  text)