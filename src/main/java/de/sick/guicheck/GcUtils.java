// Copyright 2013 SICK AG. All rights reserved.
package de.sick.guicheck;


/**
 * Utilities for developers using GUIcheck
 * 
 * @author linggol (created)
 */
public final class GcUtils
{
    private static final int MAX_NUMBER_OF_THREADS = 128;

    private GcUtils()
    {
        // Prevent instantiation
    }

    /**
     * A runnable doing nothing
     */
    public static final Runnable NOOP_RUNNABLE = new Runnable()
    {
        public void run()
        {
        }
    };

    /**
     * Combination of startsWith and matches string comparison. StartsWith has the higher priority. Null checking is
     * also included. If both parameters are null, the returned value is true.
     */
    public static boolean startsWithOrMatches(String s1, String s2)
    {
        if (s1 == null)
        {
            return s2 == null;
        }

        if (s2 == null)
        {
            return false;
        }

        return s1.startsWith(s2) || s1.matches(s2);
    }

    /**
     * Sets the current thread to sleep. Use this method with care because InterruptedExceptions interrupt the sleep but
     * are not re-thrown.
     */
    public static void sleepAndIgnoreInterrupts(int millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException e1)
        {
            // ignore interrupts
            // be aware that the exception is not thrown again, thus the sleeping thread is not interrupted
        }
    }

    /**
     * @return The name of the getter method for the given property using Java Bean style.
     */
    public static String getPropertyGetter(String property, Class<?> clazz)
    {
        StringBuffer l_sb = new StringBuffer((clazz == Boolean.class || clazz == boolean.class) ? "is" : "get");

        if (property.length() > 0)
        {
            l_sb.append(property.substring(0, 1).toUpperCase());

            if (property.length() > 1)
            {
                l_sb.append(property.substring(1));
            }
        }

        return l_sb.toString();
    }

    public interface IEvaluator<T>
    {
        T eval();
    }

    /**
     * Wait for a thread given by name.
     * <p>
     * Throws a GcException if thread was found but not finished within the timeout. If the thread is not found, this
     * method does nothing.
     */
    public static void waitWhileThreadIsAlive(String name, long timeout)
    {
        try
        {
            final Thread l_thread = findThread(name);
            if (l_thread != null)
            {
                l_thread.join(timeout);
    
                if (l_thread.isAlive())
                {
                    throw new GcException("Thread " + name + " is still alive after " + timeout + " millis.");
                }
            }
        }
        catch (InterruptedException e)
        {
            throw new GcException("Interrupted while waiting for thread " + name + ".");            
        }
    }

    private static Thread findThread(String name)
    {
        ThreadGroup l_group = Thread.currentThread().getThreadGroup();
        while (l_group.getParent() != null)
        {
            l_group = l_group.getParent();
        }

        final Thread[] l_threads = new Thread[MAX_NUMBER_OF_THREADS];
        final int l_size = l_group.enumerate(l_threads, true);
        for (int i = 0; i < l_size; i++)
        {
            final String l_name = l_threads[i].getName();

            if (l_name != null && l_name.equals(name))
            {
                return l_threads[i];
            }
        }

        return null;
    }
}
