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

package net.sf.hale.loading;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import net.sf.hale.resource.Sprite;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/**
 * An object allowing for the seamless thread safe loading of textures into the OpenGL
 * context.  The {@link #update()} method is called from the main thread and does all of the actual
 * loading, while the {@link #loadTexture(ByteBuffer, int, int, List)} method can be
 * called from any thread and will eventually set the texture for any sprites once the
 * OpenGL loading is complete
 *
 * @author Jared Stephen
 */

public class AsyncTextureLoader
{
    private List< TextureSpriteLoadList > loadLists;

    /**
     * Creates an empty AsyncTextureLoader
     */

    public AsyncTextureLoader( )
    {
        loadLists = new ArrayList< TextureSpriteLoadList >( );
    }

    /**
     * Clears this texture loader.  Any textures that have yet to be loaded will not be bound in
     * texture memory
     */

    public void clear( )
    {
        synchronized ( loadLists )
        {
            loadLists.clear( );
        }
    }

    /**
     * This method should only be called by the main OpenGL context owning Thread.  Loads all
     * outstanding textures that have been specified by {@link #loadTexture(ByteBuffer, int, int, List)}
     */

    public void update( )
    {
        synchronized ( loadLists )
        {
            for ( TextureSpriteLoadList list : loadLists )
            {
                list.loadTexture( );
            }

            loadLists.clear( );
        }
    }

    /**
     * Tells the loader to add the specified texture to the queue of textures to be loaded.
     * The next time {@link #update()} is called, the specified Sprite will have its
     * texture set to the value returned from OpenGL by loading the texture
     *
     * @param pixels  the pixel data to load
     * @param width   the width of the pixel data
     * @param height  the height of the pixel data
     * @param sprites the sprites to set the texture for
     */

    public void loadTexture( ByteBuffer pixels, int width, int height, List< Sprite > sprites )
    {
        TextureSpriteLoadList list = new TextureSpriteLoadList( pixels, width, height, sprites );

        synchronized ( loadLists )
        {
            loadLists.add( list );
        }
    }

    private class TextureSpriteLoadList
    {
        private ByteBuffer pixels;
        private int width;
        private int height;

        private List< Sprite > sprites;

        private TextureSpriteLoadList( ByteBuffer pixels, int width, int height, List< Sprite > sprites )
        {
            this.pixels = pixels;
            this.width = width;
            this.height = height;
            this.sprites = sprites;
        }

        private void loadTexture( )
        {
            IntBuffer textures = BufferUtils.createIntBuffer( 1 );

            // bind a new texture for the image
            GL11.glGenTextures( textures );
            GL11.glBindTexture( GL11.GL_TEXTURE_2D, textures.get( 0 ) );
            GL11.glTexParameteri( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST );
            GL11.glTexParameteri( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST );

            GL11.glTexImage2D( GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width,
                               height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels );

            for ( Sprite sprite : sprites )
            {
                sprite.setTexture( textures.get( 0 ) );
            }
        }
    }
}
