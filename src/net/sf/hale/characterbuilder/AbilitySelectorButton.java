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

package net.sf.hale.characterbuilder;

import java.util.ArrayList;
import java.util.List;

import net.sf.hale.ability.Ability;
import net.sf.hale.entity.PC;
import net.sf.hale.icon.Icon;
import net.sf.hale.view.AbilityDetailsWindow;
import net.sf.hale.view.DragAndDropHandler;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.AnimationState.StateKey;
import de.matthiasmann.twl.utils.TintAnimator;

/**
 * The button used to select an Ability within the BuilderPaneAbilities
 *
 * @author Jared Stephen
 */

public class AbilitySelectorButton extends Button implements Runnable
{
    private static StateKey STATE_ALREADY_OWNED = StateKey.get( "alreadyOwned" );
    private static StateKey STATE_PREREQS_NOT_MET = StateKey.get( "prereqsNotMet" );

    private List< Callback > callbacks;

    private Widget widgetToAddWindowsTo;
    private HoverHolder widgetToAddHoverTo;
    private final boolean showSelectable;

    private Ability ability;
    private DragAndDropHandler dragAndDropHandler;
    private PC parent;

    private AbilityHover abilityHover;
    private AbilityHoverDetails abilityHoverDetails;

    private boolean isHovering;

    private String hoverText;
    private Color hoverTextColor;

    private Icon icon;

    /**
     * Creates a new AbilitySelectorButton for the specified Ability.  The onMouseOver
     * hover widgets are added to the specified parent widget.
     *
     * @param ability            the ability that this selector will view and select
     * @param widgetToAddHoverTo the widget to add hover widgets to
     * @param showSelectable     true if abilities where prereqs are met should be highlighted, false if not
     */

    public AbilitySelectorButton( Ability ability, PC parent, HoverHolder widgetToAddHoverTo, boolean showSelectable )
    {
        this.ability = ability;
        this.parent = parent;
        this.icon = ability.getIcon( );
        this.showSelectable = showSelectable;

        this.widgetToAddHoverTo = widgetToAddHoverTo;
        this.addCallback( this );
        this.setSize( ability.getIcon( ).getWidth( ), ability.getIcon( ).getHeight( ) );

        callbacks = new ArrayList< Callback >( );
    }

    /**
     * Sets the enabled / disabled and animation state of this button based
     * on whether the specified creature owns this ability or meets the
     * prereqs for this ability
     *
     * @param workingCopy the creature to use
     */

    public void setState( PC workingCopy )
    {
        parent = workingCopy;

        if ( workingCopy.abilities.has( ability ) )
        {
            getAnimationState( ).setAnimationState( STATE_ALREADY_OWNED, true );
            icon = ability.getIcon( );
            setEnabled( false );
        }
        else if ( ability.meetsPrereqs( workingCopy ) && showSelectable )
        {
            icon = ability.getIcon( );
            setEnabled( true );
        }
        else
        {
            getAnimationState( ).setAnimationState( STATE_PREREQS_NOT_MET, true );
            icon = ability.getIcon( ).multiplyByColor( new Color( 0xFF444444 ) );
            setEnabled( false );
        }
    }

    /**
     * Set the text that is displayed when hovering over this Widget, in
     * addition to displaying the ability name
     *
     * @param text  the text to display
     * @param color the Color to display the text
     */

    protected void setHoverText( String text, Color color )
    {
        this.hoverText = text;
        this.hoverTextColor = color;
    }

    /**
     * Sets the widget that details windows created by this widget will be added to
     *
     * @param widget the widget to add details windows to
     */

    protected void setWidgetToAddWindowsTo( Widget widget )
    {
        this.widgetToAddWindowsTo = widget;
    }

    /**
     * Adds the specified callback to the list of callbacks that are called
     * whenever a selection is made
     *
     * @param callback the callback to add
     */

    public void addCallback( Callback callback )
    {
        callbacks.add( callback );
    }

    @Override
    protected void paintWidget( GUI gui )
    {
        icon.draw( getInnerX( ), getInnerY( ) );
    }

    // left click callback
    @Override
    public void run( )
    {
        if ( ability.meetsPrereqs( parent ) && showSelectable )
        {
            for ( Callback callback : callbacks )
            {
                callback.selectionMade( ability );
            }
        }
    }

    private void handleHover( Event evt )
    {
        if ( evt.isMouseEvent( ) )
        {
            boolean hover = ( evt.getType( ) != Event.Type.MOUSE_EXITED ) &&
                    ( isMouseInside( evt ) || abilityHoverDetails.isInside( evt.getMouseX( ), evt.getMouseY( ) ) );

            if ( hover && ! isHovering )
            {
                startHover( );
            }
            else if ( ! hover && isHovering )
            {
                endHover( );
            }
        }
    }

    @Override
    protected boolean handleEvent( Event evt )
    {
        handleHover( evt );

        switch ( evt.getType( ) )
        {
            case MOUSE_ENTERED:
            case MOUSE_MOVED:
                return true;
            default:
                // do nothing
        }

        if ( dragAndDropHandler != null )
        {
            if ( ! dragAndDropHandler.handleEvent( evt ) )
            {
                dragAndDropHandler = null;
            }
        }

        return super.handleEvent( evt );
    }

    private void startHover( )
    {
        abilityHover = new AbilityHover( );
        abilityHoverDetails = new AbilityHoverDetails( );
        widgetToAddHoverTo.setHoverWidgets( abilityHover, abilityHoverDetails );

        isHovering = true;
    }

    private void endHover( )
    {
        if ( abilityHover != null )
        {
            widgetToAddHoverTo.removeHoverWidgets( abilityHover, abilityHoverDetails );
        }

        isHovering = false;
    }

    /**
     * The interface for a callback for this Widget
     *
     * @author Jared Stephen
     */

    public interface Callback
    {
        /**
         * Called whenever the widget is left clicked
         *
         * @param ability the ability that has been selected
         */
        public void selectionMade( Ability ability );
    }

    private class AbilityHover extends Widget
    {
        private Label abilityNameLabel;
        private Label hoverMessageLabel;

        private AbilityHover( )
        {
            abilityNameLabel = new HoverLabel( ability.getName( ) );
            abilityNameLabel.setTheme( "abilitynamelabel" );
            this.add( abilityNameLabel );

            hoverMessageLabel = new HoverLabel( hoverText );
            hoverMessageLabel.setTheme( "hovermessagelabel" );
            this.add( hoverMessageLabel );

            hoverMessageLabel.setTintAnimator( new TintAnimator( new TintAnimator.GUITimeSource( this ), hoverTextColor ) );
        }

        @Override
        protected boolean handleEvent( Event evt )
        {
            handleHover( evt );
            return false;
        }

        @Override
        protected void layout( )
        {
            super.layout( );

            abilityNameLabel.setSize( abilityNameLabel.getPreferredWidth( ),
                                      abilityNameLabel.getPreferredHeight( ) );

            hoverMessageLabel.setSize( hoverMessageLabel.getPreferredWidth( ),
                                       hoverMessageLabel.getPreferredHeight( ) );

            int width = Math.max( abilityNameLabel.getWidth( ), hoverMessageLabel.getWidth( ) );
            int height = abilityNameLabel.getHeight( ) + hoverMessageLabel.getHeight( );

            setSize( width + getBorderHorizontal( ), height + getBorderVertical( ) );

            abilityNameLabel.setPosition( getInnerX( ) + ( getInnerWidth( ) - abilityNameLabel.getWidth( ) ) / 2,
                                          getInnerY( ) );

            hoverMessageLabel.setPosition( getInnerX( ) + ( getInnerWidth( ) - hoverMessageLabel.getWidth( ) ) / 2,
                                           getInnerY( ) + abilityNameLabel.getHeight( ) );

            Widget parent = AbilitySelectorButton.this;
            int x = parent.getX( ) + ( parent.getWidth( ) ) / 2 - getWidth( ) / 2;
            setPosition( x, parent.getInnerY( ) - getHeight( ) );
        }
    }

    /**
     * An interface for a parent widget to hold the hover over widgets of this button
     *
     * @author Jared
     */

    public interface HoverHolder
    {
        /**
         * Sets the hover widgets currently being held by the HoverHolder
         *
         * @param hoverTop    the top hover widget
         * @param hoverBottom the bottom hover widget
         */

        public void setHoverWidgets( Widget hoverTop, Widget hoverBottom );

        /**
         * Removes the specified widgets if they are still the hover widgets for the
         * hoverHolder
         *
         * @param hoverTop    the top hover widget
         * @param hoverBottom the bottom hover widget
         */

        public void removeHoverWidgets( Widget hoverTop, Widget hoverBottom );

        public GUI getGUI( );
    }

    private class HoverLabel extends Label
    {
        private HoverLabel( String name )
        {
            super( name );
        }

        @Override
        protected boolean handleEvent( Event evt )
        {
            handleHover( evt );
            return false;
        }
    }

    private class AbilityHoverDetails extends Button implements Runnable
    {
        private AbilityHoverDetails( )
        {
            super( "View Details" );
            addCallback( this );
        }

        @Override
        protected boolean handleEvent( Event evt )
        {
            handleHover( evt );

            return super.handleEvent( evt );
        }

        @Override
        protected void layout( )
        {
            super.layout( );

            setSize( getPreferredWidth( ), getPreferredHeight( ) );

            Widget parent = AbilitySelectorButton.this;
            int x = parent.getX( ) + ( parent.getWidth( ) ) / 2 - getWidth( ) / 2;
            setPosition( x, parent.getInnerBottom( ) );
        }

        // left click callback
        @Override
        public void run( )
        {
            Widget window = new AbilityDetailsWindow( ability, parent, false );

            int x = getX( ) + getWidth( ) / 2 - window.getWidth( ) / 2;
            int y = getY( ) + getHeight( ) / 2 - window.getHeight( ) / 2;

            window.setPosition( x, y );

            widgetToAddWindowsTo.add( window );

            endHover( );
        }
    }

    /**
     * Returns the ability associated with this button
     *
     * @return the associated ability
     */

    public Ability getAbility( ) { return ability; }
}
