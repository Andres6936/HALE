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

package net.sf.hale.widgets;

import org.lwjgl.opengl.GL11;

import net.sf.hale.entity.Creature;
import net.sf.hale.resource.Sprite;
import net.sf.hale.resource.SpriteManager;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.ToggleButton;

/**
 * The basic widget for viewing a portrait.  The PortraitViewer that also displays
 * name, Action Points, and Hit Points extends this class
 *
 * @author Jared Stephen
 */

public class BasePortraitViewer extends ToggleButton
{
    private Creature creature;
    private Sprite portraitSprite;

    private int portraitScale = 1;
    private float invPortraitScale;

    private boolean enableEventHandling;

    private int portraitY;

    /**
     * Create a new portrait viewer for the specified creature.  By default,
     * event handling will be disabled for this BasePortraitViewer
     *
     * @param creature the Creature to view the portrait of
     */

    public BasePortraitViewer( Creature creature )
    {
        setTheme( "portraitviewer" );

        this.creature = creature;
        this.enableEventHandling = false;

        if ( creature != null && creature.getTemplate( ).getPortrait( ) != null )
        { portraitSprite = SpriteManager.getPortrait( creature.getTemplate( ).getPortrait( ) ); }
        else
        { portraitSprite = null; }
    }

    /**
     * Sets the creature whose portrait is being viewed
     *
     * @param creature the creature
     */

    public void setCreature( Creature creature )
    {
        this.creature = creature;

        if ( creature.getTemplate( ).getPortrait( ) != null )
        { portraitSprite = SpriteManager.getPortrait( creature.getTemplate( ).getPortrait( ) ); }
        else
        { portraitSprite = null; }
    }

    /**
     * Returns the creature that this BasePortraitViewer is viewing
     *
     * @return the creature that this BasePortraitViewer is viewing
     */

    public Creature getCreature( )
    {
        return creature;
    }

    /**
     * Sets whether this BasePortraitViewer will handle events including
     * mouse overs and button clicks.  By default, this is false
     *
     * @param enable whether this BasePortraitViewer will handle events
     *               including mouse overs and button clicks
     */

    public void setEnableEventHandling( boolean enable )
    {
        this.enableEventHandling = enable;
    }

    /**
     * Sets the Y coordinate to draw the portrait at relative to the inner
     * Y coordinate of this Widget.  Defaults to 0.
     *
     * @param portraitY the Y coordinate to draw the portrait at
     */

    protected void setPortraitY( int portraitY )
    {
        this.portraitY = portraitY;
    }

    /**
     * Returns the height of the portrait sprite that is currently being drawn
     * scaled to the theme specified portraitScale, or 0 if there is no
     * portrait sprite
     *
     * @return the drawn height of the portrait sprite
     */

    protected int getPortraitSpriteHeight( )
    {
        if ( portraitSprite == null ) return 0;

        return portraitSprite.getHeight( ) / portraitScale;
    }

    @Override
    protected boolean handleEvent( Event evt )
    {
        if ( ! enableEventHandling ) return false;

        return super.handleEvent( evt );
    }

    @Override
    protected void applyTheme( ThemeInfo themeInfo )
    {
        super.applyTheme( themeInfo );
        portraitScale = themeInfo.getParameter( "portraitScale", 2 );
        invPortraitScale = 1.0f / ( float ) portraitScale;
    }

    @Override
    public int getPreferredInnerWidth( )
    {
        if ( portraitSprite == null ) { return 0; }
        else { return portraitSprite.getWidth( ) / portraitScale; }
    }

    @Override
    public int getPreferredInnerHeight( )
    {
        if ( portraitSprite == null ) { return 0; }
        else { return portraitSprite.getHeight( ) / portraitScale; }
    }

    @Override
    public int getPreferredWidth( )
    {
        return getPreferredInnerWidth( ) + getBorderHorizontal( );
    }

    @Override
    public int getPreferredHeight( )
    {
        return getPreferredInnerHeight( ) + getBorderVertical( );
    }

    // override the default behavior which draws the overlay on top
    // of the children

    @Override
    protected void paint( GUI gui )
    {
        paintBackground( gui );
        paintWidget( gui );
        paintOverlay( gui );
        paintChildren( gui );
    }

    @Override
    protected void paintWidget( GUI gui )
    {
        if ( portraitSprite != null )
        {
            GL11.glPushMatrix( );
            GL11.glScalef( invPortraitScale, invPortraitScale, 1.0f );
            GL11.glColor3f( 1.0f, 1.0f, 1.0f );
            portraitSprite.draw( portraitScale * getInnerX( ), ( getInnerY( ) + portraitY ) * portraitScale );
            GL11.glPopMatrix( );
        }
    }
}
