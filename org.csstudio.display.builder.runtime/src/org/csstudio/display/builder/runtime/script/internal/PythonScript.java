package org.csstudio.display.builder.runtime.script.internal;

import java.util.concurrent.Future;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.runtime.pv.RuntimePV;

/** Python script */
public class PythonScript implements Script
{
    private final PythonScriptSupport support;
    private final String name;
    private final String path;

    /**
     * Prepare submittable script object
     *
     * @param support {@link PythonScriptSupport} that will execute this script
     * @param name Name of script (file name)
     * @param path Path (including name) of script
     */
    public PythonScript(final PythonScriptSupport support, final String name, final String path)
    {
        this.support = support;
        this.name = name;
        this.path = name;
    }

    /** @return Name of script (file name, URL) */
    public String getName()
    {
        return name;
    }

    /** @return Path (including name) of script */
    public String getPath()
    {
        return path;
    }

    @Override
    public Future<Object> submit(final Widget widget, final RuntimePV... pvs)
    {
        return support.submit(this, widget, pvs);
    }

    @Override
    public String toString()
    {
        return "Python script " + name;
    }
}
