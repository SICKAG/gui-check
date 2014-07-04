// Copyright 2013 SICK AG. All rights reserved.
package de.sick.guicheck.fx;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.stage.Stage;
import javafx.stage.Window;

import javax.imageio.ImageIO;

import com.sun.javafx.robot.FXRobotFactory;

import de.sick.guicheck.GcAssertException;
import de.sick.guicheck.GcUtils;

/**
 * A wrapper for stages used within GUIcheck.
 * 
 * @author linggol (created)
 */
public class GcStageFX extends GcComponentFX<GcStageFX>
{
    private final GcRobotFX m_robot;
    private final Stage m_stage;

    protected GcStageFX(final Stage stage)
    {
        m_stage = stage;
        m_robot = new GcRobotFX(this, FXRobotFactory.createRobot(stage.getScene()));
    }

    /**
     * @return Use this robot to simulate user input on this stage.
     */
    public GcRobotFX robot()
    {
        return m_robot;
    }

    /**
     * Get the root node of the scene contained in this stage.
     */
    @Override
    public Node getNode()
    {
        final Scene l_scene = m_stage.getScene();
        if (l_scene == null)
        {
            throw new GcAssertException("The stage has no scene");
        }

        return l_scene.getRoot();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getFXComponent()
    {
        return (T)m_stage;
    }

    public GcStageFX close()
    {
        GcUtilsFX.runLaterAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                m_stage.close();
            }
        });
        return this;
    };

    /**
     * Take a snapshot of the current scene contained in this stage and save it to a PNG file.
     * 
     * @param filename The name of the file without extension
     */
    public void takeSceneSnapshot(final String filename)
    {
        GcUtilsFX.runLaterAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                final File l_file = new File(filename + ".png");
                try
                {
                    ImageIO.write(SwingFXUtils.fromFXImage(m_stage.getScene().snapshot(null), null), "png", l_file);
                    System.out.println("Snapshot saved to : " + l_file.getAbsolutePath());
                }
                catch (final IOException l_exception)
                {
                    l_exception.printStackTrace();
                }
            }
        });
    }

    /**
     * Moves the mouse to the center point of the node given via CSS ID and clicks the primary mouse button.
     * 
     * @param selector
     * @return {@link ContextMenu} which has been opened by the {@link GcRobotFX#mouseClick()}
     * @see GcRobotFX#mouseMoveToCenter(String)
     * @see GcRobotFX#mouseClick()
     */
    public GcContextMenuFX contextMenu(String selector)
    {
        robot().mouseMoveToCenter(selector).mouseClick();
        return findContextMenu();
    }

    /**
     * Moves the mouse to the center point of the given node and clicks the primary mouse button.
     * 
     * @param node
     * @return {@link ContextMenu} which has been opened by the {@link GcRobotFX#mouseClick()}
     * @see GcRobotFX#mouseMoveToCenter(String)
     * @see GcRobotFX#mouseClick()
     */
    public GcContextMenuFX contextMenu(GcNodeFX node)
    {
        robot().mouseMoveToCenter(node).mouseClick();
        return findContextMenu();
    }

    /**
     * Moves the mouse to the center point of the node given via CSS ID and clicks the secondary mouse button.
     * 
     * @param selector
     * @return {@link ContextMenu} which has been opened by the {@link GcRobotFX#mouseClickSecondary()}
     * @see GcRobotFX#mouseMoveToCenter(String)
     * @see GcRobotFX#mouseClickSecondary()
     */
    public GcContextMenuFX contextMenuViaSecondaryClick(String selector)
    {
        robot().mouseMoveToCenter(selector).mouseClickSecondary();
        return findContextMenu();
    }

    /**
     * Moves the mouse to the center point of the given node and clicks the secondary mouse button.
     * 
     * @param node
     * @return {@link ContextMenu} which has been opened by the {@link GcRobotFX#mouseClick()}
     * @see GcRobotFX#mouseMoveToCenter(String)
     * @see GcRobotFX#mouseClick()
     */
    public GcContextMenuFX contextMenuViaSecondaryClick(GcNodeFX node)
    {
        robot().mouseMoveToCenter(node).mouseClickSecondary();
        return findContextMenu();
    }

    /**
     * Searches the currently open {@link ContextMenu}
     * 
     * @param selector
     * @return
     */
    @SuppressWarnings("deprecation")
    private GcContextMenuFX findContextMenu()
    {
        return GcUtilsFX.eval(new GcUtils.IEvaluator<GcContextMenuFX>()
        {
            @Override
            public GcContextMenuFX eval()
            {
                Iterator<Window> l_windows = Window.impl_getWindows();
                while (l_windows.hasNext())
                {
                    Window l_window = (Window)l_windows.next();
                    if (l_window instanceof ContextMenu)
                    {
                        return new GcContextMenuFX((ContextMenu)l_window);
                    }
                }
                throw new GcAssertException("Cannot find context menu");
            }
        });
    }
}
