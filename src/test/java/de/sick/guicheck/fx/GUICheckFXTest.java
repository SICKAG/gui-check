package de.sick.guicheck.fx;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.sick.guicheck.GcAssertException;

public class GUICheckFXTest extends GcTestFX
{
    @BeforeClass
    public static void setUpBeforeClass()
    {
        // GcUtilsFX.setSlowMotion(100);
        startApp("de.sick.guicheck.fx.GUICheckFX");
    }

    @Test
    public void test() throws Exception
    {
        GcStageFX l_stage = stage("GUI Check FX");
        l_stage.propertyIs("title", "GUI Check FX 0.1");
        l_stage.menuBar("#menubar").menuPathExists("edit/delete");
        l_stage.menuBar("#menubar").menuPathExists("help");
        l_stage.menuBar("#menubar").fireMenuItem("file/close");
        l_stage.menuBar("#menubar").menuPathPropertyIs("file/close", "disable", false);
        l_stage.menuBar("#menubar").menuPathPropertyIs("device", "disable", false);
        l_stage.menuBar("#menubar").menuPathPropertyIs("device/export", "disable", true);

        l_stage.robot().mouseMoveToCenter("#button13").mouseClick();
        l_stage.node("#button8").propertyIs("text", "8");

        l_stage.robot().focus("#text1");
        l_stage.node("#text1").propertyIs("focused", true);
        l_stage.robot().keyType("a");
        l_stage.node("#text1").propertyIs("text", "a");

        l_stage.robot().focus("#text2");
        l_stage.robot().keyType("Danke für den Fisch");
        l_stage.node("#text2").propertyIs("text", "Danke für den Fisch");

        l_stage.node("#text2").ensurePropertyIs("text", "TROET");
        l_stage.node("#text2").propertyIs("text", "TROET");

        // Opens a dialog and checks if other buttons are still clickable
        l_stage.robot().mouseMoveToCenter("#button14").mouseClick();
        GcUtilsFX.waitForIdle();
        
        // Pressing it again should fail since now a modal dialog is visible
        try
        {
            l_stage.robot().mouseMoveToCenter("#button14").mouseClick();
            GcUtilsFX.waitAndExitWhenAllStagesClosed();
            Assert.fail("Pressing a button on a dialog which is the parent of a modal dialog should not work");
        }
        catch (GcAssertException e)
        {
            // expected
        }
    }
}
