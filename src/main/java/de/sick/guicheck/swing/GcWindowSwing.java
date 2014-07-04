// Copyright 2013 SICK AG. All rights reserved.
package de.sick.guicheck.swing;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Robot;
import java.awt.Window;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

/**
 * A wrapper for windows used within GUIcheck.
 * 
 * @author linggol (created)
 */
public class GcWindowSwing extends GcComponentSwing<GcWindowSwing>
{
    private final Robot m_awtRobot;
    private final GcRobotSwing m_robot;
    private final Window m_window;

    GcWindowSwing(Window window)
    {
        m_window = window;
        
        try
        {
            m_awtRobot = new Robot();
            m_robot = new GcRobotSwing(this, m_awtRobot);
        }
        catch (AWTException e)
        {
            throw new RuntimeException("Failed to create Swing robot");
        }
    }

    /**
     * @return Use this robot to simulate user input on this window.
     */
    public GcRobotSwing robot()
    {
        return m_robot;
    }

    /**
     * Get the first child of the window.
     */
    @Override
    public JComponent getComponent()
    {
        Component l_root = m_window.getComponent(0);
        if (l_root instanceof JComponent)
        {
            return (JComponent)l_root;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Component> T getSwingComponent()
    {
        return (T)m_window;
    }

    public GcWindowSwing close()
    {
        GcUtilsSwing.runLaterAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                m_window.dispose();
            }
        });
        return this;
    };

    /**
     * Take a snapshot of the current window content and save it to a PNG file.
     * 
     * @param filename The name of the file without extension
     */
    public void takeWindowSnapshot(final String filename)
    {
        GcUtilsSwing.runLaterAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                File l_file = new File(filename + ".png");
                try
                {
                    ImageIO.write(m_awtRobot.createScreenCapture(m_window.getBounds()), "png", l_file);
                    System.out.println("Snapshot saved to : " + l_file.getAbsolutePath());
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
}
