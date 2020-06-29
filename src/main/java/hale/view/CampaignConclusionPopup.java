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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import main.java.hale.Game;
import main.java.hale.SavedParty;
import main.java.hale.characterbuilder.CharacterBuilder;
import main.java.hale.entity.CreatedItem;
import main.java.hale.entity.Creature;
import main.java.hale.entity.EntityManager;
import main.java.hale.entity.PC;
import main.java.hale.mainmenu.CharacterSelector;
import main.java.hale.mainmenu.ConfirmQuitPopup;
import main.java.hale.mainmenu.MainMenu;
import main.java.hale.mainmenu.MainMenuAction;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;

/**
 * A popup for showing the completion of a campaign
 *
 * @author Jared
 */

public class CampaignConclusionPopup extends PopupWindow
{
    private Content content;
    private StringBuilder text;

    private String nextCampaignID;

    /**
     * Creates a new PopupWindow with the specified parent Widget
     *
     * @param parent the parent Widget
     */

    public CampaignConclusionPopup(Widget parent)
    {
        super(parent);

        setCloseOnEscape(false);
        setCloseOnClickedOutside(false);

        this.text = new StringBuilder();
        this.content = new Content();

        add(content);

        content.window.populate();
    }

    /**
     * appends the specified text to be displayed by this popup
     *
     * @param text the text to display
     */

    public void addText(String text)
    {
        this.text.append(text);
    }

    /**
     * Shows this popup on the next iteration of the main viewer update
     */

    public void show()
    {
        this.content.window.textAreaModel.setHtml(this.text.toString());

        Game.mainViewer.showPopup(this);
    }

    /**
     * Sets the next campaign option for this campaign conclusion popup.  By default, there
     * is no next option
     *
     * @param id          the ID of the campaign to continue to
     * @param buttonLabel the text to show on the continue button
     */

    public void setNextCampaign(String id, String buttonLabel)
    {
        this.content.window.next.setText(buttonLabel);
        this.content.window.next.setVisible(true);

        this.nextCampaignID = id;
    }

    /**
     * Forces the text area to be the specified height rather than the default theme value
     *
     * @param height the height of the text area in pixels
     */

    public void setTextAreaHeight(int height)
    {
        this.content.window.textAreaHeight = height;
    }

    private void nextCampaign()
    {
        Iterator<Creature> partyIter = Game.curCampaign.party.allCreaturesIterator();
        while (partyIter.hasNext()) {
            Creature current = partyIter.next();

            current.abilities.cancelAllAuras();
        }

        MainMenuAction action = new MainMenuAction(MainMenuAction.Action.NewGame);
        action.setPreActionCallback(new CampaignLoader());

        Game.mainViewer.exitToMainMenu();
        Game.mainViewer.setMainMenuAction(action);
    }

    private class CampaignLoader implements Runnable
    {
        private String partyName;
        private String campaignID;
        private List<PC> characters;
        private String difficulty;
        private int currencyCP;
        private List<CreatedItem> createdItems;

        private CampaignLoader()
        {
            this.campaignID = nextCampaignID;
            this.difficulty = Game.ruleset.getDifficultyManager().getCurrentDifficulty();
            this.characters = new ArrayList<PC>();

            for (CharacterSelector selector : content.window.selectors) {
                characters.add(selector.getCreature());
            }

            currencyCP = Game.curCampaign.partyCurrency.getValue();
            partyName = Game.curCampaign.party.getName();

            this.createdItems = new ArrayList<CreatedItem>();
            for (CreatedItem createdItem : Game.curCampaign.getCreatedItems()) {
                this.createdItems.add(createdItem);
            }
        }

        @Override
        public void run()
        {
            MainMenu.writeLastOpenCampaign(campaignID);

            EntityManager.clear();

            MainMenu mainMenu = new MainMenu();

            for (CreatedItem createdItem : this.createdItems) {
                Game.curCampaign.addCreatedItem(createdItem);
            }

            Game.curCampaign.addPartyCreatures(characters, partyName);
            Game.curCampaign.party.setSelected(characters.get(0));
            Game.curCampaign.partyCurrency.addValue(currencyCP);
            mainMenu.setExitOnLoad();

            mainMenu.setExitCallback(new MainMenuExitCallback(difficulty));

            mainMenu.mainLoop();
        }
    }

    private class MainMenuExitCallback implements Runnable
    {
        private String difficulty;

        private MainMenuExitCallback(String difficulty)
        {
            this.difficulty = difficulty;
        }

        @Override
        public void run()
        {
            // persist the difficulty
            Game.ruleset.getDifficultyManager().setCurrentDifficulty(difficulty);
        }
    }

    private class Content extends Widget
    {
        private Window window;

        private Content()
        {
            window = new Window();

            add(window);
        }

        @Override
        protected void layout()
        {
            window.setSize(window.getPreferredWidth(), window.getPreferredHeight());

            window.setPosition(getInnerX() + (getInnerWidth() - window.getWidth()) / 2,
                    getInnerY() + (getInnerHeight() - window.getHeight()) / 2);
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
    }

    private class Window extends Widget
    {
        private String exportPressedText;
        private int textAreaHeight;
        private int defaultWidth, defaultHeight;
        private int gap;

        private ScrollPane selectorPane;
        private Widget selectorPaneContent;
        private List<CharacterSelector> selectors;

        private TextArea textArea;
        private HTMLTextAreaModel textAreaModel;

        private Button next, export;
        private Button quitToMenu, exit;

        private Window()
        {
            selectors = new ArrayList<CharacterSelector>();
        }

        private void exportAll()
        {
            export.setText(exportPressedText);
            export.setEnabled(false);

            List<String> ids = new ArrayList<String>();
            int minLevel = Integer.MAX_VALUE;
            int maxLevel = 0;

            for (PC creature : Game.curCampaign.party) {
                minLevel = Math.min(minLevel, creature.roles.getTotalLevel());
                maxLevel = Math.max(maxLevel, creature.roles.getTotalLevel());

                String exportedID = CharacterBuilder.savePC(creature);

                ids.add(exportedID);
            }

            SavedParty party = new SavedParty(ids, Game.curCampaign.party.getName(),
                    minLevel, maxLevel, Game.curCampaign.getPartyCurrency().getValue());
            party.writeToFile();
        }

        private void populate()
        {
            next = new Button();
            next.setTheme("continuebutton");
            next.addCallback(new Runnable()
            {
                @Override
                public void run()
                {
                    nextCampaign();
                }
            });
            next.setVisible(false);
            add(next);

            export = new Button();
            export.setTheme("exportbutton");
            export.addCallback(new Runnable()
            {
                @Override
                public void run()
                {
                    exportAll();
                }
            });
            add(export);

            quitToMenu = new Button();
            quitToMenu.setTheme("quittomenubutton");
            quitToMenu.addCallback(new Runnable()
            {
                @Override
                public void run()
                {
                    new ConfirmQuitPopup(content,
                            ConfirmQuitPopup.QuitMode.QuitToMenu).openPopupCentered();
                }
            });
            add(quitToMenu);

            exit = new Button();
            exit.setTheme("exitbutton");
            exit.addCallback(new Runnable()
            {
                @Override
                public void run()
                {
                    new ConfirmQuitPopup(content,
                            ConfirmQuitPopup.QuitMode.ExitGame).openPopupCentered();
                }
            });
            add(exit);

            selectorPaneContent = new ScrollPaneContent();
            selectorPaneContent.setTheme("content");
            selectorPane = new ScrollPane(selectorPaneContent);
            selectorPane.setFixed(ScrollPane.Fixed.HORIZONTAL);

            for (PC creature : Game.curCampaign.party) {
                if (creature.isSummoned()) continue;

                CharacterSelector selector = new CharacterSelector(creature, CampaignConclusionPopup.this.content);
                selectors.add(selector);
                selectorPaneContent.add(selector);
            }
            add(selectorPane);

            textAreaModel = new HTMLTextAreaModel();
            textArea = new TextArea(textAreaModel);
            add(textArea);
        }

        @Override
        protected void layout()
        {
            int curY = getInnerBottom();

            textArea.setPosition(getInnerX(), getInnerY());
            textArea.setSize(getInnerWidth(), textAreaHeight);

            curY = textArea.getBottom() + gap;

            int totalHeight = Math.max(getInnerBottom() - curY,
                    export.getPreferredHeight() + quitToMenu.getPreferredHeight() + exit.getPreferredHeight() + next.getPreferredHeight());
            int maxWidth = Math.max(export.getPreferredWidth(), quitToMenu.getPreferredWidth());
            maxWidth = Math.max(maxWidth, exit.getPreferredWidth());
            maxWidth = Math.max(maxWidth, next.getPreferredWidth());

            next.setSize(maxWidth, next.getPreferredHeight());
            export.setSize(maxWidth, export.getPreferredHeight());
            quitToMenu.setSize(maxWidth, quitToMenu.getPreferredHeight());
            exit.setSize(maxWidth, exit.getPreferredHeight());

            next.setPosition(getInnerRight() - maxWidth, curY + totalHeight / 8 - next.getHeight() / 2);
            export.setPosition(getInnerRight() - maxWidth, curY + totalHeight * 3 / 8 - export.getHeight() / 2);
            quitToMenu.setPosition(getInnerRight() - maxWidth, curY + totalHeight * 5 / 8 - quitToMenu.getHeight() / 2);
            exit.setPosition(getInnerRight() - maxWidth, curY + totalHeight * 7 / 8 - exit.getHeight() / 2);

            selectorPane.setPosition(getInnerX(), curY);

            int contentWidth = selectorPaneContent.getPreferredWidth() + selectorPane.getBorderHorizontal();
            if (selectorPane.getVerticalScrollbar().isVisible()) {
                contentWidth += selectorPane.getVerticalScrollbar().getPreferredWidth();
            }

            selectorPane.setSize(Math.min(getInnerWidth() - maxWidth - gap, contentWidth),
                    Math.max(getInnerBottom() - curY, 0));

        }

        @Override
        protected void applyTheme(ThemeInfo themeInfo)
        {
            super.applyTheme(themeInfo);

            this.defaultWidth = themeInfo.getParameter("defaultwidth", 0);
            this.defaultHeight = themeInfo.getParameter("defaultheight", 0);
            this.exportPressedText = themeInfo.getParameter("exportpressedtext", (String)null);

            this.gap = themeInfo.getParameter("gap", 0);

            // don't override a user set value for the text area height
            if (textAreaHeight == 0) {
                this.textAreaHeight = themeInfo.getParameter("textareaheight", 0);
            }
        }

        @Override
        public int getPreferredWidth()
        {
            return defaultWidth;
        }

        @Override
        public int getPreferredHeight()
        {
            return defaultHeight;
        }
    }

    private class ScrollPaneContent extends Widget
    {
        @Override
        protected void layout()
        {
            int curY = getInnerY();

            for (int i = 0; i < getNumChildren(); i++) {
                Widget child = getChild(i);

                child.setSize(child.getPreferredWidth(), child.getPreferredHeight());
                child.setPosition(getInnerX(), curY);

                curY = child.getBottom();
            }
        }

        @Override
        public int getPreferredHeight()
        {
            int height = getBorderVertical();

            for (int i = 0; i < getNumChildren(); i++) {
                height += getChild(i).getPreferredHeight();
            }

            return height;
        }

        @Override
        public int getPreferredWidth()
        {
            int width = 0;

            for (int i = 0; i < getNumChildren(); i++) {
                width = Math.max(width, getChild(i).getPreferredWidth());
            }

            return width + getBorderHorizontal();
        }
    }
}
