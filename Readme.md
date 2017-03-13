Display Builder
===============

Update of CS-Studio 'BOY',
i.e. the `org.csstudio.opibuilder.*` code in 
https://github.com/ControlSystemStudio/cs-studio.

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

Since Feb. 2017, the SNS products include the display builder.

Download the 'Basic EPICS' or SNS version of CS-Studio from http://ics-web.sns.ornl.gov/css/nightly

When you use a different CS-Studio product that doesn't already include the display builder,
you may be able to add it.
Open the menu `Help`, `Install New Software`. Enter `http://ics-web.sns.ornl.gov/css/display.builder`
as a site, select the display builder for installation, follow the steps in the installation dialog, restart.


Finally, check the display builder examples:

1. Open the menu `CS-Studio`, `Utilities`, `Install Samples` to install the `Display Builder` examples.
2. From the Navigator, open the `Display Builder/01_main.bob` file in the editor, look around,
   press the green `Execute` button in the toolbar.
3. In the Navigator, right-click on some folder and invoke `New/Other..`, `Display Editor/New Display` to create your first own display.


JavaFX Issues
-------------

The display builder uses Java FX as its graphics library.
If the display builder editor and runtime don't open up,
try other JavaFX-based components of CS-Studio,
for example invoke the Menu `CS-Studio`, `Debugging`, `Logging Configuration`,
to check if there is a general problem with JavaFX support on your computer.

 * Install the Oracle Java 8 JDK. When you fetch the JDK from Oracle, it will include JavaFX. If you install the JDK as for example packaged by RedHat, assert that you get all the pieces, including `lib/jfxswt.jar` and `lib/ext/jfxrt.jar`.
 * Start css with command-line options `-vmargs -Dorg.osgi.framework.bundle.parent=ext  -Dosgi.framework.extensions=org.eclipse.fx.osgi`. Instead of using the command line option, these settings can also be added to the product's css.ini file (Windows, Linux) or the css.app/Contents/Info.plist (Mac OS X).
 * On Linux, either set the environment variable `export SWT_GTK3=0` or add `--launcher.GTK_version 2` to the command line parameters. Eclipse SWT can use either GTK 2 or GTK 3, and will prefer the latter. JavaFX, however, is still limited to GTK 2, so SWT must be configured to also use GTK 2.
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
 
This product executes the display builder runtime as a standalone program.
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

`org.csstudio.display.builder.model`,
Describes a DisplayModel as a hierarchy of Widgets which each have Properties.
Can load displays from local file system and "http:.." URLs.
`examples/` directory holds example displays.

`org.csstudio.display.builder.representation`, 
`org.csstudio.display.builder.representation.javafx`,
`org.csstudio.display.builder.representation.swt`:
Graphical rendering of model on screen, with implementation for Java FX and SWT.
(SWT implementation is very limited)

`org.csstudio.display.builder.runtime`:
Connects widgets to process variables, executes scripts, executes actions when
user presses buttons etc.

`org.csstudio.display.builder.model.rcp`:
RCP fragment for model adds support for workspace files.

`org.csstudio.display.builder.rcp`:
Combines model, representation (Java FX) and runtime into RCP 'View'
for executing displays inside CS-Studio.

`org.csstudio.display.builder.editor`:
Display editor, implemented in Java FX.

`org.csstudio.display.builder.editor.rcp`:
Hosts editor inside CS-Studio.

`org.csstudio.display.builder.editor.examples`:
RCP plugin for installing the examples.

`org.csstudio.display.builder.util`,
`org.csstudio.javafx`,
`org.csstudio.javafx.rtplot`:
Utilities; Generic, Java FX, Plot widget.

`org.csstudio.display.builder.feature`:
Eclipse feature for all of the above.

`repository` and `build`:
P2 repository files and Maven/Tycho build support. 


Basic widgets can be added by implementing a Model and a Representation,
see Ellipse example
https://github.com/kasemir/org.csstudio.display.builder/commit/5abd05bcdd2a3c4fdae1ade0cbaf30de8703d814
 

Development Status
------------------

#### Model

Describes Widgets and their Properties.
Widget Properties have well defined types. Access to properties is thread-safe.
Listeners can react to widget property changes.
Widgets and their properties can persist to and load from XML files.
Widget categories as well as property categories combined with a well defined order of widget properties
allow editors to present them in a consistent way.

The Model reads existing *.opi files, adapting them to the current model
and writing them in the new format.

Available basic widgets include Rectangle, Label, TextUpdate, LED, ProgressBar with their essential properties.

Widgets with key functionality:
* Group that contains child widgets,
* EmbeddedDisplay widget that (at runtime) loads other *.opi file,
* ActionButton that opens new *.opi, either in new window or replacing existing model.

To add a new widget, implement a new widget model based on the `Widget` class.
Register via extension point.
To support standalone testing w/o RCP, also add to `WidgetFactory#registerKnownWidgets`.

Major TODOs:
 * Add more widgets and their properties.

####  Representation

Represents Widgets in a UI toolkit, i.e. makes them visible on the screen.
Implemented for SWT and JavaFX to demonstrate that different toolkits can be supported,
but SWT implementation is limited because emphasis is on JavaFX.

To represent a new widget, implement a `WidgetRepresentation` for either JavaFX or SWT (or both)
and register with the `JFXRepresentation` respectively `SWTRepresentation`
via an extension point.
To support standalone testing w/o RCP, also add to `JFXRepresentation#registerKnownRepresentations`
or the corresponding `SWTRepresentation`.

The representation needs to add listeners to model properties of interest.
On change, it can prepare the UI update, which is then scheduled via `ToolkitRepresentation.scheduleUpdate()`
to occur on the UI thread in a throttled manner.

Major TODOs:
 * Mode widgets and their representation.
 
####  Runtime

Connects to PVs, executes Jython and JavaScript in background threads.
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

Major TODOs:
 * None?
 
####  Editor

Interactive display editor.

New JFX-based development has Palette, Property Panel, Widget Tree,
copy/paste,
move/resize via tracker, snap-to-grid, snap-to-other-widgets,
align, distribute,
editor for points of polyline/polygon.

Considered GEF 4 which supports JFX, but still lacks basics like palette & property panel.

Major TODOs:
 * Rulers, Guides

####  Eclipse Integration

RCP integration uses an SWT FXCanvas to display the JavaFX representation within
a current version of Eclipse/RCP.

An RCP 'View' hosts the display runtime, while an RCP 'Editor' is used for the display editor.

Major TODOs:
 * None?


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


Macros
------

Similar to BOY, macros can be in the format `$(macro_name)` as well as `${macro_name}`.

In contrast to EDM and BOY, macros are simply defined and possibly re-defined in the following order:

  1. Preferences
  2. OpenDisplayAction
  3. EmbeddedWidget
  4. DisplayModel
  5. GroupWidget

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

Compared to `org.csstudio.apputil.macros.MacroUtil`, `org.csstudio.display.builder.model.macros.MacroHandler` simply recurses instead of maintaining its own stack/lockstack/parsedMacros.
Need to check for infinite loop.

Properties that support macros are based on `MacroizedWidgetProperty`.
They distinguish between the original value specification,
which is a text that may contain macros like `"$(INSTANCE_NUMBER)"`,
and the current value, which evaluates the current macro settings and may be an integer like `8`.


Fonts
-----

Available fonts differ between installations of Windows, Linux, Mac OS X.
New developments should install open source fonts that are available for all operating systems,
for example the "Liberation" fonts.

Even when the same true-type-fonts were available, the legacy CS-Studio displays rendered
fonts differently across operating systems because it failed to distinguish between
pixels on the screen and font size points.
Font sizes are specified in "points", a unit equal to 1/72 of an inch when printed on paper.
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

Goal for the display builder is some level of compatibility with existing *.opi displays
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


Scripts
-------

The legacy helper classes from `org.csstudio.opibuilder.scriptUtil` are being replaced with similar classes.
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

In addition to Jython, the display builder supports real C-Python scripts.
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
