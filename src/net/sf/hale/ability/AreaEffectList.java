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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.hale.area.Area;
import net.sf.hale.area.AreaEntityList;
import net.sf.hale.bonus.Bonus;
import net.sf.hale.bonus.BonusStackTypeList;
import net.sf.hale.entity.Creature;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.LoadGameException;
import net.sf.hale.loading.ReferenceHandler;
import net.sf.hale.loading.Saveable;
import net.sf.hale.util.Logger;
import net.sf.hale.util.Point;
import net.sf.hale.util.SaveGameUtil;
import net.sf.hale.util.SimpleJSONArray;
import net.sf.hale.util.SimpleJSONArrayEntry;
import net.sf.hale.util.SimpleJSONObject;

/**
 * Contains the set of all effects that have been applied to a given {@link net.sf.hale.area.Area}.
 * When applied in this manner, each effect will be active in a collection of points.  This class
 * keeps track of which points each effect is applied to.
 *
 * @author Jared Stephen
 */

public class AreaEffectList implements Saveable
{
    private Map< Effect, List< Point > > effects;

    private EffectList[][] effectsAtPosition;

    private Area area;

    public Object save( )
    {
        JSONOrderedObject[] effectsData = new JSONOrderedObject[ effects.size( ) ];
        int i = 0;
        for ( Effect effect : effects.keySet( ) )
        {
            if ( effect.getTarget( ) == area )
            {
                // save the effect if it is targeted on the area, otherwise save only a
                // reference
                effectsData[ i ] = effect.save( );
            }
            else
            {
                effectsData[ i ] = new JSONOrderedObject( );
                effectsData[ i ].put( "ref", SaveGameUtil.getRef( effect ) );
            }

            int j = 0;
            JSONOrderedObject[] points = new JSONOrderedObject[ effects.get( effect ).size( ) ];
            for ( Point p : effects.get( effect ) )
            {
                points[ j ] = new JSONOrderedObject( );
                points[ j ].put( "x", p.x );
                points[ j ].put( "y", p.y );
                j++;
            }

            effectsData[ i ].put( "points", points );
            i++;
        }

        return effectsData;
    }

    public void load( SimpleJSONArray data, ReferenceHandler refHandler ) throws LoadGameException
    {
        for ( SimpleJSONArrayEntry entry : data )
        {
            SimpleJSONObject entryObject = entry.getObject( );

            Effect effect = null;
            try
            {

                if ( entryObject.containsKey( "duration" ) )
                {
                    // the complete effect has been saved, so load it
                    effect = Effect.load( entryObject, refHandler, area );
                }
                else
                {
                    // only a reference was saved
                    effect = refHandler.getEffect( entryObject.get( "ref", null ) );
                }

            }
            catch ( Exception e )
            {
                Logger.appendToErrorLog( "Error loading effect ", e );
                continue;
            }

            if ( effect == null )
            {
                Logger.appendToWarningLog( "Warning: unable to load area effect " + entryObject.get( "ref", null ) );
                continue;
            }

            List< Point > points = new ArrayList< Point >( );
            for ( SimpleJSONArrayEntry pointEntry : entryObject.getArray( "points" ) )
            {
                SimpleJSONObject pointObject = pointEntry.getObject( );

                Point p = new Point( pointObject.get( "x", 0 ), pointObject.get( "y", 0 ) );

                points.add( p );

                if ( effectsAtPosition[ p.x ][ p.y ] == null )
                {
                    effectsAtPosition[ p.x ][ p.y ] = new EffectList( );
                }

                effectsAtPosition[ p.x ][ p.y ].add( effect );
            }

            effects.put( effect, points );
        }
    }

    /**
     * Creates a new AreaEffectList of the specified size.  The AreaEffectList
     * uses the size to determine the set of valid coordinates that Effects
     * can occupy.
     *
     * @param area the area that the size is taken from
     */

    public AreaEffectList( Area area )
    {
        this.area = area;

        effects = new HashMap< Effect, List< Point > >( );

        effectsAtPosition = new EffectList[ area.getWidth( ) ][ area.getHeight( ) ];
    }

    /**
     * Starts any animations on effects in this list
     */

    public void startAnimations( )
    {
        for ( Effect effect : effects.keySet( ) )
        {
            effect.startAnimations( );
        }
    }

    /**
     * Moves the effect currently in this AreaEffectList from its current set of points to the new
     * set of points
     *
     * @param aura   the effect to move
     * @param points the new set of positions for the Effect
     */

    public void move( Aura aura, List< Point > points )
    {
        if ( ! effects.containsKey( aura ) ) return;

        // remove any invalid points from the list of points that
        // will be used with the effects list
        ArrayList< Point > newPoints = new ArrayList< Point >( points.size( ) );
        for ( Point p : points )
        {
            if ( checkCoordinates( p.x, p.y ) ) newPoints.add( new Point( p ) );
        }
        newPoints.trimToSize( );
        List< Point > oldPoints = effects.get( aura );

        Set< Creature > oldCreatures = new HashSet< Creature >( );
        Set< Creature > newCreatures = new HashSet< Creature >( );

        // get the set of creatures previously in the aura
        for ( Point p : oldPoints )
        {
            Creature creature = area.getEntities( ).getCreature( p.x, p.y );

            // don't add the aura target, which can neither enter or exit the aura
            if ( creature != null && creature != aura.getTarget( ) )
            {
                oldCreatures.add( creature );
            }
        }

        // get the set of creatures now in the aura
        for ( Point p : newPoints )
        {
            Creature creature = area.getEntities( ).getCreature( p.x, p.y );

            // don't add the aura target, which can neither enter or exit the aura
            if ( creature != null && creature != aura.getTarget( ) )
            {
                newCreatures.add( creature );
            }
        }

        // check for creatures exiting
        for ( Creature creature : oldCreatures )
        {
            if ( ! newCreatures.contains( creature ) )
            {
                aura.executeFunction( ScriptFunctionType.onTargetExit, creature, aura );
            }
        }

        // check for creatures entering
        for ( Creature creature : newCreatures )
        {
            if ( ! oldCreatures.contains( creature ) )
            {
                aura.executeFunction( ScriptFunctionType.onTargetEnter, creature, aura );
            }
        }

        // remove old points
        for ( Point p : oldPoints )
        {
            effectsAtPosition[ p.x ][ p.y ].remove( aura );
        }

        // add new points
        for ( Point p : newPoints )
        {
            if ( effectsAtPosition[ p.x ][ p.y ] == null )
            {
                effectsAtPosition[ p.x ][ p.y ] = new EffectList( );
            }

            effectsAtPosition[ p.x ][ p.y ].add( aura );
        }

        effects.put( aura, newPoints );
    }

    /**
     * Adds the specified Effect to this AreaEffectList at each point in the List of Points.
     *
     * @param effect the Effect to add
     * @param points the grid positions to add the Effect to
     */

    public void add( Effect effect, List< Point > points )
    {
        if ( effects.containsKey( effect ) ) return;

        // remove any invalid points from the list of points that
        // will be used with the effects list
        ArrayList< Point > newPoints = new ArrayList< Point >( points.size( ) );
        for ( Point p : points )
        {
            if ( checkCoordinates( p.x, p.y ) ) newPoints.add( new Point( p ) );
        }
        newPoints.trimToSize( );

        effects.put( effect, newPoints );

        effect.executeFunction( ScriptFunctionType.onApply, effect );

        // add the effect to each point
        for ( Point p : effects.get( effect ) )
        {
            // create the list at the specified position if it does not exist
            if ( effectsAtPosition[ p.x ][ p.y ] == null )
            {
                effectsAtPosition[ p.x ][ p.y ] = new EffectList( );
            }

            effectsAtPosition[ p.x ][ p.y ].add( effect );

            // run the onEnter script for each creature in the area
            Creature creature = area.getEntities( ).getCreature( p.x, p.y );

            if ( creature != null )
            {
                effect.executeFunction( ScriptFunctionType.onTargetEnter, creature, effect );
            }
        }

        effect.startAnimations( );
    }

    /**
     * Removes the specified Effect from this AreaEffectList.  It is removed
     * from all Points.
     *
     * @param effect the Effect to remove.
     */

    public void remove( Effect effect )
    {
        if ( ! effects.containsKey( effect ) )
        {
            Logger.appendToWarningLog( "Effect " + effect + " is not in the area effect list" );
            return;
        }

        for ( Point p : effects.get( effect ) )
        {
            effectsAtPosition[ p.x ][ p.y ].remove( effect );

            // remove the list if it is empty to conserve memory
            if ( effectsAtPosition[ p.x ][ p.y ].isEmpty( ) )
            {
                effectsAtPosition[ p.x ][ p.y ] = null;
            }

            // run the onExit script for creatures in the area
            Creature creature = area.getEntities( ).getCreature( p.x, p.y );

            if ( creature != null )
            { effect.executeFunction( ScriptFunctionType.onTargetExit, creature, effect ); }
        }

        effects.remove( effect );

        effect.endAnimations( );
    }

    /**
     * Returns true if and only if this AreaEffectList contains the specified Effect
     * at one or more Points
     *
     * @param effect the Effect to look for
     * @return true if and only if this AreaEffectList contains the specified Effect
     */

    public boolean contains( Effect effect )
    {
        return effects.containsKey( effect );
    }

    /**
     * Returns a List of all Creatures in the specified AreaEntityList standing on tiles
     * occupied by the specified Effect.
     *
     * @param effect   the Effect to check the list of points for
     * @param entities the List of Entities and their associated positions
     * @return the List of all Creatures affected by the specified effect
     */

    public List< Creature > getAffectedCreatures( Effect effect, AreaEntityList entities )
    {
        List< Creature > creatures = new ArrayList< Creature >( );

        if ( effects.get( effect ) == null ) return creatures;

        for ( Point p : effects.get( effect ) )
        {
            Creature creature = entities.getCreature( p.x, p.y );
            if ( creature != null ) creatures.add( creature );
        }

        return creatures;
    }

    /**
     * Returns all Effects at the specified position.  Returns an empty List if there are
     * no Effects at the specified position.  If the supplied coordinates are invalid
     * (outside the size bounds of the Area) then an empty List is returned
     *
     * @param x the x grid coordinate
     * @param y the y grid coordinate
     * @return the List of all Effects at the specified position.
     */

    public List< Effect > getEffectsAt( int x, int y )
    {
        if ( ! checkCoordinates( x, y ) || effectsAtPosition[ x ][ y ] == null ) return new ArrayList< Effect >( 0 );

        List< Effect > effects = new ArrayList< Effect >( effectsAtPosition[ x ][ y ].size( ) );
        for ( Effect effect : effectsAtPosition[ x ][ y ] )
        {
            effects.add( effect );
        }

        return effects;
    }

    /**
     * Returns true if one or more Effects at the specified coordinates have a Bonus
     * of the specified Type, false otherwise.
     *
     * @param bonusType the Type of Bonus to check for
     * @param x         the x grid coordinate
     * @param y         the y grid coordinate
     * @return true if and only if an Effect with a Bonus of the specified Type is
     * found at the specified coordinates
     */

    public boolean hasBonusAt( Bonus.Type bonusType, int x, int y )
    {
        if ( ! checkCoordinates( x, y ) ) return false;

        if ( effectsAtPosition[ x ][ y ] == null ) return false;

        for ( Effect effect : effectsAtPosition[ x ][ y ] )
        {
            if ( effect.getBonuses( ).hasBonusOfType( bonusType ) ) return true;
        }

        return false;
    }

    /**
     * Returns the value of the Bonus of the specified Type at the
     * specified grid coordinates.  If there are multiple Effects at the
     * specified coordinates with the same Bonus Type or multiple Bonuses
     * with the same Type within one Effect, normal Bonus stacking rules
     * are applied in order to determine the total bonus.
     *
     * @param bonusType the Type of Bonus
     * @param x         the x grid coordinate
     * @param y         the y grid coordinate
     * @return the value of the Bonus of the specified Type at the
     * specified coordinates
     */

    public int getBonusAt( Bonus.Type bonusType, int x, int y )
    {
        if ( ! checkCoordinates( x, y ) ) return 0;

        if ( effectsAtPosition[ x ][ y ] == null ) return 0;

        BonusStackTypeList list = new BonusStackTypeList( );

        for ( Effect effect : effectsAtPosition[ x ][ y ] )
        {
            for ( Bonus bonus : effect.getBonuses( ) )
            {
                if ( bonus.getType( ) == bonusType ) list.add( bonus );
            }
        }

        return list.getCurrentTotal( );
    }

    /**
     * Returns the List of Points that the specified Effect is applied to in this
     * AreaEffectList.  If the Effect is not present, returns an empty List.
     *
     * @param effect
     * @return the set of points that this effect is applied to
     */

    public List< Point > getPoints( Effect effect )
    {
        if ( ! effects.containsKey( effect ) ) return Collections.emptyList( );

        return getArrayListDeepCopy( effects.get( effect ) );
    }

    /**
     * Resize this AreaEffectList in order to work with an Area of the
     * specified width and height.  All Effects currently in the
     * AreaEffectList are retained, unless the new size parameters
     * cause the coordinates of an Effect to no longer be within the
     * Area.  In this case, only coordinates that are no longer within
     * the Area are removed.  If all coordinates associated with an Effect
     * are removed, then the Effect is removed as well.
     *
     * @param newWidth  the new width for the Area
     * @param newHeight the new height for the Area
     */

    public void resize( int newWidth, int newHeight )
    {
        EffectList[][] newEffectsAtPosition = new EffectList[ newWidth ][ newHeight ];
        Map< Effect, List< Point > > newEffects = new HashMap< Effect, List< Point > >( );

        int width = Math.min( this.effectsAtPosition.length, newWidth );
        int height = Math.min( this.effectsAtPosition[ 0 ].length, newHeight );

        for ( int i = 0; i < width; i++ )
        {
            for ( int j = 0; j < height; j++ )
            {
                newEffectsAtPosition[ i ][ j ] = this.effectsAtPosition[ i ][ j ];

                if ( this.effectsAtPosition[ i ][ j ] == null ) continue;

                for ( Effect effect : this.effectsAtPosition[ i ][ j ] )
                {
                    if ( newEffects.containsKey( effect ) )
                    {
                        newEffects.get( effect ).add( new Point( i, j ) );
                    }
                    else
                    {
                        List< Point > points = new ArrayList< Point >( );
                        points.add( new Point( i, j ) );
                        newEffects.put( effect, points );
                    }
                }
            }
        }

        this.effectsAtPosition = newEffectsAtPosition;
        this.effects = newEffects;
    }

    /**
     * Returns the number of effects in this list
     *
     * @return the number of effects
     */

    public int size( )
    {
        return effects.size( );
    }

    /*
     * Returns a deep copy of the specified List of Points.
     */

    private List< Point > getArrayListDeepCopy( List< Point > points )
    {
        List< Point > newPoints = new ArrayList< Point >( points.size( ) );

        for ( Point p : points )
        {
            newPoints.add( new Point( p ) );
        }

        return newPoints;
    }

    /*
     * Verifies that the specified coordinates will not generate an ArrayIndexOutOfBounds
     * exception
     */

    private final boolean checkCoordinates( int x, int y )
    {
        if ( x < 0 || y < 0 || x >= effectsAtPosition.length || y >= effectsAtPosition[ 0 ].length )
        { return false; }
        else
        { return true; }
    }

    private class EffectList extends ArrayList< Effect >
    {
        private static final long serialVersionUID = 877898924941324323L;

        // create an ArrayList with default capacity 1
        // usually there should be at most 1 or 2 effects at a given point

        private EffectList( )
        {
            super( 1 );
        }
    }
}
