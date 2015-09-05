package org.csstudio.display.builder.representation.javafx.sandbox;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/** Uses 2 canvas widgets,
 *  updating one in background, then swapping them.
 *  
 *  @author Kay Kasemir
 */
public class Canvas2 extends Application
{
	final private Canvas[] canvas = new Canvas[]
	{ 
		DemoHelper.createCanvas(),
		DemoHelper.createCanvas()
	};
	private final StackPane canvas_stack = new StackPane(canvas[0]);
	private final AtomicLong counter = new AtomicLong();
	private final Text updates = new Text("0");
	
	public static void main(final String[] args)
	{
		launch(args);
	}

	@Override
	public void start(final Stage stage)
	{
		final Label label1 = new Label("Canvas:");
		final Label label2 = new Label("Updates:");		
		final VBox root = new VBox(label1, canvas_stack, label2, updates);
		 
		final Scene scene = new Scene(root, 800, 700);
		stage.setScene(scene);
		stage.setTitle("Swapping two Canvases");
    	 
		stage.show();
		
		final Thread thread = new Thread(this::thread_main);
		thread.setName("Redraw");
		thread.setDaemon(true);
		thread.start();
	}
	
	private void thread_main()
	{
		final Semaphore done = new Semaphore(0);
		int to_refresh = 1;
		try
		{
			while (true)
			{
				final Canvas prepared = canvas[to_refresh];
				
				// Prepare canvas off-screen
				DemoHelper.refresh(prepared);
				counter.incrementAndGet();
				
				// Swap on UI thread
				Platform.runLater(() ->
				{
					canvas_stack.getChildren().setAll(prepared);
					updates.setText(Long.toString(counter.get()));
					done.release();
				});
				
				// Wait for UI thread
				done.acquire();
				
				to_refresh = 1 - to_refresh;
				Thread.sleep(20);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}		
	}
}
