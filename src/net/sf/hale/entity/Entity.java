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

import java.util.ArrayList;
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.HasScriptState;
import net.sf.hale.ScriptState;
import net.sf.hale.ability.Aura;
import net.sf.hale.ability.Effect;
import net.sf.hale.ability.EffectTarget;
import net.sf.hale.ability.EntityEffectSet;
import net.sf.hale.ability.ScriptFunctionType;
import net.sf.hale.area.Area;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.LoadGameException;
import net.sf.hale.loading.ReferenceHandler;
import net.sf.hale.loading.Saveable;
import net.sf.hale.rules.Faction;
import net.sf.hale.util.Point;
import net.sf.hale.util.SaveGameUtil;
import net.sf.hale.util.SimpleJSONObject;
import net.sf.hale.view.ConversationPopup;

/**
 * The abstract base class for all items and creatures in the world.  The Comparable interface is
 * used when drawing entities in the area - containers are drawn below doors which are drawn below
 * creatures
 *
 * @author Jared
 */

public abstract class Entity implements EffectTarget, Saveable, HasScriptState, Comparable< Entity >
{

    // the immutable parts of this entity that are duplicated by others
    private final EntityTemplate template;

    private String faction;

    private Location location;

    // used by scripts to store variables
    private final ScriptState scriptState;

    private final EntityEffectSet effects;

    // widgets currently viewing this entity that need to be updated
    // when changes are made
    private List< EntityListener > viewers;

    /**
     * Creates an entity from the specified template
     *
     * @param template the template to use as the base for this entity
     */

    protected Entity( EntityTemplate template )
    {
        if ( template == null )
        { throw new NullPointerException( "entity template must not be null" ); }

        this.template = template;

        this.scriptState = new ScriptState( );

        this.effects = new EntityEffectSet( );

        this.faction = Game.ruleset.getString( "DefaultFaction" );

        this.location = Location.Inventory;

        this.viewers = new ArrayList< EntityListener >( );
    }

    /**
     * Used for creating a copy of an entity.  Copies permanent fields (effects, faction)
     * but not everything (such as location, script state)
     *
     * @param other
     */

    protected Entity( Entity other )
    {
        this.template = other.template;

        this.scriptState = new ScriptState( other.scriptState );

        this.effects = new EntityEffectSet( other.effects, this );
        this.faction = other.faction;

        this.location = Location.Inventory;
        this.viewers = new ArrayList< EntityListener >( );
    }

    /**
     * Parses the current entity state based on the specified JSON data
     *
     * @param data       the data to parse
     * @param area       the area that this entity is located in
     * @param refHandler the object handling references
     */

    public void load( SimpleJSONObject data, Area area, ReferenceHandler refHandler ) throws LoadGameException
    {
        this.faction = data.get( "faction", null );

        refHandler.add( data.get( "ref", null ), this );

        if ( data.containsKey( "location" ) )
        { this.location = Location.load( data.getObject( "location" ), area ); }
        else
        { this.location = Location.Inventory; }

        if ( data.containsKey( "scriptState" ) )
        { this.scriptState.load( data.getObject( "scriptState" ) ); }

        if ( data.containsKey( "effects" ) )
        {
            this.effects.load( data.getObject( "effects" ), refHandler, this );
        }
    }

    @Override
    public JSONOrderedObject save( )
    {
        JSONOrderedObject out = new JSONOrderedObject( );

        out.put( "id", template.getID( ) );
        out.put( "class", getClass( ).getSimpleName( ) );
        out.put( "ref", SaveGameUtil.getRef( this ) );
        out.put( "faction", faction );

        if ( location.getArea( ) != null )
        { out.put( "location", location.save( ) ); }

        if ( ! scriptState.isEmpty( ) )
        { out.put( "scriptState", scriptState.save( ) ); }

        if ( effects.size( ) > 0 )
        { out.put( "effects", effects.save( ) ); }

        return out;
    }

    /**
     * Adds the specified EntityViewer to the list of EntityViewers
     * for this Entity
     *
     * @param viewer the EntityViewer to add
     */

    public void addViewer( EntityListener viewer )
    {
        viewers.add( viewer );
    }

    /**
     * Removes the specified EntityViewer from the list of EntityViewers
     * for this Entity
     *
     * @param viewer the viewer to remove
     */

    public void removeViewer( EntityListener viewer )
    {
        viewers.remove( viewer );
    }

    /**
     * Causes all EntityViewers that are registered with this Entity via
     * {@link #addViewer(EntityListener)} to be updated via {@link EntityListener#entityUpdated()}
     */

    public void updateListeners( )
    {
        for ( EntityListener viewer : viewers )
        {
            viewer.entityUpdated( this );
        }
    }

    /**
     * Closes and removes all currently active viewers
     */

    public void removeAllListeners( )
    {
        for ( EntityListener viewer : viewers )
        {
            viewer.removeListener( );
        }

        viewers.clear( );
    }

    @Override
    public int getSpellResistance( )
    {
        return 0;
    }

    @Override
    public boolean isValidEffectTarget( )
    {
        return true;
    }

    @Override
    public Object get( String key )
    {
        return scriptState.get( key );
    }

    @Override
    public void put( String key, Object value )
    {
        scriptState.put( key, value );
    }

    /**
     * Gets the template that this entity is based on
     *
     * @return the template
     */

    public EntityTemplate getTemplate( ) { return template; }

    /**
     * Gets the faction this entity is associated with
     *
     * @return the faction
     */

    public Faction getFaction( ) { return Game.ruleset.getFaction( faction ); }

    /**
     * Gets the current location of this entity
     *
     * @return the entity location
     */

    public Location getLocation( ) { return location; }

    /**
     * Gets the set of effects currently applied to this entity
     *
     * @return the set of effects
     */

    public EntityEffectSet getEffects( ) { return effects; }

    /**
     * A shortcut for {@link EntityTemplate#getName()} for this
     * creature's template
     *
     * @return the name of this entity
     */

    public String getName( )
    {
        return template.getName( );
    }

    /**
     * Sets the location of this entity to the specified coordinates within
     * the area that this entity currently resides.
     *
     * @param x
     * @param y Note that for creatures, visibility will be automatically recomputed
     *          when this function is called
     * @return true if the location was set succesfully without any interruptions.
     * false if the location was set, but the entity's movement was interrupted
     * by one or more traps or triggers.
     */

    public boolean setLocationInCurrentArea( int x, int y )
    {
        try
        {

            Area area = location.getArea( );

            // set the current player area if this entity is not in an area
            if ( area == null ) area = Game.curCampaign.curArea;

            return setLocation( new Location( area, x, y ) );

        }
        catch ( Exception e )
        {
            e.printStackTrace( );
            return false;
        }
    }

    /**
     * Sets the location of this entity to a new location with the specified parameters.
     * See {@link #setLocation(Location)}
     *
     * @param area
     * @param x
     * @param y
     * @return if the location was set succesfully without any interruptions.
     * false if the location was set, but the entity's movement was interrupted
     * by one or more traps or triggers.
     */

    public boolean setLocation( Area area, int x, int y )
    {
        return setLocation( new Location( area, x, y ) );
    }

    /**
     * Sets the location of this entity to the specified Location
     *
     * @param newLocation the location specifying the area and coordinates
     *                    within that area
     *                    <p>
     *                    Note that for creatures, visibility will be automatically recomputed
     *                    when this function is called
     * @return true if the location was set succesfully without any interruptions.
     * false if the location was set, but the entity's movement was interrupted
     * by one or more traps or triggers.
     */

    public boolean setLocation( Location newLocation )
    {
        Location oldLocation = this.location;

        Point oldScreen = this.location.getScreenPoint( );
        Point newScreen = newLocation.getScreenPoint( );

        if ( oldLocation.getArea( ) != null && oldLocation.getArea( ) != newLocation.getArea( ) )
        {
            // moving to a new area
            oldLocation.getArea( ).getEntities( ).removeEntity( this );
        }

        this.location = newLocation;

        effects.offsetAnimationPositions( newScreen.x - oldScreen.x, newScreen.y - oldScreen.y );
        effects.moveAuras( );

        if ( oldLocation == null || oldLocation.getArea( ) == null )
        {
            // not within an area, do nothing

        }
        else if ( oldLocation.getArea( ) == newLocation.getArea( ) )
        {
            // moving within an area
            this.location.getArea( ).getEntities( ).moveEntity( this, oldLocation );
        }
        else
        {
            // moving to a new area
            newLocation.getArea( ).getEntities( ).addEntity( this );
        }

        return true;
    }

    /**
     * Sets the faction for this Entity
     *
     * @param faction
     */

    public void setFaction( Faction faction )
    {
        this.faction = faction.getName( );
    }

    /**
     * Sets the faction for this Entity
     *
     * @param faction the ID of the faction
     */

    public void setFaction( String faction )
    {
        setFaction( Game.ruleset.getFaction( faction ) );
    }

    /**
     * Returns true if the faction of this entity is the player character
     * faction, false otherwise
     *
     * @return whether this entity is a member of the player character faction
     */

    public boolean isPlayerFaction( )
    {
        return faction.equals( Game.ruleset.getString( "PlayerFaction" ) );
    }

    /**
     * Draws this Entity for the UI view
     *
     * @param x the x screen coordinate
     * @param y the y screen coordinate
     */

    public void uiDraw( int x, int y )
    {
        template.getIcon( ).draw( x, y );
    }

    /**
     * Draws this Entity for the main area view
     *
     * @param x the x screen coordinate
     * @param y the y screen coordinate
     */

    public void areaDraw( int x, int y )
    {
        template.getIcon( ).drawCentered( x, y, Game.TILE_SIZE, Game.TILE_SIZE );
    }

    /**
     * Elapses the specified number of rounds for this entity, all applied
     * effects, and any children entities (such as items in a creature's inventory)
     *
     * @param numRounds
     */

    public boolean elapseTime( int numRounds )
    {
        effects.elapseRounds( this, numRounds );

        return false;
    }

    /**
     * Starts a conversation between this entity and the specified talker.
     * This entity's conversation script is used.
     *
     * @param talker
     */

    public void startConversation( PC talker )
    {
        ConversationPopup popup = new ConversationPopup( this, talker, template.getConversation( ) );
        popup.startConversation( );
    }

    /**
     * Creates a new effect with no script
     *
     * @return a new effect
     */

    public Effect createEffect( )
    {
        return new Effect( );
    }

    /**
     * Creates a new effect with the specified script
     *
     * @param scriptID
     * @return a new effect
     */

    public Effect createEffect( String scriptID )
    {
        return new Effect( scriptID );
    }

    /**
     * Creates a new aura with the specified script
     *
     * @param scriptID
     * @return a new effect
     */

    public Aura createAura( String scriptID )
    {
        return new Aura( scriptID );
    }

    public void applyEffect( Effect effect )
    {
        effect.setTarget( this );
        effect.executeFunction( ScriptFunctionType.onApply, effect );

        applyEffectBonuses( effect );

        effect.startAnimations( );

        effects.add( effect, getLocation( ).getArea( ) != null );

        this.updateListeners( );
    }

    @Override
    public void removeEffect( Effect effect )
    {
        if ( effect == null ) return;

        effect.executeFunction( ScriptFunctionType.onRemove, effect );

        removeEffectBonuses( effect );

        effect.endAnimations( );

        effects.remove( effect );

        this.updateListeners( );
    }

    /**
     * Called when adding an effect.  The default implementation does nothing
     *
     * @param effect
     */

    protected void applyEffectBonuses( Effect effect ) { }

    /**
     * Called when removing an effect.  The default implementation does nothing
     *
     * @param effect
     */

    protected void removeEffectBonuses( Effect effect ) { }

    @Override
    public int compareTo( Entity other )
    {
        return hashCode( ) - other.hashCode( );
    }
}
