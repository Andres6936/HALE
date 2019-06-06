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

package net.sf.hale.particle;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptException;

import net.sf.hale.Game;
import net.sf.hale.interfacelock.EntityOffsetAnimation;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.util.JSEngine;
import net.sf.hale.util.Logger;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

/**
 * Handles updating and storing all particles and animations
 *
 * @author Jared Stephen
 */

public class ParticleManager
{
    private long lastTime;

    private final Map< String, ParticleGenerator > baseGenerators;
    private final Map< String, Animation > baseAnimations;

    private final List< Animated > activeBelowAnimations;
    private final List< Animated > activeAboveAnimations;

    private final List< Animated > animationsToAdd;

    private final List< EntityOffsetAnimation > entityOffsetAnimations;

    public ParticleManager( )
    {
        this.baseGenerators = new HashMap< String, ParticleGenerator >( );
        this.baseAnimations = new HashMap< String, Animation >( );

        this.activeBelowAnimations = new LinkedList< Animated >( );
        this.activeAboveAnimations = new LinkedList< Animated >( );

        this.animationsToAdd = new LinkedList< Animated >( );

        this.entityOffsetAnimations = new LinkedList< EntityOffsetAnimation >( );

        lastTime = System.currentTimeMillis( );
    }

    public void loadBaseResources( )
    {
        Set< String > resources = ResourceManager.getResourcesInDirectory( "scripts/particleEffects" );
        for ( String resource : resources )
        {
            if ( resource.endsWith( ResourceType.JavaScript.getExtension( ) ) )
            {
                loadBaseGenerator( resource );
            }
        }

        resources = ResourceManager.getResourcesInDirectory( "scripts/animations" );
        for ( String resource : resources )
        {
            if ( resource.endsWith( ResourceType.JavaScript.getExtension( ) ) )
            {
                loadAnimation( resource );
            }
        }
    }

    private void loadAnimation( String resource )
    {
        JSEngine engine = Game.scriptEngineManager.getEngine( );

        engine.put( "game", Game.scriptInterface );

        Animation animation = null;

        try
        {
            animation = ( Animation ) engine.eval( ResourceManager.getResourceAsString( resource ) );
            engine.release( );
        }
        catch ( ScriptException e )
        {
            Logger.appendToErrorLog( "Error running script for animation " + resource, e );
        }

        String id = ResourceManager.getResourceIDNoPath( resource, ResourceType.JavaScript );

        if ( animation != null )
        {
            baseAnimations.put( id, animation );
        }
        else
        {
            Logger.appendToErrorLog( "Warning: " + resource + " did not return an Animation." );
        }
    }

    private void loadBaseGenerator( String resource )
    {
        JSEngine engine = Game.scriptEngineManager.getEngine( );

        engine.put( "game", Game.scriptInterface );

        ParticleGenerator generator = null;

        try
        {
            generator = ( ParticleGenerator ) engine.eval( ResourceManager.getResourceAsString( resource ) );
            engine.release( );
        }
        catch ( ScriptException e )
        {
            Logger.appendToErrorLog( "Error running script for particle effect " + resource, e );
        }

        String id = ResourceManager.getResourceIDNoPath( resource, ResourceType.JavaScript );

        if ( generator != null )
        {
            baseGenerators.put( id, generator );
        }
        else
        {
            Logger.appendToErrorLog( "Warning: " + resource + " did not return a Particle Generator." );
        }
    }

    public Animation getAnimation( String ref )
    {
        Animation a = this.baseAnimations.get( ref );

        if ( a == null ) return null;

        return new Animation( a );
    }

    public ParticleGenerator getParticleGenerator( String ref )
    {
        ParticleGenerator g = this.baseGenerators.get( ref );

        if ( g == null ) return null;

        if ( g instanceof LineParticleGenerator ) { return new LineParticleGenerator( ( LineParticleGenerator ) g ); }
        else if ( g instanceof RectParticleGenerator )
        {
            return new RectParticleGenerator( ( RectParticleGenerator ) g );
        }
        else if ( g instanceof CircleParticleGenerator )
        { return new CircleParticleGenerator( ( CircleParticleGenerator ) g ); }
        else { return new ParticleGenerator( this.baseGenerators.get( ref ) ); }
    }

    public void addEntityOffsetAnimation( EntityOffsetAnimation animation )
    {
        entityOffsetAnimations.add( animation );
    }

    public void add( Animated animated )
    {
        animated.initialize( );

        synchronized ( animationsToAdd )
        {
            animationsToAdd.add( animated );
        }
    }

    /**
     * Clears all currently active particles.  Does not clear particles that are queued up
     * but have not been added yet
     */

    public void clear( )
    {
        activeAboveAnimations.clear( );
        activeBelowAnimations.clear( );
    }

    public void update( long curTime )
    {
        float durationSeconds = ( curTime - lastTime ) / 1000.0f;

        Iterator< EntityOffsetAnimation > entIter = entityOffsetAnimations.iterator( );
        while ( entIter.hasNext( ) )
        {
            if ( entIter.next( ).elapseTime( durationSeconds ) )
            {
                entIter.remove( );
            }
        }

        Iterator< Animated > animIter = activeAboveAnimations.iterator( );
        while ( animIter.hasNext( ) )
        {
            if ( animIter.next( ).elapseTime( durationSeconds ) )
            {
                animIter.remove( );
            }
        }

        animIter = activeBelowAnimations.iterator( );
        while ( animIter.hasNext( ) )
        {
            if ( animIter.next( ).elapseTime( durationSeconds ) )
            {
                animIter.remove( );
            }
        }

        synchronized ( animationsToAdd )
        {
            for ( Animated animation : animationsToAdd )
            {
                animation.cacheSprite( );

                if ( animation.isDrawable( ) )
                {
                    switch ( animation.getDrawingMode( ) )
                    {
                        case BelowEntities:
                            activeBelowAnimations.add( animation );
                            break;
                        case AboveEntities:
                            activeAboveAnimations.add( animation );
                            break;
                    }
                }
            }
            animationsToAdd.clear( );
        }

        lastTime = curTime;
    }

    public final void drawBelowEntities( )
    {
        GL11.glEnable( GL11.GL_TEXTURE_2D );

        for ( Animated animation : activeBelowAnimations )
        {
            animation.draw( );
        }

        GL11.glColor3f( 1.0f, 1.0f, 1.0f );
        GL14.glSecondaryColor3f( 0.0f, 0.0f, 0.0f );
    }

    public final void drawAboveEntities( )
    {
        GL11.glEnable( GL11.GL_TEXTURE_2D );

        for ( Animated animation : activeAboveAnimations )
        {
            animation.draw( );
        }

        GL11.glColor3f( 1.0f, 1.0f, 1.0f );
        GL14.glSecondaryColor3f( 0.0f, 0.0f, 0.0f );
    }
}
