Plot for JavaFX
===============

Uses JavaFX as visible API.

Internally, it could use JavaFX "Axis" etc., but those paint on the UI thread, don't support 'inverted' limits.

Could paint JFX Canvas in background, but at least on Linux that only results in queued drawing operations
which are executed on the UI thread once the Canvas becomes visible, or result in out-of-memory if the canvas
is never shown but continued to be drawn.

Could use JFX WritableImage to paint in background, but that has very limited API.

--> Creating AWT image in background.

