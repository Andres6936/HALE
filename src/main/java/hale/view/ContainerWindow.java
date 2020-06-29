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
import main.java.hale.entity.Container;
import main.java.hale.entity.Creature;
import main.java.hale.entity.PC;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.ThemeInfo;

/**
 * A widget for showing the contents of a container and allowing a creature
 * to take or drop items
 *
 * @author Jared Stephen
 */

public class ContainerWindow extends GameSubWindow
{
    private int buttonGap;

    private PC opener;
    private Container container;

    private final Button takeAllButton;

    private final ItemListViewer viewer;

    /**
     * Creates a new empty container window.  This widget will not have any content
     * until {@link #setOpenerContainer(PC, Container)} is called and then
     * {@link #updateContent()}
     */

    public ContainerWindow()
    {
        takeAllButton = new Button();
        takeAllButton.setTheme("takeallbutton");
        takeAllButton.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                takeAll();
            }
        });
        add(takeAllButton);

        viewer = new ItemListViewer();
        add(viewer);
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo)
    {
        super.applyTheme(themeInfo);

        buttonGap = themeInfo.getParameter("buttonGap", 0);
    }

    @Override
    public void layout()
    {
        super.layout();

        takeAllButton.setSize(takeAllButton.getPreferredWidth(), takeAllButton.getPreferredHeight());
        takeAllButton.setPosition(getInnerX(), getInnerY());

        viewer.setPosition(getInnerX(), takeAllButton.getBottom() + buttonGap);
        viewer.setSize(getInnerWidth(), getInnerBottom() - viewer.getY());
    }

    @Override
    public void setVisible(boolean visible)
    {
        super.setVisible(visible);

        viewer.clearAllItemHovers();

        if (!visible) {
            this.opener = null;
            this.container = null;
        }
    }

    private void takeAll()
    {
        if (opener == null || container == null) return;

        opener.inventory.getTakeAllCallback(container).run();

        this.setVisible(false);
    }

    /**
     * Sets the container that this Widget is viewing and the Creature that is interacting with that
     * container
     *
     * @param opener    the creature that is opening the container and can pick up or drop items
     * @param container the container that is being viewed
     */

    public void setOpenerContainer(PC opener, Container container)
    {
        this.opener = opener;
        this.container = container;
    }

    /**
     * Updates this Widget to view any changes to the underlying container
     */

    public void updateContent()
    {
        if (!this.isVisible()) return;

        if (this.opener == null || this.container == null) return;

        this.setTitle(opener.getTemplate().getName() + " opens " + container.getTemplate().getName());

        takeAllButton.setEnabled(!Game.isInTurnMode());

        if (this.container != null) {
            viewer.updateContent(ItemListViewer.Mode.CONTAINER, this.opener, null, this.container.getCurrentItems());
        } else {
            viewer.updateContent(ItemListViewer.Mode.CONTAINER, this.opener, null, null);
        }

        layout();
    }

    /**
     * Returns the container that this Widget is currently viewing or null if there
     * is no container
     *
     * @return the container that this Widget is currently viewing
     */

    public Container getContainer()
    {
        return container;
    }

    /**
     * Returns the creature that is currently opening this widget or null if there is no
     * container being opened
     *
     * @return the creature opening the currently open container
     */

    public Creature getOpener()
    {
        return opener;
    }
}
