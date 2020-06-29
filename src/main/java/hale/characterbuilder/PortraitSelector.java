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

package hale.characterbuilder;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import hale.Game;
import hale.resource.ResourceManager;
import hale.resource.ResourceType;
import hale.resource.Sprite;
import hale.resource.SpriteManager;
import hale.rules.Race;
import hale.util.FileUtil;
import hale.util.Logger;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;

/**
 * A widget for selecting the portrait for a Buildable character
 *
 * @author Jared Stephen
 */

public class PortraitSelector extends PopupWindow
{
    private Callback callback;

    private PortraitViewer selectedPortrait;

    private Buildable character;

    private int numColumns, buttonGap, portraitSize;

    private Label title;
    private Button accept, cancel;

    private Content content;
    private ScrollPane portraitPane;
    private PortraitPaneContent portraitPaneContent;

    /**
     * Creates a new PortraitSelector Widget with the specified parent
     *
     * @param parent the owning parent
     */

    public PortraitSelector(Widget parent, Buildable character)
    {
        super(parent);
        this.character = character;

        setCloseOnClickedOutside(false);
        setCloseOnEscape(false);

        content = new Content();
        add(content);

        title = new Label("Select a Portrait");
        title.setTheme("titlelabel");
        content.add(title);

        accept = new Button("Accept");
        accept.setTheme("acceptbutton");
        accept.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                accept();
            }
        });
        content.add(accept);

        cancel = new Button("Cancel");
        cancel.setTheme("cancelbutton");
        cancel.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                closePopup();
            }
        });
        content.add(cancel);

        portraitPaneContent = new PortraitPaneContent();
        portraitPane = new ScrollPane(portraitPaneContent);
        portraitPane.setTheme("portraitpane");
        portraitPane.setFixed(ScrollPane.Fixed.HORIZONTAL);
        content.add(portraitPane);

        setAcceptEnabled();
    }

    @Override
    protected void afterAddToGUI(GUI gui)
    {
        Race race = character.getSelectedRace();
        portraitPaneContent.updatePortraits(race);
    }

    /**
     * Sets the callback that is called when a selection is accepted
     *
     * @param callback the callback
     */

    public void setCallback(Callback callback)
    {
        this.callback = callback;
    }

    private void setAcceptEnabled()
    {
        accept.setEnabled(selectedPortrait != null);
    }

    private void accept()
    {
        closePopup();

        String portraitString = PortraitSelector.getPortraitString(selectedPortrait.portrait);
        if (callback != null) {
            callback.portraitSelected(portraitString);
        }
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo)
    {
        super.applyTheme(themeInfo);

        numColumns = themeInfo.getParameter("numcolumns", 0);
        buttonGap = themeInfo.getParameter("buttongap", 0);
        portraitSize = themeInfo.getParameter("portraitsize", 0);
    }

    private static String getPortraitString(String resource)
    {
        String portrait = FileUtil.getRelativePath("portraits", resource);
        portrait = portrait.substring(0, portrait.length() - ResourceType.PNG.getLength());

        return portrait;
    }

    private class Content extends Widget
    {
        @Override
        protected void layout()
        {
            title.setSize(title.getPreferredWidth(), title.getPreferredHeight());
            title.setPosition(getInnerX() + getInnerWidth() / 2 - title.getWidth() / 2, getInnerY());

            accept.setSize(accept.getPreferredWidth(), accept.getPreferredHeight());
            cancel.setSize(cancel.getPreferredWidth(), cancel.getPreferredHeight());

            int paneHeight = getInnerHeight() - title.getHeight() - accept.getHeight() - buttonGap;

            portraitPane.setSize(portraitPane.getPreferredWidth(), paneHeight);
            portraitPane.setPosition(getInnerX(), title.getBottom());

            accept.setPosition(getInnerX() + getInnerWidth() / 2 - accept.getWidth() - buttonGap,
                    portraitPane.getBottom() + buttonGap);

            cancel.setPosition(getInnerX() + getInnerWidth() / 2 + buttonGap, portraitPane.getBottom() + buttonGap);
        }

        @Override
        public int getPreferredInnerWidth()
        {
            return portraitPane.getPreferredWidth() + portraitPane.getVerticalScrollbar().getWidth();
        }

        @Override
        public int getPreferredInnerHeight()
        {
            return Math.min(Game.config.getResolutionY(), portraitPane.getPreferredHeight() + title.getPreferredHeight() +
                    accept.getPreferredHeight() + buttonGap);
        }
    }

    /**
     * The interface for an object that wants to be notified when a selection is accepted
     *
     * @author Jared
     */

    public interface Callback
    {

        /**
         * Called when a selection is accepted
         *
         * @param portrait the portrait that was accepted
         */

        public void portraitSelected(String portrait);
    }

    private class PortraitLoader implements Runnable
    {
        private List<PortraitViewer> viewers;

        private PortraitLoader(List<PortraitViewer> viewers)
        {
            this.viewers = viewers;
        }

        @Override
        public void run()
        {
            for (PortraitViewer viewer : viewers) {
                viewer.loadSprite();
            }
        }
    }

    private class PortraitLoaderListener implements GUI.AsyncCompletionListener<PortraitLoader>
    {
        @Override
        public void completed(PortraitLoader loader)
        {
        }

        @Override
        public void failed(Exception exception)
        {
            Logger.appendToErrorLog("Error loading portraits", exception);
        }
    }

    private class PortraitPaneContent extends Widget
    {
        private int numPortraits;

        private PortraitPaneContent()
        {
            setTheme("content");
        }

        private void updatePortraits(Race race)
        {
            this.removeAllChildren();

            String directory = "portraits/" + race.getID();

            List<PortraitViewer> viewers = new ArrayList<PortraitViewer>();
            for (String resource : ResourceManager.getResourcesInDirectory(directory)) {
                PortraitViewer viewer = new PortraitViewer(resource);
                viewer.setScale(0.5f);
                add(viewer);

                String portrait = PortraitSelector.getPortraitString(resource);
                if (portrait.equals(character.getSelectedPortrait())) {
                    viewer.setActive(true);
                    selectedPortrait = viewer;
                }

                numPortraits++;
                viewers.add(viewer);
            }

            PortraitLoader loader = new PortraitLoader(viewers);
            getGUI().invokeAsync(loader, new PortraitLoaderListener());
        }

        @Override
        public int getPreferredInnerWidth()
        {
            return portraitSize * numColumns;
        }

        @Override
        public int getPreferredInnerHeight()
        {
            return portraitSize * ((int)Math.ceil(numPortraits / numColumns) + 1);
        }

        @Override
        protected void layout()
        {
            super.layout();

            int currentX = getInnerX();
            int currentY = getInnerY();
            int curColumn = 0;

            for (int i = 0; i < getNumChildren(); i++) {
                Widget child = getChild(i);

                child.setSize(child.getPreferredWidth(), child.getPreferredHeight());

                child.setPosition(currentX, currentY);

                currentX += child.getWidth();
                curColumn++;

                if (curColumn >= numColumns) {
                    curColumn = 0;
                    currentX = getInnerX();
                    currentY = child.getBottom();
                }
            }
        }
    }

    private class PortraitViewer extends ToggleButton implements Runnable
    {
        private String portrait;
        private Sprite sprite;
        private float scale;
        private float invScale;

        private PortraitViewer(String portrait)
        {
            this.portrait = portrait;
            this.addCallback(this);
        }

        private void loadSprite()
        {
            this.sprite = SpriteManager.getImage(portrait);
        }

        @Override
        public void run()
        {
            if (selectedPortrait != null) {
                selectedPortrait.setActive(false);
            }

            selectedPortrait = this;
            this.setActive(true);

            setAcceptEnabled();
        }

        private void setScale(float scale)
        {
            this.scale = scale;
            this.invScale = 1.0f / scale;
        }

        @Override
        public int getPreferredWidth()
        {
            return portraitSize;
        }

        @Override
        public int getPreferredHeight()
        {
            return portraitSize;
        }

        @Override
        protected void paintWidget(GUI gui)
        {
            super.paintWidget(gui);

            if (sprite != null) {
                GL11.glPushMatrix();
                GL11.glScalef(scale, scale, 1.0f);
                GL11.glColor3f(1.0f, 1.0f, 1.0f);

                sprite.draw((int)(getInnerX() * invScale), (int)(getInnerY() * invScale));

                GL11.glPopMatrix();
            }
        }
    }
}
