import re
import os
import urllib

print("I'm in " + __name__)

# Hack for jython's urllib, see https://github.com/PythonScanClient/PyScanClient/issues/18
if os.name == 'java':
    import sys, _socket

    def checkSocketLib():
        # Workaround: Detect closed NIO_GROUP and ee-create it
        try:
            if _socket.NIO_GROUP.isShutdown():
                _socket.NIO_GROUP = _socket.NioEventLoopGroup(2, _socket.DaemonThreadFactory("PyScan-Netty-Client-%s"))
                sys.registerCloser(_socket._shutdown_threadpool)
        except AttributeError:
            raise Exception("Jython _socket.py has changed from jython_2.7.0")
else:
    def checkSocketLib():
        # C-Python _socket.py needs no fix
        pass

def read_html():
    checkSocketLib()
    # Returns something like this:
    # html="""
    # <table class='styled'>
    # <tbody>
    #   <tr><th>Time</th><th>Title</th></tr>
    #   <tr><td>2016-04-22 07:37</td><td><a href='logbook.jsp#522722' target='_top'>IHC Beamline 11B - MaNDi</a></td></tr>
    #   <tr><td>2016-04-22 07:35</td><td><a href='logbook.jsp#522721' target='_top'>IHC Beamline 11A - PowGen</a></td></tr>
    # </tbody>
    # </table>
    # """
    f = urllib.urlopen('http://status.sns.ornl.gov/logbook_titles.jsp')
    return f.read()


def format_html(html):
    pattern = re.compile(".*<td>(.+)</td>.*<a.*>(.+)</a>.*")
    
    text = []
    for line in html.split("\n"):
        match = pattern.match(line)
        if match and len(match.groups()) == 2:
            text.append("%s - %s" % match.groups())
    return "\n".join(text)


widget.setPropertyValue("text", "Fetching entries...")
text = format_html(read_html())
widget.setPropertyValue("text",  text)