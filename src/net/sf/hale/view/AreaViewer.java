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

package net.sf.hale.view;

import net.sf.hale.AreaListener;
import net.sf.hale.Game;
import net.sf.hale.ability.Targeter;
import net.sf.hale.area.Area;
import net.sf.hale.area.Transition;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.PC;
import net.sf.hale.entity.Entity;
import net.sf.hale.interfacelock.InterfaceLock;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;
import net.sf.hale.widgets.EntityMouseover;
import net.sf.hale.widgets.OverHeadFadeAway;
import net.sf.hale.tileset.AreaTileGrid;

import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.AnimationState;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.AnimationState.StateKey;
import de.matthiasmann.twl.renderer.Image;

/**
 * The widget that comprises the main portion of the Game screen.  Views the set
 * of tiles associated with a given area and all entities within that area.
 *
 * @author Jared Stephen
 */

public class AreaViewer extends Widget implements AreaTileGrid.AreaRenderer
{

    /**
     * The "active" and enabled GUI state for this AreaViewer, used as a time source
     * for animations
     */

    public static final StateKey STATE_ACTIVE = StateKey.get( "active" );

    private Image hexAnim;
    private Image hexFilledBlack, hexFilledGrey;
    private Image hexWhite, hexRed, hexBlue, hexGreen, hexGrey;

    public Point mouseHoverTile;
    public boolean mouseHoverValid;

    private Area area;
    private final Point scroll;
    private final Point maxScroll;
    private final Point minScroll;

    private AreaListener areaListener;

    private DelayedScroll delayedScroll;
    private ScreenShake screenShake;
    private long fadeInTime;

    /**
     * Creates a new AreaViewer viewing the specified Area.
     *
     * @param area the Area to view
     */

    public AreaViewer( Area area )
    {
        mouseHoverTile = new Point( );
        mouseHoverValid = true;

        this.scroll = new Point( 0, 0 );
        this.maxScroll = new Point( 0, 0 );
        this.minScroll = new Point( 0, 0 );
        this.area = area;

        fadeInTime = System.currentTimeMillis( );
    }

    /**
     * Sets the area being viewed by this AreaViewer
     *
     * @param area the Area to view
     */

    public void setArea( Area area )
    {
        this.area = area;
        this.scroll.x = 0;
        this.scroll.y = 0;

        invalidateLayout( );

        setMaxScroll( );

        // validate scroll position
        scroll( 0, 0 );

        // load tileset
        Game.curCampaign.getTileset( area.getTileset( ) ).loadTiles( );
        area.getTileGrid( ).cacheSprites( );

        fadeInTime = System.currentTimeMillis( );
        Game.timer.resetTime( );
    }

    /**
     * Causes this viewer to fade in, with the fade start time set to the current time.
     */

    public void fadeIn( )
    {
        fadeInTime = System.currentTimeMillis( );
    }

    /**
     * Returns the Area currently being viewed by this AreaViewer
     *
     * @return the Area currently being viewed by this AreaViewer
     */

    @Override
    public Area getArea( ) { return area; }

    /**
     * Sets the listener which handles all events sent to this AreaViewer
     *
     * @param areaListener the listener for this areaViewer
     */

    public void setListener( AreaListener areaListener )
    {
        this.areaListener = areaListener;
    }

    @Override
    public void drawInterface( AnimationState as )
    {
        if ( ! Game.interfaceLocker.locked( ) )
        {
            Creature activeCreature = areaListener.getCombatRunner( ).getActiveCreature( );

            if ( Game.isInTurnMode( ) && ! activeCreature.isPlayerFaction( ) )
            {
                // draw a hex around the active hostile
                drawBlueHex( areaListener.getCombatRunner( ).getActiveCreature( ).getLocation( ).toPoint( ), as );
            }
            else
            {
                // draw a hex around the selected party member
                drawBlueHex( Game.curCampaign.party.getSelected( ).getLocation( ).toPoint( ), as );
            }
        }

        Targeter currentTargeter = areaListener.getTargeterManager( ).getCurrentTargeter( );
        if ( currentTargeter != null )
        {
            currentTargeter.draw( as );
        }

        if ( mouseHoverValid ) { drawWhiteHex( mouseHoverTile, as ); }
        else { drawRedHex( mouseHoverTile, as ); }

        if ( Game.isInTurnMode( ) && ! areaListener.getTargeterManager( ).isInTargetMode( ) )
        {
            Creature active = areaListener.getCombatRunner( ).getActiveCreature( );
            if ( active != null && ! Game.interfaceLocker.locked( ) )
            {
                drawAnimHex( active.getLocation( ).toPoint( ), as );
            }
        }

        GL11.glColor3f( 1.0f, 1.0f, 1.0f );
    }

    @Override
    protected void paintWidget( GUI gui )
    {
        AnimationState as = this.getAnimationState( );

        GL11.glPushMatrix( );
        GL11.glTranslatef( ( - scroll.x + getX( ) ), ( - scroll.y + getY( ) ), 0.0f );

        GL11.glEnable( GL11.GL_TEXTURE_2D );

        // compute drawing bounds
        Point topLeft = AreaUtil.convertScreenToGrid( scroll.x - getX( ), scroll.y - getY( ) );
        Point bottomRight = AreaUtil.convertScreenToGrid( scroll.x - getX( ) + getWidth( ), scroll.y - getY( ) + getHeight( ) );

        topLeft.x = Math.max( 0, topLeft.x - 2 );
        topLeft.y = Math.max( 0, topLeft.y - 2 );

        // ensure topLeft.x is even
        if ( topLeft.x % 2 == 1 ) topLeft.x -= 1;

        bottomRight.x = Math.min( area.getWidth( ) - 1, bottomRight.x + 2 );
        bottomRight.y = Math.min( area.getHeight( ) - 1, bottomRight.y + 2 );

        // draw the area
        area.getTileGrid( ).draw( this, as, topLeft, bottomRight );

        Entity mouseOverEntity = Game.mainViewer.getMouseOver( ).getSelectedEntity( );

        if ( ! Game.isInTurnMode( ) || ! ( mouseOverEntity instanceof PC ) )
        {
            drawVisibility( area.getVisibility( ), as, topLeft, bottomRight );
        }
        else
        {
            drawCreatureVisibility( ( Creature ) mouseOverEntity, as, topLeft, bottomRight );
        }

        GL11.glDisable( GL11.GL_TEXTURE_2D );

        GL11.glPopMatrix( );

        GL11.glColor3f( 0.0f, 0.0f, 0.0f );
        GL11.glBegin( GL11.GL_POLYGON );
        GL11.glVertex2i( ( area.getWidth( ) - 2 ) * Game.TILE_WIDTH + getX( ), 0 );
        GL11.glVertex2i( Game.config.getResolutionX( ), 0 );
        GL11.glVertex2i( Game.config.getResolutionX( ), Game.config.getResolutionY( ) );
        GL11.glVertex2i( ( area.getWidth( ) - 2 ) * Game.TILE_WIDTH + getX( ), Game.config.getResolutionY( ) );
        GL11.glEnd( );

        GL11.glBegin( GL11.GL_POLYGON );
        GL11.glVertex2i( 0, ( area.getHeight( ) - 1 ) * Game.TILE_SIZE - Game.TILE_SIZE / 2 );
        GL11.glVertex2i( Game.config.getResolutionX( ), ( area.getHeight( ) - 1 ) * Game.TILE_SIZE - Game.TILE_SIZE / 2 );
        GL11.glVertex2i( Game.config.getResolutionX( ), Game.config.getResolutionY( ) );
        GL11.glVertex2i( 0, Game.config.getResolutionY( ) );
        GL11.glEnd( );

        // do a fade in if needed
        if ( fadeInTime != 0 )
        {
            long curTime = System.currentTimeMillis( );

            float fadeAlpha = Math.min( 1.0f, 1.3f - ( curTime - fadeInTime ) / 1500.0f );

            if ( fadeAlpha > 0.0f )
            {
                GL11.glColor4f( 0.0f, 0.0f, 0.0f, fadeAlpha );

                GL11.glBegin( GL11.GL_QUADS );
                GL11.glVertex2i( getX( ), getY( ) );
                GL11.glVertex2i( getX( ), getBottom( ) );
                GL11.glVertex2i( getRight( ), getBottom( ) );
                GL11.glVertex2i( getRight( ), getY( ) );
                GL11.glEnd( );
            }
            else
            {
                fadeInTime = 0;
            }
        }

        GL11.glEnable( GL11.GL_TEXTURE_2D );
    }

    /**
     * Performs any delayed scrolls or screen shakes that have been queued up
     *
     * @param curTime the current time in milliseconds
     */

    public void update( long curTime )
    {
        performDelayedScrolling( curTime );

        performScreenShake( curTime );
    }

    private void performScreenShake( long curTime )
    {
        if ( screenShake == null ) return;

        if ( curTime > screenShake.endTime )
        {
            scroll( - screenShake.lastShakeX, - screenShake.lastShakeY, true );
            screenShake = null;

        }
        else if ( curTime > screenShake.lastTime + 110 )
        {
            screenShake.lastTime = curTime;

            int newShakeX = - 1 * ( ( int ) Math.signum( screenShake.lastShakeX ) ) * Game.dice.rand( 20, 35 );
            int newShakeY = Game.dice.rand( - 2, 2 );

            scroll( newShakeX - screenShake.lastShakeX, newShakeY - screenShake.lastShakeY, true );

            screenShake.lastShakeX = newShakeX;
            screenShake.lastShakeY = newShakeY;
        }
    }

    /**
     * Performs the classic "screen shake" effect
     */

    public void addScreenShake( )
    {
        ScreenShake shake = new ScreenShake( );

        long curTime = System.currentTimeMillis( );

        shake.endTime = curTime + 600;
        shake.lastTime = curTime;
        shake.lastShakeX = 1;


        this.screenShake = shake;

        Game.interfaceLocker.add( new InterfaceLock( Game.curCampaign.party.getSelected( ), 600 ) );
    }

    /**
     * Performs any gradual scrolling currently queued up for this AreaViewer
     *
     * @param curTime the current time in milliseconds
     */

    private void performDelayedScrolling( long curTime )
    {
        if ( delayedScroll == null ) return;

        long elapsedTime = curTime - delayedScroll.startTime;

        float destx = delayedScroll.scrollXPerMilli * elapsedTime + delayedScroll.startScrollX;
        float desty = delayedScroll.scrollYPerMilli * elapsedTime + delayedScroll.startScrollY;

        scroll( ( int ) ( destx - scroll.x ), ( int ) ( desty - scroll.y ), true );

        if ( delayedScroll.endTime < curTime )
        { delayedScroll = null; }
    }

    /**
     * Adds a delayed scroll to the specified point using the DelayedScrollTime.
     * After the scroll completes, the view will be centered on the specified point
     *
     * @param pScreen the point to scroll to
     */

    public void addDelayedScrollToScreenPoint( Point pScreen )
    {
        this.delayedScroll = new DelayedScroll( );

        int destx = pScreen.x - this.getInnerWidth( ) / 2;
        int desty = pScreen.y - this.getInnerHeight( ) / 2;

        delayedScroll.startScrollX = scroll.x;
        delayedScroll.startScrollY = scroll.y;

        float x = destx - scroll.x;
        float y = desty - scroll.y;

        int totalTime = Game.config.getCombatDelay( ) * 2;

        delayedScroll.scrollXPerMilli = x / totalTime;
        delayedScroll.scrollYPerMilli = y / totalTime;

        delayedScroll.startTime = System.currentTimeMillis( );
        delayedScroll.endTime = delayedScroll.startTime + totalTime;
    }

    /**
     * Adds a delayed scroll to the specified creature using the DelayedScrollTime.
     * After the scroll completes, the view will be centered on the specified creature
     * in the same way as {@link #scrollToCreature(Entity)}
     *
     * @param creature the creature to scroll to
     */

    public void addDelayedScrollToCreature( Entity creature )
    {
        addDelayedScrollToScreenPoint( creature.getLocation( ).getCenteredScreenPoint( ) );
    }

    /**
     * Scrolls the view of this area so that it is centered on the specified creature.  If the
     * creature is near the edges of the area and the view cannot center on the creature, the
     * view is moved as close to centered as possible
     *
     * @param creature the entity to center the view on
     */

    public void scrollToCreature( Entity creature )
    {
        Point pScreen = creature.getLocation( ).getCenteredScreenPoint( );

        int destx = pScreen.x - this.getInnerWidth( ) / 2;
        int desty = pScreen.y - this.getInnerHeight( ) / 2;

        // clear any delayed scroll
        delayedScroll = null;

        scroll( destx - scroll.x, desty - scroll.y );
    }

    @Override
    protected void layout( )
    {
        super.layout( );

        setMaxScroll( );

        // validate scroll position
        scroll( 0, 0 );
    }

    /**
     * Sets the maximum and minimum scroll for this AreaViewer based on the
     * size of the underlying area.  This method can be overriden to show a larger
     * border area, useful for the editor.
     */

    protected void setMaxScroll( )
    {
        minScroll.x = Game.TILE_SIZE;
        minScroll.y = Game.TILE_SIZE;

        maxScroll.x = ( area.getWidth( ) - 1 ) * Game.TILE_WIDTH - this.getInnerWidth( );
        maxScroll.y = area.getHeight( ) * Game.TILE_SIZE - Game.TILE_SIZE / 2 - this.getInnerHeight( );
        if ( maxScroll.x < minScroll.x ) maxScroll.x = minScroll.x;
        if ( maxScroll.y < minScroll.y ) maxScroll.y = minScroll.y;
    }

    /**
     * Returns the maximum scroll coordinates for this Widget
     *
     * @return the maximum scroll coordinates for this widget
     */

    protected Point getMaxScroll( ) { return maxScroll; }

    /**
     * Returns the minimum scroll coordinates for this Widget
     *
     * @return the minimum scroll coordinates for this widget
     */

    protected Point getMinScroll( ) { return minScroll; }

    /**
     * Returns the current x scroll coordinate
     *
     * @return the current x scroll coordinate
     */

    public int getScrollX( ) { return scroll.x; }

    /**
     * Returns the current y scroll coordinate
     *
     * @return the current y scroll coordinate
     */

    public int getScrollY( ) { return scroll.y; }

    /**
     * Scrolls the view of this AreaViewer by the specified amount, capped
     * by the maximum and minimum scrolls.  Also scrolls appropriate widgets
     * such as overheadfadeaways
     *
     * @param x the x scroll amount
     * @param y the y scroll amount
     * @return the amount that was actually scrolled by.  This can be closer
     * to 0 than x or y if the scroll was capped by the max or min scrolls.
     */

    public Point scroll( int x, int y )
    {
        return scroll( x, y, false );
    }

    private Point scroll( int x, int y, boolean delayedScroll )
    {
        // don't allow scrolling by other means when a delayed scroll is active
        if ( ! delayedScroll && this.delayedScroll != null )
        { return new Point( ); }

        int scrollXAmount, scrollYAmount;

        scrollXAmount = Math.min( maxScroll.x - scroll.x, x );
        scrollXAmount = Math.max( minScroll.x - scroll.x, scrollXAmount );

        scrollYAmount = Math.min( maxScroll.y - scroll.y, y );
        scrollYAmount = Math.max( minScroll.y - scroll.y, scrollYAmount );

        scroll.x += scrollXAmount;
        scroll.y += scrollYAmount;

        if ( Game.mainViewer != null )
        {
            EntityMouseover mo = Game.mainViewer.getMouseOver( );
            int moX = mo.getX( );
            int moY = mo.getY( );

            mo.setPosition( moX - scrollXAmount, moY - scrollYAmount );

            for ( OverHeadFadeAway fadeAway : Game.mainViewer.getFadeAways( ) )
            {
                fadeAway.scroll( scrollXAmount, scrollYAmount );
            }
        }

        return new Point( scrollXAmount, scrollYAmount );
    }

    /**
     * draws the visibility of the Area viewed by this AreaViewer
     *
     * @param visibility the visibility matrix to be drawn
     */

    private void drawVisibility( boolean[][] visibility, AnimationState as, Point topLeft, Point bottomRight )
    {
        boolean[][] explored = area.getExplored( );

        for ( int x = topLeft.x; x <= bottomRight.x; x++ )
        {
            for ( int y = topLeft.y; y <= bottomRight.y; y++ )
            {
                Point screenPoint = AreaUtil.convertGridToScreen( x, y );

                if ( ! explored[ x ][ y ] )
                {
                    hexFilledBlack.draw( as, screenPoint.x, screenPoint.y );
                }
                else if ( ! visibility[ x ][ y ] )
                {
                    hexFilledGrey.draw( as, screenPoint.x, screenPoint.y );
                }
            }
        }
        GL11.glColor3f( 1.0f, 1.0f, 1.0f );
    }

    private void drawCreatureVisibility( Creature creature, AnimationState as, Point topLeft, Point bottomRight )
    {
        boolean[][] explored = area.getExplored( );

        for ( int x = topLeft.x; x <= bottomRight.x; x++ )
        {
            for ( int y = topLeft.y; y <= bottomRight.y; y++ )
            {
                Point screenPoint = AreaUtil.convertGridToScreen( x, y );

                if ( ! explored[ x ][ y ] )
                {
                    hexFilledBlack.draw( as, screenPoint.x, screenPoint.y );
                }
                else if ( ! creature.hasVisibilityInCurrentArea( x, y ) )
                {
                    hexFilledGrey.draw( as, screenPoint.x, screenPoint.y );
                }
            }
        }
        GL11.glColor3f( 1.0f, 1.0f, 1.0f );
    }

    /**
     * Draws all Area Transitions within the currently viewed Area
     */

    @Override
    public void drawTransitions( )
    {
        GL11.glColor3f( 1.0f, 1.0f, 1.0f );

        for ( String s : area.getTransitions( ) )
        {
            Transition transition = Game.curCampaign.getAreaTransition( s );

            if ( ! transition.isActivated( ) ) continue;

            Transition.EndPoint endPoint = transition.getEndPointInArea( area );
            Point screen = AreaUtil.convertGridToScreen( endPoint.getX( ), endPoint.getY( ) );
            transition.getIcon( ).draw( screen.x, screen.y );
        }
    }

    public final void drawRedHex( Point gridPoint, AnimationState as )
    {
        Point screen = AreaUtil.convertGridToScreen( gridPoint );
        hexRed.draw( as, screen.x, screen.y );
    }

    public final void drawWhiteHex( Point gridPoint, AnimationState as )
    {
        Point screen = AreaUtil.convertGridToScreen( gridPoint );
        hexWhite.draw( as, screen.x, screen.y );
    }

    public final void drawGreenHex( Point gridPoint, AnimationState as )
    {
        Point screen = AreaUtil.convertGridToScreen( gridPoint );
        hexGreen.draw( as, screen.x, screen.y );
    }

    public final void drawBlueHex( Point gridPoint, AnimationState as )
    {
        Point screen = AreaUtil.convertGridToScreen( gridPoint );
        hexBlue.draw( as, screen.x, screen.y );
    }

    public final void drawGreyHex( Point gridPoint, AnimationState as )
    {
        Point screen = AreaUtil.convertGridToScreen( gridPoint );
        hexGrey.draw( as, screen.x, screen.y );
    }

    public final void drawAnimHex( int gridX, int gridY, AnimationState as )
    {
        Point screen = AreaUtil.convertGridToScreen( gridX, gridY );
        hexAnim.draw( as, screen.x, screen.y );
    }

    public final void drawAnimHex( Point point, AnimationState as )
    {
        drawAnimHex( point.x, point.y, as );
    }

    @Override
    protected boolean handleEvent( Event evt )
    {
        if ( super.handleEvent( evt ) ) return true;

        return areaListener.handleEvent( evt );
    }

    @Override
    protected void applyTheme( ThemeInfo themeInfo )
    {
        super.applyTheme( themeInfo );

        areaListener.setThemeInfo( themeInfo );

        hexFilledBlack = themeInfo.getImage( "hexfilledblack" );
        hexFilledGrey = themeInfo.getImage( "hexfilledgrey" );

        hexWhite = themeInfo.getImage( "hexwhite" );
        hexRed = themeInfo.getImage( "hexred" );
        hexBlue = themeInfo.getImage( "hexblue" );
        hexGreen = themeInfo.getImage( "hexgreen" );
        hexGrey = themeInfo.getImage( "hexgrey" );
        hexAnim = themeInfo.getImage( "hexanim" );

        this.getAnimationState( ).setAnimationState( STATE_ACTIVE, true );
    }

    private class DelayedScroll
    {
        private int startScrollX;
        private int startScrollY;

        private float scrollXPerMilli;
        private float scrollYPerMilli;

        private long startTime, endTime;
    }

    private class ScreenShake
    {
        private long endTime;

        private long lastTime;

        private int lastShakeX, lastShakeY;
    }
}
