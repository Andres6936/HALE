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

package main.java.hale;

import java.util.ArrayList;
import java.util.List;

import main.java.hale.util.AreaUtil;
import main.java.hale.util.Point;

/**
 * Class for keeping track of time elapsed while not in combat mode,
 * and elapsing rounds as needed.
 *
 * @author Jared Stephen
 */

public class GameTimer
{
    private final List<TemporarySightArea> sightAreas;

    private long lastRoundTime;

    /**
     * Creates a new GameTimer object
     */

    public GameTimer()
    {
        sightAreas = new ArrayList<>();
    }

    /**
     * Adds a temporary party sight area that the party will be able to see into
     *
     * @param position the center location of the sight area
     * @param radius   the radius of the sight area
     * @param duration the duration in rounds that the party will be able to see
     *                 into the area, if set to 0, this sight area will need to be manually cleared
     */

    public void addTemporarySightArea(Point position, int radius, int duration)
    {
        TemporarySightArea area = new TemporarySightArea();

        int width = Game.curCampaign.curArea.getWidth();
        int height = Game.curCampaign.curArea.getHeight();

        area.points = new ArrayList<>();

        if (position.x >= 0 && position.y >= 0 && position.x < width && position.y < height) {
            area.points.add(position);
        }

        for (int r = 1; r <= radius; r++) {
            for (int i = 0; i < r * 6; i++) {
                Point grid = AreaUtil.convertPolarToGrid(position, r, i);

                if (grid.x >= 0 && grid.y >= 0 && grid.x < width && grid.y < height) {
                    area.points.add(grid);
                }
            }
        }

        area.roundsRemaining = duration;

        sightAreas.add(area);

        Game.curCampaign.curArea.getUtil().setPartyVisibility();
    }

    public void clearTemporarySightAreas()
    {
        sightAreas.clear();

        Game.curCampaign.curArea.getUtil().setPartyVisibility();
    }

    /**
     * Sets the current time being tracked by this GameTimer to the specified value.
     * If the number of seconds per round has elapsed since the last round update
     * and the game is in real time (non combat) mode, then a new round is started.
     *
     * @param curTime the current time in milliseconds
     */

    public void updateTime(long curTime)
    {
        if (Game.isInTurnMode()) {
            lastRoundTime = curTime;
        } else
            if (curTime - lastRoundTime > Game.curCampaign.getDate().roundMillis) {
                lastRoundTime = curTime;

                Game.areaListener.nextTurn();

                for (int i = 0; i < sightAreas.size(); i++) {
                    TemporarySightArea area = sightAreas.get(i);

                    if (area.roundsRemaining != 0) {
                        area.roundsRemaining--;
                        if (area.roundsRemaining == 0) {
                            sightAreas.remove(i);
                            i--;
                        }
                    }
                }
            }
    }

    /**
     * Updates the visibility matrix with any temporary visibility areas being tracked by
     * the timer
     *
     * @param vis the visibility matrix
     */

    public void getTemporaryVisibilityAreas(boolean[][] vis)
    {
        for (TemporarySightArea area : sightAreas) {
            for (Point point : area.points) {
                vis[point.x][point.y] = true;
            }
        }
    }

    /**
     * Resets the last round update time to the current time.
     */

    public void resetTime()
    {
        lastRoundTime = System.currentTimeMillis();
    }

    private static class TemporarySightArea
    {
        private List<Point> points;
        private int roundsRemaining;
    }
}
