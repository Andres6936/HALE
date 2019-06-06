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

package net.sf.hale.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.hale.Cutscene;
import net.sf.hale.DifficultyManager;
import net.sf.hale.ability.Ability;
import net.sf.hale.ability.AbilitySelectionList;
import net.sf.hale.entity.SavedItemList;
import net.sf.hale.quickbar.QuickbarGroup;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.util.Logger;
import net.sf.hale.util.SimpleJSONArray;
import net.sf.hale.util.SimpleJSONArrayEntry;
import net.sf.hale.util.SimpleJSONObject;
import net.sf.hale.util.SimpleJSONParser;

/**
 * The Ruleset stores data relating to the rules of the campaign.  The includes
 * abilities, races, roles, skills, factions, damage types, base weapons, armor types,
 * item lists, ability lists, racial types, item qualities, cutscenes, and other rules
 *
 * @author Jared Stephen
 */

public class Ruleset
{
    public enum Gender
    {
        Female,
        Male;
    }

    ;

    private final Map< String, Ability > abilities;
    private final Map< String, Race > races;
    private final Map< String, Role > roles;
    private final Map< String, Skill > skills;
    private final Map< String, Faction > factions;
    private final Map< String, DamageType > damageTypes;
    private final Map< String, BaseWeapon > baseWeapons;
    private final Map< String, ArmorType > armorTypes;
    private final Map< String, SavedItemList > itemLists;
    private final Map< String, AbilitySelectionList > abilitySelectionLists;
    private final Map< String, RacialType > racialTypes;

    private final Map< String, Quality > itemQualities;

    private final Map< String, Integer > ruleValues;
    private final Map< String, String > ruleStrings;

    private final Map< String, Cutscene > cutscenes;

    private DifficultyManager difficultyManager;

    private final Map< String, QuickbarGroup > quickbarGroups;

    /**
     * Creates a new empty Ruleset.  The readData method must be called after this
     * to initialize all the rules.  This should not be done until both the
     * core and campaign resources are registered.
     */

    public Ruleset( )
    {
        abilities = new HashMap< String, Ability >( );
        abilitySelectionLists = new HashMap< String, AbilitySelectionList >( );

        races = new LinkedHashMap< String, Race >( );
        roles = new LinkedHashMap< String, Role >( );
        skills = new LinkedHashMap< String, Skill >( );
        factions = new HashMap< String, Faction >( );
        damageTypes = new LinkedHashMap< String, DamageType >( );

        baseWeapons = new HashMap< String, BaseWeapon >( );
        armorTypes = new HashMap< String, ArmorType >( );

        itemLists = new LinkedHashMap< String, SavedItemList >( );

        racialTypes = new HashMap< String, RacialType >( );

        ruleValues = new HashMap< String, Integer >( );
        ruleStrings = new HashMap< String, String >( );

        itemQualities = new HashMap< String, Quality >( );

        cutscenes = new HashMap< String, Cutscene >( );

        quickbarGroups = new LinkedHashMap< String, QuickbarGroup >( );
    }

    /**
     * Reads in all data for this Ruleset.
     */

    public void readData( )
    {
        readRuleValuesAndStrings( );

        readItemQualities( );

        readFactions( );
        readDamageTypes( );
        readBaseWeapons( );
        readArmorTypes( );

        readRacialTypes( );

        readAbilities( );
        readAbilitySelectionLists( );
        readQuickbarGroups( );

        readRaces( );
        readSkills( );
        readRoles( );

        readItemLists( );

        readCutscenes( );

        XP.initXPTable( );

        difficultyManager = new DifficultyManager( );
    }

    /**
     * Returns the difficulty manager for this ruleset
     *
     * @return the difficulty manager
     */

    public DifficultyManager getDifficultyManager( )
    {
        return difficultyManager;
    }

    /**
     * Loads all itemSets from the "itemLists" resource directory
     */

    private void readItemLists( )
    {
        itemLists.clear( );

        Set< String > resources = ResourceManager.getResourcesInDirectory( "itemLists" );
        for ( String resource : resources )
        {
            String id = ResourceManager.getResourceID( resource, "itemLists", ResourceType.JSON );
            if ( id == null ) continue;

            SimpleJSONParser parser = new SimpleJSONParser( "itemLists/" + id, ResourceType.JSON );

            itemLists.put( id, new SavedItemList( parser.getObject( ) ) );
        }
    }

    /**
     * Loads all cutscenes from the "cutscenes" resource directory
     */

    private void readCutscenes( )
    {
        cutscenes.clear( );

        for ( String resource : ResourceManager.getResourcesInDirectory( "cutscenes" ) )
        {
            String id = ResourceManager.getResourceID( resource, "cutscenes", ResourceType.JSON );
            if ( id == null ) continue;

            cutscenes.put( id, new Cutscene( id, resource ) );
        }
    }

    /**
     * Loads all races from the "races" resource directory
     */

    private void readRaces( )
    {
        races.clear( );

        Set< String > resources = ResourceManager.getResourcesInDirectory( "races" );
        for ( String resource : resources )
        {
            String id = ResourceManager.getResourceIDNoPath( resource, ResourceType.JSON );
            if ( id == null ) continue;

            try
            {
                races.put( id, new Race( id, new SimpleJSONParser( resource ) ) );
            }
            catch ( Exception e )
            {
                Logger.appendToErrorLog( "Error loading race " + id, e );
            }
        }
    }

    /**
     * Loads all roles from the "roles" resource directory
     */

    private void readRoles( )
    {
        roles.clear( );

        Set< String > resources = ResourceManager.getResourcesInDirectory( "roles" );
        for ( String resource : resources )
        {
            String id = ResourceManager.getResourceIDNoPath( resource, ResourceType.JSON );
            if ( id == null ) continue;

            try
            {
                roles.put( id, new Role( id, new SimpleJSONParser( resource ).getObject( ) ) );
            }
            catch ( Exception e )
            {
                Logger.appendToErrorLog( "Error loading role " + id, e );
            }
        }
    }

    /**
     * Loads all skills from the "skills" directory
     */

    private void readSkills( )
    {
        skills.clear( );

        Set< String > resources = ResourceManager.getResourcesInDirectory( "skills" );
        for ( String resource : resources )
        {
            String id = ResourceManager.getResourceID( resource, "skills", ResourceType.JSON );
            if ( id == null ) continue;
            skills.put( id, new Skill( id ) );
        }
    }

    /**
     * Loads all abilities from the "abilities" directory
     */

    private void readAbilities( )
    {
        abilities.clear( );

        for ( String resource : ResourceManager.getResourcesInDirectory( "abilities" ) )
        {
            String id = ResourceManager.getResourceIDNoPath( resource, ResourceType.JSON );
            if ( id == null ) continue;

            abilities.put( id, Ability.createAbilityFromResource( id, resource ) );
        }
    }

    /**
     * Note that this method must be run after reading in the abilities
     */

    private void readQuickbarGroups( )
    {
        quickbarGroups.clear( );

        // create the set of abilities by group
        Map< String, List< Ability > > abilitiesByGroup = new HashMap< String, List< Ability > >( );
        for ( Ability ability : abilities.values( ) )
        {
            if ( ! ability.isActivateable( ) || ability.getQuickbarGroup( ) == null ) continue;

            String group = ability.getQuickbarGroup( );

            if ( ! abilitiesByGroup.containsKey( group ) )
            {
                abilitiesByGroup.put( group, new ArrayList< Ability >( ) );
            }

            abilitiesByGroup.get( group ).add( ability );
        }

        SimpleJSONParser parser = new SimpleJSONParser( "quickbarGroups", ResourceType.JSON );
        parser.setWarnOnMissingKeys( true );

        SimpleJSONArray array = parser.getArray( "quickbarGroups" );
        for ( SimpleJSONArrayEntry entry : array )
        {
            SimpleJSONObject object = entry.getObject( );

            QuickbarGroup group = new QuickbarGroup( object, abilitiesByGroup );
            quickbarGroups.put( group.getName( ), group );
        }

        parser.warnOnUnusedKeys( );
    }

    private void readItemQualities( )
    {
        itemQualities.clear( );

        SimpleJSONParser parser = new SimpleJSONParser( "itemQualities", ResourceType.JSON );
        parser.setWarnOnMissingKeys( true );

        SimpleJSONArray array = parser.getArray( "qualities" );
        for ( SimpleJSONArrayEntry entry : array )
        {
            SimpleJSONObject object = entry.getObject( );

            // create an ItemQuality by parsing the JSON
            Quality quality = new Quality( object );
            itemQualities.put( quality.getName( ), quality );
        }

        parser.warnOnUnusedKeys( );
    }

    /**
     * Loads all ability lists from the "abilitySelectionLists" resource directory
     */

    private void readAbilitySelectionLists( )
    {
        abilitySelectionLists.clear( );

        for ( String resource : ResourceManager.getResourcesInDirectory( "abilitySelectionLists" ) )
        {
            String id = ResourceManager.getResourceIDNoPath( resource, ResourceType.JSON );
            if ( id == null ) continue;

            try
            {
                abilitySelectionLists.put( id, new AbilitySelectionList( id, resource ) );
            }
            catch ( Exception e )
            {
                Logger.appendToErrorLog( "Error reading selection list " + id, e );
            }
        }

        // verify sublist references are ok
        for ( String listID : abilitySelectionLists.keySet( ) )
        {
            AbilitySelectionList list = abilitySelectionLists.get( listID );

            for ( String subListID : list.getSubListIDs( ) )
            {
                if ( ! abilitySelectionLists.containsKey( subListID ) )
                {
                    Logger.appendToWarningLog( "Referenced SubList " + subListID +
                                                       " in AbilitySelectionList " + listID + " does not exist." );
                }
            }
        }
    }

    /*
     * Loads miscellaneous rules from the "rules.txt" resource
     */

    private void readRuleValuesAndStrings( )
    {
        ruleValues.clear( );
        ruleStrings.clear( );

        SimpleJSONParser parser = new SimpleJSONParser( "rules", ResourceType.JSON );

        for ( String key : parser.keySet( ) )
        {
            if ( parser.isInteger( key ) )
            {
                ruleValues.put( key, parser.get( key, 0 ) );
            }
            else if ( parser.isString( key ) )
            {
                ruleStrings.put( key, parser.get( key, null ) );
            }
        }

        parser.warnOnUnusedKeys( );
    }

    /**
     * Loads all damage types from the "damageTypes.txt" resource
     */

    private void readDamageTypes( )
    {
        damageTypes.clear( );

        SimpleJSONParser parser = new SimpleJSONParser( "damageTypes", ResourceType.JSON );

        for ( String key : parser.keySet( ) )
        {
            String type = parser.get( key, "Energy" );
            if ( type.equals( "Energy" ) )
            { damageTypes.put( key, new DamageType( key, true ) ); }
            else
            { damageTypes.put( key, new DamageType( key, false ) ); }
        }

        parser.warnOnUnusedKeys( );
    }

    /**
     * Loads all racial types from the "racialTypes.txt" resource
     */

    private void readRacialTypes( )
    {
        racialTypes.clear( );

        SimpleJSONParser parser = new SimpleJSONParser( "racialTypes", ResourceType.JSON );

        for ( SimpleJSONArrayEntry entry : parser.getArray( "racialTypes" ) )
        {
            String name = entry.getString( );
            racialTypes.put( name, new RacialType( name ) );
        }

        parser.warnOnUnusedKeys( );
    }

    /**
     * Loads all factions and relationships from the "factions.json" resource
     */

    private void readFactions( )
    {
        factions.clear( );

        Map< Faction, SimpleJSONArray > relationshipArrays = new HashMap< Faction, SimpleJSONArray >( );

        // load factions and get the list of relationships
        SimpleJSONParser parser = new SimpleJSONParser( "factions", ResourceType.JSON );
        for ( SimpleJSONArrayEntry entry : parser.getArray( "factions" ) )
        {
            SimpleJSONObject factionData = entry.getObject( );

            Faction faction = new Faction( factionData.get( "id", null ) );
            // a faction is always friendly with itself
            faction.setRelationship( faction, Faction.Relationship.Friendly );

            if ( factionData.containsKey( "relationships" ) )
            {
                // save relationship array to parse after loading all factions
                relationshipArrays.put( faction, factionData.getArray( "relationships" ) );
            }

            factions.put( faction.getName( ), faction );
        }

        // add relationships
        for ( Faction faction : relationshipArrays.keySet( ) )
        {
            for ( SimpleJSONArrayEntry entry : relationshipArrays.get( faction ) )
            {
                SimpleJSONObject relationshipObject = entry.getObject( );

                String relationID = relationshipObject.get( "id", null );
                String relationType = relationshipObject.get( "relationship", null );
                Faction.Relationship relationship = Faction.Relationship.valueOf( relationType );

                // set the factions with the appropriate relationship with each other
                faction.setRelationship( relationID, relationship );
                factions.get( relationID ).setRelationship( faction, relationship );
            }
        }

        parser.warnOnUnusedKeys( );
    }

    /**
     * loads all armor types from the "armorTypes" resource file
     */

    private void readArmorTypes( )
    {
        armorTypes.clear( );

        SimpleJSONParser parser = new SimpleJSONParser( "armorTypes", ResourceType.JSON );
        for ( SimpleJSONArrayEntry entry : parser.getArray( "armorTypes" ) )
        {
            String name = entry.getString( );
            armorTypes.put( name, new ArmorType( name ) );
        }
        parser.warnOnUnusedKeys( );
    }

    /**
     * Loads all base weapons from the "baseWeapons" resource file
     */

    private void readBaseWeapons( )
    {
        baseWeapons.clear( );

        SimpleJSONParser parser = new SimpleJSONParser( "baseWeapons", ResourceType.JSON );
        for ( SimpleJSONArrayEntry entry : parser.getArray( "baseWeapons" ) )
        {
            String name = entry.getString( );
            baseWeapons.put( name, new BaseWeapon( name ) );
        }
        parser.warnOnUnusedKeys( );
    }

    /**
     * Returns the integer value with the specified rule id from the rules.txt resource
     *
     * @param rule the rule ID
     * @return the integer value corresponding to the specified rule ID
     */

    public int getValue( String rule )
    {
        return ruleValues.get( rule );
    }

    /**
     * Returns the string value with the specified rule ID from the rules.txt resource
     *
     * @param rule the rule ID
     * @return the string value
     */

    public String getString( String rule )
    {
        return ruleStrings.get( rule );
    }

    public QuickbarGroup getQuickbarGroup( String id ) { return quickbarGroups.get( id ); }

    public Collection< QuickbarGroup > getAllQuickbarGroups( ) { return quickbarGroups.values( ); }

    public Collection< DamageType > getAllDamageTypes( ) { return damageTypes.values( ); }

    public Collection< Race > getAllRaces( ) { return races.values( ); }

    public Collection< Role > getAllRoles( ) { return roles.values( ); }

    public Collection< Skill > getAllSkills( ) { return skills.values( ); }

    public Quality getItemQuality( String id ) { return itemQualities.get( id ); }

    public Cutscene getCutscene( String id ) { return cutscenes.get( id ); }

    public RacialType getRacialType( String ref ) { return racialTypes.get( ref ); }

    /**
     * Return the saved item list with the specified ID.  The returned item list must not
     * be modified
     *
     * @param ref
     * @return the saved item list
     */

    public SavedItemList getItemList( String ref )
    {
        return itemLists.get( ref );
    }

    public BaseWeapon getBaseWeapon( String ref ) { return baseWeapons.get( ref ); }

    public ArmorType getArmorType( String ref ) { return armorTypes.get( ref ); }

    public DamageType getDamageType( String ref ) { return damageTypes.get( ref ); }

    public Faction getFaction( String ref ) { return factions.get( ref ); }

    public Race getRace( String ref ) { return races.get( ref ); }

    public Role getRole( String ref ) { return roles.get( ref ); }

    public Skill getSkill( String ref ) { return skills.get( ref ); }

    public Ability getAbility( String id ) { return abilities.get( id ); }

    public AbilitySelectionList getAbilitySelectionList( String id ) { return abilitySelectionLists.get( id ); }
}
