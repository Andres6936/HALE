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

package hale.view;

import java.util.List;

import hale.Game;
import hale.area.Transition;
import hale.entity.Entity;
import hale.icon.Icon;
import hale.resource.Sprite;
import hale.rules.Date;
import hale.rules.WorldMapLocation;
import hale.util.Logger;

import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;

/**
 * A Widget that displays the {@link Sprite} that is specified as the
 * world map image for a given Campaign.  The Widget can also display
 * icons for a list of WorldMapLocations.
 *
 * @author Jared Stephen
 */

public class WorldMapViewer extends Widget
{
    private Sprite backgroundSprite;
    private WorldMapLocation origin;

    private WorldMapPopup popup;

    /**
     * Create a new WorldMapViewer widget.  No locations are shown
     * until {@link #updateLocations(List)} is called.  The sprite that
     * is drawn will be the current campaign world map sprite.
     *
     * @param transition the current transition being used by the player, or null if no
     *                   transition is currently active
     */

    public WorldMapViewer(Transition transition)
    {
        if (transition != null) {
            this.origin = Game.curCampaign.getWorldMapLocation(transition.getWorldMapLocation());
        }

        updateSprite();
    }

    /**
     * Sets the owning popup window for this viewer
     *
     * @param popup
     */

    public void setWorldMapPopup(WorldMapPopup popup)
    {
        this.popup = popup;
    }

    /**
     * Sets the sprite drawn by this viewer to the current campaign world map sprite,
     * if one exists
     */

    public void updateSprite()
    {
        if (Game.curCampaign != null) {
            this.backgroundSprite = Game.curCampaign.getWorldMapSprite();
        } else {
            this.backgroundSprite = null;
        }
    }

    @Override
    public void paintWidget(GUI gui)
    {
        super.paintWidget(gui);

        GL11.glColor3f(1.0f, 1.0f, 1.0f);

        if (backgroundSprite != null) {
            backgroundSprite.draw(getInnerX(), getInnerY());
        }
    }

    @Override
    public int getPreferredWidth()
    {
        if (backgroundSprite == null) {
            return getBorderHorizontal();
        } else {
            return backgroundSprite.getWidth() + getBorderHorizontal();
        }
    }

    @Override
    public int getPreferredHeight()
    {
        if (backgroundSprite == null) {
            return getBorderVertical();
        } else {
            return backgroundSprite.getHeight() + getBorderVertical();
        }
    }

    @Override
    protected void layout()
    {
        for (int i = 0; i < getNumChildren(); i++) {
            Widget child = getChild(i);

            if (!(child instanceof LocationViewer)) continue;

            LocationViewer viewer = (LocationViewer)child;

            viewer.setSize(viewer.getPreferredWidth(), viewer.getPreferredHeight());
            viewer.setPosition(getInnerX() + viewer.relativeX, getInnerY() + viewer.relativeY);
        }
    }

    private void travel(WorldMapLocation origin, WorldMapLocation destination)
    {
        // used for mock world map viewers
        if (origin == null) return;

        popup.closePopup();

        String transitionID = destination.getStartingTransition();
        if (transitionID == null) {
            Logger.appendToErrorLog("World Map Location " + destination.getName() +
                    " has no area transition defined.");
        }

        Transition transition = Game.curCampaign.getAreaTransition(transitionID);

        int travelTime = origin.getTravelTime(destination);
        Date date = Game.curCampaign.getDate();
        int travelTimeRounds = date.ROUNDS_PER_HOUR * travelTime;

        // elapse the rounds for all entities being tracked
        for (Entity entity : Game.curCampaign.curArea.getEntities()) {
            entity.elapseTime(travelTimeRounds);
        }

        date.incrementHours(travelTime);
        Game.curCampaign.transition(transition, true);
        Game.timer.resetTime();
    }

    /**
     * The specified WorldMapLocations will be displayed as icons in
     * future rendering of this Widget.  Any previous locations not in
     * the supplied list are discarded.
     *
     * @param locations The List of WorldMapLocations to be shown
     */

    public void updateLocations(List<WorldMapLocation> locations)
    {
        removeAllChildren();

        // first, add the location viewer for the origin (so it is always below other hovers)
        if (origin != null) {
            LocationViewer originViewer = new LocationViewer(origin);
            add(originViewer);
        }

        // now, add the location viewer for all other locations
        for (WorldMapLocation location : locations) {
            if (location == origin) continue;

            LocationViewer viewer = new LocationViewer(location);
            add(viewer);
        }
    }

    private class LocationHover extends Widget
    {
        private int labelOverlap;

        private LocationViewer parent;

        private Label name;
        private Icon icon;
        private int spriteX, spriteY;

        private Button travel;

        private WorldMapLocation location;

        private boolean isHovering = true;

        private LocationHover(LocationViewer parent)
        {
            this.parent = parent;
            this.location = parent.location;

            name = new Label(location.getName());
            add(name);

            icon = location.getIcon();

            travel = new Button();
            travel.addCallback(new Runnable()
            {
                @Override
                public void run()
                {
                    travel(origin, location);
                }
            });
            add(travel);

            if (origin == null) {
                travel.setText("Travel: 9 Days 23 Hours");
            } else
                if (origin == location) {
                    travel.setVisible(false);
                } else {
                    travel.setText("Travel: " + Game.curCampaign.getDate().getDateString(0, 0, origin.getTravelTime(location), 0, 0));
                }
        }

        @Override
        protected void applyTheme(ThemeInfo themeInfo)
        {
            super.applyTheme(themeInfo);

            labelOverlap = themeInfo.getParameter("labeloverlap", 0);
        }

        @Override
        public void paintWidget(GUI gui)
        {
            super.paintWidget(gui);

            GL11.glColor3f(1.0f, 1.0f, 1.0f);

            icon.draw(getInnerX() + spriteX, getInnerY() + spriteY);
        }

        @Override
        public int getPreferredInnerWidth()
        {
            int width = Math.max(name.getPreferredWidth(), icon.getWidth());
            width = Math.max(width, travel.getPreferredWidth());

            return width;
        }

        @Override
        public int getPreferredWidth()
        {
            return getPreferredInnerWidth() + getBorderHorizontal();
        }

        @Override
        public int getPreferredHeight()
        {
            int height = name.getPreferredHeight() + getBorderVertical() + icon.getHeight() - labelOverlap;

            if (travel.isVisible()) height += travel.getPreferredHeight();

            return height;
        }

        @Override
        protected void layout()
        {
            setSize(getPreferredWidth(), getPreferredHeight());

            name.setSize(name.getPreferredWidth(), name.getPreferredHeight() - labelOverlap);

            spriteY = name.getHeight();
            spriteX = (getWidth() - icon.getWidth()) / 2;

            setPosition(WorldMapViewer.this.getInnerX() + parent.relativeX - spriteX - getBorderLeft(),
                    WorldMapViewer.this.getInnerY() + parent.relativeY - spriteY - getBorderTop());

            name.setPosition(getInnerX() + (getInnerWidth() - name.getWidth()) / 2, getInnerY());

            travel.setSize(travel.getPreferredWidth(), travel.getPreferredHeight());
            travel.setPosition(getInnerX() + (getInnerWidth() - travel.getWidth()) / 2,
                    getInnerY() + spriteY + icon.getHeight());
        }

        private void handleHover(Event evt)
        {
            if (evt.isMouseEvent()) {
                boolean hover = evt.getType() != Event.Type.MOUSE_EXITED && isMouseInside(evt);

                if (hover && !isHovering) {
                    isHovering = true;
                } else
                    if (!hover && isHovering) {
                        isHovering = false;
                        parent.endHover();
                    }
            }
        }

        @Override
        protected boolean handleEvent(Event evt)
        {
            handleHover(evt);

            // swallow up all events
            return true;
        }
    }

    private class LocationViewer extends Widget
    {
        private LocationHover hover;

        private WorldMapLocation location;
        private Icon icon;

        private boolean isHovering = false;

        private final int relativeX, relativeY;

        private LocationViewer(WorldMapLocation location)
        {
            this.location = location;

            if (location.getIcon() != null) {
                icon = location.getIcon();
            }

            relativeX = location.getIconPositionX();
            relativeY = location.getIconPositionY();

            // always show the hover for the origin
            if (location == origin) {
                startHover();
            }
        }

        @Override
        public void paintWidget(GUI gui)
        {
            super.paintWidget(gui);

            GL11.glColor3f(1.0f, 1.0f, 1.0f);

            icon.draw(getInnerX(), getInnerY());
        }

        @Override
        public int getPreferredWidth()
        {
            int width = getBorderHorizontal();

            width += icon.getWidth();

            return width;
        }

        @Override
        public int getPreferredHeight()
        {
            int height = getBorderVertical();

            height += icon.getHeight();

            return height;
        }

        private void startHover()
        {
            hover = new LocationHover(LocationViewer.this);
            WorldMapViewer.this.add(hover);
        }

        private void endHover()
        {
            // don't allow the hover to be removed for the origin
            if (location == origin) return;

            if (hover != null) {
                WorldMapViewer.this.removeChild(hover);
                hover = null;
            }
        }

        private void handleHover(Event evt)
        {
            if (evt.isMouseEvent()) {
                boolean hover = evt.getType() != Event.Type.MOUSE_EXITED && isMouseInside(evt);

                if (hover && !isHovering) {
                    isHovering = true;
                    startHover();
                } else
                    if (!hover && isHovering) {
                        isHovering = false;
                        endHover();
                    }
            }
        }

        @Override
        protected boolean handleEvent(Event evt)
        {
            if (hover != null) return false;

            // don't allow hover events for the origin location
            if (location == origin) return false;

            handleHover(evt);

            switch (evt.getType()) {
                case MOUSE_ENTERED:
                    return true;
                default:
                    return super.handleEvent(evt);
            }
        }
    }
}
