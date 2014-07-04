package de.sick.guicheck.fx;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import de.sick.guicheck.GcAssertException;
import de.sick.guicheck.GcException;
import de.sick.guicheck.GcUtils;

/**
 * @author briemla
 */
public class GcContextMenuFX
{

    private final ContextMenu m_contextMenu;

    public GcContextMenuFX(ContextMenu contextMenu)
    {
        super();
        m_contextMenu = contextMenu;
    }
    
    private ArrayList<MenuItem> getMenuPath(String menuPath)
    {
        return GcUtilsFX.getMenuPath(m_contextMenu.getItems(), menuPath);
    }

    /**
     * Check whether the given menu path exists.
     * 
     * @param menuPath Path of menus and a menu item separated by / chars.
     * @throws GcAssertException Thrown if the path does not exist.
     */
    public GcContextMenuFX menuPathExists(final String menuPath)
    {
        GcUtilsFX.menuPathExists(m_contextMenu.getItems(), menuPath);
        return this;
    }

    /**
     * Fire the given menu item.
     * 
     * @param menuPath Path of menus and the menu item separated by / chars.
     * @throws GcAssertException Thrown if the path does not exist.
     */
    public GcContextMenuFX fireMenuItem(final String menuPath)
    {
        return GcUtilsFX.eval(new GcUtils.IEvaluator<GcContextMenuFX>()
        {
            @Override
            public GcContextMenuFX eval()
            {
                // Open the menu path ...
                final ArrayList<MenuItem> l_path = getMenuPath(menuPath);
                for (MenuItem i : l_path)
                {
                    if (i instanceof Menu)
                    {
                        final Menu l_menu = (Menu)i;
                        GcUtilsFX.runLaterAndWait(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                l_menu.show();
                            }
                        });
                    }

                    if (i.isDisable())
                    {
                        throw new GcAssertException("Menu item is disabled: " + i.getId());
                    }
                }

                // ... and close the menu path again
                //
                // If we do not close it again, the next click on the GUI just closes the menu and
                // has no other effect
                hideContextMenu();

                // ... fire the menu item in the FX application thread ...
                Platform.runLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        l_path.get(l_path.size() - 1).fire();
                    }
                });

                return GcContextMenuFX.this;
            }
        });
    }

    private void hideContextMenu()
    {
        GcUtilsFX.runLaterAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                m_contextMenu.hide();
            }
        });
    }

    /**
     * Check a property of the given menu entry which can be a menu or a menu item.
     * 
     * @param menuPath Path to the menu entry separated by / chars.
     * @throws GcAssertException Thrown if the path does not exist or the property does not have the given value.
     */
    public GcContextMenuFX menuPathPropertyIs(final String menuPath, String property, boolean value)
    {
        ArrayList<MenuItem> l_path = GcUtilsFX.eval(new GcUtils.IEvaluator<ArrayList<MenuItem>>()
        {
            @Override
            public ArrayList<MenuItem> eval()
            {
                return getMenuPath(menuPath);
            }
        });

        // only last item in path need to be checked
        MenuItem l_menuItem = l_path.get(l_path.size() - 1);
        propertyIs(l_menuItem, property, value, true);

        hideContextMenu();

        return this;
    }

    /**
     * Duplicate method
     * 
     * @see GcComponentFX#propertyIs(Object, String, Object, boolean)
     */
    @SuppressWarnings("unchecked")
    final <TT> GcContextMenuFX propertyIs(final Object obj, final String property, final TT value, final boolean expectedResult)
    {
        try
        {
            // Get the property getter method ...
            final Class<?> l_clazz = value == null ? null : value.getClass();
            final Method l_method = obj.getClass().getMethod(GcComponentFX.getPropertyGetter(property, l_clazz), (Class<?>[])null);

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
            return this;
        }
        catch (NoSuchMethodException e)
        {
            throw new GcException("Failed to access property " + property, e);
        }
    }
}