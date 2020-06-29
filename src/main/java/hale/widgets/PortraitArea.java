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
import hale.entity.PC;

import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;

/**
 * The main widget showing the list of portraits associated with the party
 *
 * @author Jared Stephen
 */

public class PortraitArea extends ScrollPane
{
    private int dragPlacesForward;

    private int portraitGap;

    private List<PortraitViewer> portraits;

    private final Content content;

    private boolean levelUpEnabled = true;

    /**
     * Create a new portrait area widget
     */

    public PortraitArea()
    {
        content = new Content();
        setContent(content);

        portraits = new ArrayList<PortraitViewer>();

        updateParty();

        setFixed(ScrollPane.Fixed.HORIZONTAL);
    }

    /**
     * Closes all Level Up (CharacterBuilder) windows associated created
     * by PortraitViewers contained within this Widget
     */

    public void closeLevelUpWindows()
    {
        for (PortraitViewer p : portraits) {
            p.closeLevelUpWindow();
        }
    }

    /**
     * Disables any visible level up buttons, preventing the user from
     * leveling up any characters until the level up buttons are enabled.
     */

    public void disableAllLevelUp()
    {
        closeLevelUpWindows();

        levelUpEnabled = false;

        for (PortraitViewer p : portraits) {
            p.setLevelUpEnabled(false);
        }
    }

    /**
     * Enables all visible level up buttons, allowing the user to level
     * up characters if they push the button.
     */

    public void enableAllLevelUp()
    {
        levelUpEnabled = true;

        for (PortraitViewer p : portraits) {
            p.setLevelUpEnabled(true);
        }
    }

    /**
     * Called whenever the formation of the party has been updated and this
     * Widget needs to lay itself out from scratch
     */

    public void updateParty()
    {
        content.removeAllChildren();
        portraits.clear();

        for (PC pc : Game.curCampaign.party) {
            if (pc.isSummoned()) continue; // don't add portraits for summoned creatures

            PortraitViewer p = new PortraitViewer(pc, this);
            p.setLevelUpEnabled(levelUpEnabled);
            portraits.add(p);
            content.add(p);
        }

        content.invalidateLayout();
    }

    /**
     * Called by mainViewer to update the content of this part of the interface.
     * Normally, simply updates the stats displayed on each PortraitViewer.  If
     * the party has set recomputePortraits() to true due to a party formation
     * change, calls #updateParty()
     */

    public void updateContent()
    {
        if (Game.curCampaign.party.recomputePortraits()) {
            updateParty();

            // scroll to the currently selected character
            int index = Game.curCampaign.party.getSelectedIndex();
            Widget widget = content.getChild(index);
            scrollToAreaY(widget.getY() - content.getY(), widget.getHeight(), 0);
        }

        for (PortraitViewer viewer : portraits) {
            viewer.updateContent();
        }
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo)
    {
        super.applyTheme(themeInfo);

        portraitGap = themeInfo.getParameter("portraitGap", 0);
    }

    /**
     * Called by portrait viewers to indicate that the mouse has stopped dragging.
     * This is used to reposition the portrait viewer permanently if needed
     *
     * @param viewer the portrait viewer that originated the drag release
     */

    protected void checkMouseDragRelease(PortraitViewer viewer)
    {
        PC baseCreature = viewer.getPC();

        if (dragPlacesForward != 0) {
            Game.curCampaign.party.movePartyMember(baseCreature, dragPlacesForward);
            Game.mainViewer.updateInterface();

            dragPlacesForward = 0;
        }
    }

    /**
     * Called by portrait viewers to indicate the mouse is being
     * dragged from the specified viewer.  Used by this class to
     * allow dragging to reorder portraits
     *
     * @param viewer the portrait viewer that originated the drag
     * @param evt    the mouse drag event
     */

    protected void checkMouseDrag(PortraitViewer viewer, Event evt)
    {
        int currentIndex = portraits.indexOf(viewer);

        // check for movement up the list
        for (int i = 0; i < currentIndex; i++) {
            if (portraits.get(i).isMouseInside(evt)) {
                dragPlacesForward += i - currentIndex;

                // add the viewer at the current position
                portraits.add(i, viewer);

                // the list has shifted down by 1, so remove
                // this viewer at its old position
                portraits.remove(currentIndex + 1);
                currentIndex = i;
                content.invalidateLayout();
                return;
            }
        }

        // check for movement down the list
        for (int i = currentIndex + 1; i < portraits.size(); i++) {
            if (portraits.get(i).isMouseInside(evt)) {
                dragPlacesForward += i - currentIndex;

                // add the viewer at the current position
                portraits.add(i + 1, viewer);

                portraits.remove(currentIndex);
                currentIndex = i;
                content.invalidateLayout();
                return;
            }
        }
    }

    private class Content extends Widget
    {
        @Override
        public int getPreferredHeight()
        {
            int height = portraitGap * (portraits.size() - 1) + getBorderVertical();
            for (PortraitViewer viewer : portraits) {
                height += viewer.getPreferredHeight();
            }

            return height;
        }

        @Override
        public int getPreferredWidth()
        {
            int width = 0;
            for (PortraitViewer viewer : portraits) {
                width = Math.max(width, viewer.getPreferredWidth());
            }

            return width + getBorderHorizontal();
        }

        @Override
        protected void layout()
        {
            super.layout();

            int y = getInnerY();
            for (PortraitViewer viewer : portraits) {
                viewer.setSize(viewer.getPreferredWidth(), viewer.getPreferredHeight());
                viewer.setPosition(getInnerX(), y);

                y += viewer.getHeight() + portraitGap;
            }
        }
    }
}
