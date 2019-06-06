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

import de.matthiasmann.twl.Button;
import net.sf.hale.Game;
import net.sf.hale.entity.ItemList;
import net.sf.hale.entity.ItemList.Entry;
import net.sf.hale.entity.PC;
import net.sf.hale.entity.Item;
import net.sf.hale.icon.Icon;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.rules.Quality;
import net.sf.hale.widgets.RightClickMenu;

/**
 * A quickbar slot for holding a usable item.  When activated, the specified
 * item is used if possible.
 *
 * @author Jared Stephen
 */

public class ItemUseSlot extends QuickbarSlot implements ItemList.Listener
{
    private Item item; // cached copy of the item being used
    private ItemList.Entry entry;
    private PC parent;

    @Override
    public Object save( )
    {
        JSONOrderedObject data = new JSONOrderedObject( );

        data.put( "type", "use" );
        data.put( "itemID", item.getTemplate( ).getID( ) );

        if ( item.getTemplate( ).hasQuality( ) )
        {
            data.put( "itemQuality", item.getQuality( ).getName( ) );
        }

        return data;
    }

    /**
     * Create a new ItemUseSlot with the specified Item owned by the specified parent
     *
     * @param item
     * @param parent
     */

    public ItemUseSlot( ItemList.Entry entry, PC parent )
    {
        this.entry = entry;
        this.parent = parent;
        this.item = entry.createItem( );

        parent.inventory.getUnequippedItems( ).addListener( this );
    }

    @Override
    public Icon getIcon( )
    {
        return item.getTemplate( ).getIcon( );
    }

    @Override
    public String getLabelText( )
    {
        return Integer.toString( entry.getQuantity( ) );
    }

    @Override
    public boolean isChildActivateable( )
    {
        return parent.timer.canPerformAction( item.getTemplate( ).getUseAP( ) );
    }

    @Override
    public void childActivate( QuickbarSlotButton button )
    {
        if ( item.canUse( parent ) && entry.getQuantity( ) > 0 )
        {
            item.getUseCallback( parent ).run( );
        }

        if ( entry.getQuantity( ) == 0 )
        {
            button.getClearSlotCallback( ).run( );
        }
        else
        {
            // cache a new copy of the item, as the old one may have been used up
            item = entry.createItem( );
        }
    }

    @Override
    public void showExamineWindow( QuickbarSlotButton button )
    {
        item.getExamineDetailsCallback( button.getX( ), button.getY( ) ).run( );
    }

    @Override
    public void createRightClickMenu( QuickbarSlotButton button )
    {
        RightClickMenu menu = Game.mainViewer.getMenu( );
        menu.addMenuLevel( item.getTemplate( ).getName( ) );

        Button activate = new Button( item.getTemplate( ).getUseText( ) );
        activate.setEnabled( isActivateable( ) );
        activate.addCallback( button.getActivateSlotCallback( this ) );
        menu.addButton( activate );

        Button examine = new Button( "View Details" );
        examine.addCallback( item.getExamineDetailsCallback( menu.getX( ), menu.getY( ) ) );
        menu.addButton( examine );

        Button clearSlot = new Button( "Clear Slot" );
        clearSlot.addCallback( button.getClearSlotCallback( ) );
        menu.addButton( clearSlot );

        menu.show( );
        // show popup immediately
        if ( menu.shouldPopupToggle( ) )
        {
            menu.togglePopup( );
        }
    }

    @Override
    public String getTooltipText( )
    {
        return "Use " + item.getLongName( );
    }

    @Override
    public Icon getSecondaryIcon( ) { return null; }

    @Override
    public String getSaveDescription( )
    {
        return "Use \"" + item.getTemplate( ).getID( ) + "\" \"" + item.getQuality( ).getName( ) + "\"";
    }

    @Override
    public QuickbarSlot getCopy( PC parent )
    {
        return new ItemUseSlot( this.entry, parent );
    }

    @Override
    public void itemListItemAdded( String id, Quality quality, int quantity ) { }

    @Override
    public boolean itemListEntryRemoved( Entry entry )
    {
        if ( entry == this.entry )
        {
            parent.quickbar.setSlot( null, this.getIndex( ) );
            Game.mainViewer.updateInterface( );
            return true;
        }
        else
        {
            return false;
        }
    }
}
