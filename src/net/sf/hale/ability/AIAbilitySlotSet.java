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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.hale.Game;
import net.sf.hale.ability.Ability.ActionType;
import net.sf.hale.ability.Ability.GroupType;
import net.sf.hale.ability.Ability.RangeType;
import net.sf.hale.util.Logger;
import net.sf.hale.widgets.RightClickMenu;
import net.sf.hale.widgets.RightClickMenuLevel;

/**
 * A class for efficient storage of the set of activateable ability slots
 * contained in a given Creature's CreatureAbilitySet.  This class contains
 * many helper functions to allow the Creature's AI to easily and efficiently
 * pick the best AbilitySlot to activate in any given situation.
 *
 * @author Jared Stephen
 */

public class AIAbilitySlotSet
{
    // list of all ability slots sorted by their ai priority times their ai power
    private List< AbilitySlot > sortedSlots;

    /**
     * Creates a new AIAbilitySlotSet from the AbilitySlots contained in the
     * specified Map.  Only AbilitySlots that have a non null ActionType,
     * GroupType, or RangeType and AbilitySlots that are currently
     * activateable by their parent will be included in this set.
     *
     * @param slots the set of AbilitySlots that will make up this
     *              AIAbilitySlotSet.  The top level of the map is keyed on Ability Type.
     *              Each contained type then contains a list of all AbilitySlots readying
     *              Abilities of that type.
     */

    public AIAbilitySlotSet( Map< String, List< AbilitySlot > > slots )
    {
        sortedSlots = new ArrayList< AbilitySlot >( );

        for ( String type : slots.keySet( ) )
        {
            for ( AbilitySlot slot : slots.get( type ) )
            {
                if ( ! slot.canActivate( ) && ! slot.canDeactivate( ) ) continue;
                sortedSlots.add( slot );
            }
        }

        Collections.sort( sortedSlots, new AbilityComparator( ) );
    }

    /**
     * Returns the total number of ability slots in this AI Ability Slot set
     *
     * @return the total number of ability slots
     */

    public int getNumAbilities( )
    {
        return sortedSlots.size( );
    }

    /**
     * Returns a list of all AbilitySlots in this AIAbilitySlotSet, sorted by
     * ai priority times ai power
     *
     * @return a sorted list of all AbilitySlots
     */

    public List< AbilitySlot > getAllAbilitySlots( )
    {
        return sortedSlots;
    }

    /**
     * Returns a list of all ability slots in this set with one of the specified action types
     *
     * @param actionTypes the array of action types
     * @return all ability slots with one of the specified action types
     */

    public List< AbilitySlot > getWithActionTypes( String[] actionTypes )
    {
        // first construct the set of specified action types
        Set< ActionType > types = new HashSet< ActionType >( );

        for ( String typeString : actionTypes )
        {
            try
            {
                types.add( ActionType.valueOf( typeString ) );
            }
            catch ( Exception e )
            {
                Logger.appendToWarningLog( "Error in AI ability set, action type " + typeString + " not found." );
            }
        }

        // now look through the list of sorted slots for slots with the right action type
        List< AbilitySlot > slots = new ArrayList< AbilitySlot >( );

        for ( AbilitySlot slot : this.sortedSlots )
        {
            if ( types.contains( slot.getAbility( ).getActionType( ) ) )
            {
                slots.add( slot );
            }
        }

        return slots;
    }

    /**
     * Returns a List of all AbilitySlots in this AIAbilitySlotSet that have
     * the specified ActionType.  The returned list can be modified if it is
     * non-empty, and the modifications will affect this AIAbilitySlotSet.
     *
     * @param actionTypeID the ID (enum name) of the ActionType
     * @return the List of AbilitySlots with the specified ActionType
     */

    public List< AbilitySlot > getWithActionType( String actionTypeID )
    {
        ActionType actionType;
        try
        {
            actionType = ActionType.valueOf( actionTypeID );
        }
        catch ( Exception e )
        {
            Logger.appendToWarningLog( "Error in AI ability set, action type " + actionTypeID + " not found." );
            return Collections.emptyList( );
        }

        List< AbilitySlot > slots = new ArrayList< AbilitySlot >( );

        for ( AbilitySlot slot : this.sortedSlots )
        {
            if ( slot.getAbility( ).getActionType( ) == actionType )
            { slots.add( slot ); }
        }

        return slots;
    }

    /**
     * Returns a List of all AbilitySlots in this AIAbilitySlotSet that have
     * the specified GroupType.  The returned list can be modified if it is
     * non-empty, and the modifications will affect this AIAbilitySlotSet.
     *
     * @param groupTypeID the ID (enum name) of the GroupType
     * @return the List of AbilitySlots with the specified GroupType
     */

    public List< AbilitySlot > getWithGroupType( String groupTypeID )
    {
        GroupType groupType;
        try
        {
            groupType = GroupType.valueOf( groupTypeID );
        }
        catch ( Exception e )
        {
            Logger.appendToWarningLog( "Error in AI ability set, group type " + groupTypeID + " not found." );
            return Collections.emptyList( );
        }

        List< AbilitySlot > slots = new ArrayList< AbilitySlot >( );

        for ( AbilitySlot slot : this.sortedSlots )
        {
            if ( slot.getAbility( ).getGroupType( ) == groupType )
            { slots.add( slot ); }
        }

        return slots;
    }

    /**
     * Returns a List of all AbilitySlots in this AIAbilitySlotSet that have
     * the specified RangeType.  The returned list can be modified if it is
     * non-empty, and the modifications will affect this AIAbilitySlotSet.
     *
     * @param rangeTypeID the ID (enum name) of the RangeType
     * @return the List of AbilitySlots with the specified RangeType
     */

    public List< AbilitySlot > getWithRangeType( String rangeTypeID )
    {
        RangeType rangeType;
        try
        {
            rangeType = RangeType.valueOf( rangeTypeID );
        }
        catch ( Exception e )
        {
            Logger.appendToWarningLog( "Error in AI ability set, range type " + rangeTypeID + " not found." );
            return Collections.emptyList( );
        }

        List< AbilitySlot > slots = new ArrayList< AbilitySlot >( );

        for ( AbilitySlot slot : this.sortedSlots )
        {
            if ( slot.getAbility( ).getRangeType( ) == rangeType )
            { slots.add( slot ); }
        }

        return slots;
    }

    /**
     * Sorts the specified list of ability slots to be in order with the range type of the
     * specified type first, and subsequent entries ordered by their closeness to the specified range
     *
     * @param slots the list of slots to sort
     * @param order "CLOSEST" to sort with shortest distance range types first, "FURTHEST" to sort with
     *              longest range types first
     */

    public void sortByRangeType( List< AbilitySlot > slots, String order )
    {
        if ( order.equals( "CLOSEST" ) )
        {
            Collections.sort( slots, new RangeSorter( + 1 ) );
        }
        else if ( order.equals( "FURTHEST" ) )
        {
            Collections.sort( slots, new RangeSorter( - 1 ) );
        }
        else
        {
            throw new IllegalArgumentException( "Range type sort order must be either CLOSEST or FURTHEST" );
        }
    }

    /**
     * Sorts the specified list of ability slots to be in the specified order
     *
     * @param slots the list of slots to sort
     * @param order "SINGLE" to sort with single targeted abilities first, "MULTIPLE" to sort with
     *              multiple targeted abilities first
     */

    public void sortByGroupType( List< AbilitySlot > slots, String order )
    {
        if ( order.equals( "SINGLE" ) )
        {
            Collections.sort( slots, new GroupSorter( + 1 ) );
        }
        else if ( order.equals( "MULTIPLE" ) )
        {
            Collections.sort( slots, new GroupSorter( - 1 ) );
        }
        else
        {
            throw new IllegalArgumentException( "Group type sort order must be either SINGLE or MULTIPLE" );
        }
    }

    /**
     * Calls the standard AbilityActivateCallback for the given AbilitySlot.
     * If the AbilitySlot creates a targeter in its onActivate script function,
     * then returns that Targeter.  If the script opens a menu in its onActivate,
     * selects the first menu selection that is found in the passed array.
     * If no menu selection is found matching one of the array entries, selects a menu item randomly instead
     * This method is used by AI scripts to activate AbilitySlots.
     *
     * @param slot           the AbilitySlot to activate
     * @param menuSelections the menu selections to make if a menu is opened, in order of priority
     * @return the Targeter created by the onActivate script function
     */

    public Targeter activateAndGetTargeter( AbilitySlot slot, String[] menuSelections )
    {
        Targeter curTargeter = tryActivateSlotAndGetTargeter( slot );

        RightClickMenu menu = Game.mainViewer.getMenu( );

        if ( curTargeter == null && ( menu.isOpen( ) || menu.isOpening( ) ) )
        {
            synchronized ( menu )
            {
                // don't show the menu popup if it hasn't opened yet
                // or close it if it has
                menu.hide( );
            }

            RightClickMenuLevel level = menu.getLowestMenuLevel( );

            // attempt to select the specified menu selection
            boolean activated = false;
            for ( int j = 0; j < menuSelections.length; j++ )
            {
                String menuSelection = menuSelections[ j ];

                for ( int i = 0; i < level.getNumSelections( ); i++ )
                {
                    String text = level.getSelectionText( i );

                    if ( text.equals( menuSelection ) )
                    {
                        level.activateSelection( i );
                        activated = true;
                        break;
                    }
                }

                if ( activated )
                { break; }
            }

            // fall back to a random selection if needed
            if ( ! activated )
            {
                level.activateSelection( Game.dice.rand( 0, level.getNumSelections( ) - 1 ) );
            }
        }

        return Game.areaListener.getTargeterManager( ).getCurrentTargeter( );
    }

    /**
     * Calls the standard AbilityActivateCallback for the given AbilitySlot.
     * If the AbilitySlot creates a targeter in its onActivate script function,
     * then returns that Targeter.  If the script opens a menu in its onActivate,
     * selects the specified menu selection and then attempts to return the targeter from that.
     * If no menu selection is found with the specified text, selects a menu item randomly instead
     * This method is used by AI scripts to activate AbilitySlots.
     *
     * @param slot          the AbilitySlot to activate
     * @param menuSelection the menu selection to make if a menu is opened
     * @return the Targeter created by the onActivate script function
     */

    public Targeter activateAndGetTargeter( AbilitySlot slot, String menuSelection )
    {
        String[] selections = new String[ 1 ];
        selections[ 0 ] = menuSelection;

        return activateAndGetTargeter( slot, menuSelection );
    }

    /**
     * Calls the standard AbilityActivateCallback for the given AbilitySlot.
     * If the AbilitySlot creates a targeter in its onActivate script function,
     * then returns that Targeter.  If the script opens a menu in its onActivate,
     * make a random menu selection and then attempts to return the targeter from that
     * This method is used by AI scripts to activate AbilitySlots.
     *
     * @param slot the AbilitySlot to activate
     * @return the Targeter created by the onActivate script function
     */

    public Targeter activateAndGetTargeter( AbilitySlot slot )
    {
        Targeter curTargeter = tryActivateSlotAndGetTargeter( slot );

        RightClickMenu menu = Game.mainViewer.getMenu( );

        if ( curTargeter == null && ( menu.isOpen( ) || menu.isOpening( ) ) )
        {
            synchronized ( menu )
            {
                // don't show the menu popup if it hasn't opened yet
                // or close it if it has
                menu.hide( );
            }

            RightClickMenuLevel level = menu.getLowestMenuLevel( );

            int max = level.getNumSelections( );

            level.activateSelection( Game.dice.rand( 0, max - 1 ) );
        }

        return Game.areaListener.getTargeterManager( ).getCurrentTargeter( );
    }

    private Targeter tryActivateSlotAndGetTargeter( AbilitySlot slot )
    {
        if ( slot.canActivate( ) )
        {
            new AbilityActivateCallback( slot, ScriptFunctionType.onActivate ).run( );
        }
        else if ( slot.canDeactivate( ) )
        {
            new AbilityActivateCallback( slot, ScriptFunctionType.onDeactivate ).run( );
        }

        return Game.areaListener.getTargeterManager( ).getCurrentTargeter( );
    }

    private class GroupSorter implements Comparator< AbilitySlot >
    {
        private int sense;

        /**
         * creates a new groupsorter
         *
         * @param sense +1 to sort single to multiple, -1 to sort multiple to single
         */

        private GroupSorter( int sense )
        {
            this.sense = sense;
        }

        @Override
        public int compare( AbilitySlot o1, AbilitySlot o2 )
        {
            return sense * ( o1.getAbility( ).getUpgradedGroupType( o1.getParent( ) ).ordinal( ) -
                    o2.getAbility( ).getUpgradedGroupType( o2.getParent( ) ).ordinal( ) );
        }
    }

    private class RangeSorter implements Comparator< AbilitySlot >
    {
        private int sense;

        /**
         * creates a new rangesorter
         *
         * @param sense +1 to sort closest to furthest, -1 to sort furthest to closest
         */

        private RangeSorter( int sense )
        {
            this.sense = sense;
        }

        @Override
        public int compare( AbilitySlot arg0, AbilitySlot arg1 )
        {
            return sense * ( arg0.getAbility( ).getUpgradedRangeType( arg0.getParent( ) ).ordinal( ) -
                    arg1.getAbility( ).getUpgradedRangeType( arg1.getParent( ) ).ordinal( ) );
        }

    }

    private class AbilityComparator implements Comparator< AbilitySlot >
    {
        @Override
        public int compare( AbilitySlot a, AbilitySlot b )
        {
            return b.getAbility( ).getAIPriority( ) * b.getAbility( ).getUpgradedAIPower( b.getParent( ) ) -
                    a.getAbility( ).getAIPriority( ) * a.getAbility( ).getUpgradedAIPower( a.getParent( ) );
        }
    }
}
