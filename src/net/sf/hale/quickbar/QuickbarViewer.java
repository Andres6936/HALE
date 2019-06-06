/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2011 Jared Stephen
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

package net.sf.hale.quickbar;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.hale.Game;
import net.sf.hale.ability.Ability;
import net.sf.hale.entity.PC;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;

/**
 * A widget for viewing an entire Quickbar
 *
 * @author Jared Stephen
 */

public class QuickbarViewer extends Widget
{
    private Quickbar quickbar;
    private List< QuickbarSlotButton > itemButtons;
    private Map< QuickbarGroup, List< QuickbarSlotButton > > abilityButtons;

    private int buttonGap, groupGap;

    private static final int Rows = 3;
    private static final int Cols = 12;

    /**
     * Create a new QuickbarViewer widget.  The widget
     * is empty until a {@link #setQuickbar(Quickbar)} is
     * called.
     */

    public QuickbarViewer( )
    {
        itemButtons = new ArrayList< QuickbarSlotButton >( Quickbar.ItemSlots );

        abilityButtons = new LinkedHashMap< QuickbarGroup, List< QuickbarSlotButton > >( );
        for ( QuickbarGroup group : Game.ruleset.getAllQuickbarGroups( ) )
        {
            abilityButtons.put( group, new ArrayList< QuickbarSlotButton >( ) );
        }

        for ( int i = 0; i < Quickbar.ItemSlots; i++ )
        {
            QuickbarSlotButton button = new QuickbarSlotButton( i );
            itemButtons.add( button );
            add( button );
        }

        setQuickbar( quickbar );
    }

    @Override
    protected void layout( )
    {
        int row = 0;
        int col = 0;
        int groupGapCount = 0;

        for ( Button b : itemButtons )
        {
            b.setSize( b.getPreferredWidth( ), b.getPreferredHeight( ) );
            b.setPosition( getInnerX( ) + col * ( b.getWidth( ) + buttonGap ) + groupGapCount * groupGap,
                           getInnerY( ) + row * ( b.getHeight( ) + buttonGap ) );

            row++;
            if ( row == Rows )
            {
                row = 0;
                col++;
            }
        }

        groupGapCount++;
        if ( row != 0 )
        {
            row = 0;
            col++;
        }

        for ( QuickbarGroup group : abilityButtons.keySet( ) )
        {
            for ( QuickbarSlotButton b : abilityButtons.get( group ) )
            {
                b.setSize( b.getPreferredWidth( ), b.getPreferredHeight( ) );
                b.setPosition( getInnerX( ) + col * ( b.getWidth( ) + buttonGap ) + groupGapCount * groupGap,
                               getInnerY( ) + row * ( b.getHeight( ) + buttonGap ) );

                row++;
                if ( row == Rows )
                {
                    row = 0;
                    col++;
                }
            }


            if ( ! abilityButtons.get( group ).isEmpty( ) )
            {
                groupGapCount++;
            }

            if ( row != 0 )
            {
                row = 0;
                col++;
            }
        }
    }

    @Override
    public int getPreferredInnerWidth( )
    {
        return itemButtons.get( 0 ).getPreferredWidth( ) * Cols + ( Cols - 1 ) * buttonGap + groupGap * abilityButtons.size( );
    }

    @Override
    public int getPreferredInnerHeight( )
    {
        return itemButtons.get( 0 ).getPreferredHeight( ) * Rows + ( Rows - 1 ) * buttonGap;
    }

    @Override
    protected void applyTheme( ThemeInfo themeInfo )
    {
        super.applyTheme( themeInfo );

        buttonGap = themeInfo.getParameter( "buttonGap", 0 );
        groupGap = themeInfo.getParameter( "groupGap", 0 );
    }

    /**
     * Sets the Quickbar being viewed by this Widget to the specified Quickbar
     *
     * @param quickbar the Quickbar to be viewed
     */

    public void setQuickbar( Quickbar quickbar )
    {
        this.quickbar = quickbar;
        if ( quickbar == null )
        {
            for ( QuickbarSlotButton button : itemButtons )
            {
                button.setSlot( null, null );
            }
        }
        else
        {
            for ( int i = 0; i < Quickbar.ItemSlots; i++ )
            {
                QuickbarSlot slot = quickbar.getSlot( i );
                itemButtons.get( i ).setSlot( slot, quickbar );
            }
        }
    }

    /**
     * Finds the QuickbarSlotButton that is under the specified mouse coordinates,
     * if any, and returns the Quickbar index corresponding to that Slot.
     *
     * @param x the mouse x coordinate
     * @param y the mouse y coordinate
     * @return the Quickbar index the mouse coordinates are over, or -1 if the mouse
     * is not over a QuickbarSlotButton
     */

    public int findSlotIndexUnderMouse( int x, int y )
    {
        for ( QuickbarSlotButton button : itemButtons )
        {
            if ( button.isInside( x, y ) )
            { return button.getIndex( ); }
        }

        return - 1;
    }

    /**
     * Returns the QuickbarSlotButton with the specified index
     *
     * @param index the Quickbar index of the QuickbarSlotButton to find.
     * @return the QuickbarSlotButton viewing the specified index.
     */

    public QuickbarSlotButton getButton( int index )
    {
        if ( index < 0 || index >= Quickbar.ItemSlots ) return null;

        return itemButtons.get( index );
    }

    /**
     * Returns the QuickbarSlot at the specified view index.  This is the index of the
     * Button as shown on the screen from left to right.  (Note that the displayed index
     * is 1 greater than the actual index)
     *
     * @param index the view index of QuickbarSlotButton to retrieve
     * @return the QuickbarSlotButton at the specified view index
     */

    public QuickbarSlotButton getButtonAtViewIndex( int index )
    {
        return itemButtons.get( index );
    }

    /**
     * Returns the Quickbar that is currently being viewed by this QuickbarViewer.
     * Returns null if no Quickbar is being viewed.
     *
     * @return the Quickbar currently being viewed by this QuickbarViewer
     */

    public Quickbar getQuickbar( ) { return quickbar; }

    /**
     * All buttons in this QuickbarViewer are updated with the current state of the
     * associated Quickbar entry.  For example, cooldown rounds and item quantities
     * are updated.
     *
     * @param selected the Creature that is currently selected, whose Quickbar should
     *                 be displayed
     */

    public void updateContent( PC selected )
    {
        if ( selected == null ) return;

        if ( quickbar != selected.quickbar )
        {
            setQuickbar( selected.quickbar );
        }

        this.removeAllChildren( );
        for ( QuickbarSlotButton itemButton : itemButtons )
        {
            this.add( itemButton );
        }

        for ( QuickbarGroup group : abilityButtons.keySet( ) )
        {
            List< QuickbarSlotButton > buttons = abilityButtons.get( group );
            buttons.clear( );

            // add buttons for relevant abilities
            for ( Ability ability : group.getAbilities( ) )
            {
                if ( ! selected.abilities.has( ability ) ) continue;

                QuickbarSlotButton slotButton = new QuickbarSlotButton( - 1 );
                slotButton.setSlot( new AbilityActivateSlot( ability, selected ), selected.quickbar );
                slotButton.setShowIndexLabel( false );
                slotButton.setDisabledExceptActivate( true );
                buttons.add( slotButton );
                this.add( slotButton );
            }
        }
    }
}
