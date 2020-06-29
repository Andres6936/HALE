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

import org.lwjgl.opengl.GL11;

import hale.Cutscene;
import hale.Game;
import hale.ability.DelayedScriptCallback;
import hale.ability.Scriptable;
import hale.resource.ResourceManager;
import hale.resource.Sprite;
import hale.resource.SpriteManager;
import hale.util.Point;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;

/**
 * A popup for showing a cutscene, disabling all other input
 *
 * @author Jared Stephen
 */

public class CutscenePopup extends PopupWindow
{
    private Cutscene cutscene;
    private int currentFrameIndex;

    private Content content;

    /**
     * Creates a new CutscenePopup with the specified parent Widget and Cutscene
     *
     * @param parent   the parent Widget that will be blocked by this PopupWindow
     * @param cutscene the Cutscene to show
     */

    public CutscenePopup(Widget parent, Cutscene cutscene)
    {
        super(parent);
        this.cutscene = cutscene;
        this.currentFrameIndex = 0;

        content = new Content();
        content.setFrame(cutscene.getFrames().get(currentFrameIndex));
        add(content);

        setCloseOnEscape(false);
        setCloseOnClickedOutside(false);

        // load the first bg image immediately
        SpriteManager.getSpriteAnyExtension(cutscene.getFrames().get(currentFrameIndex).getBGImage());

        // load the rest of the frame backgrounds asynchronously
        new BackgroundLoader(cutscene).start();
    }

    private void nextFrame()
    {
        currentFrameIndex++;
        if (currentFrameIndex >= cutscene.getNumFrames()) {
            Game.mainViewer.hidePopup(this);
        } else {
            content.setFrame(cutscene.getFrames().get(currentFrameIndex));
        }
    }

    @Override
    public void closePopup()
    {
        super.closePopup();

        for (Cutscene.Frame frame : cutscene.getFrames()) {
            Sprite sprite = SpriteManager.getSpriteAnyExtension(frame.getBGImage());
            SpriteManager.freeTexture(sprite);
        }

        String scriptID = cutscene.getCallbackScript();
        String func = cutscene.getCallbackFunction();

        if (scriptID != null && func != null) {
            Scriptable scriptable = new Scriptable(ResourceManager.getScriptResourceAsString(scriptID), scriptID, false);
            DelayedScriptCallback cb = new DelayedScriptCallback(scriptable, func);
            cb.run();
        }

        Game.areaViewer.fadeIn();
    }

    private class Content extends Widget
    {
        private Sprite bgSprite;
        private Point bgSpriteOffset;
        private int bgBottom;

        private Label continueLabel;
        private int continueOffset;

        private TextArea textArea;
        private HTMLTextAreaModel textAreaModel;
        private int textAreaWidth, textAreaHeight;

        private Button skipButton;
        private int skipOffset;

        private Content()
        {
            continueLabel = new Label();
            continueLabel.setTheme("continuelabel");
            add(continueLabel);

            textAreaModel = new HTMLTextAreaModel();
            textArea = new CutsceneTextArea(textAreaModel);
            add(textArea);

            skipButton = new Button();
            skipButton.addCallback(new Runnable()
            {
                @Override
                public void run()
                {
                    Game.mainViewer.hidePopup(CutscenePopup.this);
                }
            });
            skipButton.setTheme("skipbutton");
            add(skipButton);
        }

        @Override
        protected void applyTheme(ThemeInfo themeInfo)
        {
            super.applyTheme(themeInfo);

            continueOffset = themeInfo.getParameter("continueOffset", 0);
            skipOffset = themeInfo.getParameter("skipOffset", 0);
        }

        @Override
        protected void layout()
        {
            super.layout();

            int centerX = getInnerX() + getInnerWidth() / 2;

            continueLabel.setPosition(centerX - continueLabel.getPreferredWidth() / 2,
                    bgBottom - continueLabel.getPreferredHeight() / 2 - continueOffset);

            textArea.setSize(textAreaWidth, textAreaHeight);
            textArea.setPosition(centerX - textArea.getWidth() / 2,
                    continueLabel.getY() - continueOffset - textArea.getHeight());

            skipButton.setSize(skipButton.getPreferredWidth(), skipButton.getPreferredHeight());
            skipButton.setPosition(getInnerRight() - skipButton.getWidth() - skipOffset,
                    getInnerBottom() - skipButton.getHeight() - skipOffset);
        }

        private void setFrame(Cutscene.Frame frame)
        {
            bgSprite = SpriteManager.getSpriteAnyExtension(frame.getBGImage());
            bgSpriteOffset = new Point();
            if (bgSprite != null) {
                bgSpriteOffset.x = (Game.config.getResolutionX() - bgSprite.getWidth()) / 2;
                bgSpriteOffset.y = (Game.config.getResolutionY() - bgSprite.getHeight()) / 2;
                bgBottom = bgSpriteOffset.y + bgSprite.getHeight();
            } else {
                bgBottom = Game.config.getResolutionY();
            }

            textAreaWidth = frame.getTextAreaWidth();
            if (textAreaWidth == 0) textAreaWidth = getInnerWidth();

            textAreaHeight = frame.getTextAreaHeight();
            if (textAreaHeight == 0) textAreaHeight = getInnerHeight();

            textAreaModel.setHtml(frame.getText());

            invalidateLayout();
        }

        @Override
        protected void paintWidget(GUI gui)
        {
            if (bgSprite != null) {
                GL11.glColor3f(1.0f, 1.0f, 1.0f);
                //bgSprite.draw(bgSpriteOffset.x, bgSpriteOffset.y);
                bgSprite.draw(0, 0, getWidth(), getHeight());
            }
        }

        @Override
        public int getPreferredWidth()
        {
            return Game.config.getResolutionX();
        }

        @Override
        public int getPreferredHeight()
        {
            return Game.config.getResolutionY();
        }

        @Override
        public boolean handleEvent(Event evt)
        {
            switch (evt.getType()) {
                case MOUSE_ENTERED:
                case MOUSE_BTNDOWN:
                    return true;
                case MOUSE_BTNUP:
                    nextFrame();
                default:
            }

            return false;
        }
    }

    private class CutsceneTextArea extends TextArea
    {
        private CutsceneTextArea(HTMLTextAreaModel model)
        {
            super(model);
            setTheme("textarea");
        }

        @Override
        protected boolean handleEvent(Event evt)
        {
            // do not handle any events to allow clicks to go through
            // to the button holding this textarea
            return false;
        }
    }

    private class BackgroundLoader extends Thread
    {
        private Cutscene cutscene;

        private BackgroundLoader(Cutscene cutscene)
        {
            this.cutscene = cutscene;
        }

        @Override
        public void run()
        {
            for (Cutscene.Frame frame : cutscene.getFrames()) {
                SpriteManager.getSpriteAnyExtension(frame.getBGImage());
            }
        }
    }
}
