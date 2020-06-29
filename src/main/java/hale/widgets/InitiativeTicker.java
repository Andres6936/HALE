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

package hale.widgets;

import java.util.ArrayList;
import java.util.List;

import hale.Game;
import hale.entity.Creature;

import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;

/**
 * The ticker showing the list of active creatures in a given combat situation
 * and the order that they will take their actions in.  Allows the player to
 * wait by dragging their creature's icon
 *
 * @author Jared Stephen
 */

public class InitiativeTicker extends Widget
{
    private int minViewerGap;
    private int viewerGap;
    private String enabledTooltip, disabledTooltip;

    private ArrayList<CreatureViewer> viewers;

    /**
     * Creates a new initiative ticker
     */

    public InitiativeTicker()
    {
        viewers = new ArrayList<CreatureViewer>();
    }

    /**
     * Updates the list of creatures shown in this initiative ticker
     * If the game has entered or exited turn mode, adds or removes
     * creatures accordingly
     */

    public void updateContent()
    {
        removeAllChildren();
        viewers.clear();

        setVisible(Game.isInTurnMode());

        if (!Game.isInTurnMode()) return;

        // get the list of upcoming creatures - we will not use the whole list as the size of
        // a viewer is greater than TILE_SIZE, but we don't know exactly how many we need yet
        int numCreatures = getInnerHeight() / Game.TILE_SIZE;
        List<Creature> creatures = Game.areaListener.getCombatRunner().getNextCreatures(numCreatures);

        int heightSoFar = 0;

        // add the first (active) creature
        int index = 0;
        CreatureViewer viewer = new CreatureViewer(creatures.get(index), index);
        viewers.add(viewer);
        this.add(viewer);
        heightSoFar += viewer.getPreferredHeight() + minViewerGap;

        // set tooltip and active state
        if (viewer.creature.isPlayerFaction()) {
            viewer.setActive(true);

            if (!viewer.creature.timer.hasTakenAnAction()) {
                viewer.setTooltipContent(enabledTooltip);
            } else {
                viewer.setTooltipContent(disabledTooltip);
            }
        }

        for (index = 1; index < creatures.size(); index++) {
            viewer = new CreatureViewer(creatures.get(index), index);

            heightSoFar += viewer.getPreferredHeight();
            if (heightSoFar > getInnerHeight()) {
                // we didn't actually use this height
                heightSoFar -= viewer.getPreferredHeight();
                break;
            }

            viewers.add(viewer);
            add(viewer);

            heightSoFar += minViewerGap;
        }

        // now determine what the viewer gap should be to evenly fill out any extra space
        // at the bottom of the ticker
        int extraSpace = getInnerHeight() - heightSoFar;

        viewerGap = minViewerGap + extraSpace / (viewers.size() - 1);
    }

    @Override
    public int getPreferredWidth()
    {
        int maxWidth = 0;
        for (CreatureViewer viewer : viewers) {
            maxWidth = Math.max(maxWidth, viewer.getPreferredWidth());
        }

        return maxWidth + getBorderHorizontal();
    }

    @Override
    public int getPreferredHeight()
    {
        int height = getBorderVertical();
        for (CreatureViewer viewer : viewers) {
            height += viewer.getPreferredHeight();
        }

        return height;
    }

    @Override
    protected void layout()
    {
        super.layout();

        int lastY = getInnerY();
        for (CreatureViewer viewer : viewers) {
            viewer.setSize(viewer.getPreferredWidth(), viewer.getPreferredHeight());
            viewer.setPosition(getInnerX(), lastY);
            lastY = viewer.getBottom() + viewerGap;
        }
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo)
    {
        super.applyTheme(themeInfo);

        minViewerGap = themeInfo.getParameter("minViewerGap", 0);

        enabledTooltip = themeInfo.getParameter("enabledTooltip", (String)null);
        disabledTooltip = themeInfo.getParameter("disabledTooltip", (String)null);
    }

    private class CreatureViewer extends ToggleButton implements Runnable
    {
        private Creature creature;
        private int currentIndex;

        // keeps track of how many positions the mouse has dragged this viewer by
        private int placesForwardDrag;

        private CreatureViewer(Creature creature, int currentIndex)
        {
            this.creature = creature;
            this.currentIndex = currentIndex;
            this.placesForwardDrag = 0;
            addCallback(this);
        }

        @Override
        public int getPreferredWidth()
        {
            return Game.TILE_SIZE + getBorderHorizontal();
        }

        @Override
        public int getPreferredHeight()
        {
            return creature.getTemplate().getInitiativeTickerHeight() + getBorderVertical();
        }

        @Override
        public boolean handleEvent(Event evt)
        {
            // don't allow interaction with viewer unless it is the active (selected) one
            if (!this.isActive()) return false;

            switch (evt.getType()) {
                case MOUSE_DRAGGED:
                    checkMouseDrag(evt);
                    break;
                case MOUSE_BTNUP:
                    checkMouseDragRelease();
                    break;
                default:
                    // do nothing
            }

            return super.handleEvent(evt);
        }

        private void checkMouseDragRelease()
        {
            if (placesForwardDrag != 0) {
                Game.areaListener.getCombatRunner().activeCreatureWait(placesForwardDrag);
                placesForwardDrag = 0;
            }
        }

        private void checkMouseDrag(Event evt)
        {
            // don't allow repositioning for creatures without full AP
            if (creature.timer.hasTakenAnAction()) return;

            // check for movement up the list
            for (int i = 0; i < currentIndex; i++) {
                if (viewers.get(i).isMouseInside(evt)) {
                    placesForwardDrag += i - currentIndex;

                    // add the viewer at the current position
                    viewers.add(i, this);

                    // the list has shifted down by 1, so remove
                    // this viewer at its old position
                    viewers.remove(currentIndex + 1);
                    currentIndex = i;
                    invalidateLayout();
                    return;
                }
            }

            // check for movement down the list
            for (int i = currentIndex + 1; i < viewers.size(); i++) {
                // don't allow moving this creature's place next turn
                if (viewers.get(i).creature == this.creature) break;

                // to prevent an issue where the mouse is within the target but on layout the
                // target changes, adjust by the height difference
                // this prevents problems where the dragged widget can appear to rapidly go
                // back and forth between positions
                int yCheck = evt.getMouseY() + this.getHeight() - viewers.get(i).getHeight();

                if (yCheck >= viewers.get(i).getY() && yCheck <= viewers.get(i).getBottom()) {
                    placesForwardDrag += i - currentIndex;

                    // add the viewer at the current position
                    viewers.add(i + 1, this);

                    viewers.remove(currentIndex);
                    currentIndex = i;
                    invalidateLayout();

                    return;
                }
            }
        }

        @Override
        public void run()
        {
            setActive(true);
        }

        @Override
        public void paintWidget(GUI gui)
        {
            super.paintWidget(gui);

            creature.uiDraw(getInnerX(), getInnerY());
        }
    }
}
