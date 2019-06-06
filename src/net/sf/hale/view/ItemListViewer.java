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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.entity.AmmoTemplate;
import net.sf.hale.entity.Armor;
import net.sf.hale.entity.Container;
import net.sf.hale.entity.EntityManager;
import net.sf.hale.entity.EquippableItem;
import net.sf.hale.entity.EquippableItemTemplate;
import net.sf.hale.entity.Inventory;
import net.sf.hale.entity.Item;
import net.sf.hale.entity.ItemList;
import net.sf.hale.entity.ItemTemplate;
import net.sf.hale.entity.PC;
import net.sf.hale.entity.TrapTemplate;
import net.sf.hale.entity.Weapon;
import net.sf.hale.entity.WeaponTemplate;
import net.sf.hale.rules.Currency;
import net.sf.hale.rules.Merchant;
import net.sf.hale.widgets.ItemIconHover;
import net.sf.hale.widgets.ItemIconViewer;
import net.sf.hale.widgets.RightClickMenu;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;

/**
 * Widget for viewing the items contained within an ItemList.  The includes viewing
 * the contents of a container, a creature's inventory, or a merchant's wares.
 *
 * @author Jared Stephen
 */

public class ItemListViewer extends Widget implements ItemIconViewer.Listener, DropTarget
{
    /**
     * Controls the types of actions available for each ItemIconViewer
     *
     * @author Jared Stephen
     */
    public enum Mode
    {
        /**
         * Viewing the inventory of a creature
         */
        INVENTORY,

        /**
         * Viewing a merchant's wares
         */
        MERCHANT,

        /**
         * Viewing the contents of a container
         */
        CONTAINER
    }

    ;

    private enum Filter
    {
        All, Weapons, Armor, Usable, Ingredients, Traps, Quest;
    }

    ;

    private int gridGap;

    private List< ToggleButton > filterButtons;

    private final ScrollPane scrollPane;
    private final Content content;

    private List< ItemIconViewer > viewers;

    private Mode mode;
    private PC creature;
    private Merchant merchant;
    private ItemList items;

    private Filter activeFilter;
    private ToggleButton activeButton;

    private List< ItemIconHover > itemHovers;

    /**
     * Creates a new ItemList viewer with no items yet viewed.  You must use
     * updateContent to set the ItemList and mode
     */

    public ItemListViewer( )
    {
        itemHovers = new ArrayList< ItemIconHover >( );

        viewers = new ArrayList< ItemIconViewer >( );

        content = new Content( );
        scrollPane = new ScrollPane( content );
        scrollPane.setFixed( ScrollPane.Fixed.HORIZONTAL );
        scrollPane.setExpandContentSize( true );
        scrollPane.setTheme( "itemspane" );
        this.add( scrollPane );

        filterButtons = new ArrayList< ToggleButton >( );
        for ( Filter filter : Filter.values( ) )
        {
            ToggleButton button = new ToggleButton( );
            button.setTheme( filter.toString( ).toLowerCase( ) + "filter" );
            button.addCallback( new FilterButtonCallback( button, filter ) );

            add( button );
            filterButtons.add( button );
        }

        // initially set the "All" filter to active
        activeButton = filterButtons.get( 0 );
        activeFilter = Filter.All;
        activeButton.setActive( true );
    }

    @Override
    protected void applyTheme( ThemeInfo themeInfo )
    {
        super.applyTheme( themeInfo );

        gridGap = themeInfo.getParameter( "gridGap", 0 );
    }

    @Override
    protected void layout( )
    {
        super.layout( );

        int curX = getInnerX( );
        int curY = getInnerY( );
        int maxRowBottom = 0;
        for ( ToggleButton button : filterButtons )
        {
            button.setSize( button.getPreferredWidth( ), button.getPreferredHeight( ) );

            if ( curX + button.getWidth( ) > getInnerRight( ) )
            {
                // move to the next row
                curX = getInnerX( );
                curY = maxRowBottom;
            }

            button.setPosition( curX, curY );

            curX = button.getRight( );
            maxRowBottom = Math.max( maxRowBottom, button.getBottom( ) );
        }

        scrollPane.setPosition( getInnerX( ), maxRowBottom );
        scrollPane.setSize( getInnerWidth( ), Math.max( 0, getInnerBottom( ) - maxRowBottom ) );
    }

    private void applyModeToViewer( ItemIconViewer viewer, Item item )
    {
        viewer.setStateProficiencies( ! item.getTemplate( ).hasPrereqsToEquip( creature ) );

        boolean unafford = false;

        switch ( mode )
        {
            case MERCHANT:
                int maxAffordable = Game.curCampaign.getPartyCurrency( ).getMaxNumberAffordable( item,
                                                                                                 merchant.getCurrentSellPercentage( ) );
                unafford = maxAffordable < 1;
                break;
            case CONTAINER:
                break;
            case INVENTORY:
                break;
        }
        viewer.setStateUnaffordable( unafford );
    }

    private void updateViewers( )
    {
        int viewerIndex = 0;

        // update the viewers, adding new ones as needed
        for ( ItemList.Entry entry : items )
        {
            if ( ! itemMatchesFilter( entry.getID( ) ) ) continue;

            ItemIconViewer viewer;
            if ( viewerIndex == viewers.size( ) )
            {
                // if there aren't enough viewers, add a new one
                viewer = new ItemIconViewer( this );
                viewer.setListener( this );

                content.add( viewer );
                viewers.add( viewer );
            }
            else
            {
                viewer = viewers.get( viewerIndex );
            }

            Item item = entry.createItem( );

            switch ( mode )
            {
                case INVENTORY:
                    viewer.setItem( item, entry.getQuantity( ), creature, null, null );
                    break;
                case CONTAINER:
                    viewer.setItem( item, entry.getQuantity( ), null, Game.mainViewer.containerWindow.getContainer( ), null );
                    break;
                case MERCHANT:
                    viewer.setItem( item, entry.getQuantity( ), null, null, merchant );
                    break;
            }

            applyModeToViewer( viewer, item );

            viewerIndex++;

        }

        // if there are too many viewers remove them
        for ( int i = viewers.size( ) - 1; i >= viewerIndex; i-- )
        {
            content.removeChild( i );
            viewers.remove( i );
        }
    }

    /**
     * Sets this ItemListViewer as operating in the specified mode viewing the specified ItemList.
     * Creature and Merchant are optional parameters used in the Creature, Container, and Merchant modes
     *
     * @param mode     the mode that this ItemListViewer is operating in
     * @param creature the parent Creature that owns the ItemList in Mode Inventory and Merchant and
     *                 is interacting with the container in mode Container
     * @param merchant the merchant that the parent is trading with in Mode Merchant
     * @param items    the List of items to view
     */

    public void updateContent( Mode mode, PC creature, Merchant merchant, ItemList items )
    {
        this.mode = mode;
        this.creature = creature;
        this.merchant = merchant;
        this.items = items;

        updateViewers( );
    }

    /**
     * Adds an item hover for the corresponding equipped item for the specified viewer
     *
     * @param viewer the item viewer to add the hover for
     */

    private void addEquippedHovers( ItemIconViewer viewer )
    {
        if ( ! Game.mainViewer.inventoryWindow.isVisible( ) ) return;

        if ( ! ( viewer.getItem( ) instanceof EquippableItem ) ) return;

        EquippableItem item = ( EquippableItem ) viewer.getItem( );

        List< ItemIconViewer > viewersToAdd = new ArrayList< ItemIconViewer >( );

        for ( Inventory.Slot slot : EquippableItemTemplate.validSlotsForType.get( item.getTemplate( ).getType( ) ) )
        {
            viewersToAdd.add( Game.mainViewer.inventoryWindow.getEquippedViewer( slot ) );
        }

        for ( ItemIconViewer equippedViewer : viewersToAdd )
        {
            // make a viewer with the source widget the original viewer
            addHover( Mode.INVENTORY, equippedViewer.getItem( ), viewer, equippedViewer.getEmptyHoverText( ),
                      equippedViewer.getX( ), equippedViewer.getY( ) );
        }
    }

    private void addHover( Mode mode, Item item, ItemIconViewer viewer, String emptyText, int x, int y )
    {
        ItemIconHover hover = new ItemIconHover( item, viewer );
        hover.setEmptyHoverText( emptyText );

        // set mode specific information
        switch ( mode )
        {
            case INVENTORY:
                if ( merchant != null )
                { hover.setValue( "Sell Price", merchant.getCurrentBuyPercentage( ) ); }
                break;
            case MERCHANT:
                hover.setValue( "Buy Price", merchant.getCurrentSellPercentage( ) );
                break;
            case CONTAINER:
                break;
        }

        // set type specific information
        if ( item instanceof Armor )
        {
            Armor armor = ( Armor ) item;

            if ( ! creature.stats.hasArmorProficiency( armor.getTemplate( ).getArmorType( ).getName( ) ) )
            {
                hover.setRequiresText( "Armor Proficiency: " + armor.getTemplate( ).getArmorType( ).getName( ) );
            }

        }
        else if ( item instanceof Weapon )
        {
            Weapon weapon = ( Weapon ) item;

            if ( ! creature.stats.hasWeaponProficiency( weapon.getTemplate( ).getBaseWeapon( ).getName( ) ) )
            {
                hover.setRequiresText( "Weapon Proficiency: " + weapon.getTemplate( ).getBaseWeapon( ).getName( ) );
            }
        }

        hover.updateText( );

        itemHovers.add( hover );

        // add and set the widget position and size
        this.getGUI( ).getRootPane( ).add( hover );
        hover.setSize( hover.getPreferredWidth( ), hover.getPreferredHeight( ) );
        hover.setPosition( x, y - hover.getHeight( ) );
    }

    /**
     * Removes all mouse over hovers for item icons
     */

    public void clearAllItemHovers( )
    {
        // clean up all old hovers
        Iterator< ItemIconHover > iter = itemHovers.iterator( );
        while ( iter.hasNext( ) )
        {
            ItemIconHover hover = iter.next( );
            iter.remove( );
            hover.getParent( ).removeChild( hover );
        }
    }

    @Override
    public void hoverStarted( ItemIconViewer viewer )
    {
        clearAllItemHovers( );

        addEquippedHovers( viewer );
        addHover( this.mode, viewer.getItem( ), viewer, viewer.getEmptyHoverText( ), viewer.getX( ), viewer.getY( ) );
    }

    @Override
    public void hoverEnded( ItemIconViewer viewer )
    {
        // set hovers invisible for now, they will be removed eventually
        // when hoverStarted() is called again
        for ( ItemIconHover hover : itemHovers )
        {
            if ( hover.getHoverSource( ) == viewer )
            {
                hover.setVisible( false );
            }
        }
    }

    @Override
    public void rightClicked( ItemIconViewer viewer, int x, int y )
    {
        Item item = viewer.getItem( );
        int quantity = viewer.getQuantity( );

        RightClickMenu menu = Game.mainViewer.getMenu( );

        menu.clear( );
        menu.addMenuLevel( item.getLongName( ) );
        menu.setPosition( x - 2, y - 25 );

        switch ( mode )
        {
            case INVENTORY:
                addInventoryButtons( item, quantity, menu );
                break;
            case MERCHANT:
                addMerchantButtons( item, quantity, menu );
                break;
            case CONTAINER:
                addContainerButtons( item, quantity, menu, Game.mainViewer.containerWindow.getContainer( ) );
                break;
        }

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

    private void addContainerButtons( Item item, int quantity, RightClickMenu menu, Container container )
    {
        if ( item instanceof EquippableItem &&
                creature.timer.canPerformAction( Game.ruleset.getValue( "PickUpAndWieldItemCost" ) ) )
        {

            EquippableItem eItem = ( EquippableItem ) item;

            boolean addButton = false;
            switch ( eItem.getTemplate( ).getType( ) )
            {
                case Weapon:
                    if ( creature.inventory.getEquippedMainHand( ) == null )
                    { addButton = true; }
                    break;
                case Shield:
                    if ( creature.inventory.getEquippedOffHand( ) == null )
                    { addButton = true; }
                    break;
                default:
                    // do nothing
            }

            if ( addButton )
            {
                Button button = new Button( "Take and Wield" );
                button.addCallback( creature.inventory.getTakeAndWieldCallback( eItem, container ) );
                menu.addButton( button );
            }
        }

        if ( creature.timer.canPerformAction( Game.ruleset.getValue( "PickUpItemCost" ) ) )
        {
            Button button = new Button( "Take" );
            button.addCallback( creature.inventory.getTakeCallback( item, 1, container ) );
            menu.addButton( button );

            if ( quantity > 1 )
            {
                button = new Button( "Take Multiple..." );
                button.addCallback( creature.inventory.getTakeCallback( item, quantity, container ) );
                menu.addButton( button );
            }
        }
    }

    public static int getMerchantBuyMaxQuantity( Merchant merchant, Item item, int quantityAvailable )
    {
        int maxAffordable = Game.curCampaign.getPartyCurrency( ).getMaxNumberAffordable( item,
                                                                                         merchant.getCurrentSellPercentage( ) );

        return Math.min( quantityAvailable, maxAffordable );
    }

    private void addMerchantButtons( Item item, int quantity, RightClickMenu menu )
    {
        int maxBuy = getMerchantBuyMaxQuantity( merchant, item, quantity );

        StringBuilder buyText = new StringBuilder( );
        buyText.append( "Buy for " );
        buyText.append( Currency.shortString( item.getQualityValue( ), merchant.getCurrentSellPercentage( ) ) );

        Button button = new Button( buyText.toString( ) );
        button.addCallback( creature.inventory.getBuyCallback( item, 1, merchant ) );
        button.setEnabled( maxBuy >= 1 );
        menu.addButton( button );

        if ( maxBuy > 1 )
        {
            button = new Button( "Buy Multiple..." );
            button.addCallback( creature.inventory.getBuyCallback( item, maxBuy, merchant ) );
            menu.addButton( button );
        }
    }

    private void checkEquipButton( Button button, EquippableItem item )
    {
        if ( ! creature.timer.canPerformEquipAction( item ) )
        {
            button.setEnabled( false );
            button.setTooltipContent( "Not enough AP to equip" );
        }
        else if ( ! creature.inventory.hasPrereqsToEquip( item ) )
        {
            button.setEnabled( false );
            button.setTooltipContent( "You do not have proficiency with this Item" );
        }
        else if ( ! creature.inventory.canEquip( item, null ) )
        {
            button.setEnabled( false );
            button.setTooltipContent( "The currently equipped item may not be removed." );
        }
    }

    private void addInventoryButtons( Item item, int quantity, RightClickMenu menu )
    {
        if ( merchant != null && ! item.getTemplate( ).isQuest( ) )
        {
            StringBuilder sellText = new StringBuilder( );
            sellText.append( "Sell for " );
            sellText.append( Currency.shortString( item.getQualityValue( ), merchant.getCurrentBuyPercentage( ) ) );

            Button button = new Button( sellText.toString( ) );
            button.addCallback( creature.inventory.getSellCallback( item, 1, merchant ) );
            menu.addButton( button );

            if ( quantity > 1 )
            {
                button = new Button( "Sell Multiple..." );
                button.addCallback( creature.inventory.getSellCallback( item, quantity, merchant ) );
                menu.addButton( button );
            }
        }

        if ( item instanceof EquippableItem )
        {
            EquippableItem eItem = ( EquippableItem ) item;

            Button button = new Button( "Equip" );
            button.addCallback( creature.inventory.getEquipCallback( eItem, null ) );
            checkEquipButton( button, eItem );

            menu.addButton( button );

            if ( eItem instanceof Weapon && creature.inventory.canEquip( eItem, Inventory.Slot.OffHand ) )
            {
                Button offHandButton = new Button( "Equip Off Hand" );
                offHandButton.addCallback( creature.inventory.getEquipCallback( eItem, Inventory.Slot.OffHand ) );
                checkEquipButton( offHandButton, eItem );

                menu.addButton( offHandButton );
            }
        }

        if ( item.canUse( creature ) )
        {
            Button button = new Button( item.getTemplate( ).getUseText( ) );
            button.addCallback( item.getUseCallback( creature ) );
            menu.addButton( button );
        }

        if ( ( creature.timer.canPerformAction( Game.ruleset.getValue( "GiveItemCost" ) ) ||
                ! Game.isInTurnMode( ) ) && Game.curCampaign.party.size( ) > 1 )
        {
            Button button = new Button( "Give >>" );
            button.addCallback( creature.inventory.getGiveCallback( item, 1 ) );
            menu.addButton( button );

            if ( quantity > 1 )
            {
                button = new Button( "Give Multiple >>" );
                button.addCallback( creature.inventory.getGiveCallback( item, quantity ) );
                menu.addButton( button );
            }
        }
        if ( ! item.getTemplate( ).isQuest( ) && creature.timer.canPerformAction( Game.ruleset.getValue( "DropItemCost" ) ) )
        {
            Button button = new Button( "Drop" );
            button.addCallback( creature.inventory.getDropCallback( item, 1 ) );
            menu.addButton( button );

            if ( quantity > 1 )
            {
                button = new Button( "Drop Multiple..." );
                button.addCallback( creature.inventory.getDropCallback( item, quantity ) );
                menu.addButton( button );
            }
        }
    }

    private boolean itemMatchesFilter( String itemID )
    {
        ItemTemplate template = EntityManager.getItemTemplate( itemID );

        switch ( activeFilter )
        {
            case All:
                return true;
            case Weapons:
                return ( template instanceof WeaponTemplate ) || ( template instanceof AmmoTemplate );
            case Armor:
                if ( ! ( template instanceof EquippableItemTemplate ) )
                {
                    return false;
                }
                switch ( ( ( EquippableItemTemplate ) template ).getType( ) )
                {
                    case Cloak:
                    case Belt:
                    case Amulet:
                    case Ring:
                    case Gloves:
                    case Helmet:
                    case Boots:
                    case Armor:
                    case Shield:
                        return true;
                    case Ammo:
                    case Weapon:
                        return false;
                }
                break;
            case Usable:
                return template.isUsable( );
            case Ingredients:
                return template.isIngredient( );
            case Traps:
                return template instanceof TrapTemplate;
            case Quest:
                return template.isQuest( );
        }

        return false;
    }

    private class FilterButtonCallback implements Runnable
    {
        private ToggleButton button;
        private Filter filter;

        private FilterButtonCallback( ToggleButton button, Filter filter )
        {
            this.button = button;
            this.filter = filter;
        }

        @Override
        public void run( )
        {
            if ( ItemListViewer.this.activeButton != null )
            {
                ItemListViewer.this.activeButton.setActive( false );
            }

            ItemListViewer.this.activeButton = button;
            ItemListViewer.this.activeFilter = filter;

            button.setActive( true );
            updateContent( mode, creature, merchant, items );
        }
    }

    private class Content extends Widget implements DropTarget
    {
        @Override
        protected void layout( )
        {
            super.layout( );

            int curX = getInnerX( );
            int curY = getInnerY( );
            int maxBottom = getInnerY( );

            for ( int i = 0; i < getNumChildren( ); i++ )
            {
                Widget child = getChild( i );

                // set the min size for ItemIconViewers instead of preferred size
                child.setSize( child.getMinWidth( ), child.getMinHeight( ) );

                if ( curX + child.getWidth( ) + gridGap > getInnerRight( ) )
                {
                    curX = getInnerX( );
                    curY = maxBottom + gridGap;
                }

                child.setPosition( curX, curY );

                curX = child.getRight( ) + gridGap;
                maxBottom = Math.max( maxBottom, child.getBottom( ) );
            }
        }

        private boolean validateTarget( DragTarget target )
        {
            if ( target.getItem( ) == null ) return false;

            switch ( mode )
            {
                case INVENTORY:
                    if ( target.getParentPC( ) != null && target.getItemEquipSlot( ) == null ) return false;

                    if ( target.getItem( ) instanceof EquippableItem )
                    {
                        if ( ! ( ( EquippableItem ) target.getItem( ) ).getTemplate( ).isUnequippable( ) )
                        { return false; }
                    }
                    break;
                case CONTAINER:
                    if ( target.getParentPC( ) == null ) return false;
                    break;
                case MERCHANT:
                    if ( target.getParentPC( ) == null ) return false;
                    break;
            }

            return true;
        }

        private void dragDropMerchantFromEquipped( DragTarget target )
        {
            target.getParentPC( ).inventory.getSellEquippedCallback( target.getItemEquipSlot( ), merchant ).run( );
        }

        private void dragDropMerchantFromInventory( DragTarget target )
        {
            int maxQuantity = target.getParentPC( ).inventory.getUnequippedItems( ).getQuantity( target.getItem( ) );

            target.getParentPC( ).inventory.getSellCallback( target.getItem( ), maxQuantity, merchant ).run( );
        }

        private void dragDropInventoryFromMerchant( DragTarget target )
        {
            Merchant merchant = target.getItemMerchant( );
            int merchantQuantity = target.getItemMerchant( ).getCurrentItems( ).getQuantity( target.getItem( ) );
            int maxQuantity = getMerchantBuyMaxQuantity( merchant, target.getItem( ), merchantQuantity );

            if ( maxQuantity > 0 )
            {
                // don't allow buy attempts when the item can't be afforded
                creature.inventory.getBuyCallback( target.getItem( ), maxQuantity, target.getItemMerchant( ) ).run( );
            }
        }

        private void dragDropInventoryFromEquipped( DragTarget target )
        {
            creature.inventory.getUnequipCallback( target.getItemEquipSlot( ) ).run( );
        }

        private void dragDropInventoryFromContainer( DragTarget target )
        {
            Container container = target.getItemContainer( );

            int quantity = container.getCurrentItems( ).getQuantity( target.getItem( ) );

            creature.inventory.getTakeCallback( target.getItem( ), quantity, target.getItemContainer( ) ).run( );
        }

        private void dragDropContainerFromInventory( DragTarget target )
        {
            Inventory srcInventory = target.getParentPC( ).inventory;

            if ( target.getItemEquipSlot( ) == null )
            {
                srcInventory.getDropEquippedCallback( target.getItemEquipSlot( ) ).run( );
            }
            else
            {
                srcInventory.getDropCallback( target.getItem( ), srcInventory.getTotalQuantity( target.getItem( ) ) ).run( );
            }
        }

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
            if ( validateTarget( target ) )
            {
                switch ( mode )
                {
                    case INVENTORY:
                        if ( target.getItemContainer( ) != null )
                        {
                            dragDropInventoryFromContainer( target );
                        }
                        else if ( target.getItemMerchant( ) != null )
                        {
                            dragDropInventoryFromMerchant( target );
                        }
                        else if ( target.getItemEquipSlot( ) != null )
                        {
                            dragDropInventoryFromEquipped( target );
                        }
                        break;
                    case CONTAINER:
                        dragDropContainerFromInventory( target );
                        break;
                    case MERCHANT:
                        if ( target.getItemEquipSlot( ) != null )
                        { dragDropMerchantFromEquipped( target ); }
                        else
                        { dragDropMerchantFromInventory( target ); }
                        break;
                }
            }

            getAnimationState( ).setAnimationState( DragAndDropHandler.STATE_DRAG_HOVER, false );
        }
    }

    @Override
    public void dragAndDropStartHover( DragTarget target )
    {
        content.dragAndDropStartHover( target );
    }

    @Override
    public void dragAndDropStopHover( DragTarget target )
    {
        content.dragAndDropStopHover( target );
    }

    @Override
    public void dropDragTarget( DragTarget target )
    {
        content.dropDragTarget( target );
    }
}
