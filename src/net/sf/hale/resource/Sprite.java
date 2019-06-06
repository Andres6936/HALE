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

package net.sf.hale.resource;

import org.lwjgl.opengl.GL11;

/**
 * A class representing an image stored in OpenGL video memory.  This can be
 * the entire contents of an image file on disk, or only a portion of an image
 * (in the case of sprite sheet)
 *
 * @author Jared
 */

public class Sprite
{
    private int textureReference;

    private final int width;
    private final int height;

    private final double texCoordStartX, texCoordStartY;
    private final double texCoordEndX, texCoordEndY;

    /**
     * Creates a new Sprite with the specified texture, width, and height, and the
     * default texture coordinates of 0.0 to 1.0 for both x and y
     *
     * @param textureReference
     * @param width
     * @param height
     */

    public Sprite( int textureReference, int width, int height )
    {
        this( textureReference, width, height, 0.0, 0.0, 1.0, 1.0 );
    }

    /**
     * Creates a new Sprite with the specified texture, width, height, and texture coordinates
     *
     * @param textureReference
     * @param width
     * @param height
     * @param texCoordStartX
     * @param texCoordStartY
     * @param texCoordEndX
     * @param texCoordEndY
     */

    public Sprite( int textureReference, int width, int height, double texCoordStartX, double texCoordStartY,
                   double texCoordEndX, double texCoordEndY )
    {

        this.textureReference = textureReference;
        this.width = width;
        this.height = height;

        this.texCoordStartX = texCoordStartX;
        this.texCoordStartY = texCoordStartY;
        this.texCoordEndX = texCoordEndX;
        this.texCoordEndY = texCoordEndY;
    }

    /**
     * This method should only be called by the AsyncTextureLoader.  Calling it
     * anywhere else is not recommended.
     *
     * @param texture the integer representing the OpenGL texture for this Sprite
     */

    public void setTexture( int texture )
    {
        this.textureReference = texture;
    }

    /**
     * Gets the lower bound of the x texture coordinate for this sprite within its
     * larger texture
     *
     * @return the lower x texture coordinate
     */

    public final double getTexCoordStartX( ) { return texCoordStartX; }

    /**
     * Gets the lower bound of the y texture coordinate for this sprite within its
     * larger texture
     *
     * @return the lower y texture coordinate
     */

    public final double getTexCoordStartY( ) { return texCoordStartY; }

    /**
     * Gets the upper bound of the x texture coordinate for this sprite within its
     * larger texture
     *
     * @return the upper x texture coordinate
     */

    public final double getTexCoordEndX( ) { return texCoordEndX; }

    /**
     * Gets the upper bound of the y texture coordinate for this sprite within its
     * larger texture
     *
     * @return the upper y texture coordinate
     */

    public final double getTexCoordEndY( ) { return texCoordEndY; }

    /**
     * Returns the integer referencing the OpenGL texture being used by this Sprite
     *
     * @return the OpenGL texture image
     */

    public final int getTextureReference( ) { return textureReference; }

    /**
     * Returns the width of this sprite in pixels
     *
     * @return the width
     */

    public final int getWidth( ) { return width; }

    /**
     * Returns the height of this sprite in pixels
     *
     * @return the height
     */

    public final int getHeight( ) { return height; }

    /**
     * Draws this Sprite, assuming the texture has already been bound previously.  This is
     * a useful optimization when drawing large numbers of sprites from the same texture
     *
     * @param x the x coordinate relative to the current OpenGL matrix
     * @param y the y coordinate relative to the current OpenGL matrix
     */

    public final void drawNoTextureBind( int x, int y )
    {
        GL11.glBegin( GL11.GL_QUADS );

        GL11.glTexCoord2d( texCoordStartX, texCoordStartY );
        GL11.glVertex2i( x, y );

        GL11.glTexCoord2d( texCoordEndX, texCoordStartY );
        GL11.glVertex2i( x + width, y );

        GL11.glTexCoord2d( texCoordEndX, texCoordEndY );
        GL11.glVertex2i( x + width, y + height );

        GL11.glTexCoord2d( texCoordStartX, texCoordEndY );
        GL11.glVertex2i( x, y + height );

        GL11.glEnd( );
    }

    /**
     * Draws this Sprite by binding the texture and then issuing the appropriate OpenGL
     * drawing command
     *
     * @param x the x coordinate relative to the current OpenGL matrix
     * @param y the y coordinate relative to the current OpenGL matrix
     */

    public final void draw( int x, int y )
    {
        GL11.glBindTexture( GL11.GL_TEXTURE_2D, textureReference );

        GL11.glBegin( GL11.GL_QUADS );

        GL11.glTexCoord2d( texCoordStartX, texCoordStartY );
        GL11.glVertex2i( x, y );

        GL11.glTexCoord2d( texCoordEndX, texCoordStartY );
        GL11.glVertex2i( x + width, y );

        GL11.glTexCoord2d( texCoordEndX, texCoordEndY );
        GL11.glVertex2i( x + width, y + height );

        GL11.glTexCoord2d( texCoordStartX, texCoordEndY );
        GL11.glVertex2i( x, y + height );

        GL11.glEnd( );
    }

    /**
     * Draws this Sprite by binding the texture and then issuing the appropriate OpenGL
     * drawing command
     *
     * @param x the x coordinate relative to the current OpenGL matrix
     * @param y the y coordinate relative to the current OpenGL matrix
     * @param w the width to stretch to
     * @param h the height to stretch to
     */

    public final void draw( int x, int y, int w, int h )
    {
        GL11.glBindTexture( GL11.GL_TEXTURE_2D, textureReference );

        GL11.glBegin( GL11.GL_QUADS );

        GL11.glTexCoord2d( texCoordStartX, texCoordStartY );
        GL11.glVertex2i( x, y );

        GL11.glTexCoord2d( texCoordEndX, texCoordStartY );
        GL11.glVertex2i( x + w, y );

        GL11.glTexCoord2d( texCoordEndX, texCoordEndY );
        GL11.glVertex2i( x + w, y + h );

        GL11.glTexCoord2d( texCoordStartX, texCoordEndY );
        GL11.glVertex2i( x, y + h );

        GL11.glEnd( );
    }
}
