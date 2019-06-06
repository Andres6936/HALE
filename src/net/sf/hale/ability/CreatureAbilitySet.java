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

package net.sf.hale.ability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.hale.Game;
import net.sf.hale.entity.Creature;
import net.sf.hale.icon.Icon;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.ReferenceHandler;
import net.sf.hale.loading.Saveable;
import net.sf.hale.util.Logger;
import net.sf.hale.util.SimpleJSONArrayEntry;
import net.sf.hale.util.SimpleJSONObject;

/**
 * A Set of Abilities owned by a particular Creature, as well as the list of
 * readied slots available to that Creature.  Note that this Set can only
 * contain at most one copy of any given Ability
 *
 * @author Jared Stephen
 */

public class CreatureAbilitySet implements Saveable
{
    // no need to save the Widget listeners are they are recreated from scratch on load anyway
    private List< Listener > listeners;

    private final Creature parent;

    // the top level map keys are the ability types.  The lower level map keys are the ability IDs
    private final Map< String, Map< String, AbilityInstance > > abilities;
    private final Map< String, Map< String, AbilityWithActiveCount > > activateableAbilities;

    private final Map< String, List< AbilitySlot > > abilitySlots;

    // list of ability slots we need to temporarily keep track of due to being
    // activated from an item
    private final List< AbilitySlot > tempAbilitySlots;

    @Override
    public JSONOrderedObject save( )
    {
        JSONOrderedObject data = new JSONOrderedObject( );

        if ( tempAbilitySlots.size( ) > 0 )
        {
            Object[] tempSlots = new Object[ tempAbilitySlots.size( ) ];

            int i = 0;
            for ( AbilitySlot slot : tempAbilitySlots )
            {
                tempSlots[ i ] = slot.save( );
                i++;
            }

            data.put( "tempSlots", tempSlots );
        }

        List< Object > slotsData = new ArrayList< Object >( );
        for ( String key : abilitySlots.keySet( ) )
        {
            for ( AbilitySlot slot : abilitySlots.get( key ) )
            {
                slotsData.add( slot.save( ) );
            }
        }

        if ( ! slotsData.isEmpty( ) )
        { data.put( "slots", slotsData.toArray( ) ); }

        // save abilities
        List< JSONOrderedObject > abilitiesData = new ArrayList< JSONOrderedObject >( );
        for ( String key : abilities.keySet( ) )
        {
            for ( String abilityID : abilities.get( key ).keySet( ) )
            {
                AbilityInstance instance = abilities.get( key ).get( abilityID );

                // racial and role abilities will be added back automatically
                if ( instance.race || instance.role ) continue;

                JSONOrderedObject abilityData = new JSONOrderedObject( );
                abilityData.put( "abilityID", instance.abilityID );
                abilityData.put( "levelObtained", instance.level );

                abilitiesData.add( abilityData );
            }
        }

        if ( ! abilitiesData.isEmpty( ) )
        { data.put( "abilities", abilitiesData.toArray( ) ); }

        return data;
    }

    public void load( SimpleJSONObject data, ReferenceHandler refHandler )
    {
        if ( data.containsKey( "abilities" ) )
        {
            for ( SimpleJSONArrayEntry entry : data.getArray( "abilities" ) )
            {
                SimpleJSONObject entryData = entry.getObject( );

                String id = entryData.get( "abilityID", null );
                int level = entryData.get( "levelObtained", 0 );

                loadAbility( Game.ruleset.getAbility( id ), level, false, false );
            }
        }

        if ( data.containsKey( "tempSlots" ) )
        {
            for ( SimpleJSONArrayEntry entry : data.getArray( "tempSlots" ) )
            {
                tempAbilitySlots.add( AbilitySlot.load( entry.getObject( ), refHandler, parent ) );
            }
        }

        if ( data.containsKey( "slots" ) )
        {
            for ( SimpleJSONArrayEntry entry : data.getArray( "slots" ) )
            {
                this.add( AbilitySlot.load( entry.getObject( ), refHandler, parent ) );
            }
        }
    }

    /**
     * Create a new CreatureAbilitySet with the specified parent.
     *
     * @param parent the Creature that is the parent (owner) of this list.
     */

    public CreatureAbilitySet( Creature parent )
    {
        this.parent = parent;
        abilities = new HashMap< String, Map< String, AbilityInstance > >( );
        activateableAbilities = new HashMap< String, Map< String, AbilityWithActiveCount > >( );

        abilitySlots = new HashMap< String, List< AbilitySlot > >( );

        tempAbilitySlots = new ArrayList< AbilitySlot >( 2 );

        listeners = new ArrayList< Listener >( 1 );
    }

    /**
     * Creates a new CreatureAbilitySet containing all the abilities contained
     * in the specified CreatureAbilitySet and with the specified parent.
     *
     * @param other  the CreatureAbilitySet to copy from
     * @param parent the Creature that is the parent of this list
     */

    public CreatureAbilitySet( CreatureAbilitySet other, Creature parent )
    {
        this.parent = parent;

        abilities = new HashMap< String, Map< String, AbilityInstance > >( );
        activateableAbilities = new HashMap< String, Map< String, AbilityWithActiveCount > >( );
        abilitySlots = new HashMap< String, List< AbilitySlot > >( );
        listeners = new ArrayList< Listener >( 1 );

        // copy abilities from other
        for ( String type : other.abilities.keySet( ) )
        {
            Map< String, AbilityInstance > newAbilitiesOfType = new HashMap< String, AbilityInstance >( other.abilities.get( type ) );
            this.abilities.put( type, newAbilitiesOfType );
        }

        // copy activateable abilities from other
        for ( String type : other.activateableAbilities.keySet( ) )
        {
            // AbilityWithActiveCount is mutable so do a deep copy
            Map< String, AbilityWithActiveCount > newAbilitiesOfType = new HashMap< String, AbilityWithActiveCount >( );
            for ( String abilityID : other.activateableAbilities.get( type ).keySet( ) )
            {
                AbilityWithActiveCount awac = other.activateableAbilities.get( type ).get( abilityID );

                newAbilitiesOfType.put( abilityID, new AbilityWithActiveCount( awac.abilityID, awac.count ) );
            }

            this.activateableAbilities.put( type, newAbilitiesOfType );
        }

        // copy abilitySlots from other
        for ( String type : other.abilitySlots.keySet( ) )
        {
            List< AbilitySlot > otherSlots = other.abilitySlots.get( type );

            List< AbilitySlot > newSlots = new ArrayList< AbilitySlot >( 2 );
            for ( AbilitySlot slot : otherSlots )
            {
                newSlots.add( new AbilitySlot( slot, parent ) );
            }

            this.abilitySlots.put( type, newSlots );
        }

        this.tempAbilitySlots = new ArrayList< AbilitySlot >( );
        for ( AbilitySlot slot : other.tempAbilitySlots )
        {
            this.tempAbilitySlots.add( new AbilitySlot( slot, parent ) );
        }
    }

    /**
     * Adds the specified Ability to this CreatureAbilitySet.  The parent
     * Creature will gain the Effects associated with the Ability.
     * <p>
     * Note that this method does not check to see if this CreatureAbilitySet
     * already contains the specified Ability.  Thus, an Ability can be
     * added to a CreatureAbilitySet more than once.
     *
     * @param ability the ability to add
     * @param level   the level at which the ability was added
     */

    public void add( Ability ability, int level )
    {
        addAbility( ability, level, false, false );
    }

    /**
     * Adds this Ability as a Racial Ability.  See {@link CreatureAbilitySet.AbilityInstance}
     *
     * @param ability the ability to add
     */

    public void addRacialAbility( Ability ability )
    {
        addAbility( ability, 1, false, true );
    }

    /**
     * Adds this Ability as a Role Ability.  See {@link CreatureAbilitySet.AbilityInstance}
     *
     * @param ability the ability to add
     * @param level   the level at which the ability was added
     */

    public void addRoleAbility( Ability ability, int level )
    {
        addAbility( ability, level, true, false );
    }

    /**
     * Adds the specified ability to this ability set.  Will not add any ability slots, even if the
     * ability is fixed.  Will run the onApply script.  This should only be used during loading
     *
     * @param ability the ability
     * @param level   the level the ability is added at
     */

    public void loadAbility( Ability ability, int level, boolean role, boolean race )
    {
        String type = ability.getType( );

        Map< String, AbilityInstance > abilitiesOfType;
        // add the specified ability to the main abilities list
        if ( ! abilities.containsKey( type ) )
        {
            //create a new LinkedList to put in the Map if one does not
            //already exist
            abilitiesOfType = new HashMap< String, AbilityInstance >( 4 );
            abilities.put( type, abilitiesOfType );
        }
        else
        {
            abilitiesOfType = abilities.get( type );
        }

        abilitiesOfType.put( ability.getID( ), new AbilityInstance( ability, level, race, role ) );

        Map< String, AbilityWithActiveCount > activateableAbilitiesOfType;
        // add the specified ability to the activateable abilities
        // list if it is activateable
        if ( ability.isActivateable( ) )
        {
            if ( ! activateableAbilities.containsKey( type ) )
            {
                activateableAbilitiesOfType = new HashMap< String, AbilityWithActiveCount >( 4 );
                activateableAbilities.put( type, activateableAbilitiesOfType );
            }
            else
            {
                activateableAbilitiesOfType = activateableAbilities.get( type );
            }

            activateableAbilitiesOfType.put( ability.getID( ), new AbilityWithActiveCount( ability ) );
        }

        if ( parent != null )
        {
            ability.executeFunction( ScriptFunctionType.onApply, parent );
        }
    }

    /*
     * Adds an ability.  If role is set to true, it will be added with the AbilityInstance
     * role set to true.  If race is set to true, it will be added with the AbilityInstance
     * race set to true.  These values are useful when saving the set of abilities
     */

    private void addAbility( Ability ability, int level, boolean role, boolean race )
    {
        loadAbility( ability, level, role, race );

        if ( ability.isActivateable( ) && ability.isFixed( ) )
        {
            AbilitySlot slot = new AbilitySlot( ability, parent );
            this.add( slot );
        }

        // notify listeners of added abilities
        notifyListeners( );
    }

    /**
     * Adds all abilities and AbilitySlots contained in the specified
     * CreatureAbilitySet to this CreatureAbilitySet.  Abilities already in
     * this List are added a second time.  AbilitySlots are added directly (not
     * copied), so changes to the source AbilitySlots will affect this Lists
     * AbilitySlots.
     *
     * @param other the CreatureAbilitySet to copy Abilities from
     */

    public void addAll( CreatureAbilitySet other )
    {
        if ( other == null ) return;

        for ( String type : other.abilities.keySet( ) )
        {
            for ( String abilityID : other.abilities.get( type ).keySet( ) )
            {
                AbilityInstance instance = other.abilities.get( type ).get( abilityID );
                addAbility( instance.getAbility( ), instance.level, instance.role, instance.race );
            }
        }

        for ( String type : other.abilitySlots.keySet( ) )
        {
            for ( AbilitySlot slot : other.abilitySlots.get( type ) )
            {
                // fixed slots have already been added when their ability was added
                if ( slot.isFixed( ) ) continue;

                add( slot );
            }
        }

        // notify listeners of added abilities
        notifyListeners( );
    }

    public void remove( Ability ability )
    {
        // remove from the activateable list
        if ( ability.isActivateable( ) )
        {
            Map< String, AbilityWithActiveCount > abilitiesOfType = this.activateableAbilities.get( ability.getType( ) );

            // the ability isn't in this list if the type isn't present
            if ( abilitiesOfType == null ) return;

            // the ability isn't present in the type list
            if ( ! abilitiesOfType.containsKey( ability.getID( ) ) ) return;

            abilitiesOfType.remove( ability.getID( ) );
        }

        Map< String, AbilityInstance > instances = this.abilities.get( ability.getType( ) );

        // the ability isn't in this list if the type isn't present
        if ( instances == null ) return;

        // the ability isn't present in the type list
        if ( ! instances.containsKey( ability.getID( ) ) ) return;

        instances.remove( ability.getID( ) );

        if ( abilitySlots.containsKey( ability.getType( ) ) )
        {

            if ( ability.isFixed( ) )
            {
                // remove any fixed ability slots containing the Ability
                Iterator< AbilitySlot > iter = abilitySlots.get( ability.getType( ) ).iterator( );
                while ( iter.hasNext( ) )
                {
                    if ( iter.next( ).getAbility( ) == ability ) iter.remove( );
                }
            }
            else
            {
                // remove from any AbilitySlots
                for ( AbilitySlot slot : abilitySlots.get( ability.getType( ) ) )
                {
                    if ( slot.getAbility( ) == ability ) slot.setAbility( ( String ) null );
                }
            }
        }

        // notify listeners of removed ability
        notifyListeners( );
    }

    /**
     * Removes all Abilities and AbilitySlots from this CreatureAbilitySet.
     * The list will be empty after this call returns.
     */

    public void clear( )
    {
        abilitySlots.clear( );
        abilities.clear( );
        activateableAbilities.clear( );

        // notify listeners of cleared state
        notifyListeners( );
    }


    /**
     * Returns whether or not this CreatureAbilitySet contains the specified
     * Ability.
     *
     * @param ability the Ability to check
     * @return true if this contains ability, false otherwise
     */

    public boolean has( Ability ability )
    {
        if ( ! abilities.containsKey( ability.getType( ) ) ) return false;

        return abilities.get( ability.getType( ) ).containsKey( ability.getID( ) );
    }

    /**
     * Returns whether or not this CreatureAbilitySet contains the Ability
     * with the specified ID.
     *
     * @param abilityID the ID String of the Ability
     * @return true if and only if this CreatureAbilitySet contains an Ability with the
     * specified ID.
     */

    public boolean has( String abilityID )
    {
        // the type of the ability is needed to find it so we must perform the
        // step of getting the ability
        Ability ability = Game.ruleset.getAbility( abilityID );

        if ( ability == null )
        {
            Logger.appendToWarningLog( "CreatureAbilitySet.has(String abilityID) was " +
                                               "called with nonexistant abilityID" + abilityID );
            return false;
        }

        return has( ability );
    }

    /**
     * Returns a List of all the types of abilities contained in this
     * CreatureAbilitySet.
     *
     * @return the List of all types of Abilities in this CreatureAbilitySet
     */

    public List< String > getAllTypes( )
    {
        List< String > types = new ArrayList< String >( abilities.size( ) );

        for ( String type : abilities.keySet( ) )
        {
            types.add( type );
        }

        return types;
    }

    /**
     * Returns a List of all the types of abilities contained in this CreatureAbilitySet
     * that are activateable.
     *
     * @return a List of all activateable types in this CreatureAbilitySet
     */

    public List< String > getActivateableTypes( )
    {
        List< String > types = new ArrayList< String >( activateableAbilities.size( ) );

        for ( String type : activateableAbilities.keySet( ) )
        {
            types.add( type );
        }

        return types;
    }

    /**
     * Returns a List of all activateable abilities of all types currently owned
     * by this CreatureAbilitySet
     *
     * @return a List of all activateable abilities in this CreatureAbilitySet
     */

    public List< Ability > getActivateableAbilities( )
    {
        List< Ability > abilities = new ArrayList< Ability >( );

        for ( String type : activateableAbilities.keySet( ) )
        {
            for ( String abilityID : activateableAbilities.get( type ).keySet( ) )
            {
                abilities.add( Game.ruleset.getAbility( abilityID ) );
            }
        }

        return abilities;
    }

    /**
     * Adds the specified AbilitySlot to this CreatureAbilitySet.  This will
     * enable the parent Creature to ready one additional Ability.
     *
     * @param slot the AbilitySlot to add
     */

    public void add( AbilitySlot slot )
    {
        String type = slot.getType( );

        if ( abilitySlots.containsKey( type ) )
        {
            abilitySlots.get( type ).add( slot );
        }
        else
        {
            List< AbilitySlot > listOfSlots = new ArrayList< AbilitySlot >( 2 );
            listOfSlots.add( slot );
            abilitySlots.put( type, listOfSlots );
        }

        if ( ! slot.isEmpty( ) )
        {
            Ability ability = slot.getAbility( );

            AbilityWithActiveCount awac = this.activateableAbilities.get( ability.getType( ) ).get( ability.getID( ) );
            awac.count++;
        }

        // notify listeners of added ability slot
        notifyListeners( );
    }

    /**
     * Returns the number of AbilitySlots of the specified type
     * contained in this CreatureAbilitySet
     *
     * @param type the type of AbilitySlot
     * @return the number of AbilitySlots in this CreatureAbilitySet
     */

    public int getNumberOfSlots( String type )
    {
        if ( abilitySlots.containsKey( type ) )
        {
            return abilitySlots.get( type ).size( );
        }
        else
        {
            return 0;
        }
    }

    /**
     * Returns a List of all Abilities contained in this CreatureAbilitySet
     * of the specified type.  If there are no such Abilities, an empty
     * List is returned.
     *
     * @param type the type of the Abilities
     * @return a List of Abilities of the specified type
     */

    public List< Ability > getAbilitiesOfType( String type )
    {
        if ( ! abilities.containsKey( type ) ) return new ArrayList< Ability >( 0 );

        List< Ability > abilitiesOfType = new ArrayList< Ability >( abilities.get( type ).size( ) );

        for ( String abilityID : abilities.get( type ).keySet( ) )
        {
            abilitiesOfType.add( abilities.get( type ).get( abilityID ).getAbility( ) );
        }

        return abilitiesOfType;
    }

    /**
     * Returns a List containing all Abilities of all types currently owned by this
     * CreatureAbilitySet
     *
     * @return a List of all Abilities owned by this List
     */

    public List< Ability > getAllAbilities( )
    {
        List< Ability > abilities = new ArrayList< Ability >( );

        for ( String type : this.abilities.keySet( ) )
        {
            for ( String abilityID : this.abilities.get( type ).keySet( ) )
            {
                abilities.add( this.abilities.get( type ).get( abilityID ).getAbility( ) );
            }
        }

        return abilities;
    }

    /**
     * Gets a list of all abilities with associated role, race, and level obtained status
     * in this set
     *
     * @return the list of all ability instances
     */

    public List< AbilityInstance > getAllAbilityInstances( )
    {
        List< AbilityInstance > instances = new ArrayList< AbilityInstance >( );

        for ( String type : abilities.keySet( ) )
        {
            for ( String abilityID : abilities.get( type ).keySet( ) )
            {
                instances.add( abilities.get( type ).get( abilityID ) );
            }
        }

        return instances;
    }

    /**
     * Returns a List of AbilityInstances contained in this CreatureAbilitySet
     * of the specified type.  The AbilityInstance stores the Ability and also
     * the data on how the Ability was added, such as the level.
     *
     * @param type the type of Abilities
     * @return a List of AbilityInstances of the specified type
     */

    public List< AbilityInstance > getAbilityInstancesOfType( String type )
    {
        if ( ! abilities.containsKey( type ) ) return new ArrayList< AbilityInstance >( 0 );

        List< AbilityInstance > abilitiesOfType = new ArrayList< AbilityInstance >( abilities.get( type ).size( ) );

        for ( String abilityID : abilities.get( type ).keySet( ) )
        {
            abilitiesOfType.add( abilities.get( type ).get( abilityID ) );
        }

        return abilitiesOfType;
    }

    /**
     * Returns a List of all AbilitySlots contained in this CreatureAbilitySet
     * of the specified type.  If there are no such AbilitySlots, an empty List
     * is returned.
     *
     * @param type the type of the AbilitySlots
     * @return a List of AbilitySlots of the specified type
     */

    public List< AbilitySlot > getSlotsOfType( String type )
    {
        if ( ! this.abilitySlots.containsKey( type ) ) return new ArrayList< AbilitySlot >( 0 );

        List< AbilitySlot > slotsOfType = new ArrayList< AbilitySlot >( abilitySlots.get( type ).size( ) );

        for ( AbilitySlot slot : abilitySlots.get( type ) )
        {
            slotsOfType.add( slot );
        }

        return slotsOfType;
    }

    /**
     * Returns the number of AbilitySlots of the specified type that are "empty",
     * meaning they do not currently ready any ability.  Returns 0 if there
     * are no empty slots.
     *
     * @param type the type of the AbilitySlots
     * @return the number of empty AbilitySlots
     */

    public int getNumberOfEmptySlotsOfType( String type )
    {
        if ( ! this.abilitySlots.containsKey( type ) ) return 0;

        int slotsEmpty = 0;

        for ( AbilitySlot slot : abilitySlots.get( type ) )
        {
            if ( slot.isEmpty( ) ) slotsEmpty++;
        }

        return slotsEmpty;
    }

    /**
     * Returns a List of all AbilitySlots with the specified type that are empty,
     * meaning that they do not currently ready any ability.  Returns an empty list
     * if there are no such slots.
     *
     * @param type the type of the AbilitySlots
     * @return the List of empty AbilitySlots
     */

    public List< AbilitySlot > getEmptySlotsOfType( String type )
    {
        List< AbilitySlot > slots = new ArrayList< AbilitySlot >( );

        if ( ! abilitySlots.containsKey( type ) ) return slots;

        for ( AbilitySlot slot : abilitySlots.get( type ) )
        {
            if ( slot.isEmpty( ) ) slots.add( slot );
        }

        return slots;
    }

    /**
     * Returns the first AbilitySlot found of the specified type that is
     * "empty", meaning it does not ready an Ability.  This will be the first
     * slot in the List of slots that is empty.  Returns null if no empty
     * AbilitySlot is found.
     *
     * @param type the type of the AbilitySlots to search
     * @return the first empty AbilitySlot of the specified type found
     */

    public AbilitySlot getFirstEmptySlotOfType( String type )
    {
        if ( ! this.abilitySlots.containsKey( type ) ) return null;

        for ( AbilitySlot slot : abilitySlots.get( type ) )
        {
            if ( slot.isEmpty( ) ) return slot;
        }

        return null;
    }

    /**
     * Returns the first slot found that is readying the specified ability, or
     * null if no such slot exists
     *
     * @param abilityID
     * @return the first slot found readying the ability
     */

    public AbilitySlot getSlotWithReadiedAbility( String abilityID )
    {
        return getSlotWithReadiedAbility( Game.ruleset.getAbility( abilityID ) );
    }

    /**
     * Returns the first slot found that is readying the specified ability, or
     * null if no such slot exists
     *
     * @param ability
     * @return the first slot found readying the ability
     */

    public AbilitySlot getSlotWithReadiedAbility( Ability ability )
    {
        if ( ! abilitySlots.containsKey( ability.getType( ) ) ) return null;

        for ( AbilitySlot slot : abilitySlots.get( ability.getType( ) ) )
        {
            if ( slot.getAbility( ) == ability )
            { return slot; }
        }

        return null;
    }

    /**
     * Returns a List of all AbilitySlots in this CreatureAbilitySet that
     * currently ready the Ability with the specified ID.  If no such slots exist,
     * returns an empty List.
     *
     * @param abilityID the Ability ID to search for
     * @return the List of AbilitySlots readying the Ability
     */

    public List< AbilitySlot > getSlotsWithReadiedAbility( String abilityID )
    {
        return getSlotsWithReadiedAbility( Game.ruleset.getAbility( abilityID ) );
    }

    /**
     * Returns a List of all AbilitySlots in this CreatureAbilitySet that
     * currently ready the specified Ability.  If no such slots exist, returns
     * an empty List.
     *
     * @param ability the Ability to search for
     * @return the List of AbilitySlots readying the Ability
     */

    public List< AbilitySlot > getSlotsWithReadiedAbility( Ability ability )
    {
        List< AbilitySlot > slots = new ArrayList< AbilitySlot >( );

        if ( ! abilitySlots.containsKey( ability.getType( ) ) ) return slots;

        for ( AbilitySlot slot : abilitySlots.get( ability.getType( ) ) )
        {
            if ( slot.getAbility( ) == ability ) slots.add( slot );
        }

        return slots;
    }

    /**
     * Returns the number of Ability Slots currently readying the specified ability
     *
     * @param ability the ability to look for
     * @return the number of Ability Slots readying the ability
     */

    public int getNumberOfSlotsWithReadiedAbility( Ability ability )
    {
        if ( ! abilitySlots.containsKey( ability.getType( ) ) ) return 0;

        int count = 0;
        for ( AbilitySlot slot : abilitySlots.get( ability.getType( ) ) )
        {
            if ( slot.getAbility( ) == ability ) count++;
        }


        return count;
    }

    /**
     * Immediately cancels all effects being tracked by ability slots for this creature.
     * This is used at the end of combat for creatures that were killed during the combat
     */

    public void cancelAllEffects( )
    {
        for ( String type : abilitySlots.keySet( ) )
        {
            for ( AbilitySlot slot : abilitySlots.get( type ) )
            {
                slot.cancelAllEffects( );
            }
        }

        for ( AbilitySlot slot : tempAbilitySlots )
        {
            slot.cancelAllEffects( );
        }
    }

    /**
     * Cancels all currently active aura effects, ending them immediately
     *
     * @return the list of all ability slots that were deactivated as a result
     * of canceling their auras, and can be reactivated
     */

    public List< AbilitySlot > cancelAllAuras( )
    {
        List< AbilitySlot > canceledSlots = new ArrayList< AbilitySlot >( );

        for ( String type : abilitySlots.keySet( ) )
        {
            for ( AbilitySlot slot : abilitySlots.get( type ) )
            {
                if ( slot.cancelAllAuras( ) ) canceledSlots.add( slot );
            }
        }

        for ( AbilitySlot slot : tempAbilitySlots )
        {
            // don't allow reactivation of temp ability slots
            slot.cancelAllAuras( );
        }

        return canceledSlots;
    }

    /**
     * Tells this abilitySet to track the specified AbilitySlot as a temporary
     * AbilitySlot (generally created from an item).  This ability slot will have
     * rounds elapsed as a normal slot until it has no more active effects, at which
     * point it will be removed from this set and no longer tracked
     *
     * @param slot the AbilitySlot to track
     */

    public void trackTempAbilitySlot( AbilitySlot slot )
    {
        tempAbilitySlots.add( slot );
    }

    /**
     * Updates all cooldowns and durations for all AbilitySlots with the specified number
     * of rounds passed
     *
     * @return true if this CreatureAbilitySet has an AbilitySlot or temp AbilitySlot that is
     * active after elapsing the round, false otherwise.  So, this method only returns true
     * if at least one AbilitySlot has active effects and/or at least one temp ability slot is being
     * tracked after this method finishes elapsing rounds
     */

    public boolean elapseTime( int rounds )
    {
        boolean hasActiveEffects = false;

        for ( String type : abilitySlots.keySet( ) )
        {
            for ( AbilitySlot slot : abilitySlots.get( type ) )
            {
                slot.elapseRounds( rounds );

                hasActiveEffects = hasActiveEffects || slot.hasActiveEffects( );
            }
        }

        Iterator< AbilitySlot > iter = tempAbilitySlots.iterator( );
        while ( iter.hasNext( ) )
        {
            AbilitySlot slot = iter.next( );

            slot.elapseRounds( rounds );

            if ( ! slot.hasActiveEffects( ) )
            {
                iter.remove( );
            }
        }

        return hasActiveEffects || ( ! tempAbilitySlots.isEmpty( ) );
    }

    /**
     * Adds an Ability to each empty AbilitySlot so that all slots are readied with
     * an Ability.  The Abilities added are valid for each slot but are otherwise
     * not selected in any specific manner.
     */

    public void fillEmptySlots( )
    {
        // iterate through the list of types
        for ( String type : activateableAbilities.keySet( ) )
        {

            // for each type, get the list of empty slots
            List< AbilitySlot > emptySlots = getEmptySlotsOfType( type );

            // get the sorted list of activateable abilities of the specified type
            List< AbilityWithActiveCount > awacs =
                    new ArrayList< AbilityWithActiveCount >( activateableAbilities.get( type ).size( ) );
            awacs.addAll( activateableAbilities.get( type ).values( ) );

            // remove all fixed abilities from the list, as these cannot be added
            Iterator< AbilityWithActiveCount > awacIter = awacs.iterator( );
            while ( awacIter.hasNext( ) )
            {
                AbilityWithActiveCount awac = awacIter.next( );
                Ability ability = Game.ruleset.getAbility( awac.abilityID );
                if ( ability.isFixed( ) )
                {
                    awacIter.remove( );
                }
            }

            Collections.sort( awacs );

            // if we have no abilities of this type, we cannot fill the slots of this type
            if ( awacs.isEmpty( ) ) continue;

            awacIter = awacs.iterator( );

            // iterate through the list of empty slots
            // fill each slot with the least used Ability (the one with the lowest count)
            // we can iterate through the list of Abilities as many times as needed
            for ( AbilitySlot slot : emptySlots )
            {
                AbilityWithActiveCount awac = awacIter.next( );

                slot.setAbility( awac.abilityID );
                awac.count++;

                // reset the iterator to the beginning of the list if
                // we have ran out of abilities
                if ( ! awacIter.hasNext( ) )
                {
                    awacIter = awacs.iterator( );
                }
            }
        }
    }

    /**
     * Readies the specified Ability in the specified AbilitySlot.  If an Ability
     * is already readied in the slot, that Ability is removed if possible.
     *
     * @param ability the Ability to ready
     * @param slot    the AbilitySlot to ready the Ability in
     */

    public void readyAbilityInSlot( Ability ability, AbilitySlot slot )
    {
        if ( ability != null && ! ability.isActivateable( ) )
        {
            Logger.appendToWarningLog( "Attempted to ready ability " + ability.getID( ) + " on " +
                                               parent.getTemplate( ).getID( ) + " but ability is not activateable." );
            return;
        }

        if ( ability != null && ability.isFixed( ) )
        {
            Logger.appendToWarningLog( "Attempted to ready ability " + ability.getID( ) + " on " +
                                               parent.getTemplate( ).getID( ) + " but ability is fixed." );
            return;
        }

        String oldAbilityID = slot.getAbilityID( );
        String newAbilityID = ability != null ? ability.getID( ) : null;

        // update the counts in the AbilityWithActiveCount set
        AbilityWithActiveCount oldAwac = activateableAbilities.get( slot.getType( ) ).get( oldAbilityID );
        AbilityWithActiveCount newAwac = activateableAbilities.get( slot.getType( ) ).get( newAbilityID );

        if ( oldAwac != null ) oldAwac.count--;
        if ( newAwac != null ) newAwac.count--;

        slot.setAbility( ability );
    }

    /**
     * Readies the specified Ability in the first empty available AbilitySlot.  If
     * there are no empty AbilitySlots, no AbilitySlots are modified, but a warning
     * is logged.
     *
     * @param ability the Ability to ready
     */

    public void readyAbilityInFirstEmptySlot( Ability ability )
    {
        if ( ! ability.isActivateable( ) )
        {
            Logger.appendToWarningLog( "Attempted to ready ability " + ability.getID( ) + " on " +
                                               parent.getTemplate( ).getID( ) + " but ability is not activateable." );
            return;
        }

        AbilitySlot slot = this.getFirstEmptySlotOfType( ability.getType( ) );
        if ( slot == null )
        {
            Logger.appendToWarningLog( "Attempted to ready ability " + ability.getID( ) + " on " +
                                               parent.getTemplate( ).getID( ) + " but no empty slots available." );
            return;
        }

        readyAbilityInSlot( ability, slot );
    }

    /**
     * All abilities in this CreatureAbilitySet that are flagged as being racial
     * are removed.
     */

    public void removeRacialAbilities( )
    {
        List< Ability > abilitiesToRemove = new ArrayList< Ability >( );

        for ( String type : abilities.keySet( ) )
        {
            for ( String abilityID : abilities.get( type ).keySet( ) )
            {
                AbilityInstance instance = abilities.get( type ).get( abilityID );

                if ( instance.isRacialAbility( ) ) abilitiesToRemove.add( instance.getAbility( ) );
            }
        }

        for ( Ability ability : abilitiesToRemove )
        {
            remove( ability );
        }
    }

    /**
     * Adds the specified listener to the list of listeners that are notified
     * when an ability or ability slot is added or removed
     *
     * @param listener the listener to add
     */

    public void addListener( Listener listener )
    {
        listeners.add( listener );
    }

    /**
     * Removes the specified listener
     *
     * @param listener the listener to remove
     */

    public void removeListener( Listener listener )
    {
        listeners.remove( listener );
    }

    private void notifyListeners( )
    {
        for ( Listener listener : listeners )
        {
            listener.abilitySetModified( );
        }
    }

    /**
     * Returns a new {@link AIAbilitySlotSet} using the set of AbilitySlots
     * contained in this CreatureAbilitySet.  Used by AI scripts to get an
     * easily usable set of ability slots.
     *
     * @return an AIAbilitySlotSet created from the AbilitySlots in this
     * CreatureAbilityList
     */

    public AIAbilitySlotSet createAISet( )
    {
        return new AIAbilitySlotSet( abilitySlots );
    }

    /**
     * Returns the name associated with the highest priority upgrade available for this ability
     * to the parent creature
     *
     * @param abilityID
     * @return the upgraded name
     */

    public String getUpgradedName( String abilityID )
    {
        Ability ability = Game.ruleset.getAbility( abilityID );

        return ability.getUpgradedName( this.parent );
    }

    /**
     * Returns the icon associated with the highest priority upgrade available for this ability to
     * the parent creature
     *
     * @param abilityID
     * @return the upgraded icon
     */

    public Icon getUpgradedIcon( String abilityID )
    {
        Ability ability = Game.ruleset.getAbility( abilityID );

        return ability.getUpgradedIcon( this.parent );
    }

    private class AbilityWithActiveCount implements Comparable< AbilityWithActiveCount >
    {
        private final String abilityID;
        private int count;

        private AbilityWithActiveCount( Ability ability )
        {
            this.abilityID = ability.getID( );
            this.count = 0;
        }

        private AbilityWithActiveCount( String abilityID, int count )
        {
            this.abilityID = abilityID;
            this.count = count;
        }

        @Override
        public int compareTo( AbilityWithActiveCount other )
        {
            return count - other.count;
        }
    }

    /**
     * An AbilityInstance stores an ability and what level that ability
     * was added at for the parent creature.  In addition, whether this ability
     * was added as a result of the parent's race (Racial Ability) or role (Role Ability)
     * is stored.  This assists in verifying and saving characters.  The instances of
     * this class are immutable.
     */

    public class AbilityInstance
    {
        private final String abilityID;
        private final int level;
        private final boolean race;
        private final boolean role;

        private AbilityInstance( Ability ability, int level, boolean race, boolean role )
        {
            this.abilityID = ability.getID( );
            this.level = level;
            this.race = race;
            this.role = role;
        }

        /**
         * Gets the Ability stored in this AbilityInstance
         *
         * @return the Ability stored in this AbilityInstance
         */

        public Ability getAbility( ) { return Game.ruleset.getAbility( abilityID ); }

        /**
         * Returns the level that the Ability stored in this AbilityInstance was added at
         *
         * @return the level that the Ability stored in this AbilityInstance was added at
         */

        public int getLevel( ) { return level; }

        /**
         * Returns true if this Ability was added as a result of the parent Creature's
         * race.
         *
         * @return true if and only if this Ability is a racial ability.
         */

        public boolean isRacialAbility( ) { return race; }

        /**
         * Return true if this Ability was added as a direct result of gaining a role level.
         * This does not include Abilities which were selected from a list upon gaining a level.
         *
         * @return true if and only if this is a role ability
         */

        public boolean isRoleAbility( ) { return role; }

        @Override
        public String toString( )
        {
            if ( role )
            {
                return "(Role) " + getAbility( ).getName( );
            }
            else if ( race )
            {
                return "(Racial) " + getAbility( ).getName( );
            }
            else
            {
                return getAbility( ).getName( );
            }
        }
    }

    /**
     * The interface for a widget that wants to be notified
     * of changes to the basic internal state of this CreatureAbilitySet, namely
     * the addition or removal of an AbilitySlot or Ability
     *
     * @author Jared Stephen
     */

    public interface Listener
    {
        /**
         * Called whenever an Ability or AbilitySlot is added or removed from
         * this CreatureAbilitySet
         */
        public void abilitySetModified( );
    }
}
