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

package main.java.hale.ability;

import java.util.List;

import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.AnimationState;

import main.java.hale.Game;
import main.java.hale.entity.Creature;
import main.java.hale.entity.Location;
import main.java.hale.util.AreaUtil;
import main.java.hale.util.Point;

/**
 * A targeter that affects all grid Points along a line.
 * The target starts with a prespecified origin and then the
 * user selects an end point.  This end point can either be
 * freely selectable or confined to a list of specific points.
 *
 * @author Jared Stephen
 */

public class LineTargeter extends AreaTargeter
{
    private Point gridEnd;

    private Point gridOrigin;
    private Point screenOrigin;
    private int lineGridLength;

    private double lineAngle;
    private double lineLength;

    private boolean stopLineAtCreature;
    private boolean stopLineAtImpassable;

    /**
     * Create a new LineTargeter with no selection yet made.  By default, there is no
     * prespecified Line length.
     *
     * @param parent     the parent Creature that is targeting using this Targeter
     * @param scriptable the scriptable responsible for managing the script that
     *                   will be used in the callback
     * @param slot       optional AbilitySlot that can be stored along with this Targeter
     */

    public LineTargeter(Creature parent, Scriptable scriptable, AbilitySlot slot)
    {
        super(parent, scriptable, slot);

        this.lineGridLength = 0;
        this.stopLineAtCreature = false;
        this.stopLineAtImpassable = false;
    }

    /**
     * Sets whether this LineTargeter should be stopped by impassable terrain.  Note that
     * regardless of this method, the LineTargeter will always be stopped by non-transparent
     * terrain.
     *
     * @param stopLine whether this LineTargeter should stop when encountering impassable terrain
     */

    public void setStopLineAtImpassable(boolean stopLine)
    {
        this.stopLineAtImpassable = stopLine;
    }

    /**
     * Sets whether this LineTargeter should be stopped by Creatures in the same
     * way as it is always stopped by non transparent area tiles.
     *
     * @param stopLine whether this LineTargeter should stop when encountering a Creature
     */

    public void setStopLineAtCreature(boolean stopLine)
    {
        this.stopLineAtCreature = stopLine;
    }

    /**
     * Sets the grid origin for the line drawn by this LineTargeter.
     * The user then selects the other end of the line or the
     * direction of the line.
     *
     * @param location the origin of the line drawn by this LineTargeter
     */

    public void setOrigin(Location location)
    {
        setOrigin(location.toPoint());
    }

    /**
     * Sets the grid origin for the line drawn by this LineTargeter.
     * The user then selects the other end of the line or the
     * direction of the line.
     *
     * @param origin the origin of the line drawn by this LineTargeter
     */

    public void setOrigin(Point origin)
    {
        this.gridOrigin = new Point(origin);
        this.screenOrigin = AreaUtil.convertGridToScreenAndCenter(gridOrigin);
    }

    /**
     * Returns the grid point that this Line Targeter ends on.  This will
     * either be the selected point, the lineGridLength, or a Point closer
     * to the origin if a non-passable tile blocked the Targeter.
     *
     * @return the ending grid point
     */

    public Point getEndPoint()
    {
        return gridEnd;
    }

    /**
     * Forces the generated line to be a specific length in grid tiles.
     * With this option enabled, rather than specifying an end point for
     * the line, the user will instead be specifying a direction and the
     * line length will be predetermined by this function.
     * <p>
     * Note that the line length will still be shortened if the line encounters
     * obstacles such as non transparent area tiles or creatures (if
     * {@link #setStopLineAtCreature(boolean)} has been set to true).
     * <p>
     * Furthermore, note that setting this function will override the effects of
     * {@link Targeter#setMinRange(int)} and {@link Targeter#setMaxRange(int)}
     *
     * @param gridLength the length of the line drawn by this LineTargeter
     */

    public void setForceLineLength(int gridLength)
    {
        this.lineGridLength = gridLength;
    }

    @Override
    public boolean draw(AnimationState as)
    {
        if (!super.draw(as)) {
            return false;
        }

        double offsetX = Game.TILE_SIZE * Math.cos(lineAngle) / 20.0;
        double offsetY = -Game.TILE_SIZE * Math.sin(lineAngle) / 20.0;
        double xEnd = lineLength * Game.TILE_SIZE * Math.sin(lineAngle);
        double yEnd = lineLength * Game.TILE_SIZE * Math.cos(lineAngle);

        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2d(screenOrigin.x + offsetX, screenOrigin.y + offsetY);
        GL11.glVertex2d(screenOrigin.x + xEnd + offsetX, screenOrigin.y + yEnd + offsetY);

        GL11.glVertex2d(screenOrigin.x - offsetX, screenOrigin.y - offsetY);
        GL11.glVertex2d(screenOrigin.x + xEnd - offsetX, screenOrigin.y + yEnd - offsetY);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);

        return true;
    }

    @Override
    protected boolean updateMouseStateOnlyWhenGridPointChanges()
    {
        return false;
    }

    @Override
    protected void computeAffectedPoints(int x, int y, Point gridPoint)
    {
        List<Point> affectedPoints = this.getAffectedPoints();

        // compute the angle of the line to draw
        lineAngle = AreaUtil.angle(screenOrigin.x, screenOrigin.y, x, y);

        // determine the stopping point of the line
        double xEnd, yEnd;

        if (lineGridLength == 0) {
            xEnd = x;
            yEnd = y;
        } else {
            xEnd = screenOrigin.x + lineGridLength * Game.TILE_SIZE * Math.sin(lineAngle);
            yEnd = screenOrigin.y + lineGridLength * Game.TILE_SIZE * Math.cos(lineAngle);
        }

        // find set of intersecting hexes for line
        List<Point> points = AreaUtil.findIntersectingHexes(screenOrigin.x, screenOrigin.y, (int)xEnd, (int)yEnd);
        gridEnd = gridOrigin;

        for (Point p : points) {
            gridEnd = p;
            // if we have hit a wall in the area
            if (!Game.curCampaign.curArea.isTransparent(p.x, p.y)) {
                break;
            }

            if (stopLineAtImpassable && !Game.curCampaign.curArea.isPassable(p.x, p.y)) {
                break;
            }

            affectedPoints.add(p);

            // check to see if there is a creature at the specified point
            // if the line was stopped by a creature, the creature has already been added
            // to the list of affected creatures above
            if (stopLineAtCreature && Game.curCampaign.curArea.getCreatureAtGridPoint(p) != null) {
                break;
            }
        }

        // compute the length of the line to draw
        Point pScreen = AreaUtil.convertGridToScreen(gridEnd);
        pScreen.x += Game.TILE_SIZE / 2;
        pScreen.y += Game.TILE_SIZE / 2;

        lineLength = Math.sqrt(AreaUtil.euclideanDistance2(screenOrigin.x, screenOrigin.y,
                pScreen.x, pScreen.y)) / Game.TILE_SIZE - 0.5;

        // check to see if the line was stopped before reaching the Targeter minRange
        int distance = AreaUtil.distance(gridOrigin, gridEnd);
        if (distance < getMinRange()) {
            setMouseHoverValid(false);
        }

        // if the targeter is only specifying direction, not an actual point
        // then override the mouse hover condition so we can always select
        if (lineGridLength > 0) {
            setMouseHoverValid(true);
        }
    }
}
