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

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.ColorSelector;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.ColorSpaceHSL;

/**
 * A popup window for selecting a color from an RGB color space.
 *
 * @author Jared Stephen
 */

public class ColorSelectorPopup extends PopupWindow
{
    private int buttonGap;

    private Content content;

    private List< Callback > callbacks;

    /**
     * Creates a new PopupWindow with the specified owner widget
     *
     * @param parent the owner Widget for this PopupWindow
     */

    public ColorSelectorPopup( Widget parent )
    {
        super( parent );

        this.setCloseOnClickedOutside( false );

        content = new Content( );
        this.add( content );

        callbacks = new ArrayList< Callback >( );
    }

    /**
     * Sets the color currently selected and previewed by this color selector
     *
     * @param color the color to select
     */

    public void setColor( Color color )
    {
        if ( color == null )
        {
            return;
        }
        else
        {
            content.colorSelector.setColor( color );
        }
    }

    /**
     * Adds the specified Callback to the List of callbacks that are called
     * when a color is selected
     *
     * @param callback the callback to add
     */

    public void addCallback( Callback callback )
    {
        callbacks.add( callback );
    }

    private void accept( )
    {
        ColorSelectorPopup.this.closePopup( );

        for ( Callback callback : callbacks )
        {
            callback.colorSelected( content.colorSelector.getColor( ) );
        }
    }

    /**
     * The callback interface for this ColorSelector.  Any Object
     * wishing to recieve a callback when the color is accepted should
     * implement this and use {@link #addCallback(Callback)}
     *
     * @author Jared Stephen
     */

    public interface Callback
    {
        public void colorSelected( Color color );
    }

    private class Content extends Widget
    {
        private final ColorSelector colorSelector;
        private final Button cancel, accept;
        private final Label title;

        private Content( )
        {
            title = new Label( "Choose a Color" );
            title.setTheme( "titlelabel" );
            add( title );

            colorSelector = new ColorSelector( new ColorSpaceHSL( ) );
            colorSelector.setTheme( "colorselector" );
            colorSelector.setUseColorArea2D( true );
            colorSelector.setUseLabels( false );
            colorSelector.setShowPreview( true );
            colorSelector.setShowAlphaAdjuster( false );
            this.add( colorSelector );

            cancel = new Button( "Cancel" );
            cancel.setTheme( "cancelbutton" );
            cancel.addCallback( new Runnable( )
            {
                @Override
                public void run( )
                {
                    ColorSelectorPopup.this.closePopup( );
                }
            } );
            this.add( cancel );

            accept = new Button( "Accept" );
            accept.setTheme( "acceptbutton" );
            accept.addCallback( new Runnable( )
            {
                @Override
                public void run( )
                {
                    accept( );
                }
            } );
            this.add( accept );
        }

        @Override
        public int getPreferredInnerWidth( )
        {
            return colorSelector.getPreferredWidth( );
        }

        @Override
        public int getPreferredInnerHeight( )
        {
            return title.getPreferredHeight( ) + colorSelector.getPreferredHeight( ) +
                    buttonGap + accept.getPreferredHeight( );
        }

        @Override
        protected void applyTheme( ThemeInfo themeInfo )
        {
            super.applyTheme( themeInfo );

            buttonGap = themeInfo.getParameter( "buttongap", 0 );
        }

        @Override
        protected void layout( )
        {
            title.setSize( title.getPreferredWidth( ), title.getPreferredHeight( ) );
            title.setPosition( getInnerX( ) + getInnerWidth( ) / 2 - title.getWidth( ) / 2, getInnerY( ) );

            colorSelector.setSize( colorSelector.getPreferredWidth( ), colorSelector.getPreferredHeight( ) );
            colorSelector.setPosition( getInnerX( ), title.getBottom( ) );

            cancel.setSize( cancel.getPreferredWidth( ), cancel.getPreferredHeight( ) );
            cancel.setPosition( getInnerX( ) + getInnerWidth( ) / 2 + buttonGap,
                                colorSelector.getBottom( ) + buttonGap );

            accept.setSize( accept.getPreferredWidth( ), accept.getPreferredHeight( ) );
            accept.setPosition( getInnerX( ) + getInnerWidth( ) / 2 - accept.getWidth( ) - buttonGap,
                                colorSelector.getBottom( ) + buttonGap );
        }
    }
}
