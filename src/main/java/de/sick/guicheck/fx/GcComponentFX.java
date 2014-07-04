// Copyright 2013 SICK AG. All rights reserved.
package de.sick.guicheck.fx;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;

import de.sick.guicheck.GcAssertException;
import de.sick.guicheck.GcException;
import de.sick.guicheck.GcUtils;
import de.sick.guicheck.GcUtils.IEvaluator;

/**
 * The base class for all wrappers around elements of the JavaFX UI. Not only {@link Node} is covert, also {@link Stage}
 * and {@link Menu}. The child lookup methods again return wrappers for JavaFX {@link Node}.
 * <p>
 * This class supports the evaluation of property values using retries and idle waiting.
 * 
 * @author linggol (created)
 */
abstract class GcComponentFX<T extends GcComponentFX<T>>
{

    /**
     * Used to convert a wrapper class to its corresponding primitive type.
     * 
     * If this method is used at several places. Consider to use Guava or Apache commons-lang
     */
    private final static Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE = new HashMap<Class<?>, Class<?>>();
    static
    {
        WRAPPER_TO_PRIMITIVE.put(Boolean.class, boolean.class);
        WRAPPER_TO_PRIMITIVE.put(Byte.class, byte.class);
        WRAPPER_TO_PRIMITIVE.put(Short.class, short.class);
        WRAPPER_TO_PRIMITIVE.put(Character.class, char.class);
        WRAPPER_TO_PRIMITIVE.put(Integer.class, int.class);
        WRAPPER_TO_PRIMITIVE.put(Long.class, long.class);
        WRAPPER_TO_PRIMITIVE.put(Float.class, float.class);
        WRAPPER_TO_PRIMITIVE.put(Double.class, double.class);
    }

    /**
     * @return The JavaFX {@link Node} wrapped by this component.
     */
    public abstract Node getNode();

    /**
     * @return The JavaFX component wrapped by this component. In most cases this is the same as the {@link Node}
     *         returned by {@link #getNode()}
     */
    public abstract <Z> Z getFXComponent();

    private Node findNode(final String selector)
    {
        return GcUtilsFX.eval(new IEvaluator<Node>()
        {
            @Override
            public Node eval()
            {
                Node l_found = getNode().lookup(selector);
                if (l_found != null)
                {
                    return l_found;
                }

                throw new GcAssertException("Cannot find node for selector: " + selector);
            }
        });
    }

    private <TT> Node findNode(final String selector, final String property, final TT expected)
    {
        return GcUtilsFX.eval(new GcUtils.IEvaluator<Node>()
        {
            @SuppressWarnings("unchecked")
            @Override
            public Node eval()
            {
                Set<Node> l_nodes = getNode().lookupAll(selector);

                for (Node l_found : l_nodes)
                {
                    TT l_value;
                    try
                    {
                        // Get the property getter method ...
                        final Class<?> l_clazz = expected == null ? null : expected.getClass();
                        final Method l_method = l_found.getClass().getMethod(getPropertyGetter(property, l_clazz), (Class<?>[])null);

                        // ... get the value of the property ...
                        l_value = (TT)l_method.invoke(l_found, (Object[])null);
                    }
                    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
                    {
                        throw new GcException("Failed to access property " + property, e);
                    }

                    // ... and check it against the expected value
                    if (expected == l_value || (expected != null && expected.equals(l_value)))
                    {
                        return l_found;
                    }
                }

                throw new GcAssertException("Cannot find node for selector: " + selector + " with value " + expected + " for property " + property);
            }
        });
    }

    /**
     * Find the first child {@link Node} for the given CSS selector.
     */
    public GcNodeFX node(String selector)
    {
        return new GcNodeFX(findNode(selector));
    }

    /**
     * Find the first child {@link Node} for the given CSS selector with the given value for the specified property.
     */
    public <TT> GcNodeFX node(String selector, String property, TT expected)
    {
        return new GcNodeFX(findNode(selector, property, expected));
    }

    /**
     * Find the first {@link MenuBar} for the given CSS selector.
     */
    public GcMenuBarFX menuBar(String selector)
    {
        return new GcMenuBarFX((MenuBar)findNode(selector));
    }

    /**
     * @return The name of the getter method for the given property using Java Bean style.
     */
    static String getPropertyGetter(String property, Class<?> clazz)
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

    /**
     * @return The name of the setter method for the given property using Java Bean style.
     */
    static String getPropertySetter(String property)
    {
        StringBuffer l_sb = new StringBuffer("set");

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

    /**
     * Duplicate method
     * 
     * @see GcContextMenuFX#propertyIs(Object, String, Object, boolean)
     */
    @SuppressWarnings("unchecked")
    final <TT> T propertyIs(final Object obj, final String property, final TT value, final boolean expectedResult)
    {
        try
        {
            // Get the property getter method ...
            final Class<?> l_clazz = value == null ? null : value.getClass();
            final Method l_method = obj.getClass().getMethod(getPropertyGetter(property, l_clazz), (Class<?>[])null);

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
        return propertyIs(getFXComponent(), property, value, true);
    }

    /**
     * Check if the given property does not have the given value. This method follows the fluent API style.
     */
    public final <TT> T propertyIsNot(String property, TT value)
    {
        return propertyIs(getFXComponent(), property, value, false);
    }

    @SuppressWarnings("unchecked")
    private final <TT> T ensurePropertyIs(final Object obj, final String property, final TT value)
    {
        try
        {
            // Get the property setter method ...
            final Method l_method = getSetter(obj, property, value);

            GcUtilsFX.eval(new GcUtils.IEvaluator<Void>()
            {
                @Override
                public Void eval()
                {
                    try
                    {
                        // ... set the value of the property ...
                        l_method.invoke(obj, value);
                    }
                    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
                    {
                        throw new GcException("Failed to set property " + property, e);
                    }

                    // ... and check if it is set correct
                    propertyIs(property, value);
                    return null;
                }
            });

            // Return this instance again according to the fluent API style
            return (T)this;
        }
        catch (NoSuchMethodException e)
        {
            throw new GcException("Failed to set property " + property, e);
        }
    }

    private <TT> Method getSetter(final Object obj, final String property, final TT value) throws NoSuchMethodException
    {
        final Class<?> l_clazz = value == null ? null : value.getClass();
        try
        {
            return obj.getClass().getMethod(getPropertySetter(property), l_clazz);
        }
        catch (NoSuchMethodException e)
        {
            // try primitive type
            return obj.getClass().getMethod(getPropertySetter(property), WRAPPER_TO_PRIMITIVE.get(l_clazz));
        }
    }

    /**
     * Set the value of the given property to the given value. The value will not be set via UI interaction (mouse click
     * or key press). The property will be directly accessed via its setter. This method follows the fluent API style.
     */
    public final <TT> T ensurePropertyIs(String property, TT value)
    {
        return ensurePropertyIs(getFXComponent(), property, value);
    }

}
