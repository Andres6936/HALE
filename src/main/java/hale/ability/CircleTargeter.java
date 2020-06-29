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

package hale.ability;

import java.util.List;

import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.AnimationState;

import hale.Game;
import hale.entity.Creature;
import hale.util.AreaUtil;
import hale.util.Point;

/**
 * A Targeter that affects a circular area with a specified radius.
 * The Targeter can either be restricted to selecting among a set of
 * points or it can freely select.  The targeter can optionally be
 * set to affect creatures within the affected area.
 *
 * @author Jared Stephen
 */

public class CircleTargeter extends AreaTargeter
{
    private Point screenOrigin;
    private double drawRadius;

    private int radius;

    /**
     * Create a new CircleTargeter with no selection yet made.  By default, all
     * Points are allowed to be selected.  If any allowedPoints are added then
     * allowedPoints are then restricted to that set.
     *
     * @param parent     the parent Creature that is targeting using this Targeter
     * @param scriptable the scriptable responsible for managing the script that
     *                   will be used in the callback
     * @param slot       optional AbilitySlot that can be stored along with this Targeter
     */

    public CircleTargeter(Creature parent, Scriptable scriptable, AbilitySlot slot)
    {
        super(parent, scriptable, slot);
    }

    /**
     * Sets the radius, in tiles, for the circular area affected by
     * this Targeter.
     *
     * @param radius the radius in tiles for this Targeter
     */

    public void setRadius(int radius)
    {
        this.radius = radius;
        this.drawRadius = radius - 0.75;
    }

    @Override
    protected void computeAffectedPoints(int x, int y, Point gridPoint)
    {
        if (!mouseHoverValid()) return;

        List<Point> affectedPoints = this.getAffectedPoints();
        // compute set of affected points and creatures
        affectedPoints.add(new Point(gridPoint));
        for (int r = 1; r <= this.radius; r++) {
            for (int i = 0; i < r * 6; i++) {
                Point p = AreaUtil.convertPolarToGrid(gridPoint, r, i);

                affectedPoints.add(p);
            }
        }

        screenOrigin = AreaUtil.convertGridToScreenAndCenter(getMouseGridPosition());
    }

    @Override
    protected boolean updateMouseStateOnlyWhenGridPointChanges()
    {
        return true;
    }

    @Override
    public boolean draw(AnimationState as)
    {
        if (!super.draw(as)) {
            return false;
        }

        if (mouseHoverValid() && this.radius > 0) {
            GL11.glDisable(GL11.GL_TEXTURE_2D);

            GL11.glBegin(GL11.GL_LINE_LOOP);
            for (double t = 0; t <= 2 * Math.PI + 0.001; t += Math.PI / 32) {
                double x = drawRadius * Game.TILE_SIZE * Math.sin(t);
                double y = drawRadius * Game.TILE_SIZE * Game.TILE_RATIO * Math.cos(t);
                GL11.glVertex2d(screenOrigin.x + x, screenOrigin.y + y);
            }
            GL11.glEnd();

            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        return true;
    }
}