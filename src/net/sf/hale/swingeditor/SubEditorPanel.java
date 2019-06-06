/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2012 Jared Stephen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package net.sf.hale.swingeditor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * The base class for all sub editor panels
 *
 * @author Jared
 */

public abstract class SubEditorPanel extends JPanel
{
    /**
     * The parent frame that this panel has been added to
     */

    protected JFrame parentFrame;

    /**
     * A set of grid bag constraints for easy reuse by child panels
     */

    protected GridBagConstraints c;

    public SubEditorPanel( JFrame parent )
    {
        super( new GridBagLayout( ) );
        this.parentFrame = parent;

        c = new GridBagConstraints( );
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = 0;
        c.insets = new Insets( 2, 5, 2, 5 );
    }

    /**
     * Adds a single row of elements to the specified parent component
     *
     * @param name      the text of the label which will be the first component in the row, or null for no label
     * @param component the main component to add
     * @param fill      whether the main component should fill available space (true) or size itself (false)
     */

    private void addRow( String name, JComponent component, boolean fill )
    {
        c.gridy++;
        c.fill = GridBagConstraints.NONE;

        if ( name != null )
        {
            c.gridx = GridBagConstraints.RELATIVE;

            JLabel label = new JLabel( name );
            label.setLabelFor( component );

            c.weightx = 0.0;
            c.gridwidth = 1;
            c.anchor = GridBagConstraints.EAST;
            add( label, c );
        }
        else
        {
            c.gridx = 1;
        }

        c.weightx = 0.5;
        c.gridwidth = 3;
        if ( fill ) c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        add( component, c );
    }

    /**
     * Adds a single row of elements to this Panel with no label
     *
     * @param component the main component to add
     */

    protected void addRow( JComponent component )
    {
        addRow( null, component, false );
    }

    /**
     * Adds a single row of elements to this Panel
     *
     * @param name      the text of the label which will be the first component in the row, or null for no label
     * @param component the main component to add
     */

    protected void addRow( String name, JComponent component )
    {
        addRow( name, component, false );
    }

    /**
     * Adds a single row of elements to this Panel with no label
     *
     * @param component the main component to add.  This component will be sized to fill
     *                  all available space horizontally
     */

    protected void addRowFilled( JComponent component )
    {
        addRow( null, component, true );
    }

    /**
     * Adds a single row of elements to this Panel
     *
     * @param name      the text of the label which will be the first component in the row, or null for no label
     * @param component the main component to add.  This component will be sized to fill
     *                  all available space horizontally
     */

    protected void addRowFilled( String name, JComponent component )
    {
        addRow( name, component, true );
    }

    /**
     * Adds a row of multiple elements to this panel
     *
     * @param name       the text of the label which will be the first component in the row, or null for no label
     * @param components the list of components to add
     */

    protected void addRow( String name, JComponent... components )
    {
        JPanel pane = new JPanel( );
        for ( JComponent component : components )
        {
            pane.add( component );
        }

        addRow( name, pane, false );
    }

    /**
     * Adds a row of multiple elements to this panel with no label
     *
     * @param components the list of components to add
     */

    protected void addRow( JComponent... components )
    {
        addRow( null, components );
    }
}
