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

package net.sf.hale.widgets;

import net.sf.hale.icon.Icon;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;

/**
 * A Widget with a text area for showing a basic view of
 * something or an expanded, detailed view.  This includes
 * a textArea with HTML content and an optional IconViewer
 *
 * @author Jared Stephen
 */

public abstract class ExpandableWidget extends Widget
{
    private boolean expanded;

    private final TextArea textArea;
    private final HTMLTextAreaModel textAreaModel;
    private IconViewer iconViewer;

    private final Button expand, contract;

    /**
     * Creates a new Expandable Widget with the specified Icon.  If this Icon is null,
     * no IconViewer is shown, only the TextArea
     */

    public ExpandableWidget( Icon icon )
    {
        if ( icon != null )
        {
            iconViewer = new IconViewer( icon );
            iconViewer.setTheme( "iconviewer" );
            iconViewer.setEventHandlingEnabled( false );
            add( iconViewer );
        }

        textAreaModel = new HTMLTextAreaModel( );
        textArea = new TextArea( textAreaModel );
        textArea.setTheme( "description" );
        add( textArea );

        ExpandContractCallback cb = new ExpandContractCallback( );

        expand = new Button( );
        expand.setTheme( "expandbutton" );
        expand.addCallback( cb );
        add( expand );

        contract = new Button( );
        contract.setTheme( "contractbutton" );
        contract.setVisible( false );
        contract.addCallback( cb );
        add( contract );
    }

    /**
     * Returns true if this Widget is in its full size expanded state, false otherwise
     *
     * @return whether this Widget is in its expanded state
     */

    public boolean isExpanded( )
    {
        return expanded;
    }

    /**
     * Returns the text area used to display the content of this Widget.
     *
     * @return the text area used to display the content of this Widget.
     */

    protected TextArea getTextArea( )
    {
        return textArea;
    }

    /**
     * Returns the height of the expand / contract button widgets
     *
     * @return the height of the expand / contract button widgets
     */

    protected int getButtonHeight( )
    {
        return Math.max( expand.getPreferredHeight( ), contract.getPreferredHeight( ) );
    }

    /**
     * Returns the width of the expand / contract buttons
     *
     * @return the width of the expand / contract buttons
     */

    protected int getButtonWidth( )
    {
        return Math.max( expand.getPreferredWidth( ), contract.getPreferredWidth( ) );
    }

    /**
     * Append the main part of the description that is always shown at the top,
     * when expanded or contracted
     *
     * @param sb the StringBuilder to append the description to
     */

    protected abstract void appendDescriptionMain( StringBuilder sb );


    /**
     * Append the detailed part of the description that is only shown when the
     * Widget is expanded
     *
     * @param sb the StringBuilder to append the description to
     */

    protected abstract void appendDescriptionDetails( StringBuilder sb );

    /**
     * If false, neither the expand or contract button will be shown.  If true,
     * the currently active button will be shown
     */

    public void setExpandContractVisible( boolean visible )
    {
        if ( ! visible )
        {
            expand.setVisible( false );
            contract.setVisible( false );
        }
        else
        {
            expand.setVisible( ! expanded );
            contract.setVisible( expanded );
        }
    }

    /**
     * Updates the current description shown for this Widget
     */

    public void update( )
    {
        StringBuilder sb = new StringBuilder( );
        appendDescriptionMain( sb );
        if ( expanded ) appendDescriptionDetails( sb );
        textAreaModel.setHtml( sb.toString( ) );
    }

    @Override
    public int getPreferredHeight( )
    {
        if ( expanded )
        {
            return heightMax( ) + getButtonHeight( ) + getBorderVertical( );
        }
        else
        {
            return heightMax( ) + getBorderVertical( );
        }
    }

    private int heightMax( )
    {
        if ( iconViewer != null )
        { return Math.max( iconViewer.getPreferredHeight( ), textArea.getPreferredHeight( ) ); }
        else
        { return textArea.getPreferredHeight( ); }
    }

    @Override
    public int getPreferredWidth( )
    {
        return Short.MAX_VALUE;
    }

    @Override
    protected void layout( )
    {
        super.layout( );

        if ( iconViewer != null )
        {
            iconViewer.setSize( iconViewer.getPreferredWidth( ), iconViewer.getPreferredHeight( ) );
            iconViewer.setPosition( getInnerX( ), getInnerY( ) );

            textArea.setPosition( iconViewer.getRight( ), getInnerY( ) );
        }
        else
        {
            textArea.setPosition( getInnerX( ), getInnerY( ) );
        }

        if ( getInnerRight( ) > textArea.getX( ) )
        { textArea.setSize( getInnerRight( ) - textArea.getX( ), getInnerHeight( ) ); }

        int width = Math.max( expand.getPreferredWidth( ), contract.getPreferredWidth( ) );
        int height = Math.max( expand.getPreferredHeight( ), contract.getPreferredHeight( ) );

        expand.setSize( width, height );
        expand.setPosition( getInnerRight( ) - expand.getWidth( ), getInnerBottom( ) - expand.getHeight( ) );

        contract.setSize( width, height );
        contract.setPosition( getInnerRight( ) - contract.getWidth( ), getInnerBottom( ) - expand.getHeight( ) );
    }

    private class ExpandContractCallback implements Runnable
    {
        @Override
        public void run( )
        {
            expanded = ! expanded;

            expand.setVisible( ! expanded );
            contract.setVisible( expanded );

            update( );

            // remove the hover over since the whole widget has just moved
            if ( ! expanded )
            {
                expand.getModel( ).setHover( false );
            }
            else
            {
                contract.getModel( ).setHover( false );
            }

            invalidateLayout( );
        }
    }
}
