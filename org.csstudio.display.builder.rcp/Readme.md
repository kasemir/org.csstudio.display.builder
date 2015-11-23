Top-Level Window
================

Standalone
----------
javafx.application.Application provides initial Stage.
Use that, later create additional Stages.
Stage.setScene()

RCP
---
Create part with SWT FXCanvas.
fx_canvas.setScene()


Type of RCP Part
----------------
E3 FXViewPart?
E4 POJO with FXCanvas? Moving forward to E4?


Or use Efxclipse for 100% JFX?
https://wiki.eclipse.org/Efxclipse

Issues
======

Missing JFX Classes
-------------------
org.eclipse.e4.core.di.InjectionException: java.lang.NoClassDefFoundError: javafx/scene/Parent

--> Add VM args -Dorg.osgi.framework.bundle.parent=ext -Dosgi.framework.extensions=org.eclipse.fx.osgi


Context Menu
------------
https://bugs.eclipse.org/bugs/show_bug.cgi?id=425160
https://bugs.openjdk.java.net/browse/JDK-8126170
