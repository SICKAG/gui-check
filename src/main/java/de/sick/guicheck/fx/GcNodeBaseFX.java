// Copyright 2013 SICK AG. All rights reserved.
package de.sick.guicheck.fx;

import javafx.scene.Node;

/**
 * The base class for wrappers for JavaFX scene nodes.
 * 
 * @author  linggol (created)
 */
abstract class GcNodeBaseFX<T extends GcComponentFX<T>> extends GcComponentFX<T>
{
    private final Node m_node;
    
    GcNodeBaseFX(Node node)
    {
        m_node = node;
    }
    
    @Override
    public Node getNode()
    {
        return m_node;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <Z> Z getFXComponent()
    {
        return (Z)m_node;
    }
}
