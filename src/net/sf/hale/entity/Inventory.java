/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2012 Jared Stephen
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

package net.sf.hale.entity;

import java.util.HashMap;
import java.util.Map;

import de.matthiasmann.twl.Button;
import net.sf.hale.Game;
import net.sf.hale.ability.Effect;
import net.sf.hale.ability.ScriptFunctionType;
import net.sf.hale.bonus.Bonus;
import net.sf.hale.icon.SubIcon;
import net.sf.hale.icon.SubIcon.Type;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.LoadGameException;
import net.sf.hale.loading.ReferenceHandler;
import net.sf.hale.loading.Saveable;
import net.sf.hale.rules.ArmorType;
import net.sf.hale.rules.BaseWeapon;
import net.sf.hale.rules.Currency;
import net.sf.hale.rules.Quality;
import net.sf.hale.rules.Merchant;
import net.sf.hale.rules.Weight;
import net.sf.hale.util.Logger;
import net.sf.hale.util.SimpleJSONArrayEntry;
import net.sf.hale.util.SimpleJSONObject;
import net.sf.hale.widgets.MultipleItemPopup;
import net.sf.hale.widgets.RightClickMenu;

/**
 * A class for storing the list of items currently held by a given creature
 *
 * @author Jared
 */

public class Inventory implements ItemList.Listener, Saveable
{
    private final Creature parent;

    private final ItemList unequippedItems;

    private final Map< Slot, EquippableItem > equippedItems;

    /**
     * An equipped item slot.  Each slot represents a specific physical location on
     * a creature and can only take a narrow range of items
     *
     * @author Jared
     */

    public static enum Slot
    {
        MainHand( new MainHandSlotValidator( ), new NormalSubIconGetter( SubIcon.Type.MainHandWeapon ) ),
        OffHand( new OffHandSlotValidator( ), new OffHandSubIconGetter( ) ),
        Armor( EquippableItemTemplate.Type.Armor, new NormalSubIconGetter( SubIcon.Type.Torso ) ),
        Gloves( EquippableItemTemplate.Type.Gloves, new NormalSubIconGetter( SubIcon.Type.Gloves ) ),
        Helmet( EquippableItemTemplate.Type.Helmet, new NormalSubIconGetter( SubIcon.Type.Head ) ),
        Cloak( EquippableItemTemplate.Type.Cloak, new NormalSubIconGetter( SubIcon.Type.Cloak ) ),
        Boots( EquippableItemTemplate.Type.Boots, new NormalSubIconGetter( SubIcon.Type.Boots ) ),
        Belt( EquippableItemTemplate.Type.Belt, new NormalSubIconGetter( ) ),
        Amulet( EquippableItemTemplate.Type.Amulet, new NormalSubIconGetter( ) ),
        RightRing( EquippableItemTemplate.Type.Ring, new NormalSubIconGetter( ) ),
        LeftRing( EquippableItemTemplate.Type.Ring, new NormalSubIconGetter( ) ),
        Quiver( EquippableItemTemplate.Type.Ammo, new NormalSubIconGetter( SubIcon.Type.Quiver ) );

        private final SlotValidator slotValidator;
        private final SubIconGetter subIconGetter;

        private Slot( EquippableItemTemplate.Type type, SubIconGetter subIconGetter )
        {
            this.slotValidator = new GenericSlotValidator( type );
            this.subIconGetter = subIconGetter;
        }

        private Slot( SlotValidator validator, SubIconGetter subIconGetter )
        {
            this.slotValidator = validator;
            this.subIconGetter = subIconGetter;
        }

        /**
         * Returns true if this Slot can hold the specified item, false otherwise
         * Note that this does not check any other conditions such as AP, currently
         * equipped items, etc.  It only validates the item type against the slot type
         *
         * @param item the item to check
         * @return whether the specified item can be equipped in this slot
         */

        public boolean matchesItemType( EquippableItem item )
        {
            return this.slotValidator.matchesItemType( item );
        }

        /**
         * Gets the SubIcon type associated with this EquippedItem slot
         *
         * @param item
         * @return the SubIconType
         */

        public SubIcon.Type getSubIconType( EquippableItem item )
        {
            return subIconGetter.getType( item );
        }
    }

    /**
     * Classes to get the appropriate subicon for this Slot
     *
     * @author Jared
     */

    private interface SubIconGetter
    {
        public SubIcon.Type getType( EquippableItem item );
    }

    private static class NormalSubIconGetter implements SubIconGetter
    {
        private final SubIcon.Type type;

        private NormalSubIconGetter( )
        {
            this.type = null;
        }

        private NormalSubIconGetter( SubIcon.Type type )
        {
            this.type = type;
        }

        @Override
        public Type getType( EquippableItem item )
        {
            return type;
        }
    }

    private static class OffHandSubIconGetter implements SubIconGetter
    {
        @Override
        public Type getType( EquippableItem item )
        {
            switch ( item.getTemplate( ).getType( ) )
            {
                case Shield:
                    return SubIcon.Type.Shield;
                case Weapon:
                    return SubIcon.Type.OffHandWeapon;
                default:
                    return null;
            }
        }
    }

    /**
     * Classes to check to see if an item can be equipped to a given slot
     *
     * @author Jared
     */

    private interface SlotValidator
    {
        public boolean matchesItemType( EquippableItem item );
    }

    private static class MainHandSlotValidator implements SlotValidator
    {
        @Override
        public boolean matchesItemType( EquippableItem item )
        {
            return item.getTemplate( ).getType( ) == EquippableItemTemplate.Type.Weapon;
        }
    }

    private static class OffHandSlotValidator implements SlotValidator
    {
        @Override
        public boolean matchesItemType( EquippableItem item )
        {
            EquippableItemTemplate.Type type = item.getTemplate( ).getType( );

            switch ( type )
            {
                case Shield:
                    return true;
                case Weapon:
                    return ( ( ( Weapon ) item ).getTemplate( ).getHanded( ) != WeaponTemplate.Handed.TwoHanded );
                default:
                    return false;
            }
        }
    }

    private static class GenericSlotValidator implements SlotValidator
    {
        private final EquippableItemTemplate.Type type;

        private GenericSlotValidator( EquippableItemTemplate.Type type )
        {
            this.type = type;
        }

        @Override
        public boolean matchesItemType( EquippableItem item )
        {
            return ( item.getTemplate( ).getType( ) == type );
        }
    }

    @Override
    public JSONOrderedObject save( )
    {
        JSONOrderedObject out = new JSONOrderedObject( );

        JSONOrderedObject equippedOut = new JSONOrderedObject( );
        for ( Inventory.Slot slot : Inventory.Slot.values( ) )
        {
            EquippableItem item = equippedItems.get( slot );

            if ( item == null ) continue;

            JSONOrderedObject itemOut = new JSONOrderedObject( );

            itemOut.put( "id", item.getTemplate( ).getID( ) );

            if ( item.getTemplate( ).hasQuality( ) )
            {
                itemOut.put( "quality", item.getQuality( ).getName( ) );
            }

            if ( item.getEffects( ).size( ) > 0 )
            {
                itemOut.put( "effects", item.getEffects( ).save( ) );
            }

            equippedOut.put( slot.name( ), itemOut );
        }
        out.put( "equipped", equippedOut );

        out.put( "unequipped", unequippedItems.save( ) );

        return out;
    }

    /**
     * Loads this inventory from JSON data.  If refHandler is non-null, then will attempt to
     * resolve any effect references in the JSON.  Otherwise, any effects are ignored
     *
     * @param data
     * @param refHandler
     * @throws LoadGameException
     */

    public void load( SimpleJSONObject data, ReferenceHandler refHandler ) throws LoadGameException
    {
        if ( data.containsKey( "createdItems" ) )
        {
            // add any created items from this inventory to the campaign
            // this step will only occur when loading an exported player character file

            for ( SimpleJSONArrayEntry entry : data.getArray( "createdItems" ) )
            {
                CreatedItem createdItem = CreatedItem.load( entry.getObject( ) );
                // register the created item, so that later when it is referenced
                // in this inventory, it can be found by the EntityManager
                Game.curCampaign.addCreatedItem( createdItem );
            }
        }

        if ( data.containsKey( "equipped" ) )
        {
            SimpleJSONObject equipped = data.getObject( "equipped" );

            for ( Inventory.Slot slot : Inventory.Slot.values( ) )
            {
                if ( ! equipped.containsKey( slot.toString( ) ) ) continue;

                SimpleJSONObject slotData = equipped.getObject( slot.toString( ) );

                String id = slotData.get( "id", null );

                EquippableItem item;
                if ( slotData.containsKey( "quality" ) )
                {
                    item = ( EquippableItem ) EntityManager.getItem( id, slotData.get( "quality", null ) );
                }
                else
                {
                    item = ( EquippableItem ) EntityManager.getItem( id );
                }

                if ( item == null )
                {
                    Logger.appendToWarningLog( "Item " + id + " cannot be loaded." );
                    continue;
                }

                if ( refHandler != null && slotData.containsKey( "effects" ) )
                {
                    item.getEffects( ).load( slotData.getObject( "effects" ), refHandler, item );
                    item.getEffects( ).startAnimations( );
                    for ( Effect effect : item.getEffects( ) )
                    {
                        item.applyEffectBonuses( effect );
                    }
                    //item.applyEffectBonuses(effect);
                }

                equip( item, slot, false );
            }
        }

        if ( data.containsKey( "unequipped" ) )
        {
            unequippedItems.load( data.getArray( "unequipped" ) );
        }
    }

    /**
     * Creates a new Inventory for the specified parent creature
     *
     * @param parent the parent creature owning this inventory
     */

    public Inventory( Creature parent )
    {
        this.parent = parent;

        unequippedItems = new ItemList( );
        unequippedItems.addListener( this );

        // set up empty equipment slots
        equippedItems = new HashMap< Slot, EquippableItem >( );
    }

    /**
     * Creates a new inventory which is a copy of the specified inventory,
     * but for the new specified parent
     *
     * @param other
     * @param parent
     */

    public Inventory( Inventory other, Creature parent )
    {
        this.parent = parent;

        unequippedItems = new ItemList( other.unequippedItems );
        unequippedItems.addListener( this );

        equippedItems = new HashMap< Slot, EquippableItem >( );
        for ( Inventory.Slot key : other.equippedItems.keySet( ) )
        {
            EquippableItem otherItem = other.equippedItems.get( key );

            equip( ( EquippableItem ) EntityManager.getItem( otherItem.getTemplate( ).getID( ), otherItem.getQuality( ) ), key, false );
        }
    }

    /**
     * Ends all animations on all equipped items
     */

    public void endAllAnimations( )
    {
        for ( Inventory.Slot slot : Inventory.Slot.values( ) )
        {
            EquippableItem item = equippedItems.get( slot );

            if ( item != null )
            {
                item.getEffects( ).endAllAnimations( );
            }
        }
    }

    /*
     * Responsible for removing the item from the specified slot.  If there is
     * no item equipped in the slot, no action is performed
     * This will also remove subIcons and run scripts as needed
     * @param addToUnequipped true to add the item in the slot to the list of
     * unequipped items, false to discard the item
     */

    private void unequip( Slot slot, boolean addToUnequipped )
    {
        EquippableItem currentlyEquippedItem = equippedItems.get( slot );

        if ( currentlyEquippedItem != null )
        {
            // stop any active animations
            currentlyEquippedItem.getEffects( ).endAllAnimations( );
            currentlyEquippedItem.setOwner( null );

            if ( addToUnequipped )
            {
                unequippedItems.add( currentlyEquippedItem );
            }

            equippedItems.remove( slot );

            parent.stats.changeEquipment( currentlyEquippedItem.getTemplate( ).getType( ) );
            parent.stats.removeAll( currentlyEquippedItem.getBonusList( ) );
            parent.removeSubIcon( currentlyEquippedItem, slot );

            if ( currentlyEquippedItem.getTemplate( ).hasScript( ) )
            {
                currentlyEquippedItem.getTemplate( ).getScript( ).executeFunction( ScriptFunctionType.onUnequipItem,
                                                                                   parent, currentlyEquippedItem );
            }
        }
    }

    /*
     * Responsible for the actual equipping of an item.  Performs no checks.  This will
     * also add subicons, run scripts as needed
     * @param modifyParentStats whether the parent stat manager will be notified of the
     * change in equipment.  This should always be true, except for when first creating
     * a creature.  In that case, stats will be fully recomputed after creation.
     */

    private void equip( EquippableItem item, Slot slot, boolean modifyParentStats )
    {
        equippedItems.put( slot, item );
        item.setOwner( parent );

        unequippedItems.remove( item );

        if ( modifyParentStats )
        {
            parent.stats.changeEquipment( item.getTemplate( ).getType( ) );
            parent.stats.addAll( item.getBonusList( ) );
        }

        parent.addSubIcon( item, slot );

        if ( item.getTemplate( ).hasScript( ) )
        {
            item.getTemplate( ).getScript( ).executeFunction( ScriptFunctionType.onEquipItem, parent, item );
        }
    }

    /**
     * Checks if the item can be equipped, and actually equips it if performEquip is true
     * and the item can be equipped
     *
     * @param item
     * @param slot
     * @param performEquip
     * @return
     */

    private boolean checkAndEquip( EquippableItem item, Slot slot, boolean performEquip )
    {
        // set the default slot if one was not specified
        if ( slot == null )
        {
            slot = getDefaultSlot( item );
        }

        if ( parent.getTemplate( ).getRace( ).isSlotRestricted( slot ) )
        { return false; }

        if ( ! parent.timer.canPerformEquipAction( item ) )
        { return false; }

        if ( ! slot.matchesItemType( item ) )
        { return false; }

        if ( ! hasPrereqsToEquip( item ) )
        { return false; }

        // check for dual wielding proficiency
        if ( item instanceof Weapon && slot == Slot.OffHand &&
                ! parent.stats.has( Bonus.Type.DualWieldTraining ) )
        {
            return false;
        }

        // all checks on the item have passed, now make sure the currently equipped
        // items can be removed
        EquippableItem currentlyEquippedItem = equippedItems.get( slot );

        if ( currentlyEquippedItem != null )
        {
            if ( ! currentlyEquippedItem.getTemplate( ).isUnequippable( ) ) return false;
        }


        Slot secondaryUsedSlot = null;

        if ( item instanceof Weapon )
        {
            Weapon weapon = ( Weapon ) item;

            // two handed weapons take up both main hand and off hand slots
            if ( weapon.isTwoHanded( ) )
            {
                secondaryUsedSlot = Slot.OffHand;
            }
        }

        // check the main hand weapon if we are equipping off hand
        if ( slot == Slot.OffHand )
        {
            Weapon mainHand = getEquippedMainHand( );

            if ( mainHand != null && mainHand.isTwoHanded( ) )
            {
                secondaryUsedSlot = Slot.MainHand;
            }
        }

        EquippableItem secondaryEquippedItem = null;
        if ( secondaryUsedSlot != null )
        {
            secondaryEquippedItem = equippedItems.get( secondaryUsedSlot );

            if ( secondaryEquippedItem != null )
            {
                if ( ! secondaryEquippedItem.getTemplate( ).isUnequippable( ) ) return false;
            }
        }

        // now we have verified that the item can be equipped

        if ( performEquip )
        {
            parent.timer.performEquipAction( item );

            // unequip any currently blocking items
            if ( currentlyEquippedItem != null )
            {
                unequip( slot, true );
            }

            if ( secondaryEquippedItem != null )
            {
                unequip( secondaryUsedSlot, true );
            }

            // now the item can be equipped
            equip( item, slot, true );

            if ( Game.mainViewer != null )
            {
                Game.mainViewer.updateInterface( );
            }
        }

        return true;
    }

    /**
     * Returns true if and only if the parent creature is currently able to equip
     * the specified item to the specified slot, or to the default slot if the specified
     * slot is null.  The parent must have sufficient AP, the prereqs to equip the item,
     * and the slots must not be blocked by any currently equipped but unremovable items
     *
     * @param item the item to check
     * @param slot the slot to see if the item can be equipped to.  If the slot is invalid
     *             for the item, this method returns false.  If the slot is null, then the default
     *             slot for the item will be checked, see {@link #equipItem(EquippableItem, Slot)}
     * @return whether the item can be equipped
     */

    public boolean canEquip( EquippableItem item, Slot slot )
    {
        return checkAndEquip( item, slot, false );
    }

    /**
     * Attempts to equip the specified item to the default slot.  See {@link #equipItem(EquippableItem, Slot)}
     *
     * @param itemID
     * @param quality
     * @return true if the item was successfully equipped, false otherwise
     */

    public boolean equipItem( String itemID, String quality )
    {
        EquippableItem item = ( EquippableItem ) EntityManager.getItem( itemID, quality );

        return checkAndEquip( item, null, true );
    }

    /**
     * Attempts to equip the specified item to the specified slot.  The owner of the inventory
     * must have sufficient AP to equip the item.  Any items currently in the slot or any
     * conflicting slots (for two handed weapons) are unequipped.  Items that the parent
     * creature does not have prereqs to equip will not be equipped.
     *
     * @param item the item to be equipped.  This item must currently be contained in the list
     *             of unequipped items
     * @param slot the slot to equip the item to.  If the slot is invalid for the item,
     *             the item is not equipped.  If the slot is null, then it will equip the item in the
     *             unique valid slot if that slot is unique.  Rings will be equipped in the empty
     *             ring slot, or if neither ring slot is empty, the right ring slot.  Weapons will be
     *             equipped in the main hand slot, unless the main hand slot is occupied, the off hand
     *             slot is not occupied, the weapon is light, and the owner can dual wield
     * @return true if the item was successfully equipped, false otherwise
     */

    public boolean equipItem( EquippableItem item, Slot slot )
    {
        return checkAndEquip( item, slot, true );
    }

    /**
     * Adds the specified item to this inventory, then equips it to the default
     * slot
     *
     * @param item the item to add
     */

    public void addAndEquip( EquippableItem item )
    {
        unequippedItems.add( item );

        checkAndEquip( item, null, true );
    }

    /**
     * Adds the specified item to this inventory, then equips it to the specified
     * slot
     *
     * @param item the item to add
     * @param slot
     */

    public void addAndEquip( EquippableItem item, Slot slot )
    {
        unequippedItems.add( item );

        checkAndEquip( item, slot, true );
    }

    /**
     * Adds the specified item to this inventory, then equips it to the default
     * slot
     *
     * @param itemID  the id of the item
     * @param quality the quality of the item
     */

    public void addAndEquip( String itemID, String quality )
    {
        EquippableItem item = ( EquippableItem ) EntityManager.getItem( itemID, quality );

        unequippedItems.add( item );
        checkAndEquip( item, null, true );
    }

    /**
     * Adds the specified item to this inventory, then equips it to the default
     * slot
     *
     * @param itemID the id of the item
     */

    public void addAndEquip( String itemID )
    {
        EquippableItem item = ( EquippableItem ) EntityManager.getItem( itemID );

        unequippedItems.add( item );
        checkAndEquip( item, null, true );
    }

    /**
     * Returns true if the parent of this inventory has the needed prereqs to equip
     * the item, false otherwise
     *
     * @param item
     * @return whether the parent has the prereqs to equip this item
     */

    public boolean hasPrereqsToEquip( EquippableItem item )
    {
        if ( item instanceof Weapon )
        {
            BaseWeapon baseWeapon = ( ( Weapon ) item ).getTemplate( ).getBaseWeapon( );

            return parent.stats.hasWeaponProficiency( baseWeapon.getName( ) );
        }
        else if ( item instanceof Armor )
        {
            ArmorType armorType = ( ( Armor ) item ).getTemplate( ).getArmorType( );

            return parent.stats.hasArmorProficiency( armorType.getName( ) );
        }

        return true;
    }

    /*
     * Gets the default slot for the specified item if no slot is specified
     */

    private Slot getDefaultSlot( EquippableItem item )
    {
        switch ( item.getTemplate( ).getType( ) )
        {
            case Weapon:
                Weapon mainHand = this.getEquippedMainHand( );

                if ( mainHand != null && ! mainHand.isTwoHanded( ) && getEquippedOffHand( ) == null &&
                        parent.stats.has( Bonus.Type.DualWieldTraining ) && ! ( ( Weapon ) item ).isTwoHanded( ) )
                {
                    return Slot.OffHand;
                }
                else
                {
                    return Slot.MainHand;
                }
            case Ring:
                if ( equippedItems.get( Slot.LeftRing ) == null )
                {
                    return Slot.LeftRing;
                }
                else
                {
                    return Slot.RightRing;
                }
            case Armor:
                return Slot.Armor;
            case Gloves:
                return Slot.Gloves;
            case Helmet:
                return Slot.Helmet;
            case Cloak:
                return Slot.Cloak;
            case Boots:
                return Slot.Boots;
            case Belt:
                return Slot.Belt;
            case Amulet:
                return Slot.Amulet;
            case Ammo:
                return Slot.Quiver;
            case Shield:
                return Slot.OffHand;
            default:
                return Slot.MainHand;
        }
    }

    /**
     * Returns the parent (owning) creature for this inventory
     *
     * @return the parent creature
     */

    public Creature getParent( )
    {
        return parent;
    }

    /**
     * Returns true if the current main weapon can be used based on the currently equipped ammo.  This is
     * true if the main weapon does not require ammo.  If the main weapon does require ammo, it is true if
     * and only if the equipped ammo is usable by the main weapon.
     * <p>
     * the ammo is usable by the equipped weapon.
     *
     * @return whether this inventory has ammo equipped for its main weapon
     */

    public boolean hasAmmoEquippedForWeapon( )
    {
        Weapon mainHand = parent.getMainHandWeapon( );

        switch ( mainHand.getTemplate( ).getWeaponType( ) )
        {
            case Ranged:
                return mainHand.getTemplate( ).isAmmoForThisWeapon( getEquippedQuiver( ) );
            default:
                return true;
        }
    }

    /**
     * Gets the weapon equipped in this Inventory's main hand, or null
     * if nothing is equipped in that slot
     *
     * @return the weapon in the main hand slot
     */

    public Weapon getEquippedMainHand( )
    {
        return ( Weapon ) equippedItems.get( Slot.MainHand );
    }

    /**
     * Gets the weapon or shield equipped in this Inventory's off hand, or
     * null if nothing is equipped in that slot
     *
     * @return the item in the off hand slot
     */

    public EquippableItem getEquippedOffHand( )
    {
        return equippedItems.get( Slot.OffHand );
    }

    /**
     * Gets the shield equipped in this Inventory's off hand, or null
     * if nothing is equipped in that slot or if a weapon is equipped in that slot
     *
     * @return the shield in the off hand slot
     */

    public Armor getEquippedShield( )
    {
        EquippableItem offHand = equippedItems.get( Slot.OffHand );

        if ( offHand == null ) return null;

        if ( offHand instanceof Armor )
        { return ( Armor ) offHand; }
        else
        { return null; }
    }

    /**
     * Gets the weapon equipped in this Inventory's off hand, or null
     * if nothing is equipped in that slot or if a shield is equipped in that slot
     *
     * @return the weapon in the off hand slot
     */

    public Weapon getEquippedOffHandWeapon( )
    {
        EquippableItem offHand = equippedItems.get( Slot.OffHand );

        if ( offHand == null ) return null;

        if ( offHand instanceof Weapon )
        { return ( Weapon ) offHand; }
        else
        { return null; }
    }

    /**
     * Gets the Armor equipped in the Armor slot, or null if no item is equipped in
     * the Armor Slot
     *
     * @return the item in the Armor slot
     */

    public Armor getEquippedArmor( )
    {
        return ( Armor ) equippedItems.get( Slot.Armor );
    }

    /**
     * Gets the gloves equipped in the Gloves slot, or null if no item is in that slot
     *
     * @return the item in the Gloves slot
     */

    public Armor getEquippedGloves( )
    {
        return ( Armor ) equippedItems.get( Slot.Gloves );
    }

    /**
     * Gets the helmet equipped in the Helmet slot, or null if no item is in that slot
     *
     * @return the item in the Helmet slot
     */

    public Armor getEquippedHelmet( )
    {
        return ( Armor ) equippedItems.get( Slot.Helmet );
    }

    /**
     * Gets the boots equipped in the Boots slot, or null if no item is in that slot
     *
     * @return the item in the Boots slot
     */

    public Armor getEquippedBoots( )
    {
        return ( Armor ) equippedItems.get( Slot.Boots );
    }

    /**
     * Gets the item equipped in the Cloak slot, or null if no item is in that slot
     *
     * @return the item in the Cloak slot
     */

    public EquippableItem getEquippedCloak( )
    {
        return equippedItems.get( Slot.Cloak );
    }

    /**
     * Gets the item equipped in the Belt slot, or null if no item is in that slot
     *
     * @return the item in the Belt slot
     */

    public EquippableItem getEquippedBelt( )
    {
        return equippedItems.get( Slot.Belt );
    }

    /**
     * Gets the item equipped in the Amulet slot, or null if no item is in that slot
     *
     * @return the item in the Amulet slot
     */

    public EquippableItem getEquippedAmulet( )
    {
        return equippedItems.get( Slot.Amulet );
    }

    /**
     * Gets the item equipped in the Right Ring slot, or null if no item is in that slot
     *
     * @return the item in the Right Ring slot
     */

    public EquippableItem getEquippedRightRing( )
    {
        return equippedItems.get( Slot.RightRing );
    }

    /**
     * Gets the item equipped in the Left Ring slot, or null if no item is in that slot
     *
     * @return the item in the Left Ring slot
     */

    public EquippableItem getEquippedLeftRing( )
    {
        return equippedItems.get( Slot.LeftRing );
    }

    /**
     * Gets the ammo equipped in the Quiver slot, or null if no item is in that slot
     *
     * @return the item in the Quiver slot
     */

    public Ammo getEquippedQuiver( )
    {
        return ( Ammo ) equippedItems.get( Slot.Quiver );
    }

    /**
     * Returns the item currently equipped in the specified slot, or null
     * if no item is equipped in that slot
     *
     * @param slot
     * @return the item currently equipped in the specified slot
     */

    public EquippableItem getEquippedItem( Slot slot )
    {
        return this.equippedItems.get( slot );
    }

    /**
     * Removes the equipped item from the specified slot
     *
     * @param slot the name of the slot to remove the item from
     * @return the item that was removed, or null if no item was present in the
     * slot and so no item was removed
     */

    public EquippableItem removeEquippedItem( String slot )
    {
        return removeEquippedItem( Slot.valueOf( slot ) );
    }

    /**
     * Removes the equipped item from the specified slot
     *
     * @param slot the slot to remove the item from
     * @return the item that was removed, or null if no item was present in the
     * slot and so no item was removed
     */

    public EquippableItem removeEquippedItem( Slot slot )
    {
        EquippableItem item = equippedItems.get( slot );

        if ( item != null )
        {
            unequip( slot, false );
        }

        return item;
    }

    /**
     * Returns the list of unequipped items in this inventory
     *
     * @return the unequipped items
     */

    public ItemList getUnequippedItems( )
    {
        return unequippedItems;
    }

    /**
     * Returns true if the inventory of this character is completely empty
     * other than the default clothes item that is created on new characters
     *
     * @return whether or not this inventory is empty other than the default
     * clothes armor
     */

    public boolean isEmptyOtherThanDefaultClothes( )
    {
        if ( unequippedItems.size( ) > 0 ) return false;

        for ( Inventory.Slot slot : Inventory.Slot.values( ) )
        {
            if ( equippedItems.get( slot ) != null && ( slot != Inventory.Slot.Armor ||
                    ! equippedItems.get( slot ).getTemplate( ).getID( ).equals( Game.ruleset.getString( "DefaultClothes" ) ) ) )
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Removes all items (equipped and unequipped) from this inventory
     */

    public void clear( )
    {
        for ( Slot slot : Slot.values( ) )
        {
            unequip( slot, false );
        }

        unequippedItems.clear( );
    }

    /**
     * Returns a weight representing the total weight of all the items, equipped and
     * unequipped, in this inventory
     *
     * @return the total weight of all items in this inventory
     */

    public Weight getTotalWeight( )
    {
        int grams = unequippedItems.getTotalWeight( ).grams;

        for ( Inventory.Slot slot : Inventory.Slot.values( ) )
        {
            EquippableItem item = equippedItems.get( slot );
            if ( item == null ) continue;

            grams += item.getTemplate( ).getWeightInGrams( );
        }

        return new Weight( grams );
    }

    /**
     * Returns true if this inventory contains both of the specified items, false
     * otherwise.  If the items are identical, then this inventory must contain
     * a count of at least two of that item
     *
     * @param firstItem
     * @param secondItem
     * @return whether this inventory contains both items
     */

    public boolean hasBoth( Item firstItem, Item secondItem )
    {
        if ( firstItem.equals( secondItem ) )
        {
            return getTotalQuantity( firstItem ) >= 2;
        }
        else
        {
            return getTotalQuantity( firstItem ) >= 1 && getTotalQuantity( secondItem ) >= 1;
        }
    }

    /**
     * Returns the total quantity of items held in this inventory (both equipped
     * and unequipped items) matching the specified item
     *
     * @param item
     * @return the total quantity held of the item
     */

    public int getTotalQuantity( Item item )
    {
        int quantity = unequippedItems.getQuantity( item );

        for ( Inventory.Slot slot : Inventory.Slot.values( ) )
        {
            if ( item.equals( equippedItems.get( slot ) ) )
            { quantity++; }
        }

        return quantity;
    }

    /**
     * Returns the total quantity of items held in this inventory (both
     * equipped and unequipped) with an ID matching that specified
     *
     * @param itemID
     * @return the total quantity of items with matching ID
     */

    public int getTotalQuantity( String itemID )
    {
        int quantity = unequippedItems.getQuantity( itemID );

        for ( Inventory.Slot slot : Inventory.Slot.values( ) )
        {
            Item item = equippedItems.get( slot );
            if ( item == null ) continue;

            if ( item.getTemplate( ).getID( ).equals( itemID ) )
            { quantity++; }
        }

        return quantity;
    }

    /**
     * Removes up to a quantity of one of the specified item
     * this inventory, including all equipped and unequipped items.  Note that
     * no AP is deducted for this action.  Unequipped items are checked first.
     *
     * @param item
     * @return the actual quantity of items that was removed
     */

    public int remove( Item item )
    {
        return remove( item, 1 );
    }

    /**
     * Removes up to the specified quantity of items matching the specified item
     * this inventory, including all equipped and unequipped items.  Note that
     * no AP is deducted for this action.  Unequipped items are checked first.
     *
     * @param item
     * @param quantity
     * @return the actual quantity of items that was removed
     */

    public int remove( Item item, int quantity )
    {
        int qtyRemovedFromUnequipped = unequippedItems.remove( item, quantity );

        if ( qtyRemovedFromUnequipped == quantity )
        { return quantity; }

        int qtyRemovedFromEquipped = 0;

        for ( Inventory.Slot slot : Inventory.Slot.values( ) )
        {
            if ( item.equals( equippedItems.get( slot ) ) )
            {
                qtyRemovedFromEquipped++;
                unequip( slot, false );

                // the entire requested quantity was removed
                if ( qtyRemovedFromEquipped + qtyRemovedFromUnequipped == quantity )
                { return quantity; }
            }
        }


        return qtyRemovedFromEquipped + qtyRemovedFromUnequipped;
    }

    /**
     * Removes up to a quantity of one of items matching the specified ID from
     * this inventory, including all equipped and unequipped items.  Note that
     * no AP is deducted for this action.  Unequipped items are checked first.
     *
     * @param itemID
     * @return the actual quantity of items that was removed
     */

    public int remove( String itemID )
    {
        return remove( itemID, 1 );
    }

    /**
     * Removes up to the specified quantity of items matching the specified ID from
     * this inventory, including all equipped and unequipped items.  Note that
     * no AP is deducted for this action.  Unequipped items are checked first.
     *
     * @param itemID
     * @param quantity
     * @return the actual quantity of items that was removed
     */

    public int remove( String itemID, int quantity )
    {
        int qtyRemovedFromUnequipped = unequippedItems.remove( itemID, quantity );

        if ( qtyRemovedFromUnequipped == quantity )
        { return quantity; }

        int qtyRemovedFromEquipped = 0;

        for ( Inventory.Slot slot : Inventory.Slot.values( ) )
        {
            if ( equippedItems.get( slot ) == null ) continue;

            if ( itemID.equals( equippedItems.get( slot ).getTemplate( ).getID( ) ) )
            {
                qtyRemovedFromEquipped++;
                unequip( slot, false );

                // the entire requested quantity was removed
                if ( qtyRemovedFromEquipped + qtyRemovedFromUnequipped == quantity )
                { return quantity; }
            }
        }

        return qtyRemovedFromEquipped + qtyRemovedFromUnequipped;
    }

    /**
     * Returns the inventory slot that the specified item is equipped in, or
     * null if the item is not equipped
     *
     * @param item
     * @return the inventory slot
     */

    public Slot getSlot( Item item )
    {
        for ( Inventory.Slot slot : Inventory.Slot.values( ) )
        {
            if ( item.equals( equippedItems.get( slot ) ) )
            { return slot; }
        }

        return null;
    }

    /**
     * Returns true if this Inventory has one or more of the specified item equipped,
     * false otherwise
     *
     * @param item
     * @return whether this inventory has at least one of the specified item equipped
     */

    public boolean isEquipped( Item item )
    {
        return getSlot( item ) != null;
    }

    /**
     * Elapses the specified number of rounds for all equipped items
     *
     * @param numRounds
     */

    public void elapseTime( int numRounds )
    {
        for ( Inventory.Slot slot : Inventory.Slot.values( ) )
        {
            EquippableItem item = equippedItems.get( slot );
            if ( item == null ) continue;

            item.elapseTime( numRounds );
        }
    }

    /**
     * Validates all items in this inventory, removing any with IDs that do not
     * have a valid reference
     */

    public void validateItems( )
    {
        unequippedItems.validate( );
    }

    /*
     * Callbacks section of code
     */

    /**
     * Returns a callback which, when run, will take the specified item from the container
     * and then equip it
     *
     * @param item
     * @param container
     * @return a callback for taking and then equipping an item
     */

    public Runnable getTakeAndWieldCallback( EquippableItem item, Container container )
    {
        return new TakeAndWieldCallback( item, container );
    }

    /**
     * Returns a callback which, when run, will unequip the item in the specified slot
     *
     * @param slot
     * @return a callback for unequipping the specified item
     */

    public Runnable getUnequipCallback( Slot slot )
    {
        return new UnequipCallback( slot );
    }

    /**
     * Returns a callback which, when run, will equip the item to the specified slot
     *
     * @param item
     * @param slot the specified slot, or null for the default slot for this item
     * @return a callback for equipping the item
     */

    public Runnable getEquipCallback( EquippableItem item, Slot slot )
    {
        return new EquipCallback( item, slot );
    }

    /**
     * Returns a callback that, when run, will show the menu of possible give targets.
     * If the user selects a give target, then up to the maxQuantity of the item will
     * move from this inventory to the target's inventory.
     * See {@link #getGiveCallback(Item, int, Creature)}
     *
     * @param item
     * @param maxQuantity
     * @return a callback that will give up to maxQuantity of the specified Item
     */

    public Runnable getGiveCallback( Item item, int maxQuantity )
    {
        return new ShowGiveMenuCallback( item, maxQuantity );
    }

    /**
     * Returns a callback, which, when run, will give the item in the slot specified
     * of this inventory to the specified target creature
     *
     * @param slot   the slot to give.  There must be an item in this slot when the callback
     *               is run
     * @param target
     * @return a callback for giving an equipped item
     */

    public Runnable getGiveEquippedCallback( Slot slot, Creature target )
    {
        return new GiveEquippedCallback( slot, target );
    }

    /**
     * Returns a callback which, when run, will give up to the specified quantity of the
     * item to the target.  If maxQuantity is one, then gives a quantity of one to the
     * target.  If maxQuantity is greater than one, then the player selects the quantity
     * (up to maxQuantity)
     *
     * @param item
     * @param maxQuantity
     * @param target
     * @return a callback for giving an item
     */

    public Runnable getGiveCallback( Item item, int maxQuantity, Creature target )
    {
        return new GiveCallback( item, maxQuantity, target );
    }

    /**
     * Returns a callback which, when run, will drop up to the specified quantity of the
     * item to the currently open container, or if no container is open, the ground beneath
     * the parent creature's feet
     *
     * @param item
     * @param maxQuantity
     * @return a drop item callback
     */

    public Runnable getDropCallback( Item item, int maxQuantity )
    {
        return new DropCallback( item, maxQuantity );
    }

    /**
     * Returns a callback which, when run, will drop the item in the specified inventory slot
     * to the currently open container, or if no container is open, the ground beneath
     * the parent creature's feet
     *
     * @param slot
     * @return a drop equipped item callback
     */

    public Runnable getDropEquippedCallback( Inventory.Slot slot )
    {
        return new DropEquippedCallback( slot );
    }

    /**
     * Returns a callback which, when run, will take up to the specified quantity of the item
     * from the container and put it in this inventory.  If maxQuantity is one, then a quantity
     * of one is taken.  If maxQuantity is greater than one, then the player selects a quantity up
     * to maxQuantity
     *
     * @param item
     * @param maxQuantity
     * @param container
     * @return a callback for taking an item from a container
     */

    public Runnable getTakeCallback( Item item, int maxQuantity, Container container )
    {
        return new TakeCallback( item, maxQuantity, container );
    }

    /**
     * A callback which, when run, takes all the items from the specified container and
     * puts them in this inventory
     *
     * @param container the container to take the items from
     * @return a callback for taking all items from the container
     */

    public Runnable getTakeAllCallback( Container container )
    {
        return new TakeAllCallback( container );
    }

    /**
     * Returns a callback which, when run, will buy up to the specified quantity of the item from
     * the merchant and put the item in this inventory.  If maxQuantity is one, then a quantity
     * of one is bought.  If maxQuantity is greater than one, then the player selects a quantity up
     * to maxQuantity
     *
     * @param item
     * @param maxQuantity
     * @param merchant
     * @return a callback for buying an item from a merchant
     */

    public Runnable getBuyCallback( Item item, int maxQuantity, Merchant merchant )
    {
        return new BuyCallback( item, maxQuantity, merchant );
    }

    /**
     * Returns a callback which, when run, will sell up to the specified quantity of the item to
     * the merchant and take the item from this inventory.  If maxQuantity is one, then a quantity
     * of one is sold.  If maxQuantity is greater than one, then the player selects a quantity up
     * to maxQuantity
     *
     * @param item
     * @param maxQuantity
     * @param merchant
     * @return a callback for selling an item to a merchant
     */

    public Runnable getSellCallback( Item item, int maxQuantity, Merchant merchant )
    {
        return new SellCallback( item, maxQuantity, merchant );
    }

    /**
     * Returns a callback which, when run, will sell the item in the specified inventory slot
     * to the specified merchant
     *
     * @param slot
     * @param merchant
     * @return a callback for selling an equipped item
     */

    public Runnable getSellEquippedCallback( Inventory.Slot slot, Merchant merchant )
    {
        return new SellEquippedCallback( slot, merchant );
    }

    private class TakeAndWieldCallback implements Runnable
    {
        private EquippableItem item;
        private Container container;

        private TakeAndWieldCallback( EquippableItem item, Container container )
        {
            this.item = item;
            this.container = container;
        }

        @Override
        public void run( )
        {
            if ( ! parent.timer.performAction( Game.ruleset.getValue( "PickUpAndWieldItemCost" ) ) )
            { return; }

            unequippedItems.add( item );

            equipItem( item, null );

            container.getCurrentItems( ).remove( item );
            if ( container.getCurrentItems( ).size( ) == 0 && container.getTemplate( ).isTemporary( ) )
            {
                Game.curCampaign.curArea.removeEntity( container );
            }

            Game.mainViewer.getMenu( ).hide( );
            Game.mainViewer.updateInterface( );
        }
    }

    private class UnequipCallback implements Runnable
    {
        private Slot slot;

        private UnequipCallback( Slot slot )
        {
            this.slot = slot;
        }

        @Override
        public void run( )
        {
            if ( ! parent.timer.performEquipAction( getEquippedItem( slot ) ) )
            { return; }

            unequip( slot, true );

            Game.mainViewer.getMenu( ).hide( );
            Game.mainViewer.updateInterface( );
        }
    }

    private class EquipCallback implements Runnable
    {
        private Slot slot;
        private EquippableItem item;

        private EquipCallback( EquippableItem item, Slot slot )
        {
            this.slot = slot;
            this.item = item;
        }

        @Override
        public void run( )
        {
            equipItem( item, slot );

            Game.mainViewer.getMenu( ).hide( );
            Game.mainViewer.updateInterface( );
        }
    }

    private class TakeAllCallback implements Runnable
    {
        private Container container;

        private TakeAllCallback( Container container )
        {
            this.container = container;
        }

        @Override
        public void run( )
        {
            ItemList list = container.getCurrentItems( );

            for ( ItemList.Entry entry : list )
            {
                if ( ! parent.timer.performAction( Game.ruleset.getValue( "PickUpItemCost" ) ) )
                { return; }

                unequippedItems.add( entry.getID( ), entry.getQuality( ), entry.getQuantity( ) );
            }

            container.getCurrentItems( ).clear( );
            if ( container.getTemplate( ).isTemporary( ) )
            {
                Game.curCampaign.curArea.removeEntity( container );
            }

            Game.mainViewer.getMenu( ).hide( );
            Game.mainViewer.updateInterface( );
        }
    }

    private class GiveEquippedCallback implements Runnable
    {
        private Creature target;
        private Slot slot;

        private GiveEquippedCallback( Slot slot, Creature target )
        {
            this.slot = slot;
            this.target = target;
        }

        @Override
        public void run( )
        {
            if ( ! parent.timer.performAction( Game.ruleset.getValue( "GiveItemCost" ) ) )
            { return; }

            EquippableItem item = equippedItems.get( slot );

            unequip( slot, false );

            target.inventory.getUnequippedItems( ).add( item );

            Game.mainViewer.getMenu( ).hide( );
            Game.mainViewer.updateInterface( );
        }
    }

    private abstract class MultipleCallback implements MultipleItemPopup.Callback
    {
        protected final Item item;
        private final int maxQuantity;
        private final String labelText;

        private MultipleCallback( Item item, int maxQuantity, String labelText )
        {
            this.item = item;
            this.maxQuantity = maxQuantity;
            this.labelText = labelText;
        }

        @Override
        public void run( )
        {
            if ( maxQuantity == 1 )
            {
                performItemAction( 1 );
            }
            else
            {
                MultipleItemPopup popup = new MultipleItemPopup( Game.mainViewer );
                popup.openPopupCentered( this );
            }
        }

        @Override
        public String getLabelText( ) { return labelText; }

        @Override
        public int getMaximumQuantity( ) { return maxQuantity; }

        @Override
        public String getValueText( int quantity ) { return ""; }
    }

    /*
     * Shows a menu of player character give targets
     */

    private class ShowGiveMenuCallback implements Runnable
    {
        private Item item;
        private int maxQuantity;

        private ShowGiveMenuCallback( Item item, int maxQuantity )
        {
            this.item = item;
            this.maxQuantity = maxQuantity;
        }

        @Override
        public void run( )
        {
            RightClickMenu menu = Game.mainViewer.getMenu( );
            menu.removeMenuLevelsAbove( 1 );
            menu.addMenuLevel( "Give" );
            for ( PC pc : Game.curCampaign.party )
            {
                if ( pc == parent || pc.isSummoned( ) ) continue;

                Button button = new Button( );
                button.setText( pc.getTemplate( ).getName( ) );
                button.addCallback( new GiveCallback( item, maxQuantity, pc ) );

                menu.addButton( button );
            }

            menu.show( );
        }
    }

    /*
     * The callback that actually gives the item to the target.  If maxQuantity
     * is greater than 1, it first opens a popup window in order to determine
     * the quantity.
     */

    private class GiveCallback extends MultipleCallback
    {
        private Creature target;

        private GiveCallback( Item item, int maxQuantity, Creature target )
        {
            super( item, maxQuantity, "Give" );
            this.target = target;
        }

        @Override
        public void performItemAction( int quantity )
        {
            if ( ! parent.timer.performAction( Game.ruleset.getValue( "GiveItemCost" ) ) )
            { return; }

            unequippedItems.remove( item, quantity );

            target.inventory.getUnequippedItems( ).add( item, quantity );

            Game.mainViewer.getMenu( ).hide( );
            Game.mainViewer.updateInterface( );
        }
    }

    private class DropCallback extends MultipleCallback
    {
        private DropCallback( Item item, int maxQuantity )
        {
            super( item, maxQuantity, "Drop" );
        }

        @Override
        public void performItemAction( int quantity )
        {
            //quest items cannot be dropped
            if ( item.getTemplate( ).isQuest( ) ) return;

            if ( ! parent.timer.performAction( Game.ruleset.getValue( "DropItemCost" ) ) )
            { return; }

            // if the container window is open, drop it in the container,
            // otherwise drop it at the creature's feet
            Container container = Game.mainViewer.containerWindow.getContainer( );
            if ( container != null )
            { item.setLocation( container.getLocation( ) ); }
            else
            { item.setLocation( parent.getLocation( ) ); }

            Game.curCampaign.curArea.addItem( item, quantity );

            unequippedItems.remove( item, quantity );

            Game.mainViewer.getMenu( ).hide( );
            Game.mainViewer.updateInterface( );
        }
    }

    private class DropEquippedCallback implements Runnable
    {
        private Slot slot;

        private DropEquippedCallback( Slot slot )
        {
            this.slot = slot;
        }

        @Override
        public void run( )
        {
            Item item = equippedItems.get( slot );

            if ( item.getTemplate( ).isQuest( ) ) return;

            if ( ! parent.timer.performAction( Game.ruleset.getValue( "DropItemCost" ) ) )
            { return; }

            // if the container window is open, drop it in the container,
            // otherwise drop it at the creature's feet
            Container container = Game.mainViewer.containerWindow.getContainer( );
            if ( container != null )
            { item.setLocation( container.getLocation( ) ); }
            else
            { item.setLocation( parent.getLocation( ) ); }

            unequip( slot, false );

            Game.curCampaign.curArea.addItem( item, 1 );

            Game.mainViewer.getMenu( ).hide( );
            Game.mainViewer.updateInterface( );
        }
    }

    private class TakeCallback extends MultipleCallback
    {
        private Container container;

        private TakeCallback( Item item, int maxQuantity, Container container )
        {
            super( item, maxQuantity, "Take" );

            this.container = container;
        }

        @Override
        public void performItemAction( int quantity )
        {
            if ( ! parent.timer.performAction( Game.ruleset.getValue( "PickUpItemCost" ) ) )
            { return; }

            unequippedItems.add( item );

            container.getCurrentItems( ).remove( item, quantity );
            if ( container.getCurrentItems( ).size( ) == 0 && container.getTemplate( ).isTemporary( ) )
            {
                Game.curCampaign.curArea.removeEntity( container );
            }

            Game.mainViewer.getMenu( ).hide( );
            Game.mainViewer.updateInterface( );
        }
    }

    private class BuyCallback extends MultipleCallback
    {
        private Merchant merchant;

        private BuyCallback( Item item, int maxQuantity, Merchant merchant )
        {
            super( item, maxQuantity, "Buy" );
            this.merchant = merchant;
        }

        @Override
        public String getValueText( int quantity )
        {
            int percent = merchant.getCurrentSellPercentage( );
            return "Price: " + Currency.getPlayerBuyCost( item, quantity, percent ).shortString( );
        }

        @Override
        public void performItemAction( int quantity )
        {
            merchant.sellItem( item, parent, quantity );

            Game.mainViewer.getMenu( ).hide( );
            Game.mainViewer.updateInterface( );
        }
    }

    private class SellCallback extends MultipleCallback
    {
        private Merchant merchant;

        private SellCallback( Item item, int maxQuantity, Merchant merchant )
        {
            super( item, maxQuantity, "Sell" );
            this.merchant = merchant;
        }

        @Override
        public String getValueText( int quantity )
        {
            int percent = merchant.getCurrentBuyPercentage( );
            return "Price: " + Currency.getPlayerSellCost( item, quantity, percent ).shortString( );
        }

        @Override
        public void performItemAction( int quantity )
        {
            merchant.buyItem( item, parent, quantity );

            Game.mainViewer.getMenu( ).hide( );
            Game.mainViewer.updateInterface( );
        }
    }

    private class SellEquippedCallback implements Runnable
    {
        private Slot slot;
        private Merchant merchant;

        private SellEquippedCallback( Slot slot, Merchant merchant )
        {
            this.slot = slot;
            this.merchant = merchant;
        }

        @Override
        public void run( )
        {
            Item item = equippedItems.get( slot );
            if ( item.getTemplate( ).isQuest( ) ) return;

            unequip( slot, false );

            merchant.buyItem( item, parent );

            Game.mainViewer.getMenu( ).hide( );
            Game.mainViewer.updateInterface( );
        }
    }

    @Override
    public void itemListItemAdded( String id, Quality quality, int quantity )
    {
        ItemTemplate template = EntityManager.getItemTemplate( id );

        if ( template.hasScript( ) )
        {
            Item item = EntityManager.getItem( id, quality );
            item.getTemplate( ).getScript( ).executeFunction( ScriptFunctionType.onAddItem, parent, item );
        }
    }

    @Override
    public boolean itemListEntryRemoved( ItemList.Entry entry )
    {
        return false;
    }
}
