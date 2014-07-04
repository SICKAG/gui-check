// Copyright 2013 SICK AG. All rights reserved.
package de.sick.guicheck.swing;

import java.awt.Component;

import javax.swing.JComponent;

/**
 * @author  linggol (created)
 */
public class GcJComponentSwing extends GcComponentSwing<GcJComponentSwing>
{
    private final JComponent m_component;

    GcJComponentSwing(JComponent component)
    {
        m_component = component;
    }
    
    @Override
    public JComponent getComponent()
    {
        return m_component;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Component> T getSwingComponent()
    {
        return (T)getComponent();
    }
}
