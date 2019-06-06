package net.sf.hale.ability;

import java.util.List;

import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.AnimationState;

import net.sf.hale.Game;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Location;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;

/**
 * A targeter that affects all grid points within a cone of a specified
 * angle from a specified origin with a specified radius.
 * <p>
 * The player controlling the TargetSelect can freely rotate the cone
 * around the origin.
 *
 * @author Jared Stephen
 */

public class ConeTargeter extends AreaTargeter
{
    private int coneRadius;
    private int coneAngle;

    private Point gridOrigin;
    private Point screenOrigin;

    private double lineAngle;
    private double lineLength;

    private boolean[][] visible;

    private Point gridEnd;
    private Point screenEnd;

    private boolean hasVisibilityCriterion;

    /**
     * Creates a new ConeTargeter with no selection yet made.
     *
     * @param parent     the parent Creature that is targeting using this Targeter
     * @param scriptable the scriptable responsible for managing the script that
     *                   will be used in the callback
     * @param slot       optional AbilitySlot that can be stored along with this Targeter
     */

    public ConeTargeter( Creature parent, Scriptable scriptable, AbilitySlot slot )
    {
        super( parent, scriptable, slot );

        hasVisibilityCriterion = true;
    }

    /**
     * Sets whether this targeter only affects points visible by the parent.  By default, the
     * targeter only affects visible points
     *
     * @param hasVisibilityCriterion whether this targeter has a visibility criterion
     */

    public void setHasVisibilityCriterion( boolean hasVisibilityCriterion )
    {
        this.hasVisibilityCriterion = hasVisibilityCriterion;
    }

    /**
     * Sets the origin point for the cone drawn by this ConeTargeter
     *
     * @param location the origin point
     */

    public void setOrigin( Location location )
    {
        setOrigin( location.toPoint( ) );
    }

    /**
     * Sets the origin point for the cone drawn by this ConeTargeter
     * to the specified grid point
     *
     * @param origin the origin point for the Cone
     */

    public void setOrigin( Point origin )
    {
        this.gridOrigin = origin;
        this.screenOrigin = AreaUtil.convertGridToScreenAndCenter( origin );

        visible = Game.curCampaign.curArea.getMatrixOfSize( );
        Game.curCampaign.curArea.getUtil( ).setVisibilityWithRespectToPosition( visible, gridOrigin );
    }

    /**
     * Returns the grid point that this ConeTargeter ends on.  This is the point
     * in the middle of the cone, pointing in the direction specified by the mouse
     * cursor (relative to the origin), out to the distance specified by the cone
     * radius
     *
     * @return the ending grid point
     */

    public Point getEndPoint( )
    {
        return gridEnd;
    }

    /**
     * Returns the angle to the mouse cursor position from the origin.
     * This is the angle that runs through the center of the cone from
     * the origin, in a coordinate system for drawing on the screen
     *
     * @return the angle to the mouse cursor position from the origin.
     */

    public double getCenterAngle( )
    {
        return screenOrigin.angleTo( screenEnd );
    }

    /**
     * Sets the angle subtended by this cone.  A very small cone
     * angle approximates a LineTargeter, whereas a 360 degree cone
     * angle is a simple CircleTargeter.  The angle should be specified
     * in degrees.
     *
     * @param angle the angle subtended by this cone
     */

    public void setConeAngle( int angle )
    {
        this.coneAngle = angle;
    }

    /**
     * Sets the radius of the circle that the cone drawn by this
     * ConeTargeter is a piece of.
     *
     * @param radius the radius of this cone
     */

    public void setConeRadius( int radius )
    {
        this.coneRadius = radius;
    }

    @Override
    protected void computeAffectedPoints( int x, int y, Point gridPoint )
    {
        // the targeter is only specifying direction, not an actual point
        // so override the mouse hove condition so we can always select
        setMouseHoverValid( true );

        List< Point > affectedPoints = this.getAffectedPoints( );

        // compute the angle and length of a line starting at the origin and
        // going in the direction specified by the mouse coordinates for the
        // prespecified cone radius
        lineAngle = AreaUtil.angle( screenOrigin.x, screenOrigin.y, x, y );
        lineLength = coneRadius - 0.75;

        // compute the end point of the first edge of the cone
        double curAngle = lineAngle + ( coneAngle / 2.0 ) * ( Math.PI / 180.0 );
        double lineStartX = screenOrigin.x + lineLength * Game.TILE_SIZE * Math.sin( curAngle );
        double lineStartY = screenOrigin.y + lineLength * Game.TILE_SIZE * Game.TILE_RATIO * Math.cos( curAngle );

        // compute the end point of the second edge of the cone
        curAngle = lineAngle - ( coneAngle / 2.0 ) * ( Math.PI / 180.0 );
        double lineEndX = screenOrigin.x + lineLength * Game.TILE_SIZE * Math.sin( curAngle );
        double lineEndY = screenOrigin.y + lineLength * Game.TILE_SIZE * Game.TILE_RATIO * Math.cos( curAngle );

        // now iterate around the edge of the circle centered on the origin
        // when we pass through one of the edge points specified by lineStart and lineEnd,
        // we have found a tile that corresponds to one of the edges of the cone
        boolean foundStart = false;
        boolean foundEnd = false;

        for ( int r = coneRadius; r > 0; r-- )
        {
            for ( int i = 0; i < 12 * r; i++ )
            {
                Point grid = AreaUtil.convertPolarToGrid( gridOrigin, r, i );
                Point screen = AreaUtil.convertGridToScreen( grid );

                if ( AreaUtil.lineSegmentIntersectsHex( screen.x, screen.y,
                                                        screenOrigin.x, screenOrigin.y, ( int ) lineStartX, ( int ) lineStartY ) )
                { foundStart = true; }

                if ( foundStart )
                {
                    if ( visibility( grid ) )
                    {
                        affectedPoints.add( grid );
                    }

                    if ( AreaUtil.lineSegmentIntersectsHex( screen.x, screen.y,
                                                            screenOrigin.x, screenOrigin.y, ( int ) lineEndX, ( int ) lineEndY ) )
                    { foundEnd = true; }
                }

                if ( foundStart && foundEnd )
                {
                    foundStart = false;
                    foundEnd = false;
                    break;
                }
            }
        }

        // compute the ending point
        double screenEndX = screenOrigin.x + lineLength * Game.TILE_SIZE * Math.sin( lineAngle );
        double screenEndY = screenOrigin.y + lineLength * Game.TILE_SIZE * Game.TILE_RATIO * Math.cos( lineAngle );

        screenEnd = new Point( ( int ) screenEndX, ( int ) screenEndY );

        gridEnd = AreaUtil.convertScreenToGrid( screenEnd );
    }

    /*
     * Returns true if the specified point is visible in the matrix, false otherwise
     */

    private boolean visibility( Point p )
    {
        if ( ! hasVisibilityCriterion ) return true;

        if ( p.x < 0 || p.x >= visible.length || p.y < 0 || p.y >= visible[ 0 ].length ) return false;
        return visible[ p.x ][ p.y ];
    }

    @Override
    protected boolean updateMouseStateOnlyWhenGridPointChanges( )
    {
        return false;
    }

    @Override
    public boolean draw( AnimationState as )
    {
        if ( ! super.draw( as ) )
        {
            return false;
        }

        double coneAngleOver2 = ( coneAngle / 2.0 ) * ( Math.PI / 180.0 );

        GL11.glDisable( GL11.GL_TEXTURE_2D );

        GL11.glBegin( GL11.GL_LINE_LOOP );
        GL11.glVertex2d( screenOrigin.x, screenOrigin.y );
        for ( double t = lineAngle - coneAngleOver2; t <= lineAngle + coneAngleOver2 + 0.001; t += Math.PI / 32 )
        {
            double x = lineLength * Game.TILE_SIZE * Math.sin( t );
            double y = lineLength * Game.TILE_SIZE * Game.TILE_RATIO * Math.cos( t );
            GL11.glVertex2d( screenOrigin.x + x, screenOrigin.y + y );
        }
        GL11.glEnd( );

        GL11.glEnable( GL11.GL_TEXTURE_2D );

        return true;
    }
}
