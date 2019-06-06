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

import net.sf.hale.Game;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.ValueAdjusterInt;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.SimpleIntegerModel;

/**
 * A popup allowing the user to select a quantity of items for use
 * with a specified action.  The popup has buttons to increment or
 * decrement the quantity and also allows the user to directly type
 * in the quantity that they want.
 *
 * @author Jared Stephen
 */

public class MultipleItemPopup extends PopupWindow implements Runnable
{
    private final Content content;

    private Callback callback;

    private final Label label;
    private final Label valueLabel;
    private final ValueAdjusterInt multiple;
    private final Button min, max;
    private final Button accept, cancel;

    /**
     * Creates a new MultipleItemPopup with the specified parent
     * Widget.
     *
     * @param parent the parent Widget
     */

    public MultipleItemPopup( Widget parent )
    {
        super( parent );

        this.setCloseOnClickedOutside( false );

        content = new Content( );
        this.add( content );

        label = new Label( );
        label.setTheme( "titlelabel" );
        content.add( label );

        multiple = new ValueAdjusterInt( );
        multiple.setTheme( "valueadjuster" );
        content.add( multiple );

        min = new Button( );
        min.setTheme( "minbutton" );
        min.addCallback( new Runnable( )
        {
            @Override
            public void run( )
            {
                multiple.setValue( multiple.getMinValue( ) );
            }
        } );
        content.add( min );

        max = new Button( );
        max.setTheme( "maxbutton" );
        max.addCallback( new Runnable( )
        {
            @Override
            public void run( )
            {
                multiple.setValue( multiple.getMaxValue( ) );
            }
        } );
        content.add( max );

        valueLabel = new Label( );
        valueLabel.setTheme( "valuelabel" );
        content.add( valueLabel );

        accept = new Button( );
        accept.setTheme( "acceptbutton" );
        accept.addCallback( new Runnable( )
        {
            @Override
            public void run( )
            {
                callback.performItemAction( multiple.getValue( ) );
                closePopup( );
            }
        } );
        content.add( accept );

        cancel = new Button( );
        cancel.setTheme( "cancelbutton" );
        cancel.addCallback( new Runnable( )
        {
            @Override
            public void run( )
            {
                closePopup( );
            }
        } );
        content.add( cancel );
    }

    @Override
    protected void layout( )
    {
        super.layout( );


    }

    /**
     * This method is overridden to set the value label text based on the
     * currently selected quantity.
     */

    @Override
    public void run( )
    {
        valueLabel.setText( callback.getValueText( multiple.getValue( ) ) );
    }

    /**
     * Opens this PopupWindow in a centered position.  Uses the specified callback
     * to determine label text and as the callback for when the user accepts a
     * quantity.
     *
     * @param callback the callback that is used to set label text and called when
     *                 the user accepts a quantity
     */

    public void openPopupCentered( Callback callback )
    {
        this.callback = callback;

        int maxQuantity = callback.getMaximumQuantity( );
        int defaultQuantity = Math.min( 100, maxQuantity / 2 );

        multiple.setMinMaxValue( 1, maxQuantity );
        multiple.setModel( new SimpleIntegerModel( 1, callback.getMaximumQuantity( ), defaultQuantity ) );
        multiple.getModel( ).addCallback( this );
        multiple.setValue( defaultQuantity );

        label.setText( callback.getLabelText( ) + " how many?" );
        accept.setText( callback.getLabelText( ) );

        // set the value label text
        this.run( );

        super.openPopupCentered( );

        Game.mainViewer.getMenu( ).hide( );
    }

    /**
     * The callback that is used by this class to tailor the popup
     * for the various scenarios: giving multiple items, buying or
     * selling multiple items, etc
     *
     * @author Jared Stephen
     */

    public interface Callback extends Runnable
    {
        /**
         * Returns the title label text that the associated
         * MultipleItemPopup should use
         *
         * @return the title label text
         */

        public String getLabelText( );

        /**
         * Returns the value label text that the associated
         * MultipleItemPopup should use.  This text is used
         * to display the total cost of the items.  If cost
         * is not relevant (for giving, dropping, etc) then
         * this should return an empty String.
         *
         * @param quantity the quantity to compute the value for
         * @return the value label text
         */

        public String getValueText( int quantity );

        /**
         * Returns the maximum quantity that should be available
         * for the associated MultipleItemPopup.  The user will
         * be able to select between 1 and this value for the quantity
         * of the item.
         *
         * @return the maximum quantity available for the item
         */

        public int getMaximumQuantity( );

        /**
         * Method used as the callback for the MultipleItemPopup.
         * This is called with the specified quantity in order to
         * perform whatever action.
         *
         * @param quantity the quantity selected by the MultipleItemPopup
         */

        public void performItemAction( int quantity );
    }

    private class Content extends Widget
    {
        private int titleGap, acceptCancelGap, valueGap;

        @Override
        protected void applyTheme( ThemeInfo themeInfo )
        {
            super.applyTheme( themeInfo );

            titleGap = themeInfo.getParameter( "titleGap", 0 );
            acceptCancelGap = themeInfo.getParameter( "acceptCancelGap", 0 );
            valueGap = themeInfo.getParameter( "valueGap", 0 );
        }

        @Override
        public int getPreferredWidth( )
        {
            int width = label.getPreferredWidth( );
            width = Math.max( width, min.getPreferredWidth( ) +
                    multiple.getPreferredWidth( ) + max.getPreferredWidth( ) );

            width = Math.max( width, valueLabel.getPreferredWidth( ) );
            width = Math.max( width, accept.getPreferredWidth( ) +
                    cancel.getPreferredWidth( ) + acceptCancelGap );

            return width + getBorderHorizontal( );
        }

        @Override
        public int getPreferredHeight( )
        {
            int height = label.getPreferredHeight( ) + titleGap;

            int rowHeight = Math.max( min.getPreferredHeight( ), max.getPreferredHeight( ) );
            rowHeight = Math.max( rowHeight, multiple.getPreferredHeight( ) );
            height += rowHeight;

            height += valueLabel.getPreferredHeight( ) + 2 * valueGap;

            height += Math.max( accept.getPreferredHeight( ), cancel.getPreferredHeight( ) );

            return height + getBorderVertical( );
        }

        @Override
        protected void layout( )
        {
            super.layout( );

            int centerX = getInnerX( ) + getInnerWidth( ) / 2;

            label.setSize( label.getPreferredWidth( ), label.getPreferredHeight( ) );
            label.setPosition( centerX - label.getWidth( ) / 2, getInnerY( ) );

            min.setSize( min.getPreferredWidth( ), min.getPreferredHeight( ) );
            multiple.setSize( multiple.getPreferredWidth( ), multiple.getPreferredHeight( ) );
            max.setSize( max.getPreferredWidth( ), max.getPreferredHeight( ) );

            int rowX = centerX - ( min.getWidth( ) + multiple.getWidth( ) + max.getWidth( ) ) / 2;
            int rowY = label.getBottom( ) + titleGap;
            min.setPosition( rowX, rowY );
            multiple.setPosition( min.getRight( ), rowY );
            max.setPosition( multiple.getRight( ), rowY );

            rowY = Math.max( min.getBottom( ), max.getBottom( ) );
            rowY = Math.max( rowY, multiple.getBottom( ) );

            valueLabel.setSize( valueLabel.getPreferredWidth( ), valueLabel.getPreferredHeight( ) );
            valueLabel.setPosition( centerX - valueLabel.getWidth( ) / 2, rowY + valueGap );

            accept.setSize( accept.getPreferredWidth( ), accept.getPreferredHeight( ) );
            cancel.setSize( cancel.getPreferredWidth( ), cancel.getPreferredHeight( ) );
            rowX = centerX - ( accept.getWidth( ) + cancel.getWidth( ) + acceptCancelGap ) / 2;
            rowY = valueLabel.getBottom( ) + valueGap;

            accept.setPosition( rowX, rowY );
            cancel.setPosition( accept.getRight( ) + acceptCancelGap, rowY );
        }
    }
}
