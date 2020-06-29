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

package main.java.hale.characterbuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import main.java.hale.ability.Ability;
import main.java.hale.ability.AbilitySelectionList;
import main.java.hale.entity.PC;
import main.java.hale.rules.Role;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;

/**
 * The Builder pane for selecting the set of Abilities to add to the
 * buildable character being edited by the BuilderPane
 *
 * @author Jared Stephen
 */

public class BuilderPaneAbilities extends AbstractBuilderPane implements AbilitySelectorButton.Callback, AbilitySelectorButton.HoverHolder
{
    private Selection currentSelection;
    private List<Selection> selections;

    private ScrollPane abilitiesPane;
    private Label titleLabel;

    private Button resetButton;
    private int resetGap;

    private ScrollPane selectedAbilitiesPane;

    private Widget hoverTop, hoverBottom;

    /**
     * Creates a new BuilderPaneAbilities editing the specified character with the
     * specified parent CharacterBuilder
     *
     * @param builder   the parent CharacterBuilder
     * @param character the Buildable character being edited
     */

    public BuilderPaneAbilities(CharacterBuilder builder, Buildable character)
    {
        super(builder, "Abilities", character);

        if (!character.isNewCharacter()) {
            getNextButton().setText("Finish");
        }

        titleLabel = new Label("Selections");
        titleLabel.setTheme("titlelabel");
        this.add(titleLabel);

        abilitiesPane = new ScrollPane();
        abilitiesPane.setTheme("abilitiespane");
        this.add(abilitiesPane);

        selections = new ArrayList<Selection>();

        selectedAbilitiesPane = new ScrollPane();
        selectedAbilitiesPane.setFixed(ScrollPane.Fixed.VERTICAL);
        selectedAbilitiesPane.setTheme("selectedabilitiespane");
        this.add(selectedAbilitiesPane);

        resetButton = new Button("Reset Selections");
        resetButton.setTheme("resetbutton");
        resetButton.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                resetSelections();
            }
        });
        this.add(resetButton);
    }

    @Override
    protected void next()
    {
        if (getCharacter().isNewCharacter()) {
            super.next();
        } else {
            getCharacterBuilder().finish();
        }
    }

    @Override
    protected int getAdditionalSelectorPaneHeightLimit()
    {
        return resetButton.getPreferredHeight();
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo)
    {
        resetGap = themeInfo.getParameter("resetGap", 0);
    }

    @Override
    protected void layout()
    {
        super.layout();

        titleLabel.setSize(titleLabel.getPreferredWidth(), titleLabel.getPreferredHeight());

        resetButton.setSize(resetButton.getPreferredWidth(), resetButton.getPreferredHeight());
        resetButton.setPosition(getInnerX(), getNextButton().getY() - resetGap - resetButton.getHeight());

        selectedAbilitiesPane.setPosition(getInnerX(), getInnerY() + titleLabel.getHeight());
        selectedAbilitiesPane.setSize(selectedAbilitiesPane.getPreferredWidth(),
                Math.max(selectedAbilitiesPane.getMinWidth(), resetButton.getY() - titleLabel.getBottom()));

        titleLabel.setPosition(getInnerX(), getInnerY());

        int abilitiesPaneX = Math.max(getNextButton().getRight(), selectedAbilitiesPane.getRight());
        abilitiesPaneX = Math.max(abilitiesPaneX, resetButton.getRight());

        abilitiesPane.setPosition(abilitiesPaneX, getInnerY());
        abilitiesPane.setSize(getInnerRight() - abilitiesPane.getX(),
                getInnerBottom() - abilitiesPane.getY());
    }

    @Override
    protected void updateCharacter()
    {
        int creatureLevel = getCharacter().getCreatureLevel();

        Role role = getCharacter().getSelectedRole();
        int roleLevel = getCharacter().getLevel(role) + 1;

        List<AbilitySelectionList> raceLists =
                getCharacter().getWorkingCopy().getTemplate().getRace().getAbilitySelectionsAddedAtLevel(creatureLevel);

        List<AbilitySelectionList> roleLists =
                getCharacter().getSelectedRole().getAbilitySelectionsAddedAtLevel(roleLevel);

        List<AbilitySelectionList> lists = new ArrayList<AbilitySelectionList>();
        lists.addAll(raceLists);
        lists.addAll(roleLists);

        Iterator<Ability> alreadySelectedIter = getCharacter().getSelectedAbilities().iterator();

        selections.clear();
        for (AbilitySelectionList list : lists) {
            Selection selection = new Selection(list);

            if (alreadySelectedIter.hasNext()) {
                selection.selectedAbility = alreadySelectedIter.next();
            }

            selections.add(selection);
        }

        selectedAbilitiesPane.setContent(new SelectedAbilitiesContent());

        chooseCurrentSelection();
    }

    private void chooseCurrentSelection()
    {
        int selectionsRemaining = 0;
        currentSelection = null;

        PC workingCopy = getCharacter().getWorkingCopy();

        for (Selection selection : selections) {
            if (!selection.isMade()) {
                if (currentSelection == null) {
                    currentSelection = selection;
                    AbilitySelectionListPane pane = new AbilitySelectionListPane(selection.list,
                            workingCopy, this, true, new ArrayList<String>());
                    pane.addAbilitySelectorCallback(this);
                    abilitiesPane.setContent(pane);
                }
                selectionsRemaining++;
            }
        }

        // only enable reset if at least one selection has been made
        resetButton.setEnabled(selectionsRemaining != selections.size());

        if (currentSelection != null) {
            getNextButton().setEnabled(false);
        } else {
            Widget noContent = new Widget();
            noContent.setTheme("");

            abilitiesPane.setContent(noContent);
            getNextButton().setEnabled(true);
        }

        invalidateLayout();
    }

    private void resetSelections()
    {
        for (Selection selection : selections) {
            selection.clearAbility();
        }

        getCharacter().clearSelectedAbilities();

        chooseCurrentSelection();
    }

    @Override
    public void selectionMade(Ability ability)
    {
        currentSelection.setAbility(ability);

        getCharacter().addSelectedAbility(ability);

        chooseCurrentSelection();
    }

    @Override
    public void setHoverWidgets(Widget hoverTop, Widget hoverBottom)
    {
        if (this.hoverTop != null) removeChild(this.hoverTop);
        if (this.hoverBottom != null) removeChild(this.hoverBottom);

        this.hoverTop = hoverTop;
        this.hoverBottom = hoverBottom;

        if (hoverTop != null) {
            add(hoverTop);
        }

        if (hoverBottom != null) {
            add(hoverBottom);
        }
    }

    @Override
    public void removeHoverWidgets(Widget hoverTop, Widget hoverBottom)
    {
        if (this.hoverTop != null && this.hoverTop == hoverTop) {
            removeChild(this.hoverTop);
            this.hoverTop = null;
        }

        if (this.hoverBottom != null && this.hoverBottom == hoverBottom) {
            removeChild(this.hoverBottom);
            this.hoverBottom = null;
        }
    }

    private class SelectedAbilitiesContent extends DialogLayout
    {
        private SelectedAbilitiesContent()
        {
            this.setTheme("content");

            Group mainH = createParallelGroup();
            Group mainV = createSequentialGroup();

            for (Selection selection : selections) {
                AbilitySelectionViewer selector = selection.selector;

                if (selection.isMade()) {
                    selector.setAbility(selection.selectedAbility);
                }

                mainH.addWidget(selector);
                mainV.addWidget(selector);
            }

            // widget to make dialog layout respect other widget sizes
            // if there is only 1
            Label l = new Label();
            mainH.addWidget(l);
            mainV.addWidget(l);

            setHorizontalGroup(mainH);
            setVerticalGroup(mainV);
        }
    }

    /**
     * Views a selected Ability name and Icon, but does not allow any
     * user interaction.  User must use the reset button to remove
     * selected Abilities.
     *
     * @author Jared Stephen
     */

    private class AbilitySelectionViewer extends IconButton implements IconButton.Callback
    {
        private SelectedAbilityHover hoverLabel;
        private Ability ability;
        private String emptyLabelText;

        private AbilitySelectionViewer(String emptyLabelText)
        {
            super(null);
            super.addCallback(this);

            this.emptyLabelText = emptyLabelText;
        }

        private void setAbility(Ability ability)
        {
            this.ability = ability;
            setIcon(ability.getIcon());
        }

        private void clearAbility()
        {
            this.ability = null;
            setIcon(null);
        }

        @Override
        protected boolean handleEvent(Event evt)
        {
            // eat all click events to disable the click animation
            switch (evt.getType()) {
                case MOUSE_BTNDOWN:
                case MOUSE_BTNUP:
                    return false;
                default:
                    return super.handleEvent(evt);
            }
        }

        @Override
        public void leftClicked()
        {
        }

        @Override
        public void rightClicked()
        {
        }

        @Override
        public void startHover()
        {
            if (ability == null) {
                hoverLabel = new SelectedAbilityHover(emptyLabelText, this);
            } else {
                hoverLabel = new SelectedAbilityHover(ability.getName(), this);

            }

            hoverLabel.setTheme("selectedabilityhover");
            BuilderPaneAbilities.this.add(hoverLabel);
        }

        @Override
        public void endHover()
        {
            BuilderPaneAbilities.this.removeChild(hoverLabel);
        }
    }

    private class SelectedAbilityHover extends Widget
    {
        private Label abilityNameLabel;
        private Widget parent;

        private SelectedAbilityHover(String text, Widget parent)
        {
            this.parent = parent;

            abilityNameLabel = new Label(text);
            abilityNameLabel.setTheme("abilitynamelabel");
            this.add(abilityNameLabel);
        }

        @Override
        protected void layout()
        {
            super.layout();

            abilityNameLabel.setSize(abilityNameLabel.getPreferredWidth(), abilityNameLabel.getPreferredHeight());

            int height = Math.max(abilityNameLabel.getHeight(), parent.getHeight());

            this.setSize(abilityNameLabel.getWidth() + getBorderHorizontal(), height);

            abilityNameLabel.setPosition(getInnerX(), getY() + (height - abilityNameLabel.getHeight()) / 2);

            this.setPosition(parent.getRight(), parent.getY());
        }
    }

    private class Selection
    {
        private Ability selectedAbility;
        private AbilitySelectionList list;
        private AbilitySelectionViewer selector;

        private Selection(AbilitySelectionList list)
        {
            this.list = list;
            this.selector = new AbilitySelectionViewer(list.getName() + " Ability");
        }

        private void setAbility(Ability ability)
        {
            selectedAbility = ability;
            selector.setAbility(ability);
        }

        private void clearAbility()
        {
            selectedAbility = null;
            selector.clearAbility();
        }

        private boolean isMade()
        {
            return selectedAbility != null;
        }
    }
}
