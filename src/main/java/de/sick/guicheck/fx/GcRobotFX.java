// Copyright 2013 SICK AG. All rights reserved.
package de.sick.guicheck.fx;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.sun.javafx.robot.FXRobot;
import com.sun.javafx.stage.StageHelper;

import de.sick.guicheck.GcAssertException;

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
public class GcRobotFX
{
    private final FXRobot m_robot;
    private final GcStageFX m_stage;

    GcRobotFX(GcStageFX stage, FXRobot robot)
    {
        m_stage = stage;
        m_robot = robot;
    }

    /**
     * @see FXRobot#keyPress(KeyCode)
     */
    public GcRobotFX keyPress(KeyCode code)
    {
        if (GcUtilsFX.isPlatformAlive())
        {
            checkForModalChildStages();
            m_robot.keyPress(code);
            GcUtilsFX.waitForIdle();
        }

        return this;
    }

    /**
     * @see FXRobot#keyRelease(KeyCode)
     */
    public GcRobotFX keyRelease(KeyCode code)
    {
        if (GcUtilsFX.isPlatformAlive())
        {
            checkForModalChildStages();
            m_robot.keyRelease(code);
            GcUtilsFX.waitForIdle();
        }

        return this;
    }

    /**
     * @see FXRobot#keyPress(KeyCode)
     * @see FXRobot#keyRelease(KeyCode)
     */
    public GcRobotFX keyType(KeyCode... codes)
    {
        if (GcUtilsFX.isPlatformAlive())
        {
            checkForModalChildStages();
            for (KeyCode c : codes)
            {
                m_robot.keyPress(c);
                GcUtilsFX.waitForIdle();
                m_robot.keyRelease(c);
                GcUtilsFX.waitForIdle();
            }
        }

        return this;
    }

    /**
     * @see FXRobot#keyType(KeyCode, String)
     */
    public GcRobotFX keyType(KeyCode code, String keyChar)
    {
        if (GcUtilsFX.isPlatformAlive())
        {
            checkForModalChildStages();
            m_robot.keyType(code, keyChar);
            GcUtilsFX.waitForIdle();
        }

        return this;
    }

    /**
     * Type the given string on the keyboard. The component having the focus will get the resulting {@link KeyEvent}
     */
    public GcRobotFX keyType(String s)
    {
        if (GcUtilsFX.isPlatformAlive())
        {
            checkForModalChildStages();
            for (char c : s.toCharArray())
            {
                final KeyCode l_code = KeyCode.getKeyCode(String.valueOf(c));
                m_robot.keyType(l_code == null ? KeyCode.UNDEFINED : l_code, Character.toString(c));
                GcUtilsFX.waitForIdle();
            }
        }
        return this;
    }

    /**
     * @see FXRobot#mouseWheel(int)
     */
    public GcRobotFX mouseWheel(int wheelAmt)
    {
        if (GcUtilsFX.isPlatformAlive())
        {
            checkForModalChildStages();
            m_robot.mouseWheel(wheelAmt);
            GcUtilsFX.waitForIdle();
        }

        return this;
    }

    /**
     * Moves the mouse to the center point of the node given via CSS ID.
     * 
     * @see FXRobot#mouseMove(int, int)
     */
    public GcRobotFX mouseMoveToCenter(String selector)
    {
        return mouseMoveToCenter(m_stage.node(selector));
    }

    /**
     * Moves the mouse to the center point of the given component.
     * 
     * @see FXRobot#mouseMove(int, int)
     */
    public GcRobotFX mouseMoveToCenter(GcComponentFX<?> component)
    {
        Bounds l_bounds = component.getNode().getBoundsInLocal();
        return mouseMove(component, (int)(l_bounds.getWidth() / 2), (int)(l_bounds.getHeight() / 2));
    }

    /**
     * Moves the mouse to the given point in the scene.
     * 
     * @see FXRobot#mouseMove(int, int)
     */
    public GcRobotFX mouseMove(int x, int y)
    {
        if (GcUtilsFX.isPlatformAlive())
        {
            checkForModalChildStages();
            m_robot.mouseMove(x, y);
            GcUtilsFX.waitForIdle();
        }

        return this;
    }

    /**
     * Moves the mouse to the given point relative to the component given by selector.
     * 
     * @see FXRobot#mouseMove(int, int)
     */
    public GcRobotFX mouseMove(String selector, int x, int y)
    {
        return mouseMove(m_stage.node(selector), x, y);
    }

    /**
     * Moves the mouse to the given point relative to the component.
     * 
     * @see FXRobot#mouseMove(int, int)
     */
    public GcRobotFX mouseMove(GcComponentFX<?> component, int x, int y)
    {
        Point2D l_point = component.getNode().localToScene(x, y);
        return mouseMove((int)l_point.getX(), (int)l_point.getY());
    }

    /**
     * Presses the primary mouse button.
     * 
     * @see FXRobot#mousePress(MouseButton, int)
     */
    public GcRobotFX mousePress()
    {
        checkForModalChildStages();
        internalMousePress(MouseButton.PRIMARY, 1);
        return this;
    }

    /**
     * Presses the secondary mouse button.
     * 
     * @see FXRobot#mousePress(MouseButton, int)
     */
    public GcRobotFX mousePressSecondary()
    {
        checkForModalChildStages();
        internalMousePress(MouseButton.SECONDARY, 1);
        return this;
    }

    /**
     * Releases the primary mouse button.
     * 
     * @see FXRobot#mouseRelease(MouseButton, int)
     */
    public GcRobotFX mouseRelease()
    {
        checkForModalChildStages();
        internalMouseRelease(MouseButton.PRIMARY, 1);
        return this;
    }

    /**
     * Releases the secondary mouse button.
     * 
     * @see FXRobot#mouseRelease(MouseButton, int)
     */
    public GcRobotFX mouseReleaseSecondary()
    {
        checkForModalChildStages();
        internalMouseRelease(MouseButton.SECONDARY, 1);
        return this;
    }

    /**
     * Clicks the primary mouse button. Automatically adds mousePress and mouseRelease calls.
     * 
     * @see FXRobot#mouseClick(MouseButton, int)
     */
    public GcRobotFX mouseClick()
    {
        checkForModalChildStages();
        internalMouseClick(MouseButton.PRIMARY, 1);
        return this;
    }

    /**
     * Clicks the secondary mouse button. Automatically adds mousePress and mouseRelease calls.
     * 
     * @see FXRobot#mouseClick(MouseButton, int)
     */
    public GcRobotFX mouseClickSecondary()
    {
        checkForModalChildStages();
        internalMouseClick(MouseButton.SECONDARY, 1);
        return this;
    }

    /**
     * Double-clicks the primary mouse button. Automatically adds mousePress and mouseRelease calls.
     * 
     * @see FXRobot#mouseClick(MouseButton, int)
     */
    public GcRobotFX mouseDblClick()
    {
        checkForModalChildStages();
        internalMouseClick(MouseButton.PRIMARY, 1);
        internalMouseClick(MouseButton.PRIMARY, 2);
        return this;
    }

    /**
     * Double-clicks the secondary mouse button. Automatically adds mousePress and mouseRelease calls.
     * 
     * @see FXRobot#mouseClick(MouseButton, int)
     */
    public GcRobotFX mouseDblClickSecondary()
    {
        checkForModalChildStages();
        internalMouseClick(MouseButton.SECONDARY, 1);
        internalMouseClick(MouseButton.SECONDARY, 2);
        return this;
    }

    /**
     * Drags with the primary mouse button pressed.
     * 
     * @see FXRobot#mouseDrag(MouseButton)
     */
    public GcRobotFX mouseDrag()
    {
        internalMouseDrag(MouseButton.PRIMARY);
        return this;
    }

    /**
     * Drags with the secondary mouse button pressed.
     * 
     * @see FXRobot#mouseDrag(MouseButton)
     */
    public GcRobotFX mouseDragSecondary()
    {
        internalMouseDrag(MouseButton.SECONDARY);
        return this;
    }

    private void internalMouseClick(MouseButton button, int clickCount)
    {
        internalMousePress(button, clickCount);
        internalMouseRelease(button, clickCount);

        if (GcUtilsFX.isPlatformAlive())
        {
            m_robot.mouseClick(button, clickCount);
            GcUtilsFX.waitForIdle();
        }
    }

    private void internalMouseDrag(MouseButton button)
    {
        if (GcUtilsFX.isPlatformAlive())
        {
            m_robot.mouseDrag(button);
            GcUtilsFX.waitForIdle();
        }
    }

    private void internalMousePress(MouseButton button, int clickCount)
    {
        if (GcUtilsFX.isPlatformAlive())
        {
            checkForModalChildStages();
            m_robot.mousePress(button, clickCount);
            GcUtilsFX.waitForIdle();
        }
    }

    private void internalMouseRelease(MouseButton button, int clickCount)
    {
        if (GcUtilsFX.isPlatformAlive())
        {
            checkForModalChildStages();
            m_robot.mouseRelease(button, clickCount);
            GcUtilsFX.waitForIdle();
        }
    }

    /**
     * Set the focus to the component given via CSS selector. Setting the focus is done via an explicit call to
     * {@link Node#requestFocus()} and not using any method of the {@link FXRobot}.
     */
    public GcRobotFX focus(String selector)
    {
        return focus(m_stage.node(selector));
    }

    /**
     * Set the focus to the given component. Setting the focus is done via an explicit call to
     * {@link Node#requestFocus()} and not using any method of the {@link FXRobot}.
     */
    public GcRobotFX focus(final GcComponentFX<?> component)
    {
        checkForModalChildStages();
        GcUtilsFX.runLaterAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                component.getNode().requestFocus();
            }
        });

        GcUtilsFX.waitForIdle();
        return this;
    }

    private void checkForModalChildStages()
    {
        for (Stage s : StageHelper.getStages())
        {
            if (s.getOwner() == m_stage.getFXComponent() && s.isShowing() && s.getModality() != Modality.NONE)
            {
                throw new GcAssertException("The stage <" + ((Stage)m_stage.getFXComponent()).getTitle() + "> is blocked by the modal child window <" + s.getTitle() + ">");
            }
        }
    }
}
