Display Builder
===============

Update of CS-Studio 'BOY',
i.e. the `org.csstudio.opibuilder.*` code in 
https://github.com/ControlSystemStudio/cs-studio.


----------
Note:
This repository contains the Display Builder for
the Eclipse/RCP-based CS-Studio using Java 8.
Since about May 2019, the Eclipse/RCP-based CS-Studio
has been updated to Java 9+, but this repo has not.

The Phoebus version of CS-Studio,
https://github.com/shroffk/phoebus,
includes most of the CS-Studio components,
including this Display Manager.
It runs on Java 11 and higher.

Going forward, additions and improvements
to the Display Builder will be in those latest sources,
i.e. the Phoebus version of CS-Studio.

The timeline for updating this repo
to compile and run with the Eclipse/RCP-based CS-Studio
using Java 9 or higher is at this time undefined.

----------

Goal: Similar functionality and "look",
ability to read existing *.opi files,
with improvements:

 * Model loads in background threads.
   Opening a new display will no longer result in user interface freeze,
   even if the display startup is delayed because of embedded displays
   or slow display file access over http.
 * Runtime handles PV updates and scripts in background threads,
   again lessening the likelihood of user interface freezeups.
 * Separation of Model, Representation, Runtime and Editor
   to facilitate long term maintainability.
 * Model without reference to details of the Representation (SWT/GEF, color, dimensions, ..)
   to allow each to be developed and optimized in parallel.
 * Representation could be SWT, AWT, .., JavaFX, favoring the latter
   because it currently promises best performance and long term
   Java support.
 * Runtime independent of representation and editor.

Try Nightly Snapshot
--------------------

Since Feb. 2017, the SNS products include the Display Builder.

Download the 'Basic EPICS' or SNS version of CS-Studio from https://ics-web.sns.ornl.gov/css/nightly

When you use a different CS-Studio product that doesn't already include the Display Builder,
you may be able to add it.
Open the menu `Help`, `Install New Software`. Enter `https://ics-web.sns.ornl.gov/css/display.builder`
as a site, select the Display Builder for installation, follow the steps in the installation dialog, restart.


Finally, check the Display Builder examples:

1. Open the menu `CS-Studio`, `Utilities`, `Install Samples` to install the `Display Builder` examples.
2. From the Navigator, open the `Display Builder/01_main.bob` file in the editor, look around,
   press the green `Execute` button in the toolbar.
3. In the Navigator, right-click on some folder and invoke `New/Other..`, `Display Editor/New Display` to create your first own display.


JavaFX Issues
-------------

The Display Builder uses Java FX as its graphics library.
If the Display Builder editor and runtime don't open up,
try other JavaFX-based components of CS-Studio,
for example invoke the Menu `CS-Studio`, `Debugging`, `Logging Configuration`,
to check if there is a general problem with JavaFX support on your computer.

 * Install the Oracle Java 8 JDK. When you fetch the JDK from Oracle, it will include JavaFX. If you install the JDK as for example packaged by RedHat, assert that you get all the pieces, including `lib/jfxswt.jar` and `lib/ext/jfxrt.jar`.
 * Start css with command-line options `-vmargs -Dorg.osgi.framework.bundle.parent=ext  -Dosgi.framework.extensions=org.eclipse.fx.osgi`. Instead of using the command line option, these settings can also be added to the product's css.ini file (Windows, Linux) or the css.app/Contents/Info.plist (Mac OS X).
 * On Linux, either set the environment variable `export SWT_GTK3=0` or add `--launcher.GTK_version 2` to the command line parameters. Eclipse SWT can use either GTK 2 or GTK 3, and will prefer the latter. Under Java 8, however, JavaFX is still limited to GTK 2, so SWT must be configured to also use GTK 2.
 * On Linux, including remote login to Linux via ssh, check that OpenGL is supported. See https://github.com/ControlSystemStudio/cs-studio/issues/1828 for details on adding the `iglx` option to the X server on Mac OS X and Linux.


Development Details
-------------------

### Dependencies

 * Oracle Java 8 JDK, at least 1.8.0_7x.
   Needs 1.8.0_60 for TableView.refresh().
   Needs 1.8.0_40 for javafx.scene.control.Dialog.
   1.8.0_51 causes ComboBoxes in editor's property panel to hang on Windows, fixed in later releases.
 * Eclipse IDE for RCP Development with `Tycho Configurator`.
   In `Preferences`, `Maven`, `Discovery`, `Open Catalog`, search for "tycho" and install it.
 * In `Preferences`, `Maven`, `Errors/Warnings`, disable all errors, for example select "Ignore" for all of them.
 * In Eclipse `Preferences`, `Java`, `Compiler`, `Errors/Warnings`, `Deprecated and restricted API`, change the level for "Forbidden Reference (access rules)" from `Error` to `Warning`.
 * In Eclipse `Preferences`, `Plugin Development`, `Target platform`,
   define target that's suitable for CS-Studio development.
   For example, use the current IDE and add a "Directory" pointing to a CSS product's `plugins/` directory.
 * In product start config, add VM options
   `-Dorg.osgi.framework.bundle.parent=ext  -Dosgi.framework.extensions=org.eclipse.fx.osgi`.
   If you then get the startup error "ENTRY org.eclipse.osgi .. MESSAGE Bundle org.eclipse.fx.osgi not found",
   assert that the org.eclipse.osgi and org.eclipse.fx.osgi plugin jar files reside
   in the same directory
   (https://www.eclipse.org/forums/index.php/t/757375).
 * Some demos may use PVs from an EPICS `softIoc` for `org.csstudio.display.builder.runtime.test/examples/demo.db`

### Source Import into IDE

Use `File`, `Import`, `Maven`, `Existing Maven Projects` to import the `org.csstudio.display.builder` source folder into the IDE.

__Note:__ The Tycho Configurator will adjust `.classpath` files,
the Team (git) support will indicate changes.
Replace the changes with the HEAD version of the repository.

(These .classfiles add access rules to permit use of javafx.* classes without warnings in the IDE)

When starting CS-Studio with the Display Builder from within the IDE, note that it doesn't seem to be possible
to switch from an IDE running with GTK 3 to a CS-Studio instance launched with GTK 2.
So in this case `export SWT_GTK3=0` is necessary for the IDE itself, which will then be able to start
CS-Studio with Display Builder, also using GTK 2.

### Entry Points

__Standalone 'main()'-type Java applications:__

 * org.csstudio.display.builder.runtime.test/RuntimeDemoJavaFX
 * org.csstudio.display.builder.runtime.test/RuntimeDemoSWT
 * org.csstudio.display.builder.editor.EditorDemo

These can be started from within the Eclipse IDE via `Run As`, `Java Application`.

On Mac OS X, note that the `Use the -XstartOnFirstThread argument when launching with SWT` option
in the `Arguments` tab of the `Run Configuration...` settings must be un-checked
except for the SWT demo. 

The runtime demos load a model, represent it in JavaFX respectively SWT,
and start a runtime which connects to process variables.

The editor demo loads a model into the editor.

The path of the initial model is hardcoded unless a model path is provided on
the command line, i.e. in the `Arguments` tab of the `Run Configuration...` settings.

__Feature for a CS-Studio product:__

 * org.csstudio.display.builder.feature
 
Registers the Display Builder editor for *.opi and *.bob files.
Display files can be executed from within the editor,
or via a `File`, `Top Displays` menu entry configured
via `org.csstudio.display.builder.rcp/top_displays`.

__Standalone Runtime product:__

 * org.csstudio.display.builder/repository/display_runtime.product
 
This product executes the Display Builder runtime as a standalone program.
The configuration settings (EPICS CA address list, ..) and the initial display file
to open need to be provided on the command line:

    USAGE: DisplayRuntime [options] /path/to/display.bob
    Options:
     -help                                        Display command line options
     -pluginCustomization /path/to/settings.ini   Macros, Channel Access, .. configuration
 
Note that you may also need to add
`-vmargs -Dorg.osgi.framework.bundle.parent=ext -Dosgi.framework.extensions=org.eclipse.fx.osgi`
to the end of the command line.

__Command-line build:__

 * build/make.sh

Allows compilation from command line, for example to automate a nightly build. Requires maven.


Code Overview
-------------

#### `org.csstudio.display.builder.model`

Describes a DisplayModel as a hierarchy of Widgets which each have Properties.
Widget Properties have well defined types. Access to properties is thread-safe.
Listeners can react to widget property changes.
Widgets and their properties can persist to and load from XML files,
using the file system (read, write) or "http:.." URLs (read).
The `examples/` directory holds example displays.

Widget categories as well as property categories combined with a well defined order of widget properties
allow editors to present them in a consistent way.

The Model reads existing *.opi files, adapting them to the current model
and writing them in the new format.

#### `org.csstudio.display.builder.representation*`

Represents Widgets in a UI toolkit, i.e. makes them visible on the screen.
Implemented for SWT and JavaFX to demonstrate that different toolkits can be supported,
but SWT implementation is limited because emphasis is on JavaFX.

The representation needs to add listeners to model properties of interest.
On change, it can prepare the UI update, which is then scheduled via `ToolkitRepresentation.scheduleUpdate()`
to occur on the UI thread in a throttled manner.

#### `org.csstudio.display.builder.runtime`

Connects widgets to PVs, executes Jython and JavaScript in background threads.
Throttled updates on user interface thread.
Resolves embedded displays relative to parent.

New widgets do not necessarily need to register their own runtime,
but can do so via an extension point.

The base `WidgetRuntime` handles the following:

 * If widget has "pv_name" and "value" properties, a primary PV is created for
   the "pv_name". The "value" is updated with the current VType.
   Representation then needs to reflect the current "value".
   
 * Widget can 'write' to the primary PV.
    
 * Each script in the "scripts" property is parsed, its PVs are created,
   and the script is triggered when any of its input PVs change.
   The script can then update widget properties.
   Similarly, "rules" are converted into scripts and then executed.

#### `org.csstudio.display.builder.model.rcp`

RCP fragment for model adds support for workspace files.

#### `org.csstudio.display.builder.editor`

Interactive display editor with Palette, Property Panel, Widget Tree,
copy/paste,
move/resize via tracker, snap-to-grid, snap-to-other-widgets,
align, distribute,
editor for points of polyline/polygon.

Considered GEF 4 which supports JFX, but still lacks basics like palette & property panel.

Major TODOs:
 * Rulers, Guides

#### `org.csstudio.display.builder.rcp`

Uses an SWT FXCanvas to display the JavaFX representation within
a current version of Eclipse/RCP.

Combines model, representation (Java FX) and runtime into RCP 'View'
for executing displays inside CS-Studio.

#### `org.csstudio.display.builder.editor.rcp`

Hosts editor inside CS-Studio as an RCP 'Editor'.

#### `org.csstudio.display.builder.editor.examples`

RCP plugin for installing the examples.

#### `org.csstudio.display.builder.util`

Non-UI utilities.

#### `org.csstudio.javafx`

Java FX helpers.

#### `org.csstudio.javafx.rtplot`

Plot widget.

#### `org.csstudio.display.builder.feature`

Eclipse feature for all of the above.

#### `repository` and `build`

P2 repository files and Maven/Tycho build support. 


Components of a Widget
----------------------

#### Graphical Widgets

A basic graphical widget can be added by implementing a Model and a Representation,
see Ellipse example
https://github.com/kasemir/org.csstudio.display.builder/commit/5abd05bcdd2a3c4fdae1ade0cbaf30de8703d814

The `EllipseWidget` model provides a `WidgetDescriptor`,
and its `defineProperties()` methods adds the desired properties
to the `BaseWidget`.
For convenience when directly accessing the widget from the representation
or scripts, the `EllipseWidget` also implements methods to access the added
properties, but that is not strictly necessary since one can always access
all properties via  `Widget.getProperty(..)`.

The `EllipseRepresentation` representation creates the actual JavaFX scene elements for
the widget. It registers listeners to the model, and updates the JavaFX scene elements
when the model changes.
Note that the representation does not directly update the elements in the model property listener.
The property model listeners are typically invoked in background threads.
The representation reacts by maybe pre-computing a color or other detail that it needs
to update the JavaFX scene elements, then sets a flag to note what needs to be updated,
and schedules an update on the toolkit's UI thread. Eventually, `updateChanges()` is
called on the UI thread, where the JavaFX elements are updated.

Graphical widgets don't directly use PVs, but the base widget does support rules
and scripts, so properties of a graphical widget could still change in response to PV updates.

#### Monitor Widgets

Widgets based on the `PVWidget` include a primary "pv_name" and "pv_value" property.
The default `WidgetRuntime` connects to the PV specified by the "pv_name"
and updates the "pv_value" with the received values.
The widget representation simply needs to listen to "pv_value" changes
in the same way as it would listen to any other property changes.
For an example, refer to the `TextUpdate` widget and its `TextUpdateRepresentation`.

Widgets that use multiple PVs need to implement their own runtime
to connect to these added PV names, typically updating PV value properties
similar to the default `WidgetRuntime`, and having their representation
listen to these changes. For an example, refer to the `XYPlotWidget`.

#### Control Widgets

Widgets that write to PVs typically react to user input.
For an example, refer to the `TextEntryRepresentation`.
When it receives user input from the JavaFX node,
we want to write a value to the PV.
That PV is maintained by the `WidgetRuntime`.
The representation, however, cannot directly access the runtime.
It is decoupled, because in edit mode there would in fact not be any runtime.
The representation sends an event via
`ToolkitRepresentation.fireWrite(Widget widget, Object value)`.
In runtime mode, the `WidgetRuntime` subscribes to these events
and writes to the PV. 

At this time, the widget model, representation and - if required - a widget specific runtime
are registered in two ways: Via extension points, and by directly listing them in the associated
Factory for standalone testing without RCP.
The widget model is registered via the `org.csstudio.display.builder.model.widgets` extension point,
and in `WidgetFactory#registerKnownWidgets`.
The widget representation is registered via the `org.csstudio.display.builder.representation.widgets`
extension point,
and added to `JFXRepresentation#registerKnownRepresentations`
or the corresponding `SWTRepresentation`.
The runtime is registered via `org.csstudio.display.builder.runtime.widgets`
and  in the `WidgetRuntimeFactory`.


Compatibility with BOY
----------------------

The Display Builder reads existing BOY `*.opi` files.

#### Widget Mappings

Most widgets and their properties aim to be compatible with their BOY counterpart.

In some cases, widget types are mapped.
BOY had a plain rectangle and a rounded rectangle widget, which has been turned
into a rectangle widget with a corner radius property.
BOY had an LED widget, which would either indicate a binary state, which could
be based on a bit in a number or zero vs. non-zero numeric value. That same LED
widget could also reflect one of N states, using very different configuration
parameters. In the Display Builder, there a separate (binary) LED and Multi-State LED widgets.
Rectangle and LED widgets are automatically mapped from `*.opi` files based on their configuration.

The BOY XYGraph had many different modes of operation,
some of which depend on the type of PV (scalar vs. waveform).
The Display Builder offers the XYPlot for plotting X and Y waveforms (with optional error waveform),
and the Data Browser plot for showing scalar PVs over time (with access to history).
Since the PV type is not known when loading a display file,
the Display Builder cannot automatically convert all XYGraphs from `*.opi` files.
It will default to the XYPlot, requiring manual conversion to a Data Browser widget.
In addition, the support for cursors and overlays is different between BOY
and the Display Builder, requiring manual conversion.

#### Groups

The BOY grouping container clipped contained widgets,
which often resulted in displays that included several widgets
which the user could never see because they had accidentally been
moved outside of the group border, yet remained inside the group.

The Display Builder will show such widgets from imported `*.opi` files.
To get the same end result, such 'orphaned' widgets need to be deleted
in the `*.opi` file. In the BOY editor, this is somewhat hard because
you cannot see them. So you need to use the Outline view to select and then
delete them.

#### Embedded Displays

In BOY, displays embedded in "Linking Containers" were merged into the main display.
They were loaded one by one, delaying the load of the complete display
until every embedded display was fetched.

In the Display Builder, embedded displays are treated as a black box.
They are loaded in parallel while the main display is already shown.
The content of each embedded display widget is then represented as soon as it resolves.

The content of an embedded display file can change.
The main display can thus not assume anything about the content of the embedded display.
Likewise, an embedded display can be included by arbitrary parent displays.
Embedded displays are therefore implemented as a sandbox.
Scripts within the main display cannot touch widgets in embedded displays
and vice versa.


#### Alarm Indication, Border

In BOY, the border reduced the usable widget area, causing the widget proper
to grow respectively shrink with border visibility.
Alarm-sensitive borders were only represented via a color.

In the Display Builder, the alarm borders are drawn around the widget,
not affecting the widget size. The alarm states are indicated via color
and line type. Even color blind users can thus distinguish the alarm state,
eliminating the need for alternate alarm indications.

While the alarm-based border can be disabled, the fundamental disconnected state
of a PV is always indicated via the respective border to assert that users
will always be aware of missing data.

#### Macros

Similar to BOY, macros can be in the format `$(macro_name)` as well as `${macro_name}`.

In contrast to EDM and BOY, macros are simply defined and possibly re-defined in the following order:

  1. Environment Variables
  2. System Properties
  3. Widget Property
  4. Preferences
  5. OpenDisplayAction
  6. EmbeddedWidget
  7. DisplayModel
  8. GroupWidget

BOY did not fall back to environment variables or system properties.

While BOY limits macros to string-based properties, more properties now support macros.
For example, the numeric 'x' position can be specified as $(POS).
If the macro does not expand to a valid specification, for example if the macro POS has the value 'eight'
instead of '8', the default value for that property will be used, and a warning is logged.

For displays that are meant as templates, to be invoked with macros,
standalone testing is possible by using the syntax `$(macro_name=default_value)`.
When such displays are invoked with macros, their values are replaced.
If they are invoked without macros, the default value is used.

BOY resp. EDM had options to _not_ inherit parent macros as well as to _not_ replace
the values of existing macros. The new implementation will always inherit all parent macros
and replace them in the order just described.
This simplifies the behavior of macros, since discussions with the implementor of EDM found
no good reason to duplicate the more complicated previous behavior.
As a technical detail, the BOY *.opi XML format treated `"include_parent_macros"`,
the option to inherit parent macros, just like the name of an ordinary macro.
This macro name is now ignored. 

Properties that support macros are based on `MacroizedWidgetProperty`.
They distinguish between the original value specification,
which is a text that may contain macros like `"$(INSTANCE_NUMBER)"`,
and the current value, which evaluates the current macro settings and may be an integer like `8`.


#### Fonts

Since available fonts differ between installations of Windows, Linux, Mac OS X,
the Display Builder defaults to the "Liberation" fonts,
which are included.

Even when the same true-type-fonts were available, the legacy CS-Studio displays rendered
fonts differently across operating systems because it failed to distinguish between
pixels on the screen and font size points.
Font sizes were specified in "points", a unit equal to 1/72 of an inch when printed on paper.
While operator displays use "pixels" for widget locations, sizes, line width etc.,
font specifications like "height 12" were in points.
For SWT as used in the legacy  CS-Studio displays, the on-screen size of fonts depends
on the resolution and size of the display.
For existing *.opi files, the desired font sizes are therefore unknown unless one can measure
them on the OS and hardware where the display was originally executed. 

Using JavaFX, fonts so far appear to be mapped 1 pixel per 1 "point" on Linux, Windows and Mac OS X.
To verify, execute `org.csstudio.display.builder.representation.javafx.JFXFontCalibration`.

For SWT, Mac OS X also results in a 1-to-1 mapping, while Windows and Linux scale the font "points"
to screen pixels depending on the display.
To determine this scaling factor, execute `org.csstudio.display.builder.representation.swt.SWTFontCalibation`.

Goal for the Display Builder is some level of compatibility with existing *.opi displays
that were created on Linux, and high levels of similarity across operating systems for
newly created displays.

Going forward, JavaFX is thus used which (at least so far) has scaling factor of 1.0
but self-configures the calibration factor on startup.
To support existing displays, each site needs to once determine the legacy scaling factor
by executing `SWTFontCalibation` on the production computer,
then configure this via `FontWidgetProperty.setLegacyFontSizeCalibration()`.

Java 8u60 may apply DPI scaling to all coordinates, which is OK
because then fonts, rectangles, ... are all scaled consistently for high resolution screens.
Alternatively, high resolution displays may require using a default zoom factor of for example 2.0
for the global zoom.

#### Rules

Rules are highly compatible between BOY and the Display Builder.

Internally, however, BOY translated rules into JavaScript, while
the Display Builder translates into Jython.
Rules with boolean expressions like `pv0 > 5  && pv1 < 2`
are translated into `pv0 > 5 and pv1 < 2`,
but expressions that invoked JavaScript methods will need to
be modified into the corresponding Jython code.


#### Scripts

Scripts are generally not portable, since the underlying widget model API is
completely different.

The legacy helper classes from `org.csstudio.opibuilder.scriptUtil` are replaced with similar classes.
For example, references to `org.csstudio.opibuilder.scriptUtil.PVUtil` need to be updated to
`org.csstudio.display.builder.runtime.script.PVUtil`.

__Jython__

Basic Jython scripts similar to this one will work without changes because of compatibility classes:
```
from org.csstudio.opibuilder.scriptUtil import PVUtil
widget.setPropertyValue("text", PVUtil.getString(pvs[0]))
```

For compatibility, classes with the original package name are included.
When accessed the first time, an error is logged:

`Script accessed deprecated org.csstudio.opibuilder.scriptUtil.PVUtil, update to org.csstudio.display.builder.runtime.script.PVUtil`.

Such Jython scripts should be updated to
```
from org.csstudio.display.builder.runtime.script import PVUtil
widget.setPropertyValue("text", PVUtil.getString(pvs[0]))
```

__Python__

In addition to Jython, the Display Builder supports real C-Python scripts.
They are invoked via Py4J, and a helper library is provided that allows
writing Jython as well as Python script in a common way.
Check online help for details.

__Java Script__

JavaScript execution is based on the Nashorn JS engine included in Java 8,
while the legacy tool used the Rhino engine.

Nashorn requires changes to Rhino scripts because 'importPackage' is no longer supported.
Instead of `importPackage`, use the fully qualified name.

Example:

```
importPackage(Packages.org.csstudio.opibuilder.scriptUtil);
widget.setPropertyValue("text", PVUtil.getString(pvs[0]));
```

needs to change into the following, including use of the new package name:

```
PVUtil = org.csstudio.display.builder.runtime.script.PVUtil;
widget.setPropertyValue("text", PVUtil.getString(pvs[0]));
```




Performance: JavaFX vs. SWT
---------------------------

CPU loads were measured with JVisualVM.
Results differ a lot between computers and operating systems.
What matters in the following is the comparison of two tests on the same computer.

`RepresentationDemoJavaFX` vs. `RepresentationDemoSWT`:

 200 Group widgets, each containing
 - Label widget,
 - Textupdate with 10Hz 'ramp' PV,
 - Rectangle with 10Hz 'noise' PV triggering Jython to change rectangle width.

Windows:
Both use the same amount of CPU (~14 %).
JFX version is fluid, time spent in UpdateThrottle waiting for UI is "0 ms".
SWT version shows jumpy rectangle updates, UpdateThrottle waits 140..200 ms for UI updates, profiler shows time spent in `org.eclipse.swt.internal.win32.OS.SetWindowPos`.

Older Ubunty Linux:
JFX version uses 11% CPU and is fluid
SWT version uses 25% CPU and can only update every 4 seconds because stuck in `_gtk_widget_size_allocate`.

Redhat 6:
JFX version uses 8% CPU, "1 ms" in UI updates.
SWT version uses 13% CPU, "10..16 ms" in UI updates.
Both appear fluid.


__-> JavaFX tends to be faster than SWT, especially on some Linux__


`RepresentationDemoJavaFX` vs. `RepresentationDemoJavaFXinSWT`

Windows: 
Updates appear the same,  UpdateThrottle waiting for UI is "0 ms",
but JavaFX in SWT FXCanvas uses about twice the CPU.
`javafx.embed.swt.FXCanvas.paintControl` shows up in profile.

Linux:
Works on RedHat 6, JDK 1.8.0_25, 64 bit.
Time in UI updates just "1 ms", but pure JavaFX has 8% CPU while FXCanvas version uses 20% CPU.
On Ubunty crashes in FXCanvas creation, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=469126

__-> JFX under Eclipse (in SWT FXCanvas) cannot get full performance of standalone JavaFX.__


Performance Comparison to CSS
-----------------------------

Both `RuntimeDemoJavaFX` and CSS can execute
`org.csstudio.display.builder.runtime.test/examples/legacy.opi`.

Linux: RuntimeDemo 10% CPU, CSS 20% CPU.


JavaFX Development Notes
------------------------

The default style sheet for JavaFX applications is modena.css, which is found in the JavaFX runtime JAR file, jfxrt.jar.
This style sheet defines styles for the root node and the UI controls.
To view this file, go to /jre/lib/ext directory of the Java Development Kit (JDK)
and extract the style sheet from the JAR file:

    jar xf jfxrt.jar com/sun/javafx/scene/control/skin/modena/modena.css

To debug the Scene Graph:
* Download Scenic View 8 from http://fxexperience.com/scenic-view
* Unpack
* Add ScenicView.jar to the build path of org.csstudio.display.builder.representation.javafx
* In JFXStageRepresentation:configureStage(), add
   ScenicView.show(scene)


What base class to use for all widget representations?

* Node (Currently used)
Most basic option, allows for any JFX item:
Canvas, Shape, Control, ...

* Region
Allows for Border (alarm sensitive border, ..).
Many widgets are based on a Region to support the alarm sensitive border.

* Control
Has Border, Context Menu, Tool Tip



How to draw custom widgets in background?

JFX Canvas offers good API. Canvas can be prepared off UI thread,
but turns out that drawing operations are simply buffered
to be executed on UI thread once canvas becomes visible,
loading the UI thread.

JFX WritableImage has very limited API.

Best option seems to use AWT to draw buffered image in background thread,
then show that in JFX Canvas.

