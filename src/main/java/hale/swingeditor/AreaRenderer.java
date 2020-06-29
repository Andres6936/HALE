/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2012 Jared Stephen
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

package main.java.hale.swingeditor;

import java.awt.Canvas;

import javax.swing.SpinnerNumberModel;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.AnimationState;
import main.java.hale.area.Area;
import main.java.hale.resource.SpriteManager;
import main.java.hale.tileset.AreaTileGrid;
import main.java.hale.tileset.Tile;
import main.java.hale.util.AreaUtil;
import main.java.hale.util.Point;

/**
 * A class for viewing an area in the swing editor
 *
 * @author Jared
 */

public class AreaRenderer implements AreaTileGrid.AreaRenderer
{
    private final Canvas canvas;
    private final Area area;

    private SpinnerNumberModel mouseRadius;
    private Point mouseGrid;
    private Point mouseScreen;
    private int scrollX, scrollY;

    private Tile actionPreviewTile;
    private AreaPalette.AreaClickHandler clickHandler;
    private AreaRenderer.ViewHandler viewHandler;

    private static final int MaxRadius = 20;

    private long lastClickTime;
    private static final int MouseTimeout = 400;
    private boolean[] lastMouseState;

    private Point prevMouseGrid;

    private boolean drawPassable, drawTransparent;

    /**
     * Creates a new Viewer for the specified Area
     *
     * @param area
     * @param canvas
     */

    public AreaRenderer(Area area, Canvas canvas)
    {
        this.canvas = canvas;
        this.area = area;
        mouseRadius = new SpinnerNumberModel(0, 0, AreaRenderer.MaxRadius, 1);
        lastMouseState = new boolean[Mouse.getButtonCount()];
        mouseGrid = new Point(-100, -100);
    }

    /**
     * Draws the area this viewer is viewing
     */

    public void draw()
    {
        GL11.glPushMatrix();
        GL11.glTranslatef(-scrollX, -scrollY, 0.0f);

        GL11.glEnable(GL11.GL_TEXTURE_2D);

        area.getTileGrid().draw(this);

        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glPopMatrix();
    }

    /**
     * Sets the sprite that is drawn under the mouse cursor to preview
     * the action that clicking will cause
     *
     * @param tile
     */

    public void setActionPreviewTile(Tile tile)
    {
        this.actionPreviewTile = tile;
        if (tile != null) {
            tile.cacheSprite();
        }
    }

    /**
     * Sets whether to draw the transparency of the area
     *
     * @param drawTransparent
     */

    public void setDrawTransparent(boolean drawTransparent)
    {
        this.drawTransparent = drawTransparent;
    }

    /**
     * Sets whether to draw the passability of the area
     *
     * @param drawPassable
     */

    public void setDrawPassable(boolean drawPassable)
    {
        this.drawPassable = drawPassable;
    }

    /**
     * Sets the object which is notified of mouse and view grid movement
     *
     * @param handler
     */

    public void setViewHandler(AreaRenderer.ViewHandler handler)
    {
        this.viewHandler = handler;
    }

    /**
     * Sets the object which is notified of right and left click events
     *
     * @param handler
     */

    public void setClickHandler(AreaPalette.AreaClickHandler handler)
    {
        this.clickHandler = handler;
    }

    /**
     * Handles input from the LWJGL input polling
     */

    public void handleInput()
    {
        // handle key event
        while (Keyboard.next()) {

        }

        // handle mouse
        // reset the click timeout and check for updates to the mouse state
        for (int i = 0; i < Mouse.getButtonCount(); i++) {
            if (Mouse.isButtonDown(i) != lastMouseState[i]) {
                lastClickTime = 0l;
                lastMouseState[i] = !lastMouseState[i];
            }
        }

        if (!mouseGrid.equals(prevMouseGrid)) {
            lastClickTime = 0l;
            prevMouseGrid = mouseGrid;

            if (viewHandler != null) {
                viewHandler.mouseMoved(mouseGrid.x, mouseGrid.y);
            }
        }

        int mouseX = Mouse.getX() + scrollX;
        int mouseY = (canvas.getHeight() - Mouse.getY()) + scrollY;
        mouseGrid = AreaUtil.convertScreenToGrid(mouseX, mouseY);
        mouseScreen = AreaUtil.convertGridToScreen(mouseGrid);

        if (Mouse.isButtonDown(2)) {
            int mouseDX = Mouse.getDX();
            int mouseDY = Mouse.getDY();

            if (Mouse.isGrabbed()) {
                scrollX -= (mouseDX);
                scrollY += (mouseDY);

                Point viewGrid = AreaUtil.convertScreenToGrid(new Point(scrollX, scrollY));
                viewHandler.viewMoved(viewGrid.x, viewGrid.y);
            }

            Mouse.setGrabbed(true);
        } else
            if (Mouse.isGrabbed()) {
                Mouse.setGrabbed(false);
            }

        long curTime = System.currentTimeMillis();
        if (curTime - lastClickTime > MouseTimeout) {
            if (Mouse.isButtonDown(0)) {
                clickHandler.leftClicked(mouseGrid.x, mouseGrid.y, (Integer)mouseRadius.getValue());
                lastClickTime = curTime;
            } else
                if (Mouse.isButtonDown(1)) {
                    lastClickTime = curTime;
                    clickHandler.rightClicked(mouseGrid.x, mouseGrid.y, (Integer)mouseRadius.getValue());
                }
        }

        int scrollAmount = Mouse.getDWheel();
        if (scrollAmount > 0 && mouseRadius.getNextValue() != null) {
            mouseRadius.setValue(mouseRadius.getNextValue());
        } else
            if (scrollAmount < 0 && mouseRadius.getPreviousValue() != null) {
                mouseRadius.setValue(mouseRadius.getPreviousValue());
            }
    }

    /**
     * Gets the number model containing the value of the mouse radius
     *
     * @return the mouse radius model
     */

    public SpinnerNumberModel getMouseRadiusModel()
    {
        return mouseRadius;
    }

    @Override
    public Area getArea()
    {
        return area;
    }

    @Override
    public void drawTransitions()
    {
    }

    @Override
    public void drawInterface(AnimationState as)
    {
        if (mouseScreen != null) {
            SpriteManager.getSprite("editor/hexBorder").draw(mouseScreen.x, mouseScreen.y);
            int radius = (Integer)mouseRadius.getValue();
            for (int r = 1; r <= radius; r++) {
                for (int i = 0; i < 6 * r; i++) {
                    Point pGrid = AreaUtil.convertPolarToGrid(mouseGrid.x, mouseGrid.y, r, i);
                    Point pScreen = AreaUtil.convertGridToScreen(pGrid);

                    SpriteManager.getSprite("editor/hexBorder").draw(pScreen.x, pScreen.y);
                }
            }

            if (actionPreviewTile != null) {
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);

                actionPreviewTile.draw(mouseScreen.x, mouseScreen.y);

                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            }
        }

        if (drawPassable) {
            for (int x = 0; x < area.getWidth(); x++) {
                for (int y = 0; y < area.getHeight(); y++) {
                    if (!area.isPassable(x, y)) {
                        Point pScreen = AreaUtil.convertGridToScreen(x, y);
                        SpriteManager.getSprite("editor/hexBorder").draw(pScreen.x, pScreen.y);
                    }
                }
            }
        }

        if (drawTransparent) {
            for (int x = 0; x < area.getWidth(); x++) {
                for (int y = 0; y < area.getHeight(); y++) {
                    if (!area.isTransparent(x, y)) {
                        Point pScreen = AreaUtil.convertGridToScreen(x, y);
                        SpriteManager.getSprite("editor/hexBorder").draw(pScreen.x, pScreen.y);
                    }
                }
            }
        }
    }

    /**
     * interface for listening for mouse and view changes from this area view
     *
     * @author jared
     */

    public interface ViewHandler
    {
        /**
         * Called whenever the mouse cursor is moved to a new grid coordinate
         *
         * @param gridx
         * @param gridy
         */

        public void mouseMoved(int gridx, int gridy);

        /**
         * Called whenever the view is scrolled to a new grid coordinate
         *
         * @param gridx
         * @param gridy
         */

        public void viewMoved(int gridx, int gridy);
    }
}
