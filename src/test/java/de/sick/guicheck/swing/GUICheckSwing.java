// Copyright 2014 SICK AG. All rights reserved.
package de.sick.guicheck.swing;

import javax.swing.JFrame;
import javax.swing.JTextField;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author  linggol (created)
 */
public class GUICheckSwing
{
    @BeforeClass
    public static void setUpClass() throws Exception
    {
        final JFrame f = new JFrame("GUICheckSwing Test Application");
        final JTextField l_comp = new JTextField(30);
        l_comp.setName("TestTextField");

        f.add(l_comp);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);        
    }
    
    @Test
    public void typeSpecialChars() throws Exception
    {
        final GcWindowSwing l_wnd = GcUtilsSwing.window("GUICheckSwing Test Application");
        final GcJComponentSwing l_tf = l_wnd.component("TestTextField");
        l_wnd.robot().focus(l_tf);
        
        l_wnd.robot().keyType("üäöÜÄÖß@");
        l_tf.propertyIs("text", "üäöÜÄÖß@");
    }
}
