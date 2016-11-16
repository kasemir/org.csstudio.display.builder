/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.databrowser3.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.csstudio.csdata.ProcessVariable;
import org.csstudio.display.builder.rcp.JFXCursorFix;
import org.csstudio.javafx.rtplot.RTTimePlot;
import org.csstudio.trends.databrowser3.Messages;
import org.csstudio.trends.databrowser3.model.ArchiveDataSource;
import org.csstudio.trends.databrowser3.model.ChannelInfo;
import org.csstudio.trends.databrowser3.model.Model;
import org.csstudio.ui.util.dialogs.ExceptionDetailsErrorDialog;
import org.csstudio.ui.util.dnd.ControlSystemDropTarget;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.widgets.Composite;
import javafx.scene.Scene;
import javafx.embed.swt.FXCanvas;

/** Data Browser 'Plot' that displays the samples in a {@link Model}.
 *  <p>
 *  Links the underlying {@link RTTimePlot} to the {@link Model}.
 *
 *  @author Kay Kasemir
 *  @author Laurent PHILIPPE Modify addListener method to add property changed event capability
 */
@SuppressWarnings("nls")
public class ModelBasedPlotSWT extends ModelBasedPlot
{
    /** {@link Display} used by this plot */
    //final private Display display;

    private final FXCanvas canvas;

    //final private SWTMediaPool media;

    /** Initialize plot
     *  @param parent Parent widget
     * @throws Exception
     */
    public ModelBasedPlotSWT(final Composite parent) throws Exception
    {
        //media = new SWTMediaPool(parent.getDisplay());
        //this.display = parent.getDisplay();

        canvas = new FXCanvas(parent, 0);
        initBase(true);
        final Scene scene = new Scene(plot);
        canvas.setScene(scene);

        JFXCursorFix.apply(scene, canvas);

        hookDragAndDrop();
    }

    /** @return FXCanvas */
    public FXCanvas getCanvas()
    {
        return canvas;
    }

    /**
     * Attach to drag-and-drop, notifying the plot listener
     *
     * @param canvas
     */
    private void hookDragAndDrop()
    {
        // The droptarget gets set automatically for fxcanvas in setscene
        // Which will cause the ControlSystemDropTarget constructor to fail
        // unless we remove the drop target
        canvas.setData(DND.DROP_TARGET_KEY, null);

        // Allow dropped arrays
        new ControlSystemDropTarget(canvas, ChannelInfo[].class,
                ProcessVariable[].class, ArchiveDataSource[].class,
                File.class,
                String.class)
        {
            @Override
            public void handleDrop(final Object item)
            {
                final PlotListener lst = listener.orElse(null);
                if (lst == null)
                    return;

                if (item instanceof ChannelInfo[])
                {
                    final ChannelInfo[] channels = (ChannelInfo[]) item;
                    final int N = channels.length;
                    final ProcessVariable[] pvs = new ProcessVariable[N];
                    final ArchiveDataSource[] archives = new ArchiveDataSource[N];
                    for (int i=0; i<N; ++i)
                    {
                        pvs[i] = channels[i].getProcessVariable();
                        archives[i] = channels[i].getArchiveDataSource();
                    }
                    lst.droppedPVNames(pvs, archives);
                }
                else if (item instanceof ProcessVariable[])
                {
                    final ProcessVariable[] pvs = (ProcessVariable[]) item;
                    lst.droppedPVNames(pvs, null);
                }
                else if (item instanceof ArchiveDataSource[])
                {
                    final ArchiveDataSource[] archives = (ArchiveDataSource[]) item;
                    lst.droppedPVNames(null, archives);
                }
                else if (item instanceof String)
                {
                    final List<String> pvs = new ArrayList<>();
                    // Allow passing in many names, assuming that white space separates them
                    final String[] names = ((String)item).split("[\\r\\n\\t ]+"); //$NON-NLS-1$
                    for (String one_name : names)
                    {   // Might also have received "[pv1, pv2, pv2]", turn that into "pv1", "pv2", "pv3"
                        String suggestion = one_name;
                        if (suggestion.startsWith("["))
                            suggestion = suggestion.substring(1);
                        if (suggestion.endsWith("]")  &&  !suggestion.contains("["))
                            suggestion = suggestion.substring(0, suggestion.length()-1);
                        if (suggestion.endsWith(","))
                            suggestion = suggestion.substring(0, suggestion.length()-1);
                        pvs.add(suggestion);
                    }
                    if (pvs.size() > 0)
                        lst.droppedNames(pvs.toArray(new String[pvs.size()]));
                }
                else if (item instanceof String[])
                {   // File names arrive as String[]...
                    final String[] files = (String[])item;
                    try
                    {
                        for (String filename : files)
                            lst.droppedFilename(filename);
                    }
                    catch (Exception ex)
                    {
                        ExceptionDetailsErrorDialog.openError(canvas.getShell(), Messages.Error, ex);
                    }
                }
            }
        };
    }
}
