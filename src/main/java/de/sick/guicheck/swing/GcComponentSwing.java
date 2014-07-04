// Copyright 2013 SICK AG. All rights reserved.
package de.sick.guicheck.swing;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JComponent;

import de.sick.guicheck.GcAssertException;
import de.sick.guicheck.GcException;
import de.sick.guicheck.GcUtils;
import de.sick.guicheck.fx.GcUtilsFX;

/**
 * @author linggol (created)
 */
abstract class GcComponentSwing<T extends GcComponentSwing<T>>
{
    /**
     * @return The Swing {@link JComponent} wrapped by this component.
     */
    public abstract JComponent getComponent();

    /**
     * @return The Swing component wrapped by this component. In most cases this is the same as the {@link JComponent}
     *         returned by {@link #getComponent()}
     */
    public abstract <Z extends Component> Z getSwingComponent();

    private JComponent findComponent(final String name)
    {
        return GcUtilsSwing.eval(new GcUtils.IEvaluator<JComponent>()
        {
            @Override
            public JComponent eval()
            {
                JComponent l_found = findComponent(getComponent(), name);
                if (l_found != null)
                {
                    return l_found;
                }

                throw new GcAssertException("Cannot find JComponent for name: " + name);
            }
        });
    }

    private JComponent findComponent(final String name, int evalRetries, int evalDelay)
    {
        return GcUtilsSwing.eval(new GcUtils.IEvaluator<JComponent>()
        {
            @Override
            public JComponent eval()
            {
                JComponent l_found = findComponent(getComponent(), name);
                if (l_found != null)
                {
                    return l_found;
                }

                throw new GcAssertException("Cannot find JComponent for name: " + name);
            }
        }, evalRetries, evalDelay);
    }

    private JComponent findComponent(final Class<?> clazz)
    {
        return GcUtilsSwing.eval(new GcUtils.IEvaluator<JComponent>()
        {
            @Override
            public JComponent eval()
            {
                JComponent l_found = findComponent(getComponent(), clazz);
                if (l_found != null)
                {
                    return l_found;
                }

                throw new GcAssertException("Cannot find JComponent for class: " + clazz);
            }
        });
    }

    private JComponent findComponent(JComponent parent, Class<?> clazz)
    {
        for (Component c : parent.getComponents())
        {
            if (c instanceof JComponent)
            {
                if (clazz.isAssignableFrom(c.getClass()))
                {
                    return (JComponent)c;
                }
            }
        }

        for (Component c : parent.getComponents())
        {
            if (c instanceof JComponent)
            {
                JComponent l_found = findComponent((JComponent)c, clazz);
                if (l_found != null)
                {
                    return l_found;
                }
            }
        }

        return null;
    }

    private JComponent findComponent(JComponent parent, String name)
    {
        for (Component c : parent.getComponents())
        {
            if (c instanceof JComponent)
            {
                if (GcUtils.startsWithOrMatches(c.getName(), name))
                {
                    return (JComponent)c;
                }
            }
        }

        for (Component c : parent.getComponents())
        {
            if (c instanceof JComponent)
            {
                JComponent l_found = findComponent((JComponent)c, name);
                if (l_found != null)
                {
                    return l_found;
                }
            }
        }

        return null;
    }

    /**
     * Find the first child {@link JComponent} for the given name.
     */
    public GcJComponentSwing component(String name)
    {
        return new GcJComponentSwing(findComponent(name));
    }

    /**
     * Find the first child {@link JComponent} for the given name.
     */
    public GcJComponentSwing component(String name, int evalRetries, int evalDelay)
    {
        return new GcJComponentSwing(findComponent(name, evalRetries, evalDelay));
    }
    
    /**
     * Find the first child {@link JComponent} for the given class.
     */
    public GcJComponentSwing component(Class<?> clazz)
    {
        return new GcJComponentSwing(findComponent(clazz));
    }

    @SuppressWarnings("unchecked")
    final <TT> T propertyIs(final Object obj, final String property, final TT value, final boolean expectedResult)
    {
        try
        {
            // Get the property getter method ...
            final Class<?> l_clazz = value == null ? null : value.getClass();
            final Method l_method = obj.getClass().getMethod(GcUtils.getPropertyGetter(property, l_clazz), (Class<?>[])null);

            GcUtilsFX.eval(new GcUtils.IEvaluator<Void>()
            {
                @Override
                public Void eval()
                {
                    TT l_value;
                    try
                    {
                        // ... get the value of the property ...
                        l_value = (TT)l_method.invoke(obj, (Object[])null);
                    }
                    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
                    {
                        throw new GcException("Failed to access property " + property, e);
                    }

                    // ... and check it against the expected value
                    if ((value == l_value || (value != null && value.equals(l_value))) != expectedResult)
                    {
                        final StringBuilder l_sb = new StringBuilder("Unexpected value of ");
                        l_sb.append(property).append(": ").append(expectedResult ? "Expected: " : "Not expected: ").append(value).append(", Actual: ").append(l_value);
                        throw new GcAssertException(l_sb.toString());
                    }
                    return null;
                }
            });

            // Return this instance again according to the fluent API style
            return (T)this;
        }
        catch (NoSuchMethodException e)
        {
            throw new GcException("Failed to access property " + property, e);
        }
    }

    /**
     * Check if the given property has the given value. This method follows the fluent API style.
     */
    public final <TT> T propertyIs(String property, TT value)
    {
        return propertyIs(getSwingComponent(), property, value, true);
    }

    /**
     * Check if the given property does not have the given value. This method follows the fluent API style.
     */
    public final <TT> T propertyIsNot(String property, TT value)
    {
        return propertyIs(getSwingComponent(), property, value, false);
    }
}
