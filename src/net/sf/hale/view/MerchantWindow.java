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

package net.sf.hale.view;

import net.sf.hale.Game;
import net.sf.hale.entity.PC;
import net.sf.hale.mainmenu.ConfirmationPopup;
import net.sf.hale.rules.Merchant;

import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ThemeInfo;

/**
 * A widget for displaying the list of wares and prices for a given Merchant
 *
 * @author Jared Stephen
 */

public class MerchantWindow extends GameSubWindow
{
    private int labelGap;

    private Merchant merchant;
    private PC creature;

    private final Label sellLabel, buyLabel;
    private final ItemListViewer viewer;

    /**
     * Creates a new empty MerchantWindow.  The merchant must be set with
     * {@link #setMerchant(Merchant)} and the creature set with {@link #updateContent(PC)}
     * for this Widget to have any content.
     */

    public MerchantWindow( )
    {
        sellLabel = new Label( );
        sellLabel.setTheme( "selllabel" );
        add( sellLabel );

        buyLabel = new Label( );
        buyLabel.setTheme( "buylabel" );
        add( buyLabel );

        viewer = new ItemListViewer( );
        add( viewer );
    }

    // override the close callback
    @Override
    public void run( )
    {
        if ( merchant.confirmOnExit( ) )
        {
            ConfirmationPopup popup = new ConfirmationPopup( getParent( ) );
            popup.setTitleText( "Close this merchant?" );
            popup.addCallback( new Runnable( )
            {
                @Override
                public void run( )
                {
                    MerchantWindow.super.run( );
                }
            } );
            popup.openPopupCentered( );

        }
        else
        {
            // run default close callback
            super.run( );
        }
    }

    @Override
    protected void applyTheme( ThemeInfo themeInfo )
    {
        super.applyTheme( themeInfo );

        labelGap = themeInfo.getParameter( "labelGap", 0 );
    }

    @Override
    public void layout( )
    {
        super.layout( );

        sellLabel.setSize( sellLabel.getPreferredWidth( ), sellLabel.getPreferredHeight( ) );
        buyLabel.setSize( buyLabel.getPreferredWidth( ), buyLabel.getPreferredHeight( ) );

        int centerX = getInnerX( ) + getInnerWidth( ) / 2;

        sellLabel.setPosition( centerX - sellLabel.getWidth( ) / 2, getInnerY( ) );
        buyLabel.setPosition( centerX - buyLabel.getWidth( ) / 2, sellLabel.getBottom( ) + labelGap );

        viewer.setPosition( getInnerX( ), buyLabel.getBottom( ) + labelGap );
        viewer.setSize( getInnerWidth( ), getInnerBottom( ) - viewer.getY( ) );
    }

    @Override
    public void setVisible( boolean visible )
    {
        Game.mainViewer.inventoryWindow.setVisible( visible );

        if ( visible )
        {
            setPosition( Game.mainViewer.inventoryWindow.getX( ) - getWidth( ), Game.mainViewer.inventoryWindow.getY( ) );
        }
        else
        {
            Game.mainViewer.setMerchant( null );
        }

        super.setVisible( visible );

        // manually bring this window to the top so that it is over the inventory
        // window
        Game.mainViewer.keyboardFocusChildChanged( this );

        viewer.clearAllItemHovers( );
    }

    /**
     * Sets the merchant being viewed by this Widget to the specified merchant
     *
     * @param merchant the merchant whose wares are being viewed
     */

    public void setMerchant( Merchant merchant )
    {
        this.merchant = merchant;
    }

    /**
     * Updates the content of this viewer for the merchant set with {@link #setMerchant(Merchant)}
     * and the specified creature.
     *
     * @param creature the creature to buy or sell items to
     */

    public void updateContent( PC creature )
    {
        if ( ! this.isVisible( ) ) return;

        this.creature = creature;

        if ( merchant == null ) return;

        merchant.setPartySpeech( Game.curCampaign.getBestPartySkillModifier( "Speech" ) );

        sellLabel.setText( "You are buying items for " + merchant.getCurrentSellPercentage( ) + "% value." );
        buyLabel.setText( "You are selling items for " + merchant.getCurrentBuyPercentage( ) + "% value." );

        this.setTitle( merchant.getName( ) );

        viewer.updateContent( ItemListViewer.Mode.MERCHANT, this.creature, merchant, merchant.updateCurrentItems( ) );

        layout( );
    }
}
