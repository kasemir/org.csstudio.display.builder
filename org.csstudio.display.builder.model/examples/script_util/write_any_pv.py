# Example for script that connects to PV,
# writes a value, then disconnects from the PV.
#
# This is usually a bad idea.
# It's better to have widgets connect to PVs,
# 1) More efficient. Widget connects once on start, then remains connected.
#    Widget subscribes to PV updates instead of polling its value.
# 2) Widget will reflect the connection and alarm state of the PV
# 3) Widget will properly disconnect
#
# If you don't fully understand what is happening in this script,
# don't use it.
#
# pvs[0]: PV with name of PV to which to connect
# pvs[1]: PV with value that will be written to the PV
from org.csstudio.display.builder.runtime.script import PVUtil, ScriptUtil
from org.csstudio.display.builder.runtime.pv import PVFactory
import time

pv_name = PVUtil.getString(pvs[0])
value = PVUtil.getDouble(pvs[1])

print("Should write %g to %s" % (value, pv_name))

# 1) Get PV
pv = PVFactory.getPV(pv_name)

timeout = 10

try:
    # 2) Await PV connection
    #    This is most efficiently done via a value listener:
    #    You receive the first value when the PV connects,
    #    and from then on you receive value updates.
    #    In this example, since we only want to connect once,
    #    it's easier to check for some time 
    print("Got " + str(pv))
    wait = 0
    while pv.read() is None:
        print("Wait for connection setp %d" % wait)
        time.sleep(0.5)
        wait += 1
        # 3) Handle the case that the PV doesn't connect
        if wait > timeout:
            ScriptUtil.showErrorDialog(widget, "Cannot connect to " + pv_name)
            break
    # 4) Write the value
    if pv.read() is not None:
        print("Writing %g to %s" % (value, pv_name))
        pv.write(value)
except:
    # 5) Writing might fail:
    #    PV disconnects just before we want to write,
    #    PV is read-only,
    #    PV doesn't accept this type of value
    ScriptUtil.showErrorDialog(widget, "Error writing %g to %s" % (value, pv_name))
finally:
    # 6) Finnally, on success or error, release the PV
    PVFactory.releasePV(pv)

