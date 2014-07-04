// Copyright 2013 SICK AG. All rights reserved.
package de.sick.guicheck.fx;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import com.sun.javafx.stage.StageHelper;
import com.sun.javafx.tk.Toolkit;

import de.sick.guicheck.GcAssertException;
import de.sick.guicheck.GcException;
import de.sick.guicheck.GcUtils;
import de.sick.guicheck.GcUtils.IEvaluator;

/**
 * General helpers for GUIcheck tests based on JavaFX.
 * 
 * @author linggol (created)
 */
public final class GcUtilsFX
{
    private static final int EVALUATION_RETRIES = 10;
    private static final int EVALUATION_DELAY = 50;
    private static final int IDLE_COUNT = 3;
    private static final int RUN_LATER_AND_WAIT_TIMEOUT = 500;

    // EasyMock needs a litte time for synchronisation between UI and mocked objects
    private static int ms_slowMotionFactor = 10;

    /**
     * Private method in quantum toolkit to get the current windowing thread. This method is used to detect if JavaFX is
     * fully initialized and running.
     */
    private static final Method GET_FX_USER_THREAD_METHOD;
    static
    {
        try
        {
            GET_FX_USER_THREAD_METHOD = Toolkit.class.getDeclaredMethod("getFxUserThread");
            GET_FX_USER_THREAD_METHOD.setAccessible(true);
        }
        catch (NoSuchMethodException | SecurityException e)
        {
            throw new GcException("Failed to initialize access to quantum toolkit", e);
        }
    }

    private GcUtilsFX()
    {
        // Prevent instantiation
    }
    
    public static void setSlowMotion(final int factor)
    {
        ms_slowMotionFactor = factor;
    }

    /**
     * Wait for the windowing thread to become idle.
     */
    public static void waitForIdle()
    {
        waitForIdle(IDLE_COUNT, ms_slowMotionFactor);
    }

    /**
     * Wait the given sleep cycles for the windowing thread to become idle.
     */
    public static void waitForIdle(final int count, final int sleep)
    {
        for (int i = 0; i < count; i++)
        {
            runLaterAndWait(GcUtils.NOOP_RUNNABLE);
            GcUtils.sleepAndIgnoreInterrupts(sleep);
        }
    }

    /**
     * Run the given runnable in the windowing thread and wait until its finished.
     */
    public static void runLaterAndWait(final Runnable runnable)
    {
        final CountDownLatch l_latch = new CountDownLatch(1);
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                runnable.run();
                l_latch.countDown();
            }
        });

        while (true)
        {
            try
            {
                // Always check if the platform is still alive, otherwise when closing the last stage,
                // this loop hangs forever.
                if (!isPlatformAlive() || l_latch.await(RUN_LATER_AND_WAIT_TIMEOUT, TimeUnit.MILLISECONDS))
                {
                    break;
                }
            }
            catch (final InterruptedException l_exception)
            {
            }
        }
    }

    static boolean isPlatformAlive()
    {
        try
        {
            return GET_FX_USER_THREAD_METHOD.invoke(null) != null;
        }
        catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            throw new GcException("Failed to check if platform is alive", e);
        }
    }

    /**
     * Use this method to stop the test at any point and wait until all stages get closed by the program or user. The
     * program will exit after waiting. This method is especially useful while debugging with GUI tests.
     */
    public static void waitAndExitWhenAllStagesClosed()
    {
        while (StageHelper.getStages().size() > 0)
        {
            GcUtils.sleepAndIgnoreInterrupts(500);
        }

        Platform.exit();
        System.exit(0);
    }
    
    public static void treeVisibleIs(final GcNodeFX gcNode, final boolean visible)
    {
    	treeVisibleIs(gcNode.getNode(), visible);
    }

    /**
     * Check that the given {@link GcNodeFX} is visible.
     * <p>
     * The Node is visible, if itself and all of his parents are visible.
     * It's invisible if itself or any parent is invisible. 
     */
    public static void treeVisibleIs(final Node fxNode, final boolean visible)
    {
    	if (fxNode.isVisible())
    	{
        	final Parent l_parent = fxNode.getParent();
        	if (l_parent == null)
        	{
        		if (!visible)
        		{
        			throw new GcException("Unexpected value of treeVisible: Expected: false, Actual: true");
        		}
        	}
        	else
        	{
        		treeVisibleIs(l_parent, visible);
        	}
    	}
    	else
    	{
    		if (visible)
    		{
    			throw new GcException("Unexpected value of treeVisible: Expected: true, Actual: false");    			
    		}
    	}
    }

    /**
     * Evaluate the given evaluator with retries and timeouts. Retries are only done automatically if the evaluator
     * throws a {@link GcAssertException}. After each try this method waits for the windowing thread to become idle.
     */
    public static <T> T eval(final IEvaluator<T> e)
    {
        return eval(e, EVALUATION_RETRIES, EVALUATION_DELAY);
    }

    /**
     * Evaluate the given evaluator with retries and timeouts. Retries are only done automatically if the evaluator
     * throws a {@link GcAssertException}. After each try this method waits for the windowing thread to become idle.
     */
    public static <T> T eval(final IEvaluator<T> e, int evalRetries, int evalDelay)
    {
        for (int i = 0; i < evalRetries - 1; i++)
        {
            try
            {
                return e.eval();
            }
            catch (final GcAssertException l_exception)
            {
                GcUtils.sleepAndIgnoreInterrupts(evalDelay);
                waitForIdle();
            }
        }
    
        // The last time we try it without catching any exceptions
        return e.eval();
    }
    
    /**
     * Check whether the given menu path exists.
     * 
     * @param menuPath Path of menus and a menu item separated by / chars.
     * @throws GcAssertException Thrown if the path does not exist.
     */
    static void menuPathExists(final Collection<? extends MenuItem> menuItems, final String menuPath)
    {
        GcUtilsFX.eval(new GcUtils.IEvaluator<Void>()
        {
            @Override
            public Void eval()
            {
                getMenuPath(menuItems, menuPath);
                return null;
            }
        });
    }

    static ArrayList<MenuItem> getMenuPath(Collection<? extends MenuItem> menuItems, String menuPath)
    {
        ArrayList<MenuItem> l_result = new ArrayList<MenuItem>();
        ArrayList<MenuItem> l_menus = new ArrayList<MenuItem>(menuItems);

        // Run down the menu path and collect the menus in the result collection
        for (String id : menuPath.split("/"))
        {
            boolean l_found = false;
            for (final MenuItem l_menu : l_menus)
            {
                if (id.equals(l_menu.getId()))
                {
                    l_found = true;
                    l_result.add(l_menu);
                    l_menus.clear();

                    if (l_menu instanceof Menu)
                    {
                        l_menus.addAll(((Menu)l_menu).getItems());
                    }
                    break;
                }
            }

            if (!l_found)
            {
                throw new GcAssertException("Cannot find menu path: " + menuPath);
            }
        }

        return l_result;
    }
}
