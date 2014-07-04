// Copyright 2013 SICK AG. All rights reserved.
package de.sick.guicheck.fx;

import java.lang.reflect.Method;

import javafx.collections.ObservableList;
import javafx.stage.Stage;

import com.sun.javafx.stage.StageHelper;

import de.sick.guicheck.GcAssertException;
import de.sick.guicheck.GcException;
import de.sick.guicheck.GcUtils;

/**
 * Base class of all GUIcheck tests based on JavaFX.
 * 
 * @author linggol (created)
 */
public abstract class GcTestFX
{
    private static final int FIRST_STAGE_VISIBLE_TIMEOUT = 60000;

    /**
     * Starts the given main class with the given arguments using the classloader of the current thread.
     */
    protected static final void startApp(final String clazz, final String... args)
    {
        startApp(Thread.currentThread().getContextClassLoader(), clazz, args);
    }

    /**
     * Starts the given main class with the given arguments using the given classloader.
     */
    protected static final void startApp(ClassLoader loader, final String clazz, final String... args)
    {
        Thread l_thread = new Thread("GUIcheck-FX-Runner")
        {
            @Override
            public void run()
            {
                try
                {
                    // Load the class and invoke the main method
                    Class<?> l_clazz = Thread.currentThread().getContextClassLoader().loadClass(clazz);
                    Method l_method = l_clazz.getMethod("main", String[].class);

                    l_method.invoke(null, new Object[] {args});
                }
                catch (Exception e)
                {
                    throw new GcException("Failed to invoke main method", e);
                }
            }
        };

        l_thread.setContextClassLoader(loader);
        l_thread.start();

        // Wait for platform to start and the first stage to become visible
        long l_start = System.currentTimeMillis();
        boolean l_initialized = false;
        while (System.currentTimeMillis() - l_start < FIRST_STAGE_VISIBLE_TIMEOUT)
        {
            if (GcUtilsFX.isPlatformAlive())
            {
                ObservableList<Stage> l_stages = StageHelper.getStages();
                if (l_stages.size() > 0 && l_stages.get(0).isShowing())
                {
                    l_initialized = true;
                    GcUtilsFX.waitForIdle();
                    break;
                }

                GcUtilsFX.waitForIdle();
            }
        }

        if (!l_initialized)
        {
            throw new GcException("The JavaFX platform did not initialize");
        }
    }

    /**
     * Return the stage with the given title.
     * 
     * @param titleRegEx <code>null</code> means the title is not set
     */
    public static final GcStageFX stage(final String titleRegEx)
    {
        return GcUtilsFX.eval(new GcUtils.IEvaluator<GcStageFX>()
        {
            @Override
            public GcStageFX eval()
            {
                for (Stage l_stage : StageHelper.getStages())
                {
                    if (GcUtils.startsWithOrMatches(l_stage.getTitle(), titleRegEx))
                    {
                        return new GcStageFX(l_stage);
                    }
                }

                throw new GcAssertException("Cannot find stage with title: " + titleRegEx);
            }
        });
    }

    /**
     * Return the stage with the given title.
     * 
     * @param titleRegEx <code>null</code> means the title is not set
     * @param evalRetries the number of retries
     * @param evalDelay the time in milliseconds per retry
     */
    public static final GcStageFX stage(final String titleRegEx, int evalRetries, int evalDelay)
    {
        return GcUtilsFX.eval(new GcUtils.IEvaluator<GcStageFX>()
        {
            @Override
            public GcStageFX eval()
            {
                for (Stage l_stage : StageHelper.getStages())
                {
                    if (GcUtils.startsWithOrMatches(l_stage.getTitle(), titleRegEx))
                    {
                        return new GcStageFX(l_stage);
                    }
                }

                throw new GcAssertException("Cannot find stage with title: " + titleRegEx);
            }
        }, evalRetries, evalDelay);
    }
}
