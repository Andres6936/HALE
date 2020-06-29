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

package main.java.hale.view;

import main.java.hale.Game;
import main.java.hale.area.Area;
import main.java.hale.area.Transition;
import main.java.hale.entity.Creature;
import main.java.hale.entity.Entity;
import main.java.hale.entity.Location;
import main.java.hale.entity.Trap;
import main.java.hale.util.Point;

import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.AnimationState;
import de.matthiasmann.twl.renderer.Image;

/**
 * A widget for showing a scaled down map of the current area with symbols
 * for different entities within the area
 *
 * @author Jared Stephen
 */

public class MiniMapWindow extends GameSubWindow
{
    private Image tile, impass, friendly, neutral, hostile, container, door, trap, transition;
    private Image viewport;
    private int scale, areaViewerWidth, areaViewerHeight;
    private boolean scrollToSelectedCreatureOnLayout;

    private Area area;

    private int legendGap;
    private Button legend;

    private ScrollPane scrollPane;
    private Content content;

    /**
     * Creates an empty mini map Widget.  updateContent should be used to set
     * the Area being viewed.
     */

    public MiniMapWindow()
    {
        content = new Content();
        scrollPane = new ScrollPane(content);
        scrollPane.setTheme("mappane");
        this.add(scrollPane);

        legend = new Button();
        legend.setTheme("legendbutton");
        legend.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                LegendPopup popup = new LegendPopup(Game.mainViewer);
                popup.setPosition(legend.getX(), legend.getBottom());
                popup.openPopup();
            }
        });
        this.add(legend);
    }

    @Override
    protected void layout()
    {
        super.layout();

        Widget closeButton = getChild(1);

        legend.setPosition(getInnerX(), getInnerY());
        legend.setSize(legend.getPreferredWidth(), closeButton.getHeight());
        legend.setPosition(closeButton.getX() - legend.getWidth() - legendGap, closeButton.getY());
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo)
    {
        super.applyTheme(themeInfo);

        tile = themeInfo.getImage("tile");
        impass = themeInfo.getImage("impassableTile");
        friendly = themeInfo.getImage("friendlyCreature");
        neutral = themeInfo.getImage("neutralCreature");
        hostile = themeInfo.getImage("hostileCreature");
        container = themeInfo.getImage("container");
        door = themeInfo.getImage("door");
        trap = themeInfo.getImage("trap");
        transition = themeInfo.getImage("transition");

        viewport = themeInfo.getImage("viewport");

        content.tileSize = tile.getWidth();
        content.tileWidth = content.tileSize * 3 / 4;
        content.tileHalf = content.tileSize / 2;
        content.tileQuarter = content.tileSize / 4;

        legendGap = themeInfo.getParameter("legendgap", 0);
        scale = Game.TILE_SIZE / content.tileSize;
    }

    @Override
    public void setVisible(boolean visible)
    {
        super.setVisible(visible);

        // scroll to selected creature
        if (visible) {
            scrollToSelectedCreature();
        }
    }

    /**
     * Causes the scrollpane containing the minimap to scroll to the creature
     * that is currently selected by the player
     */

    public void scrollToSelectedCreature()
    {
        Entity selected = Game.curCampaign.party.getSelected();

        Location location = selected.getLocation();

        int screenX = location.getX() * content.tileWidth;
        int screenY = location.getY() * content.tileSize;
        if (location.getX() % 2 == 1) screenY += content.tileHalf;

        scrollPane.setScrollPositionX(screenX - scrollPane.getWidth() / 2);
        scrollPane.setScrollPositionY(screenY - scrollPane.getHeight() / 2);
    }

    public void updateContent(Area area)
    {
        if (this.area != area && area != null) {
            this.area = area;
            this.setTitle(area.getName());

            this.setSize(Math.min(Game.areaViewer.getInnerWidth(), getMaxWidth()),
                    Math.min(Game.areaViewer.getInnerHeight(), getMaxHeight()));

            if (getWidth() > getMaxWidth() || getHeight() > getMaxHeight()) {

            }

            scrollToSelectedCreatureOnLayout = true;
        }

        areaViewerWidth = Game.areaViewer.getInnerWidth() / scale;
        areaViewerHeight = Game.areaViewer.getInnerHeight() / scale;
    }

    @Override
    public int getMaxWidth()
    {
        return content.getPreferredWidth() + scrollPane.getBorderHorizontal() + getBorderHorizontal();
    }

    @Override
    public int getMaxHeight()
    {
        return content.getPreferredHeight() + scrollPane.getBorderVertical() + getBorderVertical();
    }

    private class Content extends Widget
    {
        private int tileSize, tileWidth, tileHalf, tileQuarter;

        @Override
        protected void layout()
        {
            if (scrollToSelectedCreatureOnLayout) {
                scrollToSelectedCreature();
                scrollToSelectedCreatureOnLayout = false;
            }
        }

        @Override
        protected boolean handleEvent(Event evt)
        {
            switch (evt.getType()) {
                case MOUSE_ENTERED:
                    return true;
                case MOUSE_BTNDOWN:
                case MOUSE_DRAGGED:
                    int x = (evt.getMouseX() - getInnerX() + tileQuarter) * scale;
                    int y = (evt.getMouseY() - getInnerY() + tileHalf) * scale;

                    Game.areaViewer.addDelayedScrollToScreenPoint(new Point(x, y));

                    return true;
                default:
                    return super.handleEvent(evt);
            }
        }

        @Override
        public int getPreferredWidth()
        {
            return tileWidth * area.getWidth() - tileHalf + getBorderHorizontal();
        }

        @Override
        public int getPreferredHeight()
        {
            return tileSize * area.getHeight() - tileSize + getBorderVertical();
        }

        @Override
        public void paintWidget(GUI gui)
        {
            Creature selected = Game.curCampaign.party.getSelected();

            AnimationState as = getAnimationState();

            GL11.glPushMatrix();
            GL11.glTranslatef(getInnerX() - tileQuarter, getInnerY() - tileHalf, 0.0f);

            boolean[][] pass = area.getPassability();
            boolean[][] explored = area.getExplored();
            boolean[][] visible = area.getVisibility();

            for (int y = 0; y < explored[0].length; y++) {
                for (int x = 0; x < explored.length; x++) {
                    if (!explored[x][y]) continue;

                    int screenX = x * tileWidth;
                    int screenY = y * tileSize;
                    if (x % 2 == 1) screenY += tileHalf;

                    if (!pass[x][y] || area.getElevationGrid().getElevation(x, y) != 0) {
                        impass.draw(as, screenX, screenY);
                    } else {
                        tile.draw(as, screenX, screenY);
                    }

                    Transition areaTransition = area.getTransitionAtGridPoint(x, y);
                    if (areaTransition != null && areaTransition.isActivated()) {
                        transition.draw(as, screenX, screenY);
                    }

                    if (area.getContainerAtGridPoint(x, y) != null) {
                        container.draw(as, screenX, screenY);
                    }

                    if (area.getDoorAtGridPoint(x, y) != null) {
                        door.draw(as, screenX, screenY);
                    }

                    Trap areaTrap = area.getTrapAtGridPoint(x, y);

                    if (areaTrap != null && areaTrap.isSpotted()) {
                        trap.draw(as, screenX, screenY);
                    }

                    // only draw creatures for visible tiles
                    if (!visible[x][y]) continue;

                    Creature creature = area.getCreatureAtGridPoint(x, y);
                    if (creature != null) {
                        switch (selected.getFaction().getRelationship(creature)) {
                            case Hostile:
                                hostile.draw(as, screenX, screenY);
                                break;
                            case Neutral:
                                neutral.draw(as, screenX, screenY);
                                break;
                            case Friendly:
                                friendly.draw(as, screenX, screenY);
                                break;
                        }
                    }


                }
            }

            int viewportX = Game.areaViewer.getScrollX() / scale;
            int viewportY = Game.areaViewer.getScrollY() / scale;

            viewport.draw(as, viewportX, viewportY, areaViewerWidth, areaViewerHeight);

            GL11.glPopMatrix();
        }
    }

    private class LegendPopup extends PopupWindow
    {
        private LegendPopup(Widget owner)
        {
            super(owner);

            Content content = new Content();
            add(content);
        }

        private class Content extends Widget
        {
            private int labelGap;
            private Label[] labels;
            private Image[] images;

            private static final int numRows = 7;

            private Content()
            {
                labels = new Label[numRows];
                images = new Image[numRows];

                for (int i = 0; i < numRows; i++) {
                    labels[i] = new Label();
                    add(labels[i]);
                }

                labels[0].setTheme("friendlylabel");
                labels[1].setTheme("neutrallabel");
                labels[2].setTheme("hostilelabel");
                labels[3].setTheme("containerlabel");
                labels[4].setTheme("doorlabel");
                labels[5].setTheme("traplabel");
                labels[6].setTheme("transitionlabel");

                images[0] = friendly;
                images[1] = neutral;
                images[2] = hostile;
                images[3] = container;
                images[4] = door;
                images[5] = trap;
                images[6] = transition;
            }

            @Override
            protected void applyTheme(ThemeInfo themeInfo)
            {
                super.applyTheme(themeInfo);

                this.labelGap = themeInfo.getParameter("labelgap", 0);
            }

            @Override
            protected void layout()
            {
                setSize(getPreferredWidth(), getPreferredHeight());

                int curY = getInnerY();

                for (int i = 0; i < numRows; i++) {
                    labels[i].setSize(labels[i].getPreferredWidth(), images[i].getHeight());
                    labels[i].setPosition(getInnerX() + images[i].getWidth() + labelGap, curY);

                    curY = labels[i].getBottom();
                }
            }

            @Override
            protected void paintWidget(GUI gui)
            {
                AnimationState as = getAnimationState();

                int curY = getInnerY();
                for (int i = 0; i < numRows; i++) {
                    images[i].draw(as, getInnerX(), curY);
                    curY += images[i].getHeight();
                }
            }

            @Override
            public int getPreferredInnerWidth()
            {
                int max = 0;

                for (int i = 0; i < numRows; i++) {
                    max = Math.max(max, images[i].getWidth() + labels[i].getPreferredWidth() + labelGap);
                }

                return max;
            }

            @Override
            public int getPreferredInnerHeight()
            {
                int height = 0;
                for (int i = 0; i < numRows; i++) {
                    height += images[i].getHeight();
                }

                return height;
            }
        }
    }


}
