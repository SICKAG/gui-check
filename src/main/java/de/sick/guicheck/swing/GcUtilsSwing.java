// Copyright 2013 SICK AG. All rights reserved.
package de.sick.guicheck.swing;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

import javax.swing.SwingUtilities;

import de.sick.guicheck.GcAssertException;
import de.sick.guicheck.GcUtils;
import de.sick.guicheck.GcUtils.IEvaluator;

/**
 * General helpers for GUIcheck tests based on Swing.
 * 
 * @author linggol (created)
 */
public final class GcUtilsSwing
{
    private static final int EVALUATION_RETRIES = 10;
    private static final int EVALUATION_DELAY = 50;
    private static final int IDLE_COUNT = 3;
    private static final int RUN_LATER_AND_WAIT_TIMEOUT = 500;

    // EasyMock needs a litte time for synchronisation between UI and mocked objects
    private static int ms_slowMotionFactor = 10;

    public static void setSlowMotion(int factor)
    {
        ms_slowMotionFactor = factor;
    }

    private GcUtilsSwing()
    {
        // Prevent instantiation
    }

    /**
     * Wait for the EDT thread to become idle.
     */
    public static void waitForIdle()
    {
        waitForIdle(IDLE_COUNT, ms_slowMotionFactor);
    }

    /**
     * Wait the given sleep cycles for the EDT to become idle.
     */
    public static void waitForIdle(int count, int sleep)
    {
        for (int i = 0; i < count; i++)
        {
            runLaterAndWait(GcUtils.NOOP_RUNNABLE);
            GcUtils.sleepAndIgnoreInterrupts(sleep);
        }
    }

    /**
     * Run the given runnable in the EDT and wait until its finished.
     */
    public static void runLaterAndWait(final Runnable runnable)
    {
        final CountDownLatch l_latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(new Runnable()
        {
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
                if (l_latch.await(RUN_LATER_AND_WAIT_TIMEOUT, TimeUnit.MILLISECONDS))
                {
                    break;
                }
            }
            catch (InterruptedException e)
            {
            }
        }
    }

    /**
     * Use this method to stop the test at any point and wait until all windows get closed by the program or user. The
     * program will exit after waiting. This method is especially useful while debugging with GUI tests.
     */
    public static void waitAndExitWhenAllWindowsClosed()
    {
        while (Window.getWindows().length > 0)
        {
            GcUtils.sleepAndIgnoreInterrupts(500);
        }

        Platform.exit();
        System.exit(0);
    }

    /**
     * Return the window with the given title.
     * 
     * @param titleRegEx <code>null</code> means the title is not set
     */
    public static final GcWindowSwing window(final String titleRegEx)
    {
        return eval(new GcUtils.IEvaluator<GcWindowSwing>()
        {
            @Override
            public GcWindowSwing eval()
            {
                final GcWindowSwing l_window = getWindowRaw(titleRegEx);
                if (l_window == null)
                {
                    throw new GcAssertException("Cannot find window with title: " + titleRegEx);
                }
                return l_window;
            }
        });
    }

    /**
     * Return the window with the given title.
     * 
     * @param titleRegEx <code>null</code> means the title is not set
     */
    public static final GcWindowSwing window(final String titleRegEx, int evalRetries, int evalDelay)
    {
        return eval(new GcUtils.IEvaluator<GcWindowSwing>()
        {
            @Override
            public GcWindowSwing eval()
            {
                final GcWindowSwing l_window = getWindowRaw(titleRegEx);
                if (l_window == null)
                {
                    throw new GcAssertException("Cannot find window with title: " + titleRegEx);
                }
                return l_window;
            }
        }, evalRetries, evalDelay);
    }

    /**
     * Return the window with the given title. Use a more immediate mode without retries and timeouts. It also does not
     * throw any exception instead it returns <code>null</code> if the window is not found.
     * <p>
     * <b>Warning: Use this method only if you are sure it works in your case.</b>
     * 
     * @param titleRegEx <code>null</code> means the title is not set
     */
    public static final GcWindowSwing getWindowRaw(final String titleRegEx)
    {
        for (Window l_window : Window.getWindows())
        {
            if (l_window instanceof Frame)
            {
                if (((Frame)l_window).isVisible() && GcUtils.startsWithOrMatches(((Frame)l_window).getTitle(), titleRegEx))
                {
                    return new GcWindowSwing(l_window);
                }
            }
            else if (l_window instanceof Dialog)
            {
                if (((Dialog)l_window).isVisible() && GcUtils.startsWithOrMatches(((Dialog)l_window).getTitle(), titleRegEx))
                {
                    return new GcWindowSwing(l_window);
                }
            }
        }

        return null;
    }

    /**
     * Evaluate the given evaluator with retries and timeouts. Retries are only done automatically if the evaluator
     * throws a {@link GcAssertException}. After each try this method waits for the EDT to become idle.
     */
    public static <T> T eval(IEvaluator<T> e)
    {
        for (int i = 0; i < EVALUATION_RETRIES - 1; i++)
        {
            try
            {
                return e.eval();
            }
            catch (GcAssertException ex)
            {
                GcUtils.sleepAndIgnoreInterrupts(EVALUATION_DELAY);
                waitForIdle();
            }
        }

        // The last time we try it without catching any exceptions
        return e.eval();
    }
    
    /**
     * Evaluate the given evaluator with retries and timeouts. Retries are only done automatically if the evaluator
     * throws a {@link GcAssertException}. After each try this method waits for the EDT to become idle.
     */
    public static <T> T eval(IEvaluator<T> e, int evalRetries, int evalDelay)
    {
        for (int i = 0; i < evalRetries - 1; i++)
        {
            try
            {
                return e.eval();
            }
            catch (GcAssertException ex)
            {
                GcUtils.sleepAndIgnoreInterrupts(evalDelay);
                waitForIdle();
            }
        }
        
        // The last time we try it without catching any exceptions
        return e.eval();
    }
}
