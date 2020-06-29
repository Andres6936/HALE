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

package main.java.hale.mainmenu;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import main.java.hale.Game;
import main.java.hale.SavedParty;
import main.java.hale.entity.EntityManager;
import main.java.hale.entity.PC;
import main.java.hale.resource.ResourceType;
import main.java.hale.widgets.TextAreaNoInput;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.AnimationState.StateKey;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;

/**
 * A widget allowing the user to select a party and other options and start a new
 * game
 *
 * @author Jared
 */

public class NewGameWindow extends Widget
{
    public static final StateKey STATE_INVALID = StateKey.get("invalid");

    private MainMenu mainMenu;

    private Label title;
    private Button cancel, accept;

    private int acceptCancelGap;
    private int sectionGap;
    private int innerWidth, preselectedWidth, preselectedHeight;

    private DifficultySelector difficultySelector;

    private ToggleButton showInvalidPartiesButton;
    private Label partyLabel;
    private Button newPartyButton;
    private ScrollPane partyPane;
    private DialogLayout partyPaneContent;

    private Label charactersLabel;
    private ScrollPane charactersPane;
    private DialogLayout charactersPaneContent;

    private PartySelector selectedParty;

    private PC preselected;

    private HashSet<String> charactersUsedInParties;

    /**
     * Creates a NewGameWindow with the specified parent MainMenu
     *
     * @param mainMenu the parent widget of this window
     */

    public NewGameWindow(MainMenu mainMenu)
    {
        this.mainMenu = mainMenu;
        mainMenu.setButtonsVisible(false);

        title = new Label();
        title.setTheme("titlelabel");
        add(title);

        cancel = new Button();
        cancel.setTheme("cancelbutton");
        cancel.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                NewGameWindow.this.mainMenu.removeChild(NewGameWindow.this);
                NewGameWindow.this.mainMenu.setButtonsVisible(true);
            }
        });
        add(cancel);

        accept = new Button();
        accept.setTheme("acceptbutton");
        accept.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                acceptNewGame();
                NewGameWindow.this.mainMenu.removeChild(NewGameWindow.this);
                NewGameWindow.this.mainMenu.setButtonsVisible(true);
            }
        });
        add(accept);

        difficultySelector = new DifficultySelector();
        add(difficultySelector);

        partyLabel = new Label();
        partyLabel.setTheme("partylabel");
        add(partyLabel);

        showInvalidPartiesButton = new ToggleButton();
        showInvalidPartiesButton.setTheme("showinvalidpartiesbutton");
        showInvalidPartiesButton.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                selectedParty = null;
                populatePartySelectors(null);
                populateCurrentParty();
            }
        });
        add(showInvalidPartiesButton);

        newPartyButton = new Button();
        newPartyButton.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                PartyFormationWindow window = new PartyFormationWindow(NewGameWindow.this.mainMenu,
                        NewGameWindow.this, charactersUsedInParties);
                NewGameWindow.this.mainMenu.add(window);
            }
        });
        newPartyButton.setTheme("newpartybutton");
        add(newPartyButton);

        partyPaneContent = new DialogLayout();
        partyPaneContent.setTheme("content");

        partyPane = new ScrollPane(partyPaneContent);
        partyPane.setTheme("partypane");
        partyPane.setFixed(ScrollPane.Fixed.HORIZONTAL);
        add(partyPane);

        charactersLabel = new Label();
        charactersLabel.setTheme("characterslabel");
        add(charactersLabel);

        charactersPaneContent = new DialogLayout();
        charactersPaneContent.setTheme("content");

        charactersPane = new ScrollPane(charactersPaneContent);
        charactersPane.setTheme("characterspane");
        charactersPane.setFixed(ScrollPane.Fixed.HORIZONTAL);
        add(charactersPane);

        String preselectedID = Game.curCampaign.getStartingCharacter();
        if (preselectedID != null) {
            preselected = EntityManager.getPC(preselectedID);
            preselected.resetTime();
        }

        if (preselected != null) {
            newPartyButton.setVisible(false);
            partyLabel.setVisible(false);
            partyPane.setVisible(false);
            showInvalidPartiesButton.setVisible(false);
        }

        charactersUsedInParties = new HashSet<String>();

        populatePartySelectors(null);
        populateCurrentParty();
    }

    private void setAcceptState()
    {
        accept.setTooltipContent(null);

        if (preselected != null) {
            accept.setEnabled(true);
        } else
            if (selectedParty != null) {
                String tooltip = validateParty(selectedParty.party);
                accept.setEnabled(tooltip == null);
                accept.setTooltipContent(tooltip);
            } else {
                accept.setEnabled(false);
            }
    }

    private String validateParty(SavedParty party)
    {
        int maxSize = Game.curCampaign.getMaxPartySize();
        int minSize = Game.curCampaign.getMinPartySize();
        int maxLevel = Game.curCampaign.getMaxStartingLevel();
        int minLevel = Game.curCampaign.allowLevelUp() ? 1 : Game.curCampaign.getMinStartingLevel();

        if (party.size() > maxSize) {
            return "The party is too large for this campaign";
        } else
            if (party.size() < minSize) {
                return "The party is too small for this campaign";
            } else
                if (party.getMinLevel() < minLevel) {
                    return "One or more party members are of too low a level for this campaign";
                } else
                    if (party.getMaxLevel() > maxLevel) {
                        return "One or more party members are of too high a level for this campaign";
                    } else {
                        return null;
                    }
    }

    /**
     * Repopulates the list of available party selectors
     *
     * @param idToSelect the ID of the party selector that should be selected by default,
     *                   or null if no selector should be selected
     */

    public void populatePartySelectors(String idToSelect)
    {
        charactersUsedInParties.clear();

        partyPaneContent.removeAllChildren();

        DialogLayout.Group mainH = partyPaneContent.createParallelGroup();
        DialogLayout.Group mainV = partyPaneContent.createSequentialGroup();

        if (preselected == null) {
            List<SavedParty> savedParties = new ArrayList<SavedParty>();

            savedParties.addAll(getPartiesInDirectory("characters/parties/"));

            // parties added from the "characters/parties" directory cannot be deleted, while
            // parties from the Game.getPartiesBaseDirectory() can
            int lastNotDeletableIndex = savedParties.size() - 1;

            savedParties.addAll(getPartiesInDirectory(Game.plataform.getPartiesDirectory()));

            int index = 0;
            for (SavedParty savedParty : savedParties) {
                String tooltip = validateParty(savedParty);

                boolean isValid = tooltip == null;

                if (savedParty.getMinLevel() < Game.curCampaign.getMinStartingLevel() && tooltip == null) {
                    tooltip = "One or more party members will be automatically leveled up for this campaign";
                }

                PartySelector selector;
                if (index > lastNotDeletableIndex) {
                    // allow this party to be deleted
                    selector = new PartySelector(savedParty, true);
                } else {
                    // do not allow this party to be deleted
                    selector = new PartySelector(savedParty, false);
                }

                selector.setTooltipContent(tooltip);
                selector.getAnimationState().setAnimationState(STATE_INVALID, !isValid);

                if (showInvalidPartiesButton.isActive() || isValid) {
                    mainH.addWidget(selector);
                    mainV.addWidget(selector);
                }

                if (savedParty.getID().equals(idToSelect)) {
                    selector.run();
                }

                addToCharactersUsedInParties(savedParty);

                index++;
            }
        }

        partyPaneContent.setHorizontalGroup(mainH);
        partyPaneContent.setVerticalGroup(mainV);

        setAcceptState();
    }

    /**
     * Returns the set of characters which are used in one or more parties.  these
     * characters should not be deleted.  the set should not be modified
     *
     * @return the set of characters used in one or more parties
     */

    public Set<String> getCharactersUsedInParties()
    {
        return charactersUsedInParties;
    }

    private List<SavedParty> getPartiesInDirectory(String directory)
    {
        List<SavedParty> partyList = new ArrayList<SavedParty>();

        File directoryFile = new File(directory);

        for (String idPath : directoryFile.list()) {
            File partyFile = new File(directory + "/" + idPath);
            if (!partyFile.isFile()) continue;

            String partyID = idPath.substring(0, idPath.length() - 5);

            SavedParty party = new SavedParty(directory + partyID + ResourceType.JSON.getExtension(), partyID);

            partyList.add(party);
        }

        return partyList;
    }

    private void addToCharactersUsedInParties(SavedParty party)
    {
        for (String id : party.getCharacterIDs()) {
            charactersUsedInParties.add(id);
        }
    }

    private void populateCurrentParty()
    {
        charactersPaneContent.removeAllChildren();

        DialogLayout.Group mainH = charactersPaneContent.createParallelGroup();
        DialogLayout.Group mainV = charactersPaneContent.createSequentialGroup();

        if (preselected != null) {
            CharacterSelector selector = new CharacterSelector(preselected, NewGameWindow.this.mainMenu);
            mainH.addWidget(selector);
            mainV.addWidget(selector);

        } else
            if (selectedParty != null) {
                for (String characterID : selectedParty.party.getCharacterIDs()) {
                    PC pc = EntityManager.getPC(characterID);

                    // TODO verify PC is valid for current campaign

                    pc.resetTime();

                    CharacterSelector selector = new CharacterSelector(pc, NewGameWindow.this.mainMenu);
                    mainH.addWidget(selector);
                    mainV.addWidget(selector);
                }
            }

        charactersPaneContent.setHorizontalGroup(mainH);
        charactersPaneContent.setVerticalGroup(mainV);
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo)
    {
        super.applyTheme(themeInfo);

        acceptCancelGap = themeInfo.getParameter("acceptCancelGap", 0);
        sectionGap = themeInfo.getParameter("sectionGap", 0);
        innerWidth = themeInfo.getParameter("innerwidth", 0);
        preselectedWidth = themeInfo.getParameter("preselectedwidth", 0);
        preselectedHeight = themeInfo.getParameter("preselectedheight", 0);
    }

    @Override
    public void layout()
    {
        // set size and position of this widget
        if (preselected == null) {
            setSize(innerWidth + getBorderHorizontal(), getMaxHeight());
        } else {
            int width = Math.max(preselectedWidth, difficultySelector.getPreferredWidth());

            setSize(width + getBorderHorizontal(), preselectedHeight);
        }

        setPosition((Game.config.getResolutionX() - getWidth()) / 2,
                (Game.config.getResolutionY() - getHeight()) / 2);

        // set size and position of children
        int centerX = getInnerX() + getInnerWidth() / 2;

        cancel.setSize(cancel.getPreferredWidth(), cancel.getPreferredHeight());
        accept.setSize(accept.getPreferredWidth(), accept.getPreferredHeight());

        accept.setPosition(centerX - accept.getWidth() - acceptCancelGap,
                getInnerBottom() - accept.getHeight());

        cancel.setPosition(centerX + acceptCancelGap, getInnerBottom() - cancel.getHeight());

        title.setSize(title.getPreferredWidth(), title.getPreferredHeight());
        title.setPosition(centerX - title.getWidth() / 2, getInnerY());

        difficultySelector.setSize(difficultySelector.getPreferredWidth(), difficultySelector.getPreferredHeight());
        difficultySelector.setPosition(centerX - difficultySelector.getWidth() / 2,
                accept.getY() - sectionGap - difficultySelector.getHeight());

        if (preselected == null) {
            partyLabel.setPosition(getInnerX(), title.getBottom() + sectionGap);
            partyLabel.setSize(partyLabel.getPreferredWidth(), partyLabel.getPreferredHeight());

            newPartyButton.setSize(newPartyButton.getPreferredWidth(), newPartyButton.getPreferredHeight());
            newPartyButton.setPosition(getInnerX(), partyLabel.getBottom());

            showInvalidPartiesButton.setSize(showInvalidPartiesButton.getPreferredWidth(),
                    showInvalidPartiesButton.getPreferredHeight());
            showInvalidPartiesButton.setPosition(getInnerX(),
                    difficultySelector.getY() - newPartyButton.getHeight() - sectionGap);

            partyPane.setPosition(getInnerX(), newPartyButton.getBottom());
            partyPane.setSize(getInnerWidth() / 2, Math.max(0, showInvalidPartiesButton.getY() - partyPane.getY()));

            charactersLabel.setPosition(getInnerX() + getInnerWidth() / 2, title.getBottom() + sectionGap);
            charactersLabel.setSize(charactersLabel.getPreferredWidth(), charactersLabel.getPreferredHeight());

            charactersPane.setPosition(partyPane.getRight(), charactersLabel.getBottom());
            charactersPane.setSize(getInnerWidth() / 2, difficultySelector.getY() - charactersPane.getY() - sectionGap);
        } else {
            charactersLabel.setPosition(getInnerX(), title.getBottom() + sectionGap);
            charactersLabel.setSize(charactersLabel.getPreferredWidth(), charactersLabel.getPreferredHeight());

            charactersPane.setPosition(getInnerX(), charactersLabel.getBottom());
            charactersPane.setSize(getInnerWidth(),
                    Math.max(0, difficultySelector.getY() - charactersPane.getY() - sectionGap));
        }
    }

    private void acceptNewGame()
    {
        if (preselected != null) {
            Game.curCampaign.addParty(null, null);
            Game.curCampaign.party.setFirstMemberSelected();
        } else {
            Game.curCampaign.addParty(selectedParty.party.getCharacterIDs(), selectedParty.party.getName());
            Game.curCampaign.party.setFirstMemberSelected();

            int currency = Math.max(selectedParty.party.getCurrency(), Game.curCampaign.getMinCurrency());

            Game.curCampaign.partyCurrency.setValue(currency);
            Game.curCampaign.levelUpToMinIfAllowed();
        }

        Game.ruleset.getDifficultyManager().setCurrentDifficulty(difficultySelector.getSelectedDifficulty());

        mainMenu.update();
    }

    private class PartySelector extends ToggleButton implements Runnable
    {
        private TextArea textArea;
        private Button delete;
        private HTMLTextAreaModel model;

        private SavedParty party;

        private PartySelector(SavedParty party, boolean allowDelete)
        {
            this.party = party;
            addCallback(this);

            model = new HTMLTextAreaModel();
            textArea = new TextAreaNoInput(model);
            add(textArea);

            delete = new Button();
            delete.setTheme("deletebutton");
            delete.setEnabled(allowDelete);
            delete.addCallback(new Runnable()
            {
                @Override
                public void run()
                {
                    showDeleteConfirmation();
                }
            });
            add(delete);

            StringBuilder sb = new StringBuilder();

            sb.append("<div style=\"font-family: medium-white;\">");
            sb.append(party.getName());
            sb.append("</div>");

            sb.append("<div style=\"font-family: white;\">");
            if (party.getMaxLevel() == party.getMinLevel()) {
                sb.append("Level ");
                sb.append(party.getMaxLevel());
            } else {
                sb.append("Levels ");
                sb.append(party.getMinLevel());
                sb.append(" to ");
                sb.append(party.getMaxLevel());
            }

            if (Game.curCampaign.allowLevelUp() && party.getMinLevel() < Game.curCampaign.getMinStartingLevel()) {
                sb.append(" - <span style=\"font-family: red\">");
                sb.append("Requires Level Up");
                sb.append("</span>");
            }

            sb.append("</div>");

            model.setHtml(sb.toString());
        }

        private void showDeleteConfirmation()
        {
            ConfirmationPopup popup = new ConfirmationPopup(NewGameWindow.this);

            StringBuilder sb = new StringBuilder();
            sb.append("Delete ");
            sb.append(party.getName());
            sb.append("?");

            popup.setTitleText(sb.toString());

            popup.setWarningText("This action is permanent and cannot be undone.");

            popup.addCallback(new Runnable()
            {
                @Override
                public void run()
                {
                    party.deleteFile();
                    selectedParty = null;
                    populatePartySelectors(null);
                    populateCurrentParty();
                }
            });

            popup.openPopupCentered();
        }

        @Override
        public void run()
        {
            if (selectedParty != null) {
                selectedParty.setActive(false);
            }

            selectedParty = this;

            this.setActive(true);

            populateCurrentParty();

            setAcceptState();
        }

        @Override
        protected void applyTheme(ThemeInfo themeInfo)
        {
            super.applyTheme(themeInfo);

            String deleteDisabledTooltip = themeInfo.getParameter("deletedisabledtooltip", (String)null);

            if (!delete.isEnabled()) {
                delete.setTooltipContent(deleteDisabledTooltip);
            }
        }

        @Override
        protected void layout()
        {
            textArea.setSize(getInnerWidth(), getInnerHeight());
            textArea.setPosition(getInnerX(), getInnerY());

            delete.setSize(delete.getPreferredWidth(), delete.getPreferredHeight());
            delete.setPosition(getInnerRight() - delete.getWidth(), getInnerBottom() - delete.getHeight());
        }

        @Override
        public int getPreferredWidth()
        {
            return textArea.getPreferredWidth() + getBorderHorizontal();
        }

        @Override
        public int getPreferredHeight()
        {
            return textArea.getPreferredHeight() + getBorderVertical();
        }
    }
}
