// Copyright 2013 SICK AG. All rights reserved.
package de.sick.guicheck.fx;

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

import de.sick.guicheck.GcAssertException;
import de.sick.guicheck.GcUtils;

/**
 * A wrapper for the JavaFX menu bar. The menu bar is not a real node, thus finding menu items is the same as a lookup
 * in the scene graph.
 * 
 * @author linggol (created)
 */
public class GcMenuBarFX extends GcNodeBaseFX<GcMenuBarFX>
{
    private final MenuBar m_bar;

    GcMenuBarFX(MenuBar bar)
    {
        super(bar);
        m_bar = bar;
    }

    private ArrayList<MenuItem> getMenuPath(String menuPath)
    {
        return GcUtilsFX.getMenuPath(m_bar.getMenus(), menuPath);
    }

    /**
     * Check whether the given menu path exists.
     * 
     * @param menuPath Path of menus and a menu item separated by / chars.
     * @throws GcAssertException Thrown if the path does not exist.
     */
    public GcMenuBarFX menuPathExists(final String menuPath)
    {
        GcUtilsFX.menuPathExists(m_bar.getMenus(), menuPath);
        return this;
    }

    /**
     * Fire the given menu item.
     * 
     * @param menuPath Path of menus and the menu item separated by / chars.
     * @throws GcAssertException Thrown if the path does not exist.
     */
    public GcMenuBarFX fireMenuItem(final String menuPath)
    {
        return GcUtilsFX.eval(new GcUtils.IEvaluator<GcMenuBarFX>()
        {
            @Override
            public GcMenuBarFX eval()
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
                if (l_path.get(0) instanceof Menu)
                {
                    final Menu l_firstMenu = (Menu)l_path.get(0);
                    GcUtilsFX.runLaterAndWait(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            l_firstMenu.hide();
                        }
                    });
                }

                // ... fire the menu item in the FX application thread ...
                Platform.runLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        l_path.get(l_path.size() - 1).fire();
                    }
                });

                return GcMenuBarFX.this;
            }
        });
    }

    /**
     * Check a property of the given menu entry which can be a menu or a menu item.
     * 
     * @param menuPath Path to the menu entry separated by / chars.
     * @throws GcAssertException Thrown if the path does not exist or the property does not have the given value.
     */
    public GcMenuBarFX menuPathPropertyIs(final String menuPath, String property, boolean value)
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

        return this;
    }
}
