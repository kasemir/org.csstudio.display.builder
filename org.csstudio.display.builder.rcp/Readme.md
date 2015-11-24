Top-Level Window
================

Part with SWT FXCanvas, then call fx_canvas.setScene()


Type of RCP Part
----------------
E3 FXViewPart? Has no advantage over E3 ViewPart with FXCanvas.

E3 ViewPart: Has access to memento.

E4 POJO with FXCanvas? Moving forward to E4?
Unclear how to get memento, or how to get the concrete class when opening new part.

Long term: Use Efxclipse for 100% JFX.
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
