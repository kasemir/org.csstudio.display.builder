Display Builder
===============

Beginnings of an update to CS-Studio 'BOY',
i.e. the `org.csstudio.opibuilder.*` code in 
https://github.com/ControlSystemStudio/cs-studio.

Overall goal is providing the same functionality, read-compatibility with existing *.opi files,
with the following improvements:

 * Separation of Model, Representation, Runtime and Editor
 * Model without reference to SWT/GEF (color, dimensions, ..)
 * Model loads in background threads
 * Representation could be SWT, AWT, .., JavaFX, favoring the latter
 * Runtime independent of representation and editor
 * Runtime handles PV updates and scripts in background threads.
 

Dependencies
------------

 * Java 8 SDK, must be 8u40 or later
 * In Eclipse Preferences, Java, Build Path, Classpath Variables: Set `JFXSWT` to `jre/lib/jfxswt.jar`
 * As Eclipse Target platform, use a CS-Studio target. For example use "Directory" of CSS product's `plugins/`
 * Demos use EPICS `softIoc` for `org.csstudio.display.builder.runtime.test/examples/demo.db`
 * Install the "Liberation" fonts from https://fedorahosted.org/liberation-fonts.
   Mac OS X: Double-click each *.ttf to preview, then click "Install Font".
 

Development Status
------------------

** Model **

Many essentials: Widgets, widget categories, typed properties, property categories, change notifications, persistence, named colors.

Basic Rectangle, Label, TextUpdate, LED, ProgressBar.

Group that contains child widgets,

EmbeddedDisplay widget that (at runtime) loads other *.opi file,

ActionButton that opens new *.opi, either in new window or replacing existing model.

All read existing *.opi files.

Each new widget is added as its own class derived from the base `Widget` and registered in `WidgetFactory`.

Major TODOs:
 * Add many more widgets and their properties

** Representation **

Examples for SWT and JavaFX.
Emphasis is for now JavaFX.
SWT example mostly to assert that alternate representation is possible. Purely using SWT, not Draw2D.

Each new widget needs to implement a `WidgetRepresentation` for either JavaFX or SWT (or both) and register with the `JFXRepresentation` respectively `SWTRepresentation`.
The representation needs to add listeners to model properties of interest.
On change, it can prepare the UI update, which is then scheduled via `ToolkitRepresentation.scheduleUpdate()`
to occur on the UI thread in a throttled manner.

Major TODOs:
 * Overall zoom factor (should be easy with JFX), auto-size.
 * A ton of widgets and their representation
 * How to draw custom widgets in background? Draw JFX Canvas in background,then 'swap' with active canvas? Use AWT to draw image in background?
 
** Runtime **

Connects to PVs, executes Jython and JavaScript in background threads.
Throttled updates on user interface thread.
Resolves embedded displays relative to parent.

New widgets do not necessarily need to register their own runtime.
The base `WidgetRuntime` handles the following:

 * If widget has "pv_name" and "value" properties, a primary PV is created for
   the "pv_name". The "value" is updated with the current VType.
   Representation then needs to reflect the current "value".
   
 * Widget can 'write' to the primary PV.
    
 * Each script in the "scripts" property is parsed, its PVs are created,
   and the script is triggered when any of its input PVs change.
   The script can then update widget properties.

Major TODOs:

 * Check details of script support: One instance per display? One for each action?
 * Alarm-sensitive behavior
 
** Editor **

GEF 3 is tied to SWT. GEF 4 lacks basics like palette & property panel.

Features of demo editor:
Editor, Palette, Property Panel, Widget Tree,
move/resize via tracker, snap-to-grid, snap-to-other-widgets.

Major TODOs:
 * Property editor for Actions, scripts, ...
 * Rulers, Guides
 * Editor for points of polyline, polygon


** Overall **

Everything is in form of JUnit tests or 'main' type demos.

Major TODOs:

 * RCP plugins that demo this in Eclipse/CS-Studio, using SWT FXCanvas


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


Macros
------

Similar to BOY, macros can be in the format `$(macro_name)` as well as `${macro_name}`.

In contrast to EDM and BOY, macros are simply defined and possibly re-defined in the following order:

  1. TODO Preferences
  2. OpenDisplayAction
  3. EmbeddedWidget
  4. DisplayModel
  5. GroupWidget

While BOY limits macros to string-based properties, more properties now support macros.
For example, the numeric 'x' position can be specified as $(POS).
If the macro does not expand to a valid specification, for example if the macro POS has the value 'eight'
instead of '8', the default value for that property will be used, and a warning is logged.

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
