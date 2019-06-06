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

import java.util.HashMap;
import java.util.Map;

import net.sf.hale.Game;
import net.sf.hale.entity.EntityManager;
import net.sf.hale.entity.EquippableItem;
import net.sf.hale.entity.Item;
import net.sf.hale.entity.ItemList;
import net.sf.hale.entity.PC;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.Saveable;
import net.sf.hale.util.Logger;
import net.sf.hale.util.SimpleJSONObject;

/**
 * A Quickbar is a collection of 100 "slots".  Each slot is designed to allow the
 * player to quickly use or equip an Item or activate an Ability.  A Quickbar is
 * viewed through a QuickbarViewer, which views the slots in sets of 10 at a time
 * to make things more manageable.
 *
 * @author Jared Stephen
 */

public class Quickbar implements Saveable
{
    /**
     * The maximum total number of QuickbarSlots in any one Quickbar
     */
    public static final int ItemSlots = 9;

    private Map< Integer, QuickbarSlot > slots;
    private PC parent;

    @Override
    public Object save( )
    {
        JSONOrderedObject data = new JSONOrderedObject( );

        JSONOrderedObject slotsData = new JSONOrderedObject( );
        for ( Integer key : slots.keySet( ) )
        {
            QuickbarSlot slot = slots.get( key );
            if ( slot != null )
            { slotsData.put( "slot" + key.toString( ), slot.save( ) ); }
        }

        data.put( "slots", slotsData );

        return data;
    }

    public void load( SimpleJSONObject data )
    {
        this.clear( );

        SimpleJSONObject slotsObject = data.getObject( "slots" );

        for ( String key : slotsObject.keySet( ) )
        {
            int index = Integer.parseInt( key.substring( 4, key.length( ) ) );

            SimpleJSONObject slotData = slotsObject.getObject( key );

            String type = slotData.get( "type", null );

            if ( type.equals( "ability" ) )
            {
                // legacy support for transitioning old saves; do nothing

            }
            else if ( type.equals( "use" ) )
            {
                String itemID = slotData.get( "itemID", null );

                String qualityID = null;
                if ( slotData.containsKey( "itemQuality" ) )
                {
                    qualityID = slotData.get( "itemQuality", null );
                }

                ItemList.Entry entry = parent.inventory.getUnequippedItems( ).find( itemID, qualityID );
                if ( entry != null )
                {
                    putSlot( index, new ItemUseSlot( entry, parent ) );
                }
                else
                {
                    Logger.appendToWarningLog( "Warning, unable to find item in quickbar slot " + index +
                                                       " for " + parent.getTemplate( ).getID( ) );
                }

            }
            else if ( type.equals( "equip" ) )
            {
                String itemID = slotData.get( "itemID", null );

                String qualityID = null;
                if ( slotData.containsKey( "itemQuality" ) )
                {
                    qualityID = slotData.get( "itemQuality", null );
                }

                try
                {
                    Item item = EntityManager.getItem( itemID, qualityID );

                    ItemEquipSlot slot = new ItemEquipSlot( ( EquippableItem ) item, parent );

                    if ( slotData.containsKey( "secondaryItemID" ) )
                    {
                        String secondaryItemID = slotData.get( "secondaryItemID", null );

                        String secondaryQualityID = null;
                        if ( slotData.containsKey( "secondaryItemQuality" ) )
                        {
                            secondaryQualityID = slotData.get( "secondaryItemQuality", null );
                        }

                        Item secondaryItem = EntityManager.getItem( secondaryItemID, secondaryQualityID );

                        slot.setSecondaryItem( ( EquippableItem ) secondaryItem );
                    }

                    putSlot( index, slot );

                }
                catch ( Exception e )
                {
                    Logger.appendToWarningLog( "Warning, unable to load item(s) in quickbar slot " + index );
                }
            }
        }
    }

    /**
     * Creates a new Quickbar that is a copy of the specified quickbar
     *
     * @param other  the quickbar to copy
     * @param parent the new parent creature for this quickbar
     */

    public Quickbar( Quickbar other, PC parent )
    {
        slots = new HashMap< Integer, QuickbarSlot >( );

        for ( Integer index : other.slots.keySet( ) )
        {
            QuickbarSlot slot = other.slots.get( index );

            if ( slot != null )
            {
                this.putSlot( index, slot.getCopy( parent ) );
            }
        }

        this.parent = parent;
    }

    /**
     * Creates a new Quickbar with the specified Creature as the parent that will
     * be used whenever a QuickbarSlot is activated.  The Quickbar is initially
     * empty.
     *
     * @param parent the parent owner of this Quickbar
     */

    public Quickbar( PC parent )
    {
        slots = new HashMap< Integer, QuickbarSlot >( );
        this.parent = parent;
    }

    /**
     * Removes all current quickbar slots so that the quickbar is empty
     */

    public void clear( )
    {
        slots.clear( );
    }

    /**
     * Returns the QuickbarSlot with the specified Quickbar index
     *
     * @param index the index of the Slot to retrieve
     * @return the QuickbarSlot at the specified index or null if no
     * QuickbarSlot is found at that index
     */

    public QuickbarSlot getSlot( int index )
    {
        if ( index >= Quickbar.ItemSlots || index < 0 ) return null;

        return slots.get( Integer.valueOf( index ) );
    }

    /**
     * Returns the owner, parent creature of this quickbar
     *
     * @return the parent creature of this quickbar
     */

    public PC getParent( )
    {
        return parent;
    }

    /**
     * Sets the QuickbarSlot at the specified index to the specified
     * QuickbarSlot.  If the slot is already set to equip an item and the
     * specified slot is a compatible secondary item equip, then the
     * current slot will be modified rather than setting a new slot
     *
     * @param slot  the QuickbarSlot to set
     * @param index the index of the Slot to set
     */

    public void setSlot( QuickbarSlot slot, int index )
    {
        // handle the special case of one equip slot holding two items
        QuickbarSlot current = slots.get( Integer.valueOf( index ) );

        if ( current instanceof ItemEquipSlot && slot instanceof ItemEquipSlot )
        {
            if ( ( ( ItemEquipSlot ) current ).setSecondaryItem( ( ( ItemEquipSlot ) slot ).getItem( ) ) )
            {
                return;
            }
        }

        putSlot( index, slot );
    }

    /**
     * Helper function to easily add the specified Item to this Quickbar.  The
     * specified Item is added to the QuickbarSlot with the lowest index that
     * is currently empty.  If all QuickbarSlots are currently occupied, no
     * action is performed.
     *
     * @param itemID  the ID of the item
     * @param quality of the quality of the item
     */

    public void addToFirstEmptySlot( String itemID, String quality )
    {
        addToFirstEmptySlot( Quickbar.getQuickbarSlot( EntityManager.getItem( itemID, quality ), parent ) );
    }

    /**
     * Helper function to easily add the specified Item to this Quickbar.  The
     * specified Item is added to the QuickbarSlot with the lowest index that
     * is currently empty.  If all QuickbarSlots are currently occupied, no
     * action is performed.
     *
     * @param itemID the ID of the item.  If the item has quality, the default
     *               quality version of this item is added
     */

    public void addToFirstEmptySlot( String itemID )
    {
        addToFirstEmptySlot( Quickbar.getQuickbarSlot( EntityManager.getItem( itemID ), parent ) );
    }

    /**
     * Helper function to easily add the specified Item to this Quickbar.  The
     * specified Item is added to the QuickbarSlot with the lowest index that
     * is currently empty.  If all QuickbarSlots are currently occupied, no
     * action is performed.
     *
     * @param item the Item to add
     */

    public void addToFirstEmptySlot( Item item )
    {
        addToFirstEmptySlot( Quickbar.getQuickbarSlot( item, parent ) );
    }

    private void addToFirstEmptySlot( QuickbarSlot slot )
    {
        if ( slot == null ) return;

        for ( int i = 0; i < Quickbar.ItemSlots; i++ )
        {
            if ( getSlot( i ) == null )
            {
                setSlot( slot, i );
                break;
            }
        }

        Game.mainViewer.updateInterface( );
    }

    private void putSlot( int index, QuickbarSlot slot )
    {
        // set the index of the new slot
        if ( slot != null )
        {
            slot.setIndex( index );
        }

        slots.put( Integer.valueOf( index ), slot );
    }

    /**
     * Returns a QuickbarSlot for the specified Item owned by the parent.  The
     * QuickbarSlot can then be added to a Quickbar.  This QuickbarSlot may
     * either equip or use the Item depending on its ItemType.  Some Items
     * can not be added to Quickbars and this method will return null for those
     * Items.
     *
     * @param item   the Item to be encapsulated by the QuickbarSlot
     * @param parent the owner of the Item and Quickbar
     * @return a QuickbarSlot for the specified Item
     */

    public static QuickbarSlot getQuickbarSlot( Item item, PC parent )
    {
        if ( item instanceof EquippableItem )
        {
            return new ItemEquipSlot( ( EquippableItem ) item, parent );
        }
        else if ( item.getTemplate( ).isUsable( ) )
        {
            ItemList.Entry entry = parent.inventory.getUnequippedItems( ).find( item.getTemplate( ).getID( ),
                                                                                item.getQuality( ) );

            if ( entry != null )
            {
                return new ItemUseSlot( entry, parent );
            }
        }

        return null;
    }
}
