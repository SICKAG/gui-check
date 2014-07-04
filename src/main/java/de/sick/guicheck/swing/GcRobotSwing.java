// Copyright 2013 SICK AG. All rights reserved.
package de.sick.guicheck.swing;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import com.sun.javafx.robot.FXRobot;

import de.sick.guicheck.fx.GcUtilsFX;

/**
 * A JavaFX robot which automatically waits for the windowing thread to become idle. Input device methods like mouse or
 * keyboard actions are delegated to the {@link FXRobot} given in the constructor.
 * <p>
 * Every method which triggers an input action, like mouse or keyboard, checks if the platform is still alive, calls the
 * corresponding method of the robot and waits for the UI becoming idle.
 * 
 * @see FXRobot
 * @author linggol (created)
 */
public class GcRobotSwing
{
    private final Robot m_robot;
    private final GcWindowSwing m_window;

    GcRobotSwing(GcWindowSwing window, Robot robot)
    {
        m_window = window;
        m_robot = robot;
    }

    /**
     * @see Robot#keyPress(int)
     */
    public GcRobotSwing keyPress(int code)
    {
        m_robot.keyPress(code);
        GcUtilsSwing.waitForIdle();
        return this;
    }

    /**
     * @see Robot#keyRelease(int)
     */
    public GcRobotSwing keyRelease(int code)
    {
        m_robot.keyRelease(code);
        GcUtilsSwing.waitForIdle();
        return this;
    }

    /**
     * @see Robot#keyPress(int)
     * @see Robot#keyRelease(int)
     */
    public GcRobotSwing keyType(int... codes)
    {
        for (int c : codes)
        {
            keyPress(c);
            keyRelease(c);
        }

        return this;
    }

    /**
     * Type the given string on the keyboard. The component having the focus will get the resulting {@link KeyEvent}
     */
    public GcRobotSwing keyType(String s)
    {
        for (char c : s.toCharArray())
        {
            keyType(c);
        }

        return this;
    }

    /**
     * Type the given character on the keyboard. The component having the focus will get the resulting {@link KeyEvent}
     */
    public GcRobotSwing keyType(char c)
    {
        boolean l_isUpperCase = Character.isUpperCase(c);
        int l_code = KeyEvent.getExtendedKeyCodeForChar(c);

        if (l_code != KeyEvent.VK_UNDEFINED)
        {
            try
            {
                if (l_isUpperCase)
                {
                    keyPress(KeyEvent.VK_SHIFT);
                }
                keyPress(l_code);
                keyRelease(l_code);
                if (l_isUpperCase)
                {
                    keyRelease(KeyEvent.VK_SHIFT);
                }
                return this;
            }
            catch (IllegalArgumentException e)
            {
                // The key code isn't known
                // We create a custom event for this character
            }
        }

        // We didn't get a valid key code for the character, so create a custom KEY_TYPED event
        // Forget the key event if there is no focus owner at the moment
        final Component l_comp = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (l_comp != null)
        {
            Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new KeyEvent(l_comp, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, c));
        }

        return this;
    }

    /**
     * @see Robot#mouseWheel(int)
     */
    public GcRobotSwing mouseWheel(int wheelAmt)
    {
        m_robot.mouseWheel(wheelAmt);
        GcUtilsFX.waitForIdle();
        return this;
    }

    /**
     * Moves the mouse to the center point of the component given via name.
     * 
     * @see Robot#mouseMove(int, int)
     */
    public GcRobotSwing mouseMoveToCenter(String name)
    {
        return mouseMoveToCenter(m_window.component(name));
    }

    /**
     * Moves the mouse to the center point of the component given via class.
     * 
     * @see Robot#mouseMove(int, int)
     */
    public GcRobotSwing mouseMoveToCenter(Class<?> clazz)
    {
        return mouseMoveToCenter(m_window.component(clazz));
    }

    /**
     * Moves the mouse to the center point of the given component.
     * 
     * @see Robot#mouseMove(int, int)
     */
    public GcRobotSwing mouseMoveToCenter(GcComponentSwing<?> component)
    {
        Rectangle l_bounds = component.getComponent().getBounds();
        return mouseMove(component, (int)(l_bounds.getWidth() / 2), (int)(l_bounds.getHeight() / 2));
    }

    /**
     * Moves the mouse to the given point on the screen.
     * 
     * @see Robot#mouseMove(int, int)
     */
    public GcRobotSwing mouseMove(int x, int y)
    {
        m_robot.mouseMove(x, y);
        GcUtilsFX.waitForIdle();
        return this;
    }

    /**
     * Moves the mouse to the given point relative to the component given by name.
     * 
     * @see Robot#mouseMove(int, int)
     */
    public GcRobotSwing mouseMove(String name, int x, int y)
    {
        return mouseMove(m_window.component(name), x, y);
    }

    /**
     * Moves the mouse to the given point relative to the component given by clazz.
     * 
     * @see Robot#mouseMove(int, int)
     */
    public GcRobotSwing mouseMove(Class<?> clazz, int x, int y)
    {
        return mouseMove(m_window.component(clazz), x, y);
    }

    /**
     * Moves the mouse to the given point relative to the component.
     * 
     * @see Robot#mouseMove(int, int)
     */
    public GcRobotSwing mouseMove(GcComponentSwing<?> component, int x, int y)
    {
        Point l_point = new Point(x, y);
        SwingUtilities.convertPointToScreen(l_point, component.getComponent());
        return mouseMove((int)l_point.getX(), (int)l_point.getY());
    }

    /**
     * Presses the primary mouse button.
     */
    public GcRobotSwing mousePress()
    {
        mousePress(InputEvent.BUTTON1_MASK);
        return this;
    }

    /**
     * Presses the secondary mouse button.
     */
    public GcRobotSwing mousePressSecondary()
    {
        mousePress(InputEvent.BUTTON2_MASK);
        return this;
    }

    /**
     * Releases the primary mouse button.
     */
    public GcRobotSwing mouseRelease()
    {
        mouseRelease(InputEvent.BUTTON1_MASK);
        return this;
    }

    /**
     * Releases the secondary mouse button.
     */
    public GcRobotSwing mouseReleaseSecondary()
    {
        mouseRelease(InputEvent.BUTTON2_MASK);
        return this;
    }

    /**
     * Clicks the primary mouse button. Automatically adds mousePress and mouseRelease calls.
     */
    public GcRobotSwing mouseClick()
    {
        mouseClick(InputEvent.BUTTON1_MASK);
        return this;
    }

    /**
     * Clicks the secondary mouse button. Automatically adds mousePress and mouseRelease calls.
     */
    public GcRobotSwing mouseClickSecondary()
    {
        mouseClick(InputEvent.BUTTON3_MASK);
        return this;
    }

    /**
     * Double-clicks the primary mouse button. Automatically adds mousePress and mouseRelease calls.
     */
    public GcRobotSwing mouseDblClick()
    {
        mouseClick(InputEvent.BUTTON1_MASK);
        mouseClick(InputEvent.BUTTON1_MASK);
        return this;
    }

    /**
     * Double-clicks the secondary mouse button. Automatically adds mousePress and mouseRelease calls.
     */
    public GcRobotSwing mouseDblClickSecondary()
    {
        mouseClick(InputEvent.BUTTON2_MASK);
        mouseClick(InputEvent.BUTTON2_MASK);
        return this;
    }

    private void mouseClick(int buttons)
    {
        mousePress(buttons);
        mouseRelease(buttons);
        GcUtilsSwing.waitForIdle();
    }

    private void mousePress(int buttons)
    {
        m_robot.mousePress(buttons);
        GcUtilsSwing.waitForIdle();
    }

    private void mouseRelease(int buttons)
    {
        m_robot.mouseRelease(buttons);
        GcUtilsSwing.waitForIdle();
    }

    /**
     * Set the focus to the component given via name. Setting the focus is done via an explicit call to
     * {@link JComponent#requestFocus()} and not using any method of the {@link Robot}.
     */
    public GcRobotSwing focus(String name)
    {
        return focus(m_window.component(name));
    }

    /**
     * Set the focus to the given component. Setting the focus is done via an explicit call to
     * {@link JComponent#requestFocus()} and not using any method of the {@link Robot}.
     */
    public GcRobotSwing focus(final GcComponentSwing<?> component)
    {
        GcUtilsSwing.runLaterAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                component.getComponent().requestFocus();
            }
        });

        GcUtilsSwing.waitForIdle(10, 1);
        return this;
    }
}
