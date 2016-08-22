# import sys
# print("Search path:\n" + "\n".join(sys.path))

widget.setValue([ [ "-", "Fetching Logbook entries..." ] ])

from my_service import read_html, create_table
html = read_html()
widget.setValue(create_table(html))
