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

package net.sf.hale.swingeditor;

import java.awt.Canvas;

import net.sf.hale.Game;
import net.sf.hale.tileset.Tileset;
import net.sf.hale.util.Logger;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

/**
 * The thread responsible for managing the OpenGL context and running
 * the main OpenGL rendering loop
 *
 * @author Jared
 */

public class OpenGLThread extends Thread
{
    private AreaRenderer viewer;
    private AreaRenderer newViewer;

    private SwingEditor parent;
    private Canvas canvas;

    private volatile boolean running;

    private boolean draw;
    private boolean resize;

    /**
     * Creates a new OpenGL thread which will run using the specified canvas
     *
     * @param parent
     */

    public OpenGLThread( SwingEditor parent )
    {
        this.parent = parent;
        this.canvas = parent.getOpenGLCanvas( );
        this.draw = true;
        this.resize = true;
    }

    /**
     * Sets the AreaViewer which is responsible for drawing within this OpenGL context
     *
     * @param viewer
     */

    public void setAreaViewer( AreaRenderer viewer )
    {
        this.newViewer = viewer;
    }

    /**
     * This should be called prior to application exit to close out the display
     */

    public void destroyDisplay( )
    {
        Display.destroy( );
    }

    /**
     * Sets whether the OpenGL context should perform normal drawing (true)
     * or no drawing (false)
     *
     * @param draw
     */

    public void setDrawingEnabled( boolean draw )
    {
        this.draw = draw;
    }

    /**
     * Resizes the OpenGL drawing context to fit the canvas
     */

    public void canvasResized( )
    {
        resize = true;
    }

    private void resize( )
    {
        GL11.glViewport( 0, 0, canvas.getWidth( ), canvas.getHeight( ) );
        GL11.glMatrixMode( GL11.GL_PROJECTION );
        GL11.glLoadIdentity( );
        GL11.glOrtho( 0, canvas.getWidth( ), canvas.getHeight( ), 0, 0, 1 );
        GL11.glMatrixMode( GL11.GL_MODELVIEW );
    }

    private void initGL( )
    {
        GL11.glDisable( GL11.GL_DEPTH_TEST );
        GL11.glEnable( GL11.GL_BLEND );
        GL11.glBlendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );
        GL11.glEnable( GL14.GL_COLOR_SUM );
        GL11.glMatrixMode( GL11.GL_PROJECTION );
        GL11.glLoadIdentity( );
        GL11.glOrtho( 0, canvas.getWidth( ), canvas.getHeight( ), 0, 0, 1 );
        GL11.glMatrixMode( GL11.GL_MODELVIEW );
    }

    private void updateViewer( )
    {
        if ( newViewer == null ) return;

        // get the old and new tileset
        Tileset newTileset = Game.curCampaign.getTileset( newViewer.getArea( ).getTileset( ) );
        Tileset oldTileset = null;
        if ( viewer != null )
        { oldTileset = Game.curCampaign.getTileset( viewer.getArea( ).getTileset( ) ); }

        // switch to the new viewer
        viewer = newViewer;
        newViewer = null;

        // load the tileset if needed
        if ( newTileset != oldTileset )
        {
            // free the old tileset
            if ( oldTileset != null ) oldTileset.freeTiles( );

            newTileset.loadTiles( );
        }

        viewer.getArea( ).getTileGrid( ).cacheSprites( );

        parent.getPalette( ).setArea( viewer );
        parent.validate( );
    }

    @Override
    public void run( )
    {
        try
        {
            Display.setParent( canvas );
            Display.create( );

            initGL( );

        }
        catch ( LWJGLException e )
        {
            Logger.appendToErrorLog( "Error creating canvas", e );
        }

        running = true;

        while ( running )
        {
            Game.textureLoader.update( );

            GL11.glClear( GL11.GL_COLOR_BUFFER_BIT );

            GL11.glColor3f( 1.0f, 1.0f, 1.0f );

            if ( resize )
            {
                resize( );
                resize = false;
            }

            updateViewer( );

            if ( draw && viewer != null )
            {
                viewer.draw( );
                viewer.handleInput( );
            }

            Display.update( false );
            GL11.glGetError( );
            Display.sync( 60 );
            Display.processMessages( );
        }

        Display.destroy( );
    }
}
