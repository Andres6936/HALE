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

import net.sf.hale.Game;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;

import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.utils.TintAnimator;

/**
 * A string of text displaying a recent status change, such as damage taken or healed,
 * or a skill check, for a short period of time.
 *
 * @author Jared
 */

public class OverHeadFadeAway extends Widget
{
    private final Label textLabel;
    private long startTime;
    private int color;
    private boolean isFinished;
    private final TintAnimator tintAnimator;

    private final Point gridPoint;
    private Point basePosition;
    private Point offset;

    /**
     * Creates a new fade away with the specified text over the specified grid point
     *
     * @param text
     * @param gridPoint
     * @param color     the text color
     */

    public OverHeadFadeAway( String text, Point gridPoint, Color color )
    {
        this.setSize( 50, 20 );

        this.color = color.toARGB( );

        this.tintAnimator = new TintAnimator( new TintAnimator.GUITimeSource( this ) );
        this.tintAnimator.setColor( new Color( this.color ) );

        textLabel = new Label( text );
        textLabel.setTheme( "textlabel" );
        textLabel.setSize( 50, 15 );
        textLabel.setPosition( 0, 0 );
        textLabel.setTintAnimator( tintAnimator );
        this.add( textLabel );

        this.gridPoint = gridPoint;
    }

    /**
     * Returns the area grid coordinates that this fade away is over
     *
     * @return the grid coordinates of this fade away
     */

    public Point getGridPoint( ) { return gridPoint; }

    /**
     * This should be called when adding the fade away to the user interface
     * causes the fade away to begin counting down the time and slowly fading
     *
     * @param startTime the current time or the last frame update time
     */

    public void initialize( long startTime )
    {
        this.startTime = startTime;

        textLabel.setSize( textLabel.computeTextWidth( ), 15 );

        basePosition = AreaUtil.convertGridToScreen( gridPoint );
        basePosition.x -= Game.areaViewer.getScrollX( ) + textLabel.getWidth( ) / 2 - Game.TILE_SIZE / 2 - Game.areaViewer.getX( );
        basePosition.y -= Game.areaViewer.getScrollY( ) + 15 - Game.areaViewer.getY( );
        this.setPosition( basePosition.x, basePosition.y );

        if ( offset == null ) offset = new Point( );
    }

    /**
     * Sets an offset position that affects where this fadeaway is drawn
     *
     * @param x
     * @param y
     */

    public void setOffset( int x, int y )
    {
        offset = new Point( x, y );
    }

    /**
     * Called whenever the user scrolls the view, so that the fadeaway stays over the correct grid
     * tile
     *
     * @param x
     * @param y
     */

    public void scroll( int x, int y )
    {
        basePosition.x -= x;
        basePosition.y -= y;
    }

    /**
     * Called every frame, this updates the state of the fade away based on the time
     *
     * @param curTime the current frame time
     */

    public void updateTime( long curTime )
    {
        if ( curTime > startTime + 2000 )
        {
            isFinished = true;
            color = 0x00FF0000;
        }
        else if ( curTime > startTime + 1000 )
        {
            tintAnimator.setColor( new Color( color - 0x01000000 * ( int ) ( ( curTime - startTime - 1000 ) / 4 ) ) );
        }

        int yPosition = Math.max( 50, basePosition.y + offset.y ) - ( int ) ( curTime - startTime ) / 20;

        this.setPosition( basePosition.x + offset.x, yPosition );
    }

    /**
     * Returns true if this fade away has completed faded from view, false otherwise
     *
     * @return whether this fade away has faded
     */

    public boolean isFinished( )
    {
        return isFinished;
    }
}
