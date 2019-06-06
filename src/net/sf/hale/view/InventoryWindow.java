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

import java.util.HashMap;
import java.util.Map;

import net.sf.hale.Game;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.EquippableItem;
import net.sf.hale.entity.Inventory;
import net.sf.hale.entity.PC;
import net.sf.hale.rules.Merchant;
import net.sf.hale.rules.Weight;
import net.sf.hale.widgets.ItemIconViewer;
import net.sf.hale.widgets.RightClickMenu;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.Image;

/**
 * A window for displaying the Inventory of a Creature including all equipped items
 * and unequipped items.
 *
 * @author Jared Stephen
 */

public class InventoryWindow extends GameSubWindow implements ItemIconViewer.Listener
{
    private int gridSize;
    private int labelGap;

    private final Map< Inventory.Slot, EquippedItemIconViewer > equipped;
    private final CreatureViewer creatureViewer;
    private final ItemListViewer viewer;

    private Creature creature;
    private Merchant merchant;

    private final Button organize;

    private final Label currency;
    private final Label weight;

    /**
     * Creates a new InventoryWindow.  updateContent must be called to specify
     * the creature before showing this Widget.
     */

    public InventoryWindow( )
    {
        this.currency = new Label( );
        this.currency.setTheme( "currencylabel" );
        add( currency );

        this.weight = new Label( );
        this.weight.setTheme( "weightlabel" );
        add( weight );

        organize = new Button( );
        organize.setTheme( "organizebutton" );
        organize.addCallback( new Runnable( )
        {
            @Override
            public void run( )
            {
                creature.inventory.getUnequippedItems( ).sort( );
                Game.mainViewer.updateInterface( );
            }
        } );
        add( organize );

        creatureViewer = new CreatureViewer( );
        add( creatureViewer );

        equipped = new HashMap< Inventory.Slot, EquippedItemIconViewer >( );
        for ( Inventory.Slot slot : Inventory.Slot.values( ) )
        {
            EquippedItemIconViewer viewer = new EquippedItemIconViewer( slot );
            viewer.setListener( this );
            viewer.setTheme( slot.toString( ).toLowerCase( ) + "viewer" );
            equipped.put( slot, viewer );
            add( viewer );
        }

        viewer = new ItemListViewer( );
        this.add( viewer );
    }

    @Override
    public void setVisible( boolean visible )
    {
        super.setVisible( visible );

        viewer.clearAllItemHovers( );
    }

    public EquippedItemIconViewer getEquippedViewer( Inventory.Slot slot )
    {
        return equipped.get( slot );
    }

    @Override
    protected void applyTheme( ThemeInfo themeInfo )
    {
        super.applyTheme( themeInfo );

        gridSize = themeInfo.getParameter( "gridSize", 0 );
        labelGap = themeInfo.getParameter( "labelGap", 0 );
    }

    @Override
    public void layout( )
    {
        super.layout( );

        currency.setSize( currency.getPreferredWidth( ), currency.getPreferredHeight( ) );
        currency.setPosition( getInnerX( ), getInnerY( ) );

        weight.setSize( weight.getPreferredWidth( ), weight.getPreferredHeight( ) );
        weight.setPosition( getInnerX( ), currency.getBottom( ) + labelGap );

        organize.setSize( organize.getPreferredWidth( ), organize.getPreferredHeight( ) );
        organize.setPosition( getInnerRight( ) - organize.getWidth( ), weight.getY( ) - ( labelGap + organize.getHeight( ) ) / 2 );

        // the top position for the equipped items area
        int baseY = weight.getBottom( ) + labelGap;

        // compute the size of the equipped items area
        creatureViewer.setSize( creatureViewer.getMinWidth( ), creatureViewer.getMinHeight( ) );
        int creatureViewerX = getInnerX( ) + creatureViewer.gridCenterX * gridSize - creatureViewer.getWidth( ) / 2;
        int creatureViewerY = baseY + creatureViewer.gridCenterY * gridSize - creatureViewer.getHeight( ) / 2;

        int x = creatureViewerX;
        int right = creatureViewerX + creatureViewer.getWidth( );
        int y = creatureViewerY;
        int bottom = creatureViewerY + creatureViewer.getHeight( );

        for ( EquippedItemIconViewer viewer : equipped.values( ) )
        {
            viewer.setSize( viewer.getMinWidth( ), viewer.getMinHeight( ) );

            int viewerX = getInnerX( ) + viewer.gridX * gridSize;
            int viewerY = baseY + viewer.gridY * gridSize;

            x = Math.min( x, viewerX );
            y = Math.min( y, viewerY );

            right = Math.max( right, viewerX + viewer.getWidth( ) );
            bottom = Math.max( bottom, viewerY + viewer.getHeight( ) );
        }

        // compute the x offset based on the size
        int offsetX = getInnerWidth( ) / 2 - ( right - x ) / 2;

        // set the equipped area positions
        creatureViewer.setPosition( creatureViewerX + offsetX, creatureViewerY );

        for ( EquippedItemIconViewer viewer : equipped.values( ) )
        {
            viewer.setPosition( getInnerX( ) + viewer.gridX * gridSize + offsetX,
                                baseY + viewer.gridY * gridSize );
        }

        // set the unequipped items viewer size and position
        viewer.setPosition( getInnerX( ), bottom + labelGap );
        viewer.setSize( getInnerWidth( ), getInnerBottom( ) - viewer.getY( ) );
    }

    /**
     * Sets the merchant associated with this InventoryWindow.  This will be the merchant
     * that the creature owning the inventory can buy and sell items from and to.  If null,
     * there is no merchant and no buying or selling.
     *
     * @param merchant the merchant to associate with this InventoryWindow
     */

    public void setMerchant( Merchant merchant )
    {
        this.merchant = merchant;
    }

    /**
     * Sets the specified creature as the owner of the inventory being shown.  Updates
     * the view to reflect any changes to this creature's inventory since the last time
     * updateContent was called.
     *
     * @param creature the creature whose inventory is being viewed
     */

    public void updateContent( PC creature )
    {
        this.creature = creature;
        setTitle( "Inventory for " + creature.getTemplate( ).getName( ) );

        currency.setText( Game.curCampaign.getPartyCurrency( ).toString( ) );

        String curWeight = creature.inventory.getTotalWeight( ).toStringKilograms( );
        String maxWeight = new Weight( creature.stats.getWeightLimit( ) ).toStringKilograms( );

        weight.setText( curWeight + " / " + maxWeight + " kg" );

        for ( EquippedItemIconViewer viewer : equipped.values( ) )
        {
            viewer.setItem( creature.inventory.getEquippedItem( viewer.slot ), 1, creature, null, null );
        }

        viewer.updateContent( ItemListViewer.Mode.INVENTORY, creature, merchant,
                              creature.inventory.getUnequippedItems( ) );
    }

    /*
     * forward mouse hover events to the ItemListViewer
     * (non-Javadoc)
     * @see net.sf.hale.widgets.ItemIconViewer.Listener#hoverStarted(net.sf.hale.widgets.ItemIconViewer)
     */

    @Override
    public void hoverStarted( ItemIconViewer viewer )
    {
        this.viewer.hoverStarted( viewer );
    }

    @Override
    public void hoverEnded( ItemIconViewer viewer )
    {
        this.viewer.hoverEnded( viewer );
    }

    @Override
    public void rightClicked( ItemIconViewer viewer, int x, int y )
    {
        EquippedItemIconViewer equippedViewer = ( EquippedItemIconViewer ) viewer;

        EquippableItem item = ( EquippableItem ) viewer.getItem( );
        if ( item == null ) return;

        RightClickMenu menu = Game.mainViewer.getMenu( );

        menu.clear( );
        menu.addMenuLevel( item.getLongName( ) );
        menu.setPosition( x - 2, y - 25 );

        Button button = new Button( "Unequip" );
        button.addCallback( creature.inventory.getUnequipCallback( equippedViewer.slot ) );
        button.setEnabled( creature.timer.canPerformEquipAction( item ) && item.getTemplate( ).isUnequippable( ) );
        if ( ! item.getTemplate( ).isUnequippable( ) )
        {
            button.setTooltipContent( "Item may not be removed" );
        }
        else if ( ! button.isEnabled( ) )
        {
            button.setTooltipContent( "Not enough AP to unequip" );
        }
        menu.addButton( button );

        // disable all actions except view details if the targeter is enabled or interface locked
        if ( Game.interfaceLocker.locked( ) || Game.areaListener.getTargeterManager( ).isInTargetMode( ) )
        { menu.disableAllButtons( ); }

        Button details = new Button( "View Details" );
        details.addCallback( item.getExamineDetailsCallback( x, y ) );
        menu.addButton( details );

        menu.show( );

        // show popup immediately
        if ( menu.shouldPopupToggle( ) )
        {
            menu.togglePopup( );
        }
    }

    private class EquippedItemIconViewer extends ItemIconViewer
    {
        private Inventory.Slot slot;

        private int gridX, gridY;
        private String emptyTooltip;
        private Image emptyImage;
        private int emptyImageX, emptyImageY;

        private EquippedItemIconViewer( Inventory.Slot slot )
        {
            super( null );

            this.slot = slot;
        }

        @Override
        public String getEmptyHoverText( )
        {
            return emptyTooltip;
        }

        @Override
        protected void applyTheme( ThemeInfo themeInfo )
        {
            super.applyTheme( themeInfo );

            gridX = themeInfo.getParameter( "gridX", 0 );
            gridY = themeInfo.getParameter( "gridY", 0 );
            emptyTooltip = themeInfo.getParameter( "emptyTooltip", ( String ) null );
            emptyImage = themeInfo.getImage( "emptyImage" );

            emptyImageX = themeInfo.getParameter( "emptyImageX", 0 );
            emptyImageY = themeInfo.getParameter( "emptyImageY", 0 );
        }

        @Override
        protected void paintWidget( GUI gui )
        {
            if ( getItem( ) == null && emptyImage != null )
            { emptyImage.draw( getAnimationState( ), emptyImageX + getInnerX( ), emptyImageY + getInnerY( ) ); }

            super.paintWidget( gui );
        }

        private boolean validateTarget( DragTarget target )
        {
            EquippableItem item = creature.inventory.getEquippedItem( slot );

            if ( item != null && ! item.getTemplate( ).isUnequippable( ) ) return false;

            if ( target.getItem( ) == null ) return false;

            if ( target.getItemEquipSlot( ) != null ) return false;

            if ( target.getItemMerchant( ) != null ) return false;

            return true;
        }

        @Override
        public Inventory.Slot getItemEquipSlot( ) { return slot; }

        @Override
        public void dragAndDropStartHover( DragTarget target )
        {
            if ( validateTarget( target ) )
            { getAnimationState( ).setAnimationState( DragAndDropHandler.STATE_DRAG_HOVER, true ); }
        }

        @Override
        public void dragAndDropStopHover( DragTarget target )
        {
            getAnimationState( ).setAnimationState( DragAndDropHandler.STATE_DRAG_HOVER, false );
        }

        @Override
        public void dropDragTarget( DragTarget target )
        {
            getAnimationState( ).setAnimationState( DragAndDropHandler.STATE_DRAG_HOVER, false );

            if ( validateTarget( target ) )
            {
                if ( target.getParentPC( ) != null && target.getItem( ) instanceof EquippableItem )
                {
                    // equip an item from the inventory
                    creature.inventory.equipItem( ( EquippableItem ) target.getItem( ), slot );
                }
                else if ( target.getItemContainer( ) != null && target.getItem( ) instanceof EquippableItem )
                {
                    // equip an item from a container
                    creature.inventory.getTakeAndWieldCallback( ( EquippableItem ) target.getItem( ), target.getItemContainer( ) ).run( );
                }
            }

        }
    }

    private class CreatureViewer extends Widget
    {
        private int gridCenterX, gridCenterY;

        @Override
        public int getMinHeight( ) { return Game.TILE_SIZE + getBorderHorizontal( ); }

        @Override
        public int getMinWidth( ) { return Game.TILE_SIZE + getBorderVertical( ); }

        @Override
        protected void applyTheme( ThemeInfo themeInfo )
        {
            super.applyTheme( themeInfo );

            gridCenterX = themeInfo.getParameter( "gridCenterX", 0 );
            gridCenterY = themeInfo.getParameter( "gridCenterY", 0 );
        }

        @Override
        protected void paintWidget( GUI gui )
        {
            super.paintWidget( gui );

            if ( creature != null ) creature.uiDraw( getInnerX( ), getInnerY( ) );
        }
    }
}
